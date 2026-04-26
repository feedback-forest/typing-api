package dasi.typing.api.service.consent;

import static dasi.typing.domain.consent.ConsentType.AGE_LIMIT_POLICY;
import static dasi.typing.domain.consent.ConsentType.PRIVACY_POLICY;
import static dasi.typing.domain.consent.ConsentType.TERMS_OF_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dasi.typing.api.controller.consent.response.ConsentStatusResponse;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentRepository;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.memberConsent.MemberConsentRepository;
import dasi.typing.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConsentServiceTest {

  @Autowired
  private ConsentService consentService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ConsentRepository consentRepository;

  @Autowired
  private MemberConsentRepository memberConsentRepository;

  @BeforeEach
  void setUp() {
    memberConsentRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    consentRepository.deleteAllInBatch();
  }

  @AfterEach
  void tearDown() {
    memberConsentRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    consentRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("회원의 동의 상태를 조회한다.")
  void getConsentStatusTest() {
    // given
    Consent terms = consentRepository.save(new Consent(TERMS_OF_SERVICE));
    Consent privacy = consentRepository.save(new Consent(PRIVACY_POLICY));
    Consent age = consentRepository.save(new Consent(AGE_LIMIT_POLICY));

    Member member = new Member("kakao123", "testuser");
    member.addConsent(List.of(terms, privacy, age));
    memberRepository.save(member);

    // when
    List<ConsentStatusResponse> statuses = consentService.getConsentStatus("kakao123");

    // then
    assertThat(statuses).hasSize(3);
    assertThat(statuses).allMatch(ConsentStatusResponse::agreed);
  }

  @Test
  @DisplayName("동의하지 않은 약관이 있으면 agreed가 false로 반환된다.")
  void getConsentStatusNotAgreedTest() {
    // given
    Consent terms = consentRepository.save(new Consent(TERMS_OF_SERVICE));
    consentRepository.save(new Consent(PRIVACY_POLICY));

    Member member = new Member("kakao123", "testuser");
    member.addConsent(List.of(terms));
    memberRepository.save(member);

    // when
    List<ConsentStatusResponse> statuses = consentService.getConsentStatus("kakao123");

    // then
    assertThat(statuses).hasSize(2);
    assertThat(statuses)
        .filteredOn(s -> s.type() == TERMS_OF_SERVICE)
        .allMatch(ConsentStatusResponse::agreed);
    assertThat(statuses)
        .filteredOn(s -> s.type() == PRIVACY_POLICY)
        .noneMatch(ConsentStatusResponse::agreed);
  }

  @Test
  @DisplayName("약관 버전이 변경되면 agreed가 false로 반환된다.")
  void getConsentStatusVersionMismatchTest() {
    // given
    Consent oldTerms = consentRepository.save(new Consent(TERMS_OF_SERVICE, 1, "old content"));
    Member member = new Member("kakao123", "testuser");
    member.addConsent(List.of(oldTerms));
    memberRepository.save(member);

    // 기존 약관 비활성화 후 새 버전 활성화
    oldTerms.deactivate();
    consentRepository.save(oldTerms);
    consentRepository.save(new Consent(TERMS_OF_SERVICE, 2, "new content"));

    // when
    List<ConsentStatusResponse> statuses = consentService.getConsentStatus("kakao123");

    // then
    assertThat(statuses).hasSize(1);
    assertThat(statuses.getFirst().agreed()).isFalse();
    assertThat(statuses.getFirst().currentVersion()).isEqualTo(2);
    assertThat(statuses.getFirst().agreedVersion()).isEqualTo(1);
  }

  @Test
  @DisplayName("약관에 재동의한다.")
  void reAgreeTest() {
    // given
    Consent terms = consentRepository.save(new Consent(TERMS_OF_SERVICE));
    Consent privacy = consentRepository.save(new Consent(PRIVACY_POLICY));

    Member member = new Member("kakao123", "testuser");
    member.addConsent(List.of(terms));
    memberRepository.save(member);

    // when
    consentService.reAgree("kakao123", List.of(TERMS_OF_SERVICE, PRIVACY_POLICY));

    // then
    List<ConsentStatusResponse> statuses = consentService.getConsentStatus("kakao123");
    assertThat(statuses).allMatch(ConsentStatusResponse::agreed);
  }

  @Test
  @DisplayName("요청한 약관 타입과 활성 약관 수가 다르면 예외가 발생한다.")
  void reAgreeInsufficientConsentTest() {
    // given
    consentRepository.save(new Consent(TERMS_OF_SERVICE));

    Member member = new Member("kakao123", "testuser");
    memberRepository.save(member);

    // when & then
    List<ConsentType> types = List.of(TERMS_OF_SERVICE, PRIVACY_POLICY);
    assertThatThrownBy(() -> consentService.reAgree("kakao123", types))
        .isInstanceOf(CustomException.class);
  }

  @Test
  @DisplayName("존재하지 않는 회원의 동의 상태 조회 시 예외가 발생한다.")
  void getConsentStatusNotExistMemberTest() {
    // when & then
    assertThatThrownBy(() -> consentService.getConsentStatus("nonexistent"))
        .isInstanceOf(CustomException.class);
  }

  @Test
  @DisplayName("존재하지 않는 회원의 재동의 시 예외가 발생한다.")
  void reAgreeNotExistMemberTest() {
    // when & then
    assertThatThrownBy(() -> consentService.reAgree("nonexistent", List.of(TERMS_OF_SERVICE)))
        .isInstanceOf(CustomException.class);
  }
}
