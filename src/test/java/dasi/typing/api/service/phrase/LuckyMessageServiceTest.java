package dasi.typing.api.service.phrase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LuckyMessageServiceTest {

  @Autowired
  private LuckyMessageService luckyMessageService;

  @Test
  @DisplayName("랜덤으로 생성한 두 행운의 메시지는 서로 다르다.")
  void generateRandomLuckyMessageTest() {
    // given
    String luckyMessage1 = luckyMessageService.generate();
    String luckyMessage2 = luckyMessageService.generate();

    // when & then
    assertThat(luckyMessage1).isNotEqualTo(luckyMessage2);
  }
}