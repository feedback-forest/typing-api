package dasi.typing.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApiResponseTest {

  @Test
  @DisplayName("success 응답이 정상적으로 생성된다.")
  void successApiResponse() {
    // given
    String data = "응답 성공";

    // when
    ApiResponse<?> response = ApiResponse.success(data);

    // then
    assertThat(response)
        .extracting("code", "message", "data")
        .containsExactly(
            Code.OK.getCode(),
            Code.OK.getMessage(),
            data
        );
  }

  @Test
  @DisplayName("error 응답이 정상적으로 생성된다.")
  void errorApiResponse() {
    // given
    Code errorCode = Code.INVALID_CHARACTER_NICKNAME;

    // when
    ApiResponse<Void> response = ApiResponse.error(errorCode);

    // then
    assertThat(response)
        .extracting("code", "message", "data")
        .containsExactly(
            Code.INVALID_CHARACTER_NICKNAME.getCode(),
            Code.INVALID_CHARACTER_NICKNAME.getMessage(),
            null
        );
  }

}