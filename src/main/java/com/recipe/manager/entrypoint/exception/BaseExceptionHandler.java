package com.recipe.manager.entrypoint.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

public class BaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    private static final Map<Class<? extends Exception>, HttpStatus> SPRING_BUILTIN_EXCEPTION_MAPPINGS;
    private static final Map<Class<? extends Exception>, HttpStatus> KNOWN_EXCEPTION_MAPPINGS;

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException e) {
        logger.debug("{}: {}", e.getHttpStatus(), e.getMessage());
        return new ResponseEntity<>(e.getApiErrorResponse(), e.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFormatException(HttpMessageNotReadableException ex) {
        String errorMessage = "123";
        return ResponseEntity.badRequest().body(new ApiErrorResponse(errorMessage, ApiErrorCode.InvalidParameter.name()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid parameter value [%s] for '%s'", ex.getValue(), ex.getName());
        return ResponseEntity.badRequest().body(new ApiErrorResponse("invalid value", message));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        if (SPRING_BUILTIN_EXCEPTION_MAPPINGS.containsKey(e.getClass())) {
            HttpStatus httpCode = SPRING_BUILTIN_EXCEPTION_MAPPINGS.get(e.getClass());
            ApiException apiExp = new ApiException(httpCode);
            logger.error("exception in spring built in {}  {}", e.getClass().getName(), e.getMessage(), e);

            return e instanceof HttpMediaTypeNotAcceptableException ? new ResponseEntity<>(httpCode) : new ResponseEntity<>(apiExp.getApiErrorResponse(), httpCode);
        } else if (KNOWN_EXCEPTION_MAPPINGS.containsKey(e.getClass())) {
            HttpStatus httpCode = KNOWN_EXCEPTION_MAPPINGS.get(e.getClass());
            ApiException apiExp = new ApiException(httpCode);
            logger.error("exception in known mapping {}  {}", e.getClass().getName(), e.getMessage());
            return new ResponseEntity<>(apiExp.getApiErrorResponse(), httpCode);
        } else {
            logger.error("unhandled api exception", e);
            HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;
            ApiErrorResponse res = new ApiErrorResponse("Internal server error, the event will be logged and analysed.", ApiErrorCode.InternalServerError.name());
            return new ResponseEntity<>(res, code);
        }
    }

    static {
        SPRING_BUILTIN_EXCEPTION_MAPPINGS = Map.ofEntries(Map.entry(HttpRequestMethodNotSupportedException.class, HttpStatus.METHOD_NOT_ALLOWED), Map.entry(HttpMediaTypeNotSupportedException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE), Map.entry(HttpMediaTypeNotAcceptableException.class, HttpStatus.NOT_ACCEPTABLE), Map.entry(MissingPathVariableException.class, HttpStatus.BAD_REQUEST), Map.entry(MissingServletRequestParameterException.class, HttpStatus.BAD_REQUEST), Map.entry(ServletRequestBindingException.class, HttpStatus.BAD_REQUEST), Map.entry(ConversionNotSupportedException.class, HttpStatus.BAD_REQUEST), Map.entry(TypeMismatchException.class, HttpStatus.BAD_REQUEST), Map.entry(HttpMessageNotReadableException.class, HttpStatus.BAD_REQUEST), Map.entry(HttpMessageNotWritableException.class, HttpStatus.BAD_REQUEST), Map.entry(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST), Map.entry(MissingServletRequestPartException.class, HttpStatus.BAD_REQUEST), Map.entry(BindException.class, HttpStatus.BAD_REQUEST), Map.entry(NoHandlerFoundException.class, HttpStatus.BAD_REQUEST), Map.entry(AsyncRequestTimeoutException.class, HttpStatus.BAD_REQUEST), Map.entry(NoResourceFoundException.class, HttpStatus.NOT_FOUND));
        KNOWN_EXCEPTION_MAPPINGS = Map.ofEntries(Map.entry(IllegalArgumentException.class, HttpStatus.BAD_REQUEST));
    }
}
