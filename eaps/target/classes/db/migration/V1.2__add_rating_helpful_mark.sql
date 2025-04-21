CREATE TABLE rating_helpful_mark (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rating_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_rating_user (rating_id, user_id)
) COMMENT '评分有用标记表'; 