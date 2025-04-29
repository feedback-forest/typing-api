package dasi.typing.exception;

import static dasi.typing.exception.Code.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ApiResponse<Boolean> handleException(CustomException e) {
    return ApiResponse.error(e.getErrorCode());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ApiResponse<Boolean> handleException(DataIntegrityViolationException e) {
    return ApiResponse.error(ALREADY_EXIST_MEMBER);
  }

}
