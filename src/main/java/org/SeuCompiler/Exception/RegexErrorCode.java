package org.SeuCompiler.Exception;

/**
 * 错误枚举类, 定义了所有可能的错误
 * 参考自: <a href="https://blog.csdn.net/weixin_38399962/article/details/79582569">自定义异常处理</a>
 */
public enum RegexErrorCode implements ErrorCode {
    //Regex异常 11xx
    unknown_regex_error("1100", "未知的Regex错误"),
    range_expand_wrong("1101", "Regex方括号内范围扩展错误."),
    no_corresponding_former("1102", "Regex内前面成对符号没有匹配."),
    no_support_function("1103", "Regex尚不支持的功能"),
    unexpected_character("1104","错误输入的Regex符号"),

    not_find_alias("1105", "没有找到与输入对应的别名"),
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
    RegexErrorCode(final String code, final String description){
        this.code = code;
        this.description = description;
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
