package com.recipe.manager.entrypoint.exception;

public class ValidationErrorResponse {
    private String field;
    private String message;

    public ValidationErrorResponse(String field, String message) {
        this.message = message;
        this.field = field;
    }


    public void setField(String field) {
        this.field = field;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String getField() {
        return this.field;
    }
}