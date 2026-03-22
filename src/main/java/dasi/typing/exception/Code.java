package dasi.typing.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Code {

  // SUCCESS
  OK(0, "success", HttpStatus.OK),

  // Nickname
  INVALID_LENGTH_NICKNAME(1000, "띄어쓰기 없이 2자 ~ 12자까지 가능해요.", HttpStatus.BAD_REQUEST),
  INVALID_CV_NICKNAME(1001, "자음, 모음은 닉네임 설정 불가합니다.", HttpStatus.BAD_REQUEST),
  INVALID_CHARACTER_NICKNAME(1002, "한글, 영문, 숫자만 입력해주세요.", HttpStatus.BAD_REQUEST),
  ALREADY_EXIST_NICKNAME(1003, "이미 사용중인 닉네임이에요.", HttpStatus.CONFLICT),

  // Phrase
  NOT_EXIST_PHRASE(2000, "해당 문장이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

  // Member & Kakao OAuth2
  ALREADY_EXIST_MEMBER(3000, "이미 존재하는 유저입니다.", HttpStatus.CONFLICT),
  KAKAO_ACCOUNT_NOT_FOUND(3001, "Kakao 사용자 정보를 가져오는데 실패했습니다.", HttpStatus.BAD_GATEWAY),
  KAKAO_ACCOUNT_NOT_REGISTERED(3002, "카카오 계정이 등록되어 있지 않습니다. 회원가입이 필요합니다.", HttpStatus.NOT_FOUND),
  NOT_EXIST_MEMBER(3003, "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND),

  // Temp Token
  INVALID_TEMP_TOKEN(4000, "유효하지 않은 임시 토큰입니다.", HttpStatus.UNAUTHORIZED),

  // JWT Token
  INVALID_ACCESS_TOKEN(5000, "유효하지 않은 ACCESS_JWT 서명입니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_ACCESS_TOKEN(5001, "만료된 ACCESS_JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
  INVALID_REFRESH_TOKEN(5002, "유효하지 않은 REFRESH_JWT 서명입니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_REFRESH_TOKEN(5003, "만료된 REFRESH_JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
  UNSUPPORTED_JWT_TOKEN(5004, "지원되지 않는 JWT 토큰 형식입니다.", HttpStatus.UNAUTHORIZED),
  EMPTY_JWT_TOKEN(5005, "토큰이 비어있거나 제공되지 않았습니다.", HttpStatus.UNAUTHORIZED),

  // Consent
  INSUFFICIENT_CONSENT_EXCEPTION(6000, "3가지 동의가 모두 필요합니다. 동의 후 이용해주세요.", HttpStatus.BAD_REQUEST),
  CONSENT_RE_AGREEMENT_REQUIRED(6001, "약관이 변경되었습니다. 재동의가 필요합니다.", HttpStatus.BAD_REQUEST);

  private final Integer code;
  private final String message;
  private final HttpStatus httpStatus;

}
