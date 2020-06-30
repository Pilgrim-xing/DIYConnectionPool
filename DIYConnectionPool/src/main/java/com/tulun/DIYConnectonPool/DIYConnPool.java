package com.tulun.DIYConnectonPool;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * auther:XingTL
 * date:2020/2/11 19:31
 * <p>
 * 连接池的主要业务实现
 */
class DIYConnPool {
    //数据源
    private DIYDataSource dataSource;
    //核心连接集 该集合的连接不会被销毁
    private volatile List<DIYConnection> coreConnList;
    //非核心连接集
    private volatile List<DIYConnection> connList;
    //线程池 用于计算每个非核心连接空闲时间
    private ThreadPoolExecutor threadPool;
    //目前已创建的连接数量
    private volatile int count;


    /**
     * 构造函数 初始化数据源和连接集
     *
     * @param dataSource 数据源
     */
    DIYConnPool(DIYDataSource dataSource) {
        //给属性赋值
        this.dataSource = dataSource;
        coreConnList = new CopyOnWriteArrayList<>();
        connList = new CopyOnWriteArrayList<>();

        //初始化线程池
        threadPool = new ThreadPoolExecutor(
                dataSource.getMinPoolSize(),//核心线程数
                dataSource.getMaxPoolSize() - dataSource.getMinPoolSize(),//最大线程数
                60,//最大存活时间
                TimeUnit.SECONDS,//时间单位
                new SynchronousQueue<Runnable>(),//阻塞队列
                new ThreadPoolExecutor.AbortPolicy()//拒绝策略
        );

        //初始化连接
        count = 0;
        initConnection();
    }


    /**
     * 存活时间的定时器类
     * 此任务开始于connection未使用
     */
    class Survivable implements Runnable {
        private DIYDataSource dataSource;
        //connection集的下标
        private int index;
        //连接空闲开始时间
        private long freeTime;
        //连接死亡时间
        private long endTime;

        /**
         * 构造函数
         *
         * @param dataSource 数据集
         * @param index  connection集的下标
         */
        Survivable(DIYDataSource dataSource, int index) {
            this.dataSource = dataSource;
            this.index = index;
        }

        /**
         * run方法
         */
        public void run(){
            freeTime = new Date().getTime();//毫秒数
            endTime = freeTime + dataSource.getMaxIdleTime();
            DIYConnection connection = connList.get(index);//获取连接
            while (!connection.isUsed()) {//未被使用
                long time = new Date().getTime();
                if (time >= endTime){
                    synchronized (connList) {
                        //彻底关闭该元素
                        connList.remove(index);//从连接集中删除该连接
                        count--;//连接数减一
                        try {
                            connection.trueClose();//真正关闭该连接
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    return;
                }
            }
            //该连接被重新使用
        }
    }


    /**
     * 初始化连接
     */
    private void initConnection() {
        while (count < dataSource.getInitialPoolSize()) {
            createConnection();
        }
    }

    /**
     * 创建连接
     *
     * @return true:创建连接   false:已达上限，未创建连接
     */
    private DIYConnection createConnection() {
        if (count < dataSource.getMinPoolSize()) {//连接数小于核心线程数
            return createConn(coreConnList, true);
        } else if (count < dataSource.getMaxPoolSize()) {//连接数大于核心线程数
            return createConn(connList, false);
        }
        //超过最大连接数 不会创建连接 而是等待其他连接释放
        return null;
    }


    /**
     * 创建连接的子方法 该方法唯一的给连接赋予number属性
     *
     * @param list 往该连接集中添加连接
     */
    private DIYConnection createConn(List<DIYConnection> list, boolean isCore) {
        DIYConnection connection = new DIYConnection(dataSource, isCore);//创建连接
        list.add(connection);//将连接添加到连接集中
        count++;//连接数+1
        connection.setNumber(list.size());//初始化连接序号
        System.out.println(connection.getNumber()+" 号连接被创建");
        return connection;
    }

    /**
     * 获取连接，该方法返回前改变使用状态
     *
     * @return DIYConnection对象
     */
     DIYConnection getConnection() {
        //首先遍历连接集合中是否有空闲连接 若有 则交给用户
        DIYConnection connection = getAliveConnection();
        if (connection != null) {
            connection.setUsed(true);//设置连接状态
            return connection;
        }

        //若没有空闲连接 则判断是否超过最大连接数 不超过 则创建连接
        DIYConnection conn = createConnection();
        if(conn == null){
            //超过最大连接数 循环等待 直到有空闲出现 返回给用户
            while (true) {
                conn = getAliveConnection();
                if (conn != null){
                    conn.setUsed(true);//设置连接状态
                    return conn;
                }
            }
        }
        conn.setUsed(true);//设置连接状态
        return conn;
    }

    /**
     * 获取连接的子方法
     *
     * @return
     */
    private DIYConnection getAliveConnection() {
        for (DIYConnection con : coreConnList) {//遍历核心集合
            if (!con.isUsed())
                return con;
        }
        for (DIYConnection con : connList) {//遍历非核心集合
            if (!connList.isEmpty() && !con.isUsed()) {
                return con;
            }
        }

        return null;
    }


    /**
     * 外界的关闭连接操作 被DIYConnection交至此处处理
     *
     * @param number 该关闭的连接序号 即下标
     */
    synchronized void closeConnection(int number) {
        int index = number - 1;//计算出获取下标
        threadPool.execute(new Survivable(dataSource, index));//添加监听器
    }

    /**
     * 关闭整个连接池
     */
    void close() {
        threadPool.shutdown();///  maybe something else??
    }
}
