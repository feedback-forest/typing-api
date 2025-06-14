package dasi.typing.exception;

import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.OK;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  @DisplayName("success 응답이 정상적으로 생성된다.")
  void successApiResponse() {
    // given
    String data = "응답 성공";

    // when
    ApiResponse<String> response = ApiResponse.success(data);

    // then
    assertThat(response)
        .extracting("code", "message", "data")
        .containsExactly(
            OK.getCode(),
            OK.getMessage(),
            data
        );
  }

  @Test
  @DisplayName("error 응답이 정상적으로 생성된다.")
  void errorApiResponse() {
    // given
    Code errorCode = INVALID_CHARACTER_NICKNAME;

    // when
    ApiResponse<Boolean> response = ApiResponse.error(errorCode);

    // then
    assertThat(response)
        .extracting("code", "message", "data")
        .containsExactly(
            INVALID_CHARACTER_NICKNAME.getCode(),
            INVALID_CHARACTER_NICKNAME.getMessage(),
            false
        );
  }
}