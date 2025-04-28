package dasi.typing.api.service.member;

import static dasi.typing.exception.Code.ALREADY_EXIST_NICKNAME;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CV_NICKNAME;
import static dasi.typing.exception.Code.INVALID_LENGTH_NICKNAME;
import static dasi.typing.exception.Code.INVALID_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_TEMP_TOKEN;
import static dasi.typing.exception.Code.KAKAO_ACCOUNT_NOT_REGISTERED;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentRepository;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.memberConsent.MemberConsent;
import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final ConsentRepository consentRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  private static final Pattern INVALID_CV_PATTERN = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ].*");
  private static final Pattern ALLOWED_NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

  public String signIn(String tempToken) {
    String kakaoId = getKakaoIdFromTempToken(tempToken);
    validateRegisteredMember(kakaoId);
    return jwtTokenProvider.generateToken(kakaoId).getAccessToken();
  }

  @Transactional
  public String signUp(String tempToken, MemberCreateServiceRequest request) {

    String kakaoId = getKakaoIdFromTempToken(tempToken);
    String nickname = request.getNickname();

    Member member = Member.builder()
        .kakaoId(kakaoId)
        .nickname(nickname)
        .build();

    List<Consent> consents = consentRepository.findByTypeIn(request.getAgreements());

    for (Consent consent : consents) {
      MemberConsent memberConsent = MemberConsent.of(consent);
      member.addConsent(memberConsent);
    }

    memberRepository.save(member);
    return jwtTokenProvider.generateToken(kakaoId).getAccessToken();
  }

  public void validateNickname(MemberNicknameServiceRequest request) {

    String nickname = request.getNickname();

    if (StringUtils.isEmpty(nickname) || StringUtils.length(nickname) < 2
        || StringUtils.length(nickname) > 12) {
      throw new CustomException(INVALID_LENGTH_NICKNAME);
    }
    if (INVALID_CV_PATTERN.matcher(nickname).matches()) {
      throw new CustomException(INVALID_CV_NICKNAME);
    }
    if (!ALLOWED_NICKNAME_PATTERN.matcher(nickname).matches()) {
      throw new CustomException(INVALID_CHARACTER_NICKNAME);
    }
    if (validateAlreadyExistNickname(nickname)) {
      throw new CustomException(ALREADY_EXIST_NICKNAME);
    }
  }

  @Transactional
  public String reissue(String kakaoId) {

    RefreshToken refreshToken = refreshTokenRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(EXPIRED_REFRESH_TOKEN)
    );

    if (!jwtTokenProvider.validateRefreshToken(refreshToken.getToken())) {
      throw new CustomException(INVALID_REFRESH_TOKEN);
    }

    JwtToken jwtToken = jwtTokenProvider.generateToken(kakaoId);
    RefreshToken newRefreshToken = refreshToken.updateValue(jwtToken.getRefreshToken());
    refreshTokenRepository.save(newRefreshToken);

    return jwtToken.getAccessToken();
  }

  private String getKakaoIdFromTempToken(String tempToken) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(tempToken))
        .orElseThrow(() -> new CustomException(INVALID_TEMP_TOKEN));
  }

  private boolean validateAlreadyExistNickname(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  private void validateRegisteredMember(String kakaoId) {
    if (!memberRepository.existsByKakaoId(kakaoId)) {
      throw new CustomException(KAKAO_ACCOUNT_NOT_REGISTERED);
    }
  }
}
