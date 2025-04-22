package dasi.typing.domain.consent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConsentType {

  TERMS_OF_SERVICE("서비스 이용 약관"),
  PRIVACY_POLICY("개인 정보 처리 방침");

  private final String text;

}
