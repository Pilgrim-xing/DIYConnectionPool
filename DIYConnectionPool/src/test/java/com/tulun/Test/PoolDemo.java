package com.tulun.Test;


import com.tulun.DIYConnectonPool.DIYConnection;
import com.tulun.DIYConnectonPool.DIYDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 链接池的配置类
 * <p>
 * auther:XingTL
 * date:2020/2/11 18:12
 */
public class PoolDemo {
    private String jdbcUrl;
    private String jdbcDriver;
    private String userName;
    private String password;

    private String initialPoolSize;
    private String maxPoolSize;
    private String minPoolSize;
    private String maxIdleTime;

    private Properties pro;
    DIYDataSource dataSource;//连接池的数据源对象

    /**
     * 构造函数
     */
    public PoolDemo() {
        pro = new Properties();
        dataSource = new DIYDataSource();
        try {
            pro.load(new FileInputStream("F:\\Java项目\\jdbc_demo\\src\\main\\resources\\jdbc.properties"));
            jdbcDriver = pro.getProperty("jdbcDriver");
            jdbcUrl = pro.getProperty("jdbcUrl");
            userName = pro.getProperty("userName");
            password = pro.getProperty("password");

            minPoolSize = pro.getProperty("minPoolSize");
            maxPoolSize = pro.getProperty("maxPoolSize");
            initialPoolSize = pro.getProperty("initialPoolSize");
            maxIdleTime = pro.getProperty("maxIdleTime");

            dataSource.setJdbcDriver(jdbcDriver);
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUserName(userName);
            dataSource.setPassword(password);
            dataSource.setMinPoolSize(Integer.parseInt(minPoolSize));//String转int
            dataSource.setMaxPoolSize(Integer.parseInt(maxPoolSize));
            dataSource.setMaxIdleTime(Integer.parseInt(maxIdleTime));
            dataSource.setInitialPoolSize(Integer.parseInt(initialPoolSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DIYConnection getConnection() {
        DIYConnection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}
