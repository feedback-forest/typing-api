package dasi.typing.api.service.member;

import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INSUFFICIENT_CONSENT_EXCEPTION;
import static dasi.typing.exception.Code.INVALID_TEMP_TOKEN;
import static dasi.typing.utils.ConstantUtil.REQUIRED_CONSENT_COUNT;

import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import dasi.typing.api.service.member.validator.NicknameValidator;
import dasi.typing.domain.consent.Consent;
import dasi.typing.domain.consent.ConsentRepository;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.refreshToken.RefreshToken;
import dasi.typing.domain.refreshToken.RefreshTokenRepository;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import dasi.typing.jwt.JwtTokenProvider;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final NicknameValidator nicknameValidator;
  private final ConsentRepository consentRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public JwtToken signUp(String tempToken, MemberCreateServiceRequest request) {

    String kakaoId = getKakaoIdFromTempToken(tempToken);
    String nickname = request.getNickname();
    Member member = new Member(kakaoId, nickname);

    List<Consent> consents = consentRepository.findByTypeInAndActiveTrue(request.getAgreements());

    if (consents.size() != REQUIRED_CONSENT_COUNT) {
      throw new CustomException(INSUFFICIENT_CONSENT_EXCEPTION);
    }

    member.addConsent(consents);
    memberRepository.save(member);

    redisTemplate.delete(tempToken);

    return jwtTokenProvider.generateToken(kakaoId, new Date());
  }

  public void validateNickname(MemberNicknameServiceRequest request) {
    String nickname = request.nickname();
    nicknameValidator.validateLength(nickname);
    nicknameValidator.validateNoConsonantVowelOnly(nickname);
    nicknameValidator.validateAllowedCharacters(nickname);
    nicknameValidator.validateNotDuplicated(nickname);
  }

  @Transactional
  public JwtToken reissue(String kakaoId) {

    RefreshToken refreshToken = refreshTokenRepository.findByKakaoId(kakaoId).orElseThrow(
        () -> new CustomException(EXPIRED_REFRESH_TOKEN)
    );

    jwtTokenProvider.validateRefreshToken(refreshToken.getToken());

    return jwtTokenProvider.generateToken(kakaoId, new Date());
  }

  private String getKakaoIdFromTempToken(String tempToken) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(tempToken))
        .orElseThrow(() -> new CustomException(INVALID_TEMP_TOKEN));
  }
}