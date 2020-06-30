package com.tulun.DIYConnectonPool;

/**
 * auther:XingTL
 * date:2020/2/11 19:30
 * <p>
 * 模拟c3p0 ComboPooledDataSource 属性类
 */
public class DIYDataSource {
    //属性
    private String jdbcUrl = null;
    private String jdbcDriver = null;
    private String userName = null;
    private String password = null;

    private int initialPoolSize = 3;//首次创建时的连接数
    private int maxPoolSize = 15;//最大连接数
    private int minPoolSize = 3;//最小连接数
    private int maxIdleTime = 0; //最大存活时间

    private DIYConnPool connPool = null;

    /**
     * get and set
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    DIYConnPool getConnPool() {
        return connPool;
    }

    /**
     * 获取连接的方法 传给用户
     *
     * @return 连接
     */
    public synchronized DIYConnection getConnection() throws Exception {
        if (!isInit()) //未初始化
            throw new NullPointerException("初始化失败！请检查配置信息！");

        return this.connPool.getConnection();
    }

    /**
     * 判断数据源是否初始化过了 如果初始化过了 创建c3p0实例
     *
     * @return
     */
    private synchronized boolean isInit() {
        //未初始化 或不合法
        if (jdbcUrl == null || jdbcDriver == null || userName == null || password == null)
            return false;

        if (this.connPool == null) //初始化连接池类 只会被创建一次
            this.connPool = new DIYConnPool(this);

        return true;
    }
}
