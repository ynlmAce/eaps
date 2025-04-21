package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rating_helpful_mark")
public class RatingHelpfulMark {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long ratingId;

    private Long userId;

    private LocalDateTime createTime;
}