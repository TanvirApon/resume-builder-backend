package com.fullstack.resumebuilder.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>>handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error)->{
            String fieldName = ((FieldError)error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName,errorMessage);

        });
        Map<String,Object> response = new HashMap<>();
        response.put("message","Validation Failed");
        response.put("errors",errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceExitsException.class)
    public ResponseEntity<Map<String,Object>>handleResourceExits(ResourceExitsException ex){
        Map<String,Object> response = new HashMap<>();
        response.put("message","Resource Exits");
        response.put("errors",ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>>handleGenericException(ResourceExitsException ex){
        Map<String,Object> response = new HashMap<>();
        response.put("message","Something went wrong. Contact Administrator");
        response.put("errors",ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    }
}
