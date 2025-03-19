package dasi.typing.api.service.typing;

import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypingService {

  private final TypingRepository typingRepository;
  private final PhraseRepository phraseRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public TypingResponse createTyping(TypingCreateServiceRequest request) {

    Long phraseId = request.getPhraseId();
    Phrase savedPhrase = phraseRepository.findById(phraseId).orElseThrow(
        () -> new CustomException(Code.NOT_EXIST_PHRASE)
    );

    Member member = Member.builder()
        .kakaoId("3942518969")
        .nickname("용갈이").build();

    Member savedMember = memberRepository.save(member);

    Typing typing = request.toEntity(savedPhrase, savedMember);
    typingRepository.save(typing);

    return TypingResponse.builder()
        .luckyMessage("오늘 하루도 고생했어!")
        .rank(1).build();
  }
}
