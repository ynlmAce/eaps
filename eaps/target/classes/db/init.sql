-- 创建数据库
CREATE DATABASE IF NOT EXISTS eaps CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE eaps;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名/账号',
  `password` varchar(100) NOT NULL COMMENT '密码(加密存储)',
  `role` varchar(20) NOT NULL COMMENT '角色:student,enterprise,admin,super_admin,counselor',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态:active,inactive,disabled',
  `registration_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `notification_settings` json DEFAULT NULL COMMENT '通知偏好设置',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_email` (`email`),
  UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学生表
CREATE TABLE IF NOT EXISTS `student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `student_number` varchar(50) NOT NULL COMMENT '学号',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `university` varchar(100) NOT NULL COMMENT '毕业院校',
  `college` varchar(100) NOT NULL COMMENT '学院',
  `major` varchar(100) NOT NULL COMMENT '专业',
  `class_name` varchar(50) NOT NULL COMMENT '班级',
  `grade` varchar(20) NOT NULL COMMENT '年级',
  `resume_path` varchar(255) DEFAULT NULL COMMENT '简历文件存储路径',
  `public_info_settings` json DEFAULT NULL COMMENT '公开信息设置',
  `graduation_year` int DEFAULT NULL COMMENT '毕业年份',
  `employment_status` varchar(20) DEFAULT 'seeking' COMMENT '就业状态:seeking,employed,further_study',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `idx_student_number` (`student_number`),
  KEY `idx_university_major` (`university`,`major`),
  KEY `idx_grade` (`grade`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 企业表
CREATE TABLE IF NOT EXISTS `enterprise` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `enterprise_name` varchar(100) NOT NULL COMMENT '企业名称',
  `credit_code` varchar(100) NOT NULL COMMENT '统一社会信用代码',
  `legal_representative` varchar(50) NOT NULL COMMENT '法定代表人',
  `industry` varchar(50) NOT NULL COMMENT '行业',
  `scale` varchar(20) NOT NULL COMMENT '规模:small,medium,large',
  `type` varchar(20) NOT NULL COMMENT '企业类型:state,private,foreign,joint',
  `description` text COMMENT '企业介绍文本',
  `logo_path` varchar(255) DEFAULT NULL COMMENT 'Logo图片路径',
  `rating_total_score` decimal(3,1) DEFAULT '0.0' COMMENT '综合评分总分',
  `rating_count` int DEFAULT '0' COMMENT '参与评分人数',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '企业地址',
  `website` varchar(255) DEFAULT NULL COMMENT '企业网站',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending,approved,rejected,disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `idx_credit_code` (`credit_code`),
  KEY `idx_enterprise_name` (`enterprise_name`),
  KEY `idx_industry_scale_type` (`industry`,`scale`,`type`),
  CONSTRAINT `fk_enterprise_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业表';

-- 辅导员表
CREATE TABLE IF NOT EXISTS `counselor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `counselor_name` varchar(50) NOT NULL COMMENT '辅导员姓名',
  `employee_number` varchar(50) NOT NULL COMMENT '工号',
  `counselor_college` varchar(100) NOT NULL COMMENT '所属学院',
  `position` varchar(50) NOT NULL COMMENT '职务',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `idx_employee_number` (`employee_number`),
  KEY `idx_counselor_college` (`counselor_college`),
  CONSTRAINT `fk_counselor_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='辅导员表';

-- 职位表
CREATE TABLE IF NOT EXISTS `job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `job_title` varchar(100) NOT NULL COMMENT '职位名称',
  `department` varchar(50) DEFAULT NULL COMMENT '部门',
  `description` text NOT NULL COMMENT '职位描述',
  `requirements` text NOT NULL COMMENT '职位要求',
  `salary_min` int NOT NULL COMMENT '最低薪资(元/月)',
  `salary_max` int NOT NULL COMMENT '最高薪资(元/月)',
  `location` varchar(100) NOT NULL COMMENT '工作地点',
  `job_type` varchar(20) NOT NULL COMMENT '职位类型:full_time,part_time,internship',
  `education_requirement` varchar(20) DEFAULT NULL COMMENT '学历要求:high_school,bachelor,master,doctor',
  `experience_requirement` varchar(50) DEFAULT NULL COMMENT '经验要求',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `deadline` date DEFAULT NULL COMMENT '截止日期',
  `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft,pending_review,open,closed,rejected',
  `applicants_count` int DEFAULT '0' COMMENT '申请人数统计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_job_title` (`job_title`),
  KEY `idx_location_job_type` (`location`,`job_type`),
  KEY `idx_salary` (`salary_min`,`salary_max`),
  KEY `idx_status_publish_time` (`status`,`publish_time`),
  CONSTRAINT `fk_job_enterprise` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 申请表
CREATE TABLE IF NOT EXISTS `application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `job_id` bigint NOT NULL COMMENT '职位ID',
  `application_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `status` varchar(20) NOT NULL DEFAULT 'applied' COMMENT '申请状态:applied,viewed,interviewing,rejected,hired',
  `resume_path` varchar(255) DEFAULT NULL COMMENT '简历文件路径',
  `cover_letter_path` varchar(255) DEFAULT NULL COMMENT '求职信文件路径',
  `other_attachments` json DEFAULT NULL COMMENT '其他附件路径',
  `status_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_student_job` (`student_id`,`job_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_status_update_time` (`status`,`status_update_time`),
  CONSTRAINT `fk_application_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_application_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='申请表';

-- 评分评价表
CREATE TABLE IF NOT EXISTS `rating` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `job_id` bigint DEFAULT NULL COMMENT '职位ID(如关联特定职位)',
  `rating_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `overall_score` decimal(2,1) NOT NULL COMMENT '总评分星级(1-5)',
  `dimension_scores` json DEFAULT NULL COMMENT '各维度评分',
  `content` text COMMENT '评价文本',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否匿名:0否,1是',
  `is_modified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否修改过:0否,1是',
  `modified_time` datetime DEFAULT NULL COMMENT '修改时间',
  `company_reply` text COMMENT '企业回复文本',
  `reply_time` datetime DEFAULT NULL COMMENT '企业回复时间',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '评价状态:pending,approved,rejected,under_appeal',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_rating_time_status` (`rating_time`,`status`),
  CONSTRAINT `fk_rating_enterprise` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rating_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_rating_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评分评价表';

-- 评分图片表
CREATE TABLE IF NOT EXISTS `rating_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分图片ID',
  `rating_id` bigint NOT NULL COMMENT '评分ID',
  `image_path` varchar(255) NOT NULL COMMENT '图片路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rating_id` (`rating_id`),
  CONSTRAINT `fk_rating_image_rating` FOREIGN KEY (`rating_id`) REFERENCES `rating` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评分图片表';

-- 评分申诉表
CREATE TABLE IF NOT EXISTS `rating_appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
  `rating_id` bigint NOT NULL COMMENT '评分ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `appeal_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申诉时间',
  `appeal_reason` text NOT NULL COMMENT '申诉理由',
  `evidence` json DEFAULT NULL COMMENT '佐证材料',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '申诉状态:pending,approved,rejected',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_result` text COMMENT '处理结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rating_id` (`rating_id`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_status_appeal_time` (`status`,`appeal_time`),
  CONSTRAINT `fk_appeal_enterprise` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_appeal_rating` FOREIGN KEY (`rating_id`) REFERENCES `rating` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评分申诉表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` varchar(20) NOT NULL COMMENT '会话类型:student_enterprise,student_student_group',
  `participant1_id` bigint NOT NULL COMMENT '参与者1 ID',
  `participant2_id` bigint NOT NULL COMMENT '参与者2 ID',
  `job_id` bigint DEFAULT NULL COMMENT '关联职位ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '会话状态:active,closed',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_participants` (`participant1_id`,`participant2_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_status_last_time` (`status`,`last_message_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content_type` varchar(20) NOT NULL DEFAULT 'text' COMMENT '内容类型:text,image,file',
  `content` text NOT NULL COMMENT '消息内容文本或文件路径',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` varchar(20) NOT NULL DEFAULT 'sent' COMMENT '状态:sent,delivered,read',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id_send_time` (`session_id`,`send_time`),
  KEY `idx_sender_id` (`sender_id`),
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 聊天群组表
CREATE TABLE IF NOT EXISTS `chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `group_name` varchar(100) NOT NULL COMMENT '群组名称',
  `group_type` varchar(20) NOT NULL COMMENT '群组类型:student,career,enterprise,study',
  `description` text COMMENT '群组介绍',
  `avatar` varchar(255) DEFAULT NULL COMMENT '群组头像',
  `creation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `owner_id` bigint NOT NULL COMMENT '群主ID',
  `need_approval` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否需要审核:0否,1是',
  `member_count` int NOT NULL DEFAULT '1' COMMENT '成员数量',
  `tags` json DEFAULT NULL COMMENT '群组标签',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_name_type` (`group_name`,`group_type`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='聊天群组表';

-- 群组成员表
CREATE TABLE IF NOT EXISTS `chat_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `is_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否管理员:0否,1是',
  `nickname` varchar(50) DEFAULT NULL COMMENT '群内昵称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_member_group` FOREIGN KEY (`group_id`) REFERENCES `chat_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';

-- 企业宣传内容表
CREATE TABLE IF NOT EXISTS `enterprise_promotion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宣传内容ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `content_type` varchar(20) NOT NULL COMMENT '内容类型:enterprise_intro,event,other',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容文本',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft,pending_review,published,rejected,taken_down',
  `submission_time` datetime DEFAULT NULL COMMENT '提交审核时间',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `rejection_reason` text COMMENT '驳回原因',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间(宣讲会等)',
  `event_location` varchar(255) DEFAULT NULL COMMENT '事件地点',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_status_publish_time` (`status`,`publish_time`),
  KEY `idx_content_type_event_time` (`content_type`,`event_time`),
  CONSTRAINT `fk_promotion_enterprise` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业宣传内容表';

-- 企业宣传图片表
CREATE TABLE IF NOT EXISTS `promotion_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `promotion_id` bigint NOT NULL COMMENT '宣传内容ID',
  `image_path` varchar(255) NOT NULL COMMENT '图片路径',
  `image_desc` varchar(255) DEFAULT NULL COMMENT '图片描述',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_promotion_id` (`promotion_id`),
  CONSTRAINT `fk_image_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `enterprise_promotion` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业宣传图片表';

-- 管理员操作日志表
CREATE TABLE IF NOT EXISTS `admin_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_user_id` bigint NOT NULL COMMENT '管理员ID',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_module` varchar(50) NOT NULL COMMENT '操作模块',
  `target_type` varchar(50) NOT NULL COMMENT '操作对象类型',
  `target_id` bigint DEFAULT NULL COMMENT '操作对象ID',
  `details` json DEFAULT NULL COMMENT '操作详情',
  `result` varchar(20) NOT NULL COMMENT '操作结果:success,fail',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '操作者IP',
  `cost_time` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_user_id` (`admin_user_id`),
  KEY `idx_operation_time_type` (`operation_time`,`operation_type`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `channel` varchar(20) NOT NULL COMMENT '通知渠道:in_app,email,sms,push',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending,sent,failed,read',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `related_id` bigint DEFAULT NULL COMMENT '关联的业务ID',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联的业务类型',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_create_time` (`status`,`create_time`),
  KEY `idx_related` (`related_type`,`related_id`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置项键名',
  `config_value` text NOT NULL COMMENT '配置项值',
  `config_type` varchar(20) NOT NULL COMMENT '配置类型:string,number,boolean,json',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注说明',
  `last_modified_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `modified_by_user_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 辅导员负责学生关联表
CREATE TABLE IF NOT EXISTS `counselor_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `assignment_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_counselor_student` (`counselor_id`,`student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_cs_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cs_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='辅导员负责学生关联表';

-- 就业统计表
CREATE TABLE IF NOT EXISTS `employment_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `year` int NOT NULL COMMENT '年份',
  `university` varchar(100) DEFAULT NULL COMMENT '学校',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级',
  `total_students` int NOT NULL DEFAULT '0' COMMENT '学生总数',
  `employed_count` int NOT NULL DEFAULT '0' COMMENT '已就业人数',
  `further_study_count` int NOT NULL DEFAULT '0' COMMENT '继续深造人数',
  `seeking_count` int NOT NULL DEFAULT '0' COMMENT '求职中人数',
  `employment_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '就业率(%)',
  `counselor_id` bigint DEFAULT NULL COMMENT '负责辅导员ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_year_university_college_major_class` (`year`,`university`,`college`,`major`,`class_name`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_employment_rate` (`employment_rate`),
  CONSTRAINT `fk_statistics_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='就业统计表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) NOT NULL COMMENT '角色标识',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态:0正常,1停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `permission_key` varchar(100) NOT NULL COMMENT '权限标识',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态:0正常,1停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_permission_key` (`permission_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_permission` (`role_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 群组加入申请表
CREATE TABLE IF NOT EXISTS `group_join_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '申请用户ID',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `reason` varchar(255) DEFAULT NULL COMMENT '申请理由',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending,approved,rejected',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `processor_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `rejection_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_user` (`group_id`,`user_id`,`status`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_apply_time` (`status`,`apply_time`),
  CONSTRAINT `fk_join_app_group` FOREIGN KEY (`group_id`) REFERENCES `chat_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_join_app_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='群组加入申请表';

-- 插入初始数据

-- 插入角色数据
INSERT INTO `role` (`role_name`, `role_key`, `role_sort`, `status`, `remark`)
VALUES 
('超级管理员', 'super_admin', 1, '0', '超级管理员'),
('管理员', 'admin', 2, '0', '管理员'),
('辅导员', 'counselor', 3, '0', '辅导员'),
('企业', 'enterprise', 4, '0', '企业用户'),
('学生', 'student', 5, '0', '学生用户');

-- 插入系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `remark`)
VALUES 
('system_name', '大学生就业帮扶平台', 'string', '系统名称'),
('rating_min_count', '5', 'number', '企业评分显示的最小评价数量'),
('rating_dimension_weights', '{"environment":0.2,"salary":0.3,"development":0.3,"culture":0.2}', 'json', '评分维度权重'),
('upload_file_max_size', '10485760', 'number', '上传文件的最大大小（字节）'),
('allowed_resume_extensions', 'pdf,doc,docx', 'string', '允许的简历文件扩展名'),
('allowed_image_extensions', 'jpg,jpeg,png,gif', 'string', '允许的图片文件扩展名'),
('message_per_page', '20', 'number', '聊天记录每页显示条数'),
('inactive_session_timeout', '7', 'number', '聊天会话不活跃超时天数');

-- 创建超级管理员账号
INSERT INTO `user` (`username`, `password`, `role`, `status`, `email`, `phone`, `notification_settings`)
VALUES 
('admin', '$2a$10$LvMQKqLosDgVuKjVmKfI7.Upl0B8XjqTa/qaDroX.zwwfiPDDrQl.', 'super_admin', 'active', 'admin@example.com', '13800000000', '{"inApp":true,"email":true,"sms":false}');

-- 关联超级管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`)
VALUES 
(1, (SELECT id FROM `role` WHERE role_key = 'super_admin'));
