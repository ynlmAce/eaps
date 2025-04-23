-- 插入测试用户数据
INSERT INTO `user` (username, password, role, status, email, phone, notification_settings, avatar, create_time, update_time, last_login_time)
VALUES 
('student1', '$2a$10$LvMQKqLosDgVuKjVmKfI7.Upl0B8XjqTa/qaDroX.zwwfiPDDrQl.', 'student', 'active', 'student1@test.com', '13800000001', '{"inApp":true,"email":true,"sms":false}', '/avatars/student1.jpg', NOW(), NOW(), NOW()),
('student2', '$2a$10$LvMQKqLosDgVuKjVmKfI7.Upl0B8XjqTa/qaDroX.zwwfiPDDrQl.', 'student', 'active', 'student2@test.com', '13800000002', '{"inApp":true,"email":true,"sms":false}', '/avatars/student2.jpg', NOW(), NOW(), NOW()),
('enterprise1', '$2a$10$LvMQKqLosDgVuKjVmKfI7.Upl0B8XjqTa/qaDroX.zwwfiPDDrQl.', 'enterprise', 'active', 'enterprise1@test.com', '13800000003', '{"inApp":true,"email":true,"sms":true}', '/avatars/enterprise1.jpg', NOW(), NOW(), NOW()),
('counselor1', '$2a$10$LvMQKqLosDgVuKjVmKfI7.Upl0B8XjqTa/qaDroX.zwwfiPDDrQl.', 'counselor', 'active', 'counselor1@test.com', '13800000004', '{"inApp":true,"email":true,"sms":true}', '/avatars/counselor1.jpg', NOW(), NOW(), NOW());

-- 插入测试学生数据
INSERT INTO `student` (user_id, student_number, real_name, university, college, major, class_name, grade, employment_status)
VALUES 
((SELECT id FROM `user` WHERE username = 'student1'), '2021001', '张三', '云南师范大学', '信息学院', '计算机科学与技术', '计科2101', '2021', 'seeking'),
((SELECT id FROM `user` WHERE username = 'student2'), '2021002', '李四', '云南师范大学', '信息学院', '软件工程', '软工2101', '2021', 'seeking');

-- 插入测试企业数据
INSERT INTO `enterprise` (user_id, enterprise_name, credit_code, legal_representative, industry, scale, type, description, contact_person, contact_phone, address, status)
VALUES 
((SELECT id FROM `user` WHERE username = 'enterprise1'), '云南科技有限公司', '91530000123456789X', '王五', '互联网/IT', 'medium', 'private', 
'云南科技有限公司是一家专注于互联网技术开发的企业，成立于2010年，主要业务包括软件开发、系统集成等。', 
'王五', '13800000003', '云南省昆明市五华区科技路123号', 'approved');

-- 插入测试辅导员数据
INSERT INTO `counselor` (user_id, counselor_name, employee_number, counselor_college, position)
VALUES 
((SELECT id FROM `user` WHERE username = 'counselor1'), '赵六', 'T2021001', '信息学院', '辅导员');

-- 插入测试职位数据
INSERT INTO `job` (enterprise_id, job_title, department, description, requirements, salary_min, salary_max, location, job_type, education_requirement, experience_requirement, status)
VALUES 
((SELECT id FROM `enterprise` WHERE enterprise_name = '云南科技有限公司'), 'Java开发工程师', '技术部', 
'负责公司核心业务系统的开发和维护工作', 
'1. 本科及以上学历，计算机相关专业\n2. 熟练掌握Java编程语言\n3. 熟悉Spring、MyBatis等框架\n4. 具有良好的团队协作能力', 
8000, 15000, '昆明市', 'full_time', 'bachelor', '1-3年', 'open'),
((SELECT id FROM `enterprise` WHERE enterprise_name = '云南科技有限公司'), '前端开发工程师', '技术部', 
'负责公司Web前端开发工作', 
'1. 本科及以上学历，计算机相关专业\n2. 熟练掌握HTML、CSS、JavaScript\n3. 熟悉Vue、React等前端框架\n4. 具有良好的团队协作能力', 
7000, 13000, '昆明市', 'full_time', 'bachelor', '1-3年', 'open');

-- 插入测试申请数据
INSERT INTO `application` (student_id, job_id, status, application_time)
VALUES 
((SELECT id FROM `student` WHERE student_number = '2021001'), 
 (SELECT id FROM `job` WHERE job_title = 'Java开发工程师'), 
 'applied', NOW());

-- 插入测试评分数据
INSERT INTO `rating` (student_id, enterprise_id, overall_score, dimension_scores, content, is_anonymous)
VALUES 
((SELECT id FROM `student` WHERE student_number = '2021001'),
 (SELECT id FROM `enterprise` WHERE enterprise_name = '云南科技有限公司'),
 4.5, 
 '{"environment": 4.5, "salary": 4.0, "development": 4.5, "culture": 4.5}',
 '公司环境很好，团队氛围融洽，有很好的发展空间。', 
 0);

-- 插入测试群组数据
INSERT INTO `chat_group` (group_name, group_type, description, owner_id, need_approval)
VALUES 
('2021届计算机专业求职群', 'student', '2021届计算机相关专业学生求职交流群', 
 (SELECT id FROM `user` WHERE username = 'counselor1'), 1);

-- 插入测试群组成员数据
INSERT INTO `chat_group_member` (group_id, user_id, is_admin)
VALUES 
((SELECT id FROM `chat_group` WHERE group_name = '2021届计算机专业求职群'),
 (SELECT id FROM `user` WHERE username = 'student1'),
 0),
((SELECT id FROM `chat_group` WHERE group_name = '2021届计算机专业求职群'),
 (SELECT id FROM `user` WHERE username = 'student2'),
 0),
((SELECT id FROM `chat_group` WHERE group_name = '2021届计算机专业求职群'),
 (SELECT id FROM `user` WHERE username = 'counselor1'),
 1);

-- 插入测试企业宣传内容
INSERT INTO `enterprise_promotion` (enterprise_id, content_type, title, content, status, publish_time)
VALUES 
((SELECT id FROM `enterprise` WHERE enterprise_name = '云南科技有限公司'),
 'event',
 '2024届校园招聘宣讲会',
 '云南科技有限公司2024届校园招聘宣讲会将于2024年3月15日在云南师范大学举行，欢迎同学们参加！',
 'published',
 NOW()); 