package com.codekeys.execption;

public class OperationNotPermitException extends RuntimeException{
    public OperationNotPermitException(String message){
        super(message);
    }
}
