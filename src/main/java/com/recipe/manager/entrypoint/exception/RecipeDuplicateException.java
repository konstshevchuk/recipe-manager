package com.recipe.manager.entrypoint.exception;

public class RecipeDuplicateException extends RuntimeException{
    public RecipeDuplicateException(String message) {
        super(message);
    }
}
