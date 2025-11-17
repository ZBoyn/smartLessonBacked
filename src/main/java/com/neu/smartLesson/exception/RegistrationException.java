package com.neu.smartLesson.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 当注册失败时（例如，用户名或邮箱已存在）抛出此异常
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}
