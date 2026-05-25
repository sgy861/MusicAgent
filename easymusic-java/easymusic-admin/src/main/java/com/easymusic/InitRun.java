package com.easymusic;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            redisTemplate.getConnectionFactory().getConnection().isClosed();
            logger.error("Admin服务启动成功，可以开始愉快的开发了");
        } catch (SQLException e) {
            logger.error("数据库配置错误，请检查数据库配置");
        } catch (Exception e) {
            logger.error("服务启动失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("关闭数据库连接失败");
                }
            }
        }
    }
}
