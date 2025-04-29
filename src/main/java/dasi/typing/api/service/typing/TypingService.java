package dasi.typing.api.service.typing;

import static dasi.typing.exception.Code.NOT_EXIST_PHRASE;

import dasi.typing.api.service.phrase.LuckyMessageService;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypingService {

  private final TypingRepository typingRepository;
  private final PhraseRepository phraseRepository;
  private final MemberRepository memberRepository;
  private final LuckyMessageService luckyMessageService;

  @Transactional
  public TypingResponse saveTyping(Authentication authentication, TypingCreateServiceRequest request) {

    Long phraseId = request.getPhraseId();
    Phrase phrase = phraseRepository.findById(phraseId).orElseThrow(
        () -> new CustomException(NOT_EXIST_PHRASE)
    );

    boolean authenticatedUser = isAuthenticatedUser(authentication);
    String nickname = "GUEST";
    Integer rank = null;

    if (authenticatedUser) {
      String kakaoId = (String) authentication.getPrincipal();
      Member member = memberRepository.findByKakaoId(kakaoId)
          .orElseThrow(() -> new CustomException(Code.NOT_EXIST_MEMBER));

      Typing typing = request.toEntity(phrase, member);
      Typing savedTyping = typingRepository.save(typing);

      rank = typingRepository.findTypingRank(savedTyping.getId());
      nickname = member.getNickname();
    }

    return TypingResponse.builder()
        .role(authenticatedUser ? Role.USER : Role.GUEST)
        .nickname(nickname)
        .rank(rank)
        .luckyMessage(luckyMessageService.generate()).build();
  }

  private boolean isAuthenticatedUser(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("USER"));
  }
}
