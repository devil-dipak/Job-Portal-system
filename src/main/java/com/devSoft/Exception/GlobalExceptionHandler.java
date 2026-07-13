package com.devSoft.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURL(), ex.getMessage(), ex);
        ModelAndView mv = new ModelAndView("error/500");
        mv.addObject("message", ex.getMessage() != null ? ex.getMessage() : "Internal Server Error");
        mv.addObject("url", request.getRequestURL());
        return mv;
    }
}
