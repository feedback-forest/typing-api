package dasi.typing.api.service.member.validator;

import static dasi.typing.exception.Code.ALREADY_EXIST_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CV_NICKNAME;
import static dasi.typing.exception.Code.INVALID_LENGTH_NICKNAME;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.exception.CustomException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class NicknameValidatorTest {

  @Autowired
  NicknameValidator nicknameValidator;

  private static Stream<Arguments> validateLengthTestScenarios() {
    return Stream.of(
        Arguments.of((Object) null),
        Arguments.of(""),
        Arguments.of("a"),
        Arguments.of("abcdefghijklm")
    );
  }

  @DisplayName("닉네임이 null 또는 2~12자 사이의 길이가 아니면 INVALID_LENGTH_NICKNAME 예외가 발생한다.")
  @ParameterizedTest
  @MethodSource("validateLengthTestScenarios")
  void validateLengthTest(String nickname) {
    // when & then
    assertThatThrownBy(() -> nicknameValidator.validateLength(nickname))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(INVALID_LENGTH_NICKNAME.getMessage());
  }

  @DisplayName("닉네임이 2~12자 사이의 길이이면 INVALID_LENGTH_NICKNAME 예외가 발생하지 않는다.")
  @Test
  void validateLengthSuccessTest() {
    // given
    String validNickname = "validNick";

    // when & then
    assertThatCode(() -> nicknameValidator.validateLength(validNickname))
        .doesNotThrowAnyException();
  }

  @DisplayName("닉네임에 자음 또는 모음이 포함되어 있으면 INVALID_CV_NICKNAME 예외가 발생한다.")
  @ParameterizedTest
  @ValueSource(strings = {
      "ㄱㄴㄷㄹ", "ㅏㅓㅗㅜ", "가ㄱ나다라", "가나다라ㅏ", "가나다라ㅣ"
  })
  void validateNoConsonantVowelOnlyTest(String nickname) {
    // when & then
    assertThatThrownBy(() -> nicknameValidator.validateNoConsonantVowelOnly(nickname))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(INVALID_CV_NICKNAME.getMessage());
  }

  @DisplayName("닉네임에 자음 또는 모음이 포함되어 있지 않으면 INVALID_CV_NICKNAME 예외가 발생하지 않는다.")
  @Test
  void validateNoConsonantVowelOnlySuccessTest() {
    // given
    String validNickname = "validNickname";

    // when & then
    assertThatCode(() -> nicknameValidator.validateNoConsonantVowelOnly(validNickname))
        .doesNotThrowAnyException();
  }

  @DisplayName("닉네임에 허용하지 않는 문자(특수문자 등)가 포함되어 있으면 INVALID_CHARACTER_NICKNAME 예외가 발생한다.")
  @ParameterizedTest
  @ValueSource(strings = {
      "validNickname!", "validNickname@", "validNickname#", "validNickname$"
  })
  void validateAllowedCharactersTest(String nickname) {
    // when & then
    assertThatThrownBy(() -> nicknameValidator.validateAllowedCharacters(nickname))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(INVALID_CHARACTER_NICKNAME.getMessage());
  }

  @DisplayName("닉네임에 허용된 문자만 포함되어 있으면 INVALID_CHARACTER_NICKNAME 예외가 발생하지 않는다.")
  @Test
  void validateAllowedCharactersSuccessTest() {
    // given
    String validNickname = "validNickname";

    // when & then
    assertThatCode(() -> nicknameValidator.validateAllowedCharacters(validNickname))
        .doesNotThrowAnyException();
  }

  @Transactional
  @DisplayName("닉네임이 이미 존재하면 ALREADY_EXIST_NICKNAME 예외가 발생한다.")
  @Test
  void validateNotDuplicatedTest(
      @Autowired MemberRepository memberRepository
  ) {
    // given
    Member member = new Member("1234567890", "existingNickname");
    memberRepository.save(member);
    String existingNickname = "existingNickname";

    // when & then
    assertThatThrownBy(() -> nicknameValidator.validateNotDuplicated(existingNickname))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ALREADY_EXIST_NICKNAME.getMessage());
  }

  @DisplayName("닉네임이 존재하지 않으면 ALREADY_EXIST_NICKNAME 예외가 발생하지 않는다.")
  @Test
  void validateNotDuplicatedSuccessTest(
      @Autowired MemberRepository memberRepository
  ) {
    // given
    String newNickname = "newNickname";

    // when & then
    assertThatCode(() -> nicknameValidator.validateNotDuplicated(newNickname))
        .doesNotThrowAnyException();
  }
}