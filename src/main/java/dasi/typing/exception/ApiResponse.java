package dasi.typing.exception;

import java.util.Map;
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

  public static <T> ApiResponse<Map<String, T>> success(String key, T data) {
    return new ApiResponseBuilder<Map<String, T>>()
        .code(Code.OK.getCode())
        .message(Code.OK.getMessage())
        .data(Map.of(key, data)).build();
  }

  public static ApiResponse<Boolean> error(Code errorCode) {
    return new ApiResponseBuilder<Boolean>()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .data(false).build();
  }

}
