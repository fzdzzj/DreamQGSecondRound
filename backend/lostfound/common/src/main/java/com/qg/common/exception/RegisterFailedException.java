package com.qg.common.exception;

public class RegisterFailedException extends BaseException{
    public RegisterFailedException(String msg){
        super(401,msg);
    }
}
