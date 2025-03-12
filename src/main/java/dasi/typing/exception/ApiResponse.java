package dasi.typing.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

  private final Integer code;
  private final String message;
  private final T data;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponseBuilder<T>()
        .code(Code.OK.getCode())
        .message(Code.OK.getMessage())
        .data(data).build();
  }

  public static ApiResponse<Void> error(Code errorCode) {
    return new ApiResponseBuilder<Void>()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .data(null).build();
  }

}
