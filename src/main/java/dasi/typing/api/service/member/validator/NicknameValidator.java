package dasi.typing.api.service.member.validator;

import static dasi.typing.exception.Code.ALREADY_EXIST_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CV_NICKNAME;
import static dasi.typing.exception.Code.INVALID_LENGTH_NICKNAME;
import static dasi.typing.utils.PatternUtil.ALLOWED_NICKNAME_PATTERN;
import static dasi.typing.utils.PatternUtil.INVALID_CV_PATTERN;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.length;

import dasi.typing.domain.member.MemberRepository;
import dasi.typing.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NicknameValidator {

  private final MemberRepository memberRepository;

  public void validateLength(String nickname) {
    if (isEmpty(nickname) || length(nickname) < 2 || length(nickname) > 12) {
      throw new CustomException(INVALID_LENGTH_NICKNAME);
    }
  }

  public void validateNoConsonantVowelOnly(String nickname) {
    if (INVALID_CV_PATTERN.matcher(nickname).matches()) {
      throw new CustomException(INVALID_CV_NICKNAME);
    }
  }

  public void validateAllowedCharacters(String nickname) {
    if (ALLOWED_NICKNAME_PATTERN.matcher(nickname).matches()) {
      return;
    }
    throw new CustomException(INVALID_CHARACTER_NICKNAME);
  }

  public void validateNotDuplicated(String nickname) {
    if (validateAlreadyExistNickname(nickname)) {
      throw new CustomException(ALREADY_EXIST_NICKNAME);
    }
  }

  private boolean validateAlreadyExistNickname(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }
}
