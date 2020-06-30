package com.tulun.Test;


import com.tulun.DIYConnectonPool.DIYConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * 模拟用户使用mysql
 * <p>
 * auther:XingTL
 * date:2020/2/11 18:34
 */
public class MainTest {
    private static final PoolDemo polldemo = new PoolDemo();
    private static final Random random = new Random();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DIYConnection connection = polldemo.getConnection();
                    PreparedStatement pre = null;
                    try {
                        String sql = "select * from student where SID>?";

                        pre = connection.prepareStatement(sql);

                        pre.setString(1, "01");


                        System.out.println(Thread.currentThread().getName() + " 获取:" + connection.getNumber() + " isCore: " + connection.getIsCore());


                        ResultSet resultSet = pre.executeQuery();

                        //遍历集合
                        resultSet.next();
//                        while(resultSet.next()) {
                        System.out.println("SID:" + resultSet.getString(1) +
                                "   SName:" + resultSet.getString(2) +
                                "   SAge:" + resultSet.getString(3) +
                                "   SSex:" + resultSet.getString(4));

//                        }
                        try {
                            Thread.sleep(random.nextInt(5000) + 2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            pre.close();
                            connection.close();//连接归还给链接池
                            System.out.println(Thread.currentThread().getName() + " over");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.setName("thread" + i);
            thread.start();
        }


        try {
            Thread.sleep(80000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
