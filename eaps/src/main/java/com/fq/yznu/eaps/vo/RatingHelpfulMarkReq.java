package com.fq.yznu.eaps.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingHelpfulMarkReq {
    @NotNull(message = "评分ID不能为空")
    private Long ratingId;

    @NotNull(message = "有用性标记不能为空")
    private Boolean helpful;
}