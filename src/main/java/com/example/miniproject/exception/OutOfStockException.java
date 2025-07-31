package com.example.miniproject.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }
}
