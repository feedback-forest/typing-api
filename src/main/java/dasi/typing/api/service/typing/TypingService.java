package dasi.typing.api.service.typing;

import static dasi.typing.exception.Code.NOT_EXIST_PHRASE;

import dasi.typing.api.service.phrase.LuckyMessageService;
import dasi.typing.api.service.ranking.RankingCacheService;
import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.member.Role;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypingService {

  private final TypingRepository typingRepository;
  private final PhraseRepository phraseRepository;
  private final MemberRepository memberRepository;
  private final LuckyMessageService luckyMessageService;
  private final RankingCacheService rankingCacheService;

  @Transactional
  public TypingResponse saveTyping(Authentication authentication, TypingCreateServiceRequest request) {

    Long phraseId = request.getPhraseId();
    Phrase phrase = phraseRepository.findById(phraseId).orElseThrow(
        () -> new CustomException(NOT_EXIST_PHRASE)
    );

    boolean authenticatedUser = isAuthenticatedUser(authentication);
    String nickname = "GUEST";
    Long rank = null;

    if (authenticatedUser) {
      String kakaoId = (String) authentication.getPrincipal();
      Member member = memberRepository.findByKakaoId(kakaoId)
          .orElseThrow(() -> new CustomException(Code.NOT_EXIST_MEMBER));

      Typing savedTyping = typingRepository.save(request.toEntity(phrase, member));

      nickname = member.getNickname();

      try {
        rankingCacheService.addOrUpdateIfBetter(
            member.getId(),
            member.getNickname(),
            savedTyping.getScore(),
            savedTyping.getMaxCpm(),
            savedTyping.getAcc(),
            savedTyping.getCreatedDate()
        );
      } catch (Exception e) {
        log.warn("[Ranking] Redis 랭킹 업데이트 실패. 다음 Warmup에서 복구됩니다.", e);
      }

      rank = typingRepository.findRanking(savedTyping.getScore());
    }

    return TypingResponse.builder()
        .role(authenticatedUser ? Role.USER : Role.GUEST)
        .nickname(nickname)
        .rank(rank)
        .luckyMessage(luckyMessageService.generate()).build();
  }

  private boolean isAuthenticatedUser(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
  }
}
