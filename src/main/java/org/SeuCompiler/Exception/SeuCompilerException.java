package org.SeuCompiler.Exception;

/**
 * 自定义异常类
 * 参考自: <a href="https://blog.csdn.net/weixin_38399962/article/details/79582569">自定义异常处理</a>
 */
public class SeuCompilerException extends Exception {

    /** 错误码 */
    private final ErrorCode errorCode;
    /** 其他信息, 帮助解决 bug */
    private String otherInfo = null;
    /** 错误所在行号 **/
    private Integer lineNum = null;

    /**
     * 默认无参构造, 制定默认错误为 Unspecified
     */
    public SeuCompilerException(){
        super(CompilerErrorCode.UNSPECIFIED_ERROR.getDescription());
        this.errorCode = CompilerErrorCode.UNSPECIFIED_ERROR;
    }

    /**
     * 根据指定描述构造通用异常
     * @param description 对异常的描述
     */
    public SeuCompilerException(final String description){
        super(description);
        this.errorCode = CompilerErrorCode.UNSPECIFIED_ERROR;
    }

    /**
     * 根据指定描述构造通用异常
     * @param description 对异常的描述
     * @param otherInfo 帮助定位异常的其他信息
     */
    public SeuCompilerException(final String description, final String otherInfo){
        super(description);
        this.errorCode = CompilerErrorCode.UNSPECIFIED_ERROR;
        this.otherInfo = otherInfo;
    }
    /**
     * 根据指定错误码创建异常
     * @param errorCode 指定错误码
     */
    public SeuCompilerException(final ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    /**
     * 根据指定错误码创建异常
     * @param errorCode 错误码
     * @param otherInfo 帮助定位异常的其他信息
     */
    public SeuCompilerException(final ErrorCode errorCode, final String otherInfo){
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.otherInfo = otherInfo;
    }

    /**
     * 构造通用异常
     * @param errorCode 错误码
     * @param t 导火索
     */
    public SeuCompilerException(final ErrorCode errorCode, final  Throwable t){
        super(errorCode.getDescription(), t);
        this.errorCode = errorCode;
    }

    /**
     * 构造通用异常
     * @param description 错误描述
     * @param t 导火索
     */
    public SeuCompilerException(final String description, final  Throwable t){
        super(description, t);
        this.errorCode = CompilerErrorCode.UNSPECIFIED_ERROR;
    }

    /**
     * 构造通用异常
     * @param errorCode 错误码
     * @param description   描述
     * @param t 导火索
     */
    public SeuCompilerException(final ErrorCode errorCode, final String description, final  Throwable t){
        super(description, t);
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public String getDescription(){
        return errorCode.getDescription()+"\n"+super.getMessage();
    }

    public String getOtherInfo(){
        return this.otherInfo;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public Integer getLineNum() {
        return lineNum;
    }
}
