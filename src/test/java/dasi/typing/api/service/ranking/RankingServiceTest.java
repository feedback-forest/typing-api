package dasi.typing.api.service.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankingServiceTest {

  @Autowired
  private RankingService rankingService;

  @Autowired
  private TypingRepository typingRepository;

  @Autowired
  private PhraseRepository phraseRepository;

  @Autowired
  private MemberRepository memberRepository;

  @AfterEach
  void tearDown() {
    typingRepository.deleteAllInBatch();
    phraseRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
  }

  private final int USER_COUNT = 100;

  @Test
  @DisplayName("50명 이상의 유저 데이터가 있을 때, 점수를 기준으로 내림차순하여 상위 50명의 데이터를 반환할 수 있다.")
  void getRealTimeRankingTest() {
    // given
    Phrase phrase = createPhrase();

    List<Typing> typings = IntStream.rangeClosed(1, USER_COUNT)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i, 100, member, phrase);
        })
        .toList();

    typingRepository.saveAll(typings);

    // when
    List<RankingResponse> responses = rankingService.getRealTimeRanking();
    RankingResponse first = responses.getFirst();
    RankingResponse last = responses.getLast();

    // then
    assertThat(responses).hasSize(50);
    assertEquals(1L, first.getRanking());
    assertEquals(50L, last.getRanking());

    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      assertTrue(current.getScore() >= next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
  }

  @Test
  @DisplayName("50명 이상의 유저 데이터가 있을 때, 현재 날짜에 해당하는 연월에 대해서 최대 50등까지 랭킹 조회를 할 수 있다.")
  void getMonthlyRankingTest() {
    // given
    Phrase phrase = createPhrase();

    List<Typing> typings = IntStream.rangeClosed(1, USER_COUNT)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i, 100, member, phrase);
        })
        .toList();

    typingRepository.saveAll(typings);

    // when
    List<RankingResponse> responses = rankingService.getMonthlyRanking();

    // then
    assertTrue(responses.size() <= 50);
    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      assertTrue(current.getScore() >= next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
  }

  private Typing createTyping(int cpm, int acc, Member member, Phrase phrase) {
    return Typing.builder()
        .cpm(cpm)
        .acc(acc)
        .wpm(0)
        .maxCpm(0)
        .member(member)
        .phrase(phrase).build();
  }

  private Member createMember(String kakaoId, String nickname) {
    return memberRepository.save(new Member(kakaoId, nickname));
  }

  private Phrase createPhrase() {
    return phraseRepository.save(Phrase.builder()
        .sentence("test phrase").build()
    );
  }
}