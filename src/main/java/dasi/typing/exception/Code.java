package dasi.typing.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {

  // SUCCESS
  OK(0, "success"),

  // Nickname
  INVALID_LENGTH_NICKNAME(1000, "띄어쓰기 없이 2자 ~ 12자까지 가능해요."),
  INVALID_CV_NICKNAME(1001, "자음, 모음은 닉네임 설정 불가합니다."),
  INVALID_CHARACTER_NICKNAME(1002, "한글, 영문, 숫자만 입력해주세요."),
  ALREADY_EXIST_NICKNAME(1003, "이미 사용중인 닉네임이에요."),

  // Phrase
  NOT_EXIST_PHRASE(2000, "해당 문장이 존재하지 않습니다."),

  // Member & Kakao OAuth2
  ALREADY_EXIST_MEMBER(3000, "이미 존재하는 유저입니다."),
  KAKAO_ACCOUNT_NOT_FOUND(3001, "Kakao 사용자 정보를 가져오는데 실패했습니다."),
  KAKAO_ACCOUNT_NOT_REGISTERED(3002, "카카오 계정이 등록되어 있지 않습니다. 회원가입이 필요합니다."),

  // Temp Token
  INVALID_TEMP_TOKEN(4000, "유효하지 않은 임시 토큰입니다."),

  // JWT Token
  INVALID_ACCESS_TOKEN(5000, "유효하지 않은 ACCESS_JWT 서명입니다."),
  EXPIRED_ACCESS_TOKEN(5001, "만료된 ACCESS_JWT 토큰입니다."),
  INVALID_REFRESH_TOKEN(5002, "유효하지 않은 REFRESH_JWT 서명입니다."),
  EXPIRED_REFRESH_TOKEN(5003, "만료된 REFRESH_JWT 토큰입니다."),
  UNSUPPORTED_JWT_TOKEN(5004, "지원되지 않는 JWT 토큰 형식입니다."),
  EMPTY_JWT_TOKEN(5005, "토큰이 비어있거나 제공되지 않았습니다.");

  private final Integer code;
  private final String message;

}
