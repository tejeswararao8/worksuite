package com.platform.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all platform business exceptions.
 * Modules throw this — GlobalExceptionHandler catches it.
 */
@Getter
public class PlatformException extends RuntimeException {

    private final HttpStatus status;

    public PlatformException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public PlatformException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static PlatformException notFound(String resource) {
        return new PlatformException(resource + " not found", HttpStatus.NOT_FOUND);
    }

    public static PlatformException conflict(String message) {
        return new PlatformException(message, HttpStatus.CONFLICT);
    }

    public static PlatformException forbidden(String message) {
        return new PlatformException(message, HttpStatus.FORBIDDEN);
    }

    public static PlatformException badRequest(String message) {
        return new PlatformException(message, HttpStatus.BAD_REQUEST);
    }
}
