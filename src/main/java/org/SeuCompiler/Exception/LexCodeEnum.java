package org.SeuCompiler.Exception;

import java.util.Objects;

/**
 * 错误枚举类, 定义了所有可能的错误
 * 参考自: <a href="https://blog.csdn.net/weixin_38399962/article/details/79582569">自定义异常处理</a>
 */
public enum LexCodeEnum implements ErrorCode {
    //Lex异常 10xx
    UNKNOWN_LEX_ERROR("1000", "未知的Lex词法分析器错误"),

    //Regex异常 11xx
    UNKNOWN_REGEX_ERROR("1100", "未知的Regex错误"),

    REGEX_GRAMMAR_ERROR("1101", "正则表达语法错误"),

    NO_CORRESPONDING_USER_DEFINE("1102", "没有与简写对应的用户定义"),

    //LexParser异常 12xx
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
    LexCodeEnum(final String code, final String description){
        this.code = code;
        this.description = description;
    }

    /**
     * 根据错误码得到对应的错误枚举
     * @param code 错误码
     * @return 枚举
     */
    public static LexCodeEnum getByCode(String code){
        for(LexCodeEnum value : LexCodeEnum.values()){
            if(Objects.equals(code, value.getCode())){
                return value;
            }
        }
        return UNKNOWN_LEX_ERROR;
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
