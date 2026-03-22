package dasi.typing.domain.consent;

import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsentTest {

  @Test
  @DisplayName("기본 생성자로 생성하면 version=1, active=true로 초기화된다.")
  void createConsentWithDefaultVersionTest() {
    // when
    Consent consent = new Consent(TERMS_OF_SERVICE);

    // then
    assertThat(consent.getType()).isEqualTo(TERMS_OF_SERVICE);
    assertThat(consent.getDescription()).isEqualTo("서비스 이용 약관");
    assertThat(consent.getVersion()).isEqualTo(1);
    assertThat(consent.isActive()).isTrue();
  }

  @Test
  @DisplayName("버전과 내용을 지정하여 동의 항목을 생성할 수 있다.")
  void createConsentWithVersionTest() {
    // given
    String content = "개인정보 처리방침 v2 전문 내용";

    // when
    Consent consent = new Consent(PRIVACY_POLICY, 2, content);

    // then
    assertThat(consent.getType()).isEqualTo(PRIVACY_POLICY);
    assertThat(consent.getVersion()).isEqualTo(2);
    assertThat(consent.getContent()).isEqualTo(content);
    assertThat(consent.isActive()).isTrue();
  }

  @Test
  @DisplayName("deactivate 호출 시 active가 false로 변경된다.")
  void deactivateConsentTest() {
    // given
    Consent consent = new Consent(TERMS_OF_SERVICE, 1, "내용");

    // when
    consent.deactivate();

    // then
    assertThat(consent.isActive()).isFalse();
  }
}
