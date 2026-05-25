CREATE TABLE `user_preference_profile` (
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `preference_text` text COMMENT '用户偏好描述',
  `preference_vector` blob COMMENT '用户偏好向量',
  `last_behavior_time` datetime DEFAULT NULL COMMENT '最后动作时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好画像表';
