package dasi.typing.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final Code errorCode;

  public CustomException(Code errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
