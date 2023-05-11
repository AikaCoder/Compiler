package org.SeuCompiler.Exception;

/**
 * 错误枚举类, 定义了所有可能的错误
 * 参考自: <a href="https://blog.csdn.net/weixin_38399962/article/details/79582569">自定义异常处理</a>
 */
public enum LexParserErrorCode implements ErrorCode {
    //LexParser异常 12xx
    lex_file_IO_exception("1201", "Lex输入文件IO错误"),
    bad_lex_file_structure("1202", "Lex输入文件结构错误"),
    invalid_regex_alias("1203", "错误的Regex别名设置"),

    action_error("1204", "动作部分错误"),

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
    LexParserErrorCode(final String code, final String description){
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