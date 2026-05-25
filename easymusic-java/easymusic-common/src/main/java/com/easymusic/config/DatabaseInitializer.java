package com.easymusic.config;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    @org.springframework.context.annotation.Lazy
    private com.easymusic.service.MilvusService milvusService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and initializing database tables...");
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `user_preference_profile` (" +
                    "  `user_id` varchar(50) NOT NULL COMMENT '用户ID'," +
                    "  `preference_text` text COMMENT '用户偏好描述'," +
                    "  `preference_vector` blob COMMENT '用户偏好向量'," +
                    "  `last_behavior_time` datetime DEFAULT NULL COMMENT '最后动作时间'," +
                    "  `update_time` datetime NOT NULL COMMENT '更新时间'," +
                    "  PRIMARY KEY (`user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好画像表';";
            jdbcTemplate.execute(sql);
            log.info("Database table user_preference_profile checked/created successfully.");

            // Initialize Milvus and Sync creations
            log.info("Initializing Milvus collection and syncing existing creations...");
            milvusService.initCollection();
            milvusService.syncExistingCreations();
            log.info("Milvus initial sync completed.");
        } catch (Exception e) {
            log.error("Failed to initialize database tables or Milvus", e);
        }
    }
}
