package com.qg.common.exception;

public class NoPerssionException extends BaseException{
    public NoPerssionException(String message) {

        super(403,message);
    }
}
