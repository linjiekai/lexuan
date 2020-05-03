package com.zhuanbo.core.exception;


public class StockException extends RuntimeException{

    private String message;
    public StockException() {
        super();
    }

    public StockException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
