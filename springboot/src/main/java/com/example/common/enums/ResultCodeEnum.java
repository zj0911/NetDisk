package com.example.common.enums;

public enum ResultCodeEnum {
    SUCCESS("200", "success"),

    PARAM_ERROR("400", "param error"),
    TOKEN_INVALID_ERROR("401", "token invalid"),
    TOKEN_CHECK_ERROR("401", "token error，retry"),
    PARAM_LOST_ERROR("4001", "lose param"),

    SYSTEM_ERROR("500", "system error"),
    USER_EXIST_ERROR("5001", "user exist"),
    USER_NOT_LOGIN("5002", "user not login"),
    USER_ACCOUNT_ERROR("5003", "account or password error"),
    USER_NOT_EXIST_ERROR("5004", "user not exist"),
    PARAM_PASSWORD_ERROR("5005", "password error"),
    FILE_NOT_FOUND("5006", "file not found"),
    TYPE_NOT_SUPPORT("5007", "type not support"),
    ;

    public String code;
    public String msg;

    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
