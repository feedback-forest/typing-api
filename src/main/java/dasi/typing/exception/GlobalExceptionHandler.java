package dasi.typing.exception;

import static dasi.typing.exception.Code.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ApiResponse<Boolean>> handleException(CustomException e) {
    Code errorCode = e.getErrorCode();
    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(ApiResponse.error(errorCode));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity<ApiResponse<Boolean>> handleException(DataIntegrityViolationException e) {
    return ResponseEntity
        .status(ALREADY_EXIST_MEMBER.getHttpStatus())
        .body(ApiResponse.error(ALREADY_EXIST_MEMBER));
  }

}
