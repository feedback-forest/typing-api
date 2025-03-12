package dasi.typing.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ApiResponse<Void> handleException(CustomException e) {
    return ApiResponse.error(e.getErrorCode());
  }

}
