package org.SeuCompiler.Exception;

import java.util.Objects;

public enum CompilerErrorCodeEnum implements ErrorCode {
    //compiler 异常00xx
    //未定义异常
    UNSPECIFIED_ERROR("0000", "未定义的Compiler异常"),

    ;

    /**
     * 错误码, 原则上不唯一, 但实际上可以在不同ErrorEnum类中重复
     */
    private final String code;
    private final String description;

    /**
     * 定义错误枚举
     * @param code 错误码
     * @param description 错误描述
     */
    CompilerErrorCodeEnum(final String code, final String description){
        this.code = code;
        this.description = description;
    }

    /**
     * 根据错误码得到对应的错误枚举
     * @param code 错误码
     * @return 枚举
     */
    public static CompilerErrorCodeEnum getByCode(String code){
        for(CompilerErrorCodeEnum value : CompilerErrorCodeEnum.values()){
            if(Objects.equals(code, value.getCode())){
                return value;
            }
        }
        return UNSPECIFIED_ERROR;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
