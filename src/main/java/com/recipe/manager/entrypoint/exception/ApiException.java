package com.recipe.manager.entrypoint.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ApiErrorResponse apiErrorResponse;

    public ApiException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.apiErrorResponse = new ApiErrorResponse(httpStatus.getReasonPhrase(), this.translateHttpStatus(httpStatus).name());
    }

    public ApiException(HttpStatus httpStatus, String message, Enum<?> e) {
        this.httpStatus = httpStatus;
        this.apiErrorResponse = new ApiErrorResponse(message, e.name());
    }

    public String getMessage() {
        return this.apiErrorResponse.getMessage();
    }

    public String getCode() {
        return this.apiErrorResponse.getCode();
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public ApiErrorResponse getApiErrorResponse() {
        return this.apiErrorResponse;
    }

    private ApiErrorCode translateHttpStatus(HttpStatus httpStatus) {
        return switch (httpStatus) {
            case BAD_REQUEST -> ApiErrorCode.BadRequest;
            case NOT_FOUND -> ApiErrorCode.NotFound;
            case INTERNAL_SERVER_ERROR -> ApiErrorCode.InternalServerError;
            case METHOD_NOT_ALLOWED -> ApiErrorCode.MethodNotAllowed;
            case UNSUPPORTED_MEDIA_TYPE -> ApiErrorCode.UnsupportedMediaType;
            default -> ApiErrorCode.UnknownError;
        };
    }
}
