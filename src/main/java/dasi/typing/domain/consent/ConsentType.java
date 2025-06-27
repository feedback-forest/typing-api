package dasi.typing.domain.consent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConsentType {

  TERMS_OF_SERVICE("서비스 이용 약관"),
  PRIVACY_POLICY("개인 정보 처리 방침"),
  AGE_LIMIT_POLICY("14세 미만 이용 제한");

  private final String text;
}
