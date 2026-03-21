package dasi.typing.api.service.consent;

import static dasi.typing.exception.Code.INSUFFICIENT_CONSENT_EXCEPTION;
import static dasi.typing.exception.Code.NOT_EXIST_MEMBER;

import dasi.typing.api.controller.consent.response.ConsentStatusResponse;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentRepository;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.exception.CustomException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsentService {

  private final ConsentRepository consentRepository;
  private final MemberRepository memberRepository;

  public List<ConsentStatusResponse> getConsentStatus(String kakaoId) {
    Member member = findMember(kakaoId);
    List<Consent> activeConsents = consentRepository.findByActiveTrue();

    Map<ConsentType, Integer> agreedVersions = member.getAgreements().stream()
        .collect(Collectors.toMap(
            mc -> mc.getConsent().getType(),
            mc -> mc.getConsent().getVersion(),
            Math::max
        ));

    return activeConsents.stream()
        .map(active -> {
          Integer agreedVersion = agreedVersions.get(active.getType());
          boolean agreed = agreedVersion != null && agreedVersion.equals(active.getVersion());
          return ConsentStatusResponse.of(active, agreedVersion, agreed);
        })
        .toList();
  }

  @Transactional
  public void reAgree(String kakaoId, List<ConsentType> types) {
    Member member = findMember(kakaoId);
    List<Consent> activeConsents = consentRepository.findByTypeInAndActiveTrue(types);

    if (activeConsents.size() != types.size()) {
      throw new CustomException(INSUFFICIENT_CONSENT_EXCEPTION);
    }

    member.reConsent(activeConsents);
  }

  private Member findMember(String kakaoId) {
    return memberRepository.findByKakaoId(kakaoId)
        .orElseThrow(() -> new CustomException(NOT_EXIST_MEMBER));
  }
}