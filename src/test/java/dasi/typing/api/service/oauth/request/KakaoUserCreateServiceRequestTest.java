package dasi.typing.api.service.oauth.request;

import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.api.service.oauth.info.KakaoUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoUserCreateServiceRequestTest {

  @Test
  @DisplayName("카카오 유저 생성 요청을 통해서 KakaoUserInfo 객체를 생성할 수 있다.")
  void toKakaoUserInfoTest() {
    // given
    KakaoUserCreateServiceRequest request = KakaoUserCreateServiceRequest.of(
        "1234567890",
        "testName",
        "testNickname",
        "testEmail",
        true
    );

    // when
    KakaoUserInfo response = request.toKakaoUserInfo();

    // then
    assertThat(response)
        .extracting("sub", "name", "nickname")
        .containsExactly("1234567890", "testName", "testNickname");
  }
}