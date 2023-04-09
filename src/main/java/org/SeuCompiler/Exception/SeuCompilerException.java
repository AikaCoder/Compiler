package org.SeuCompiler.Exception;

public class SeuCompilerException extends RuntimeException {

    /** 错误码 */
    private final ErrorCode errorCode;
    /** 其他信息, 帮助定位bug */
    private String otherInfo = null;

    /**
     * 默认无参构造, 制定默认错误为 Unspecified
     */
    public SeuCompilerException(){
        super(CompilerErrorCodeEnum.UNSPECIFIED_ERROR.getDescription());
        this.errorCode = CompilerErrorCodeEnum.UNSPECIFIED_ERROR;
    }

    /**
     * 根据指定描述构造通用异常
     * @param description 对异常的描述
     */
    public SeuCompilerException(final String description){
        super(description);
        this.errorCode = CompilerErrorCodeEnum.UNSPECIFIED_ERROR;
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
        this.errorCode = CompilerErrorCodeEnum.UNSPECIFIED_ERROR;
    }

    /**
     * 构造通用异常
     * @param errorCode 错误码
     * @param description   描述
     * @param t 导火索
     */
    public SeuCompilerException(final ErrorCode errorCode, final String description, final  Throwable t){
        super(description, t);
        this.errorCode = CompilerErrorCodeEnum.UNSPECIFIED_ERROR;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setOtherInfo(final String info){
        this.otherInfo = info;
    }

    public String getOtherInfo(){
        return this.otherInfo;
    }
    public void addOtherInfo(final String addInfo){
        this.otherInfo = this.otherInfo+ "\t" + addInfo;
    }
}
