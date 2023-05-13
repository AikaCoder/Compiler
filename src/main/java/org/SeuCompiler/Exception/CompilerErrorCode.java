package org.SeuCompiler.Exception;

import java.util.Objects;

public enum CompilerErrorCode implements ErrorCode {
    //compiler 异常00xx
    //未定义异常
    UNSPECIFIED_ERROR("0000", "未定义的Compiler异常"),
    BUILD_NFA_FAILED("0001", "未能成果构建NFA");
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
    CompilerErrorCode(final String code, final String description){
        this.code = code;
        this.description = description;
    }

    /**
     * 根据错误码得到对应的错误枚举
     * @param code 错误码
     * @return 枚举
     */
    public static CompilerErrorCode getByCode(String code){
        for(CompilerErrorCode value : CompilerErrorCode.values()){
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
