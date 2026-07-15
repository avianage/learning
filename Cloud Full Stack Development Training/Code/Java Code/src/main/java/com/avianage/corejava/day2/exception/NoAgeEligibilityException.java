package com.avianage.corejava.day2.exception;

public class NoAgeEligibilityException extends RuntimeException {
    public static final long serialVersionUID = 9875234857345L;

    public NoAgeEligibilityException(){
        super();
    }

    public NoAgeEligibilityException(String message){
        super(message);
    }
}
