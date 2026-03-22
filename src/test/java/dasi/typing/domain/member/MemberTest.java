package dasi.typing.domain.member;

import static dasi.typing.domain.consent.ConsentType.AGE_LIMIT_POLICY;
import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;

import dasi.typing.domain.consent.Consent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

  @Test
  @DisplayName("reConsent 호출 시 기존 동의를 새 버전으로 교체한다.")
  void reConsentTest() {
    // given
    Member member = new Member("kakaoId", "nickname");

    Consent termsV1 = new Consent(TERMS_OF_SERVICE, 1, "약관 v1");
    Consent privacyV1 = new Consent(PRIVACY_POLICY, 1, "개인정보 v1");
    Consent ageV1 = new Consent(AGE_LIMIT_POLICY, 1, "연령 v1");
    member.addConsent(List.of(termsV1, privacyV1, ageV1));

    Consent privacyV2 = new Consent(PRIVACY_POLICY, 2, "개인정보 v2");

    // when
    member.reConsent(List.of(privacyV2));

    // then
    assertThat(member.getAgreements()).hasSize(3);
    assertThat(member.getAgreements())
        .extracting(mc -> mc.getConsent().getVersion())
        .containsExactlyInAnyOrder(1, 2, 1);
  }
}
