package dasi.typing.domain.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TypingTest {

  @ParameterizedTest
  @DisplayName("정확도 공식에 따라서 최종 타수를 계산할 수 있다.")
  @MethodSource("paramsForCalculateFinalScore")
  void calculateFinalScore(int cpm, int acc, int expect) {
    // given
    Typing typing = createTyping(cpm, acc);

    // when
    int result = typing.getScore();

    // then
    assertEquals(expect, result);
  }

  private Typing createTyping(int cpm, int acc) {
    return Typing.builder()
        .cpm(cpm)
        .acc(acc)
        .wpm(0)
        .member(null)
        .phrase(null).build();
  }

  private static Stream<Arguments> paramsForCalculateFinalScore() {
    return Stream.of(
        Arguments.of(300, 95, 300),
        Arguments.of(300, 89, 297),
        Arguments.of(300, 75, 255),
        Arguments.of(300, 60, 210),
        Arguments.of(300, 50, 180),
        Arguments.of(300, 39, 120),
        Arguments.of(300, 30, 90),
        Arguments.of(300, 25, 60),
        Arguments.of(300, 20, 30),
        Arguments.of(300, 15, 9),
        Arguments.of(300, 5, 0)
    );
  }

}