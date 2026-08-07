package com.library.requestservice.exception;
public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String id) {
        super("Borrow request not found with id: " + id);
    }
}
