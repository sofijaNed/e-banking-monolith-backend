package rs.ac.bg.fon.ebanking.exception.handler;

import org.slf4j.MDC;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import rs.ac.bg.fon.ebanking.exception.ErrorResponse;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    String cid = MDC.get("cid");

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(ChangeSetPersister.NotFoundException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("error",ex.getMessage());
        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.NOT_FOUND.value(),errors,System.currentTimeMillis(), cid);
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(),errors,System.currentTimeMillis(), cid);
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(UsernameNotFoundException ex){

        Map<String, String> errors = new HashMap<>();
        errors.put("error",ex.getMessage());
        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.NOT_FOUND.value(),errors,System.currentTimeMillis(), cid);
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleException(SQLException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("error",ex.getMessage());
        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(),errors,System.currentTimeMillis(), cid);
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_ACCEPTABLE);
    }


//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException ex){
//        Map<String, String> errors = new HashMap<>();
//        errors.put("error",ex.getMessage());
//        ErrorResponse errorResponse =
//                new ErrorResponse(HttpStatus.BAD_REQUEST.value(),errors,System.currentTimeMillis());
//        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
//    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception ex){
        Map<String, String> errors = new HashMap<>();
        errors.put("error",ex.getMessage());
        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),errors,System.currentTimeMillis(), cid);

        return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
