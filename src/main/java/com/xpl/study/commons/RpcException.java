package com.xpl.study.commons;

/**
 * 异常
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class RpcException extends RuntimeException{

    private String errorCode;

    private String errorMessage;

    public RpcException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
