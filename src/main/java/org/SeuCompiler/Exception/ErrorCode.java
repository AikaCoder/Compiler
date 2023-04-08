package org.SeuCompiler.Exception;

public interface ErrorCode {
    //使用接口

    /**
     * 获取错误码
     * @return 错误码
     */
    String getCode();

    /**
     * 获取错误描述
     * @return 错误描述
     */
    String getDescription();
}
