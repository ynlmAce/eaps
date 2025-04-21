-- 添加密保问题和答案字段
ALTER TABLE user
    ADD COLUMN security_question VARCHAR(255) COMMENT '密保问题',
    ADD COLUMN security_answer VARCHAR(255) COMMENT '密保答案'; 