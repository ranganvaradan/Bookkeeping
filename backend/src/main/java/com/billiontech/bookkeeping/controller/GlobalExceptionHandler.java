package com.billiontech.bookkeeping.controller;

import com.billiontech.bookkeeping.service.AuthService;
import com.billiontech.bookkeeping.service.qbo.QboOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthService.AuthException ex) {
        return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(QboOAuthService.QboException.class)
    public ResponseEntity<Map<String, String>> handleQboException(QboOAuthService.QboException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
