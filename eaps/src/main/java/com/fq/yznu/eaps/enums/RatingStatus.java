package com.fq.yznu.eaps.enums;

public enum RatingStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    APPEALED(3, "已申诉");

    private final Integer code;
    private final String description;

    RatingStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}