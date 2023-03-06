package com.jerson.hcdc_portal.util;


/*
*  CUSTOM EXCEPTION  { NOT YET IMPLEMENTED}
*
* */
public class onResponseException extends Exception{
    public onResponseException(int errorMessage,Throwable err) {
        super(String.valueOf(errorMessage), err);
    }
    public onResponseException(String errorMessage) {
        super(errorMessage);
    }
}
