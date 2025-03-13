package dasi.typing.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {

  OK(0, "success"),

  INVALID_LENGTH_NICKNAME(1000, "띄어쓰기 없이 2자 ~ 12자까지 가능해요."),
  INVALID_CV_NICKNAME(1001, "자음, 모음은 닉네임 설정 불가합니다."),
  INVALID_CHARACTER_NICKNAME(1002, "한글, 영문, 숫자만 입력해주세요."),
  ALREADY_NICKNAME_EXIST(1003, "이미 사용중인 닉네임이에요."),

  NOT_EXIST_PHRASE(2000, "해당 문장이 존재하지 않습니다.");

  private final Integer code;
  private final String message;

}
