package com.commercial.project.exceptions;

public class BusinessRuleException extends RuntimeException{
    public BusinessRuleException(String message){
        super(message);
    }
}
