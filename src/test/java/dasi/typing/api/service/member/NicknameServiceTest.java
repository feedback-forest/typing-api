package dasi.typing.api.service.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NicknameServiceTest {

  @Autowired
  private NicknameService nicknameService;

  @Test
  @DisplayName("랜덤으로 생성한 두 닉네임은 서로 다르다.")
  void generateRandomNickname() {
    // given
    String nickname1 = nicknameService.generate();
    String nickname2 = nicknameService.generate();

    // when & then
    assertThat(nickname1).isNotEqualTo(nickname2);
  }
}