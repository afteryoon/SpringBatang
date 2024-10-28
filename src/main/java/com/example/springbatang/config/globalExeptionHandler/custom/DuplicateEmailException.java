package com.example.springbatang.config.globalExeptionHandler.custom;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

}
