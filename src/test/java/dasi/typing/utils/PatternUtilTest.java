package dasi.typing.utils;

import static dasi.typing.utils.PatternUtil.ALLOWED_NICKNAME_PATTERN;
import static dasi.typing.utils.PatternUtil.INVALID_CV_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class PatternUtilTest {

  private static Stream<Arguments> allowedCVPatternScenarios() {
    return Stream.of(
        Arguments.of("안녕하세요", false),
        Arguments.of("가나다라", false),
        Arguments.of("가ㄱ나다라", true),
        Arguments.of("가나다라ㅏ", true),
        Arguments.of("가나다라ㅣ", true),
        Arguments.of("ㅏㅔㅣㅗㅜ", true),
        Arguments.of("ㄱㄴㄷㄹ", true),
        Arguments.of("ㄱㄴㅏㅣ", true)
    );
  }

  @DisplayName("자음, 모음에 대한 닉네임 검증 정규식 표현을 검증합니다.")
  @MethodSource("allowedCVPatternScenarios")
  @ParameterizedTest
  void invalidCvPatternTest(String input, boolean expected) {

    // when & then
    boolean result = INVALID_CV_PATTERN.matcher(input).matches();
    assertEquals(expected, result);
  }

  private static Stream<Arguments> allowedNicknamePatternScenarios() {
    return Stream.of(
        Arguments.of("안녕하세요", true),
        Arguments.of("HelloWorld", true),
        Arguments.of("12345", true),
        Arguments.of("가나다라", true),
        Arguments.of("abc가나다", true),
        Arguments.of("123가나다", true),
        Arguments.of("가나다123", true),
        Arguments.of("가나다_123", false),
        Arguments.of("가나다!@#", false),
        Arguments.of("ㄱㄴㄷabc", false),
        Arguments.of("가나.!@#", false),
        Arguments.of("ㅣㅏㅓㅗㅜ", false)
    );
  }

  @DisplayName("허용되는 닉네임 검증 정규식 표현을 검증합니다.")
  @ParameterizedTest
  @MethodSource("allowedNicknamePatternScenarios")
  void allowedNicknamePatternTest(String input, boolean expected) {

    // when & then
    boolean result = ALLOWED_NICKNAME_PATTERN.matcher(input).matches();
    assertEquals(expected, result);
  }
}