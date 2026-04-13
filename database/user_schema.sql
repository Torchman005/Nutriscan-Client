-- 用户信息表
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_uid` VARCHAR(64) NOT NULL COMMENT '用户唯一标识(UUID)',
  `nickname` VARCHAR(64) DEFAULT '新用户' COMMENT '用户昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  
  -- 认证相关
  `login_type` TINYINT(4) NOT NULL COMMENT '登录方式: 1-微信(已移除), 2-手机号验证码',
  `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '手机号(手机号登录时必填)',
  `wechat_open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID(已移除)',
  
  -- 身体数据 (参考图二、图四)
  `gender` TINYINT(1) DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `height` DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '当前体重(kg/斤)',
  `target_weight` DECIMAL(5,2) DEFAULT NULL COMMENT '目标体重',
  `waistline` DECIMAL(5,2) DEFAULT NULL COMMENT '腰围(cm)',
  `bmi` DECIMAL(4,1) DEFAULT NULL COMMENT 'BMI指数',
  `bmr` INT(5) DEFAULT NULL COMMENT '基础代谢(kcal)',
  
  -- 偏好与设置 (参考图三、图四)
  `group_category` VARCHAR(20) DEFAULT 'FITNESS' COMMENT '所属群体: HEALTH(养生), FITNESS(健身), TODDLER(幼儿)',
  
  -- 状态与时间
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_at` TIMESTAMP DEFAULT NULL COMMENT '最后登录时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_uid` (`user_uid`),
  UNIQUE KEY `uk_phone` (`phone_number`),
  KEY `idx_login_type` (`login_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP用户信息表';

-- 短信验证码记录表 (用于验证码登录/注册)
CREATE TABLE IF NOT EXISTS `sms_verification_code` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '接收手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `biz_type` TINYINT(4) DEFAULT 1 COMMENT '业务类型: 1-登录/注册, 2-更换手机号',
  `is_used` TINYINT(1) DEFAULT 0 COMMENT '是否已使用: 0-未使用, 1-已使用',
  `expire_at` TIMESTAMP NOT NULL COMMENT '过期时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_phone_biz` (`phone_number`, `biz_type`, `created_at`),
  KEY `idx_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码记录表';

-- 用户卡路里记录表（按时间查询）
CREATE TABLE IF NOT EXISTS `user_calorie_log` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_uid` VARCHAR(64) NOT NULL COMMENT '用户唯一标识(UUID)',
  `calories` DECIMAL(10,2) NOT NULL COMMENT '卡路里数值(kcal)',
  `recorded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',

  PRIMARY KEY (`id`),
  KEY `idx_user_uid` (`user_uid`),
  KEY `idx_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户卡路里记录表';

-- 用户体重记录表 (用于体重折线图)
CREATE TABLE IF NOT EXISTS `user_weight_log` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_uid` VARCHAR(64) NOT NULL COMMENT '用户唯一标识(UUID)',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  `recorded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_weight_user_time` (`user_uid`, `recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户体重记录';

-- 示例数据插入语句
-- INSERT INTO `app_user` (`user_uid`, `nickname`, `login_type`, `phone_number`, `height`, `weight`, `age`, `group_category`) 
-- VALUES ('uuid-1234', '新用户178338564', 2, '13800138000', 170.0, 56.0, 24, 'FITNESS');
