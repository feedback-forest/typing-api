package dasi.typing.api.service.phrase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LuckyMessageServiceTest {

  @Autowired
  private LuckyMessageService luckyMessageService;

  @Test
  @DisplayName("랜덤으로 생성한 두 행운의 메시지는 서로 다르다.")
  void generateRandomLuckyMessageTest() {
    // given
    Set<String> results = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      results.add(luckyMessageService.generate());
      if (results.size() >= 2) break;
    }

    // when & then
    assertThat(results.size()).isGreaterThan(1);
  }
}