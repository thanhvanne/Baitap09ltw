package vn.iotstar.controller;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(
        AccessDeniedException.class
    )
    public String handleAccessDenied(
            AccessDeniedException exception
    ) {

        return "redirect:/access-denied";
    }
}