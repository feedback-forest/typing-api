package dasi.typing.api.service.typing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import dasi.typing.api.controller.typing.request.TypingCreateRequest;
import dasi.typing.api.service.phrase.LuckyMessageService;
import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.member.Role;
import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.GuestPrincipal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TypingServiceTest {

  @Autowired
  private TypingService typingService;

  @Autowired
  private TypingRepository typingRepository;

  @Autowired
  private PhraseRepository phraseRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private LuckyMessageService luckyMessageService;

  @AfterEach
  void tearDown() {
    typingRepository.deleteAllInBatch();
    phraseRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("특정 문장에 대한 타자 정보를 생성할 있다.")
  void createTyping() {
    // given
    Phrase phrase = createPhrase(
        "안녕하세요.",
        "인사",
        "김용범",
        Lang.KO,
        LangType.QUOTE
    );

    Member member = createMember("3942518969", "용갈이");
    Member savedMember = memberRepository.save(member);
    Phrase savedPhrase = phraseRepository.save(phrase);
    TypingCreateRequest request = createRequest(savedPhrase);

    // when
    TypingCreateServiceRequest serviceRequest = request.toServiceRequest();
    Typing typing = serviceRequest.toEntity(savedPhrase, savedMember);
    Typing savedTyping = typingRepository.save(typing);

    // then
    assertThat(List.of(savedTyping))
        .isNotNull()
        .extracting("id", "cpm", "acc", "wpm")
        .containsExactlyInAnyOrder(
            tuple(savedTyping.getId(), 100, 100, 100)
        );
    assertThat(List.of(savedTyping.getMember()))
        .isNotNull()
        .extracting("id", "kakaoId", "nickname")
        .containsExactlyInAnyOrder(
            tuple(savedTyping.getMember().getId(), "3942518969", "용갈이")
        );
    assertThat(List.of(savedTyping.getPhrase()))
        .isNotNull()
        .extracting("id", "sentence", "title", "author", "lang", "type")
        .containsExactlyInAnyOrder(
            tuple(savedTyping.getPhrase().getId(), "안녕하세요.", "인사", "김용범", Lang.KO, LangType.QUOTE)
        );
  }

  @Test
  @DisplayName("존재하지 않는 문장에 대해서 예외가 발생한다.")
  void notExistPhrase() throws CustomException {
    // given
    Long phraseId = 999L;

    // when, then
    assertThatThrownBy(() -> {
      phraseRepository.findById(phraseId).orElseThrow(
          () -> new CustomException(Code.NOT_EXIST_PHRASE));
    }).isInstanceOf(CustomException.class)
        .hasMessage(Code.NOT_EXIST_PHRASE.getMessage());
  }

  @Test
  @DisplayName("비회원일 때, 타자 정보 생성 후에 행운의 메시지와 순위를 반환할 수 있다.")
  void createAnonymousTypingResponse() {
    // given
    String guestId = UUID.randomUUID().toString();
    AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
        "guestKey",
        new GuestPrincipal(guestId),
        AuthorityUtils.createAuthorityList("GUEST")
    );
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Phrase phrase = createPhrase(
        "안녕하세요.",
        "인사",
        "김용범",
        Lang.KO,
        LangType.QUOTE
    );
    Phrase savedPhrase = phraseRepository.save(phrase);

    // when
    TypingCreateRequest request = createRequest(savedPhrase);
    TypingResponse response = typingService.saveTyping(authentication, request.toServiceRequest());

    // then
    assertThat(response).isNotNull();
    assertThat(response.getRole()).isEqualTo(Role.GUEST);
    assertThat(response.getNickname()).isEqualTo("GUEST");
    assertThat(response.getRank()).isNull();
    assertThat(luckyMessageService.getLuckyMessages()).contains(response.getLuckyMessage());
  }

  @Test
  @DisplayName("회원일 때, 타자 정보 생성 후에 행운의 메시지와 순위를 반환할 수 있다.")
  void createUserTypingResponse() {
    // given
    String kakaoId = "1234567890";
    Member member = createMember(kakaoId, "dt10002");
    List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("USER");

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(kakaoId, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Phrase phrase = createPhrase(
        "안녕하세요.",
        "인사",
        "김용범",
        Lang.KO,
        LangType.QUOTE
    );
    Phrase savedPhrase = phraseRepository.save(phrase);
    Member savedMember = memberRepository.save(member);

    // when
    TypingCreateRequest request = createRequest(savedPhrase);
    TypingResponse response = typingService.saveTyping(authentication, request.toServiceRequest());

    // then
    assertThat(response).isNotNull();
    assertThat(response.getRole()).isEqualTo(Role.USER);
    assertThat(response.getNickname()).isEqualTo(savedMember.getNickname());
    assertThat(response.getRank()).isNotNull();
    assertThat(luckyMessageService.getLuckyMessages()).contains(response.getLuckyMessage());
  }

  private Phrase createPhrase(String sentence, String title, String author, Lang lang,
      LangType type) {
    return Phrase.builder()
        .sentence(sentence)
        .title(title)
        .author(author)
        .lang(lang)
        .type(type)
        .build();
  }

  private static Member createMember(String kakaoId, String nickname) {
    return Member.builder()
        .kakaoId(kakaoId)
        .nickname(nickname).build();
  }

  private static TypingCreateRequest createRequest(Phrase savedPhrase) {
    return TypingCreateRequest.builder()
        .phraseId(savedPhrase.getId())
        .cpm(100)
        .acc(100)
        .wpm(100)
        .maxCpm(120).build();
  }

}