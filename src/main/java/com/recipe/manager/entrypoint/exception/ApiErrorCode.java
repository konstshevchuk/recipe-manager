package com.recipe.manager.entrypoint.exception;

public enum ApiErrorCode {
    InternalServerError,
    BadRequest,
    NotFound,
    MethodNotAllowed,
    UnsupportedMediaType,
    UnknownError,

    RequiredParameter,
    InvalidParameter,
}
