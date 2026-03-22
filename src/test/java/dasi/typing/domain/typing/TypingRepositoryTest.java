package dasi.typing.domain.typing;

import static dasi.typing.utils.DateTimeUtil.getMonthEndDate;
import static dasi.typing.utils.DateTimeUtil.getMonthStartDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class TypingRepositoryTest {

  @PersistenceContext
  private EntityManager em;

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

  private final int RANKING_COUNT = 50;

  private static Stream<Arguments> paramsForFindUpTo50UserRanking() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(1, 1),
        Arguments.of(49, 49),
        Arguments.of(50, 50),
        Arguments.of(51, 50),
        Arguments.of(100, 50)
    );
  }

  @ParameterizedTest(name = "[{index}] 실시간 전체 유저 수: {0}명 / 결과: {1}명 랭킹 조회")
  @MethodSource("paramsForFindUpTo50UserRanking")
  @DisplayName("실시간 랭킹에서 총점을 기반으로 최대 50명의 랭킹 정보를 조회할 수 있다.")
  void findRealtimeRankingInfoUpTo50ByTotalScoreTest(int userCnt, int expected) {
    // given
    Phrase phrase = createPhrase();

    List<Typing> typings = IntStream.rangeClosed(1, userCnt)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i, 100, member, phrase);
        })
        .toList();

    // when
    typingRepository.saveAll(typings);
    List<RankingResponse> sequentialRankingResponses = typingRepository
        .findRealtimeTopNWithSequentialRank(RANKING_COUNT);

    // then
    assertThat(sequentialRankingResponses).hasSize(expected);
  }

  @ParameterizedTest(name = "[{index}] 월별 전체 유저 수: {0}명 / 결과: {1}명 랭킹 조회")
  @MethodSource("paramsForFindUpTo50UserRanking")
  @DisplayName("월별 랭킹에서 총점을 기반으로 최대 50명의 랭킹 정보를 조회할 수 있다.")
  void findMonthlyRankingInfoUpTo50ByTotalScoreTest(int userCnt, int expected) {
    // given
    LocalDateTime monthStartDate = getMonthStartDate(LocalDate.now());
    LocalDateTime monthEndDate = getMonthEndDate(LocalDate.now());

    Phrase phrase = createPhrase();
    List<Typing> typings = IntStream.rangeClosed(1, userCnt)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i, 100, member, phrase);
        })
        .toList();

    // when
    typingRepository.saveAll(typings);
    List<RankingResponse> monthlySequentialRankingResponses = typingRepository
        .findMonthlyTopNWithSequentialRank(monthStartDate, monthEndDate, RANKING_COUNT);

    // then
    assertThat(monthlySequentialRankingResponses).hasSize(expected);
  }

  @Test
  @DisplayName("실시간 랭킹 조 시, 총점 기준으로 내림차순하여 랭킹 정보를 조회할 수 있다.")
  void findRealTimeRankingsSortedByTotalScoreDescendingTest() {
    // given
    Phrase phrase = createPhrase();
    List<Typing> typings = IntStream.rangeClosed(1, 20)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i * 10, 100, member, phrase);
        })
        .toList();

    typingRepository.saveAll(typings);

    // when
    List<RankingResponse> responses = typingRepository
        .findRealtimeTopNWithSequentialRank(RANKING_COUNT);

    // then
    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      // Allow ties in scores; ensure rankings are still sequential.
      assertTrue(current.getScore() >= next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
  }

  @Test
  @DisplayName("월별 랭킹 조 시, 총점 기준으로 내림차순하여 랭킹 정보를 조회할 수 있다.")
  void findMonthlyRankingsSortedByTotalScoreDescendingTest() {
    // given
    LocalDate now = LocalDate.now();
    LocalDateTime monthStartDate = getMonthStartDate(now);
    LocalDateTime monthEndDate = getMonthEndDate(now);

    Phrase phrase = createPhrase();
    List<Typing> typings = IntStream.rangeClosed(1, 20)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i * 10, 100, member, phrase);
        })
        .toList();

    typingRepository.saveAll(typings);

    // when
    List<RankingResponse> responses = typingRepository
        .findMonthlyTopNWithSequentialRank(monthStartDate, monthEndDate, RANKING_COUNT);

    // then
    for (int i = 0; i < responses.size() - 1; i++) {
      RankingResponse current = responses.get(i);
      RankingResponse next = responses.get(i + 1);

      assertTrue(current.getScore() > next.getScore());
      assertEquals(current.getRanking() + 1, next.getRanking());
    }
  }

  @Transactional
  @Test
  @DisplayName("현재 날짜에 해당하는 연월에 대한 랭킹 정보만 정확히 조회할 수 있다.")
  void findRankingsExcludingPastAndFutureMonthsTest() {
    // given
    LocalDate now = LocalDate.now();
    LocalDate pastDate = LocalDate.now().minusMonths(1);
    LocalDate futureDate = LocalDate.now().plusMonths(1);

    LocalDateTime startDate = getMonthStartDate(now);
    LocalDateTime endDate = getMonthEndDate(now);

    Phrase phrase = createPhrase();

    // when
    Member member1 = createMember("1", "testUser1");
    Typing savedTyping1 = typingRepository.save(createTyping(100, 100, member1, phrase));
    updateTypingCreatedDate(pastDate, savedTyping1);

    Member member2 = createMember("2", "testUser2");
    typingRepository.save(createTyping(100, 100, member2, phrase));

    Member member3 = createMember("3", "testUser3");
    typingRepository.save(createTyping(100, 100, member3, phrase));

    Member member4 = createMember("4", "testUser4");
    Typing savedTyping2 = typingRepository.save(createTyping(100, 100, member4, phrase));
    updateTypingCreatedDate(futureDate, savedTyping2);

    em.flush();
    em.clear();

    List<RankingResponse> responses = typingRepository
        .findMonthlyTopNWithSequentialRank(startDate, endDate, RANKING_COUNT);

    // then
    assertThat(responses).hasSize(2);
  }

  @Test
  @DisplayName("타자 결과 정보를 저장했을 때, 해당 유저의 가장 최대 점수에 대한 순위를 반환한다.")
  void returnsHighestRankForMemberWithHighestScoreTest() {
    // given
    Phrase phrase = createPhrase();
    List<Typing> typings = IntStream.rangeClosed(1, 10)
        .mapToObj(i -> {
          String str = String.valueOf(i);
          Member member = createMember(str, str);
          return createTyping(i * 10, 100, member, phrase);
        })
        .toList();

    // when
    typingRepository.saveAll(typings);
    Member findMember = memberRepository.findByKakaoId("1").orElseThrow(
        () -> new CustomException(Code.NOT_EXIST_MEMBER)
    );

    // 가장 낮은 점수를 가진 [kakaoId == 1] 유저에게 가장 높은 점수를 부여한다.
    typingRepository.save(createTyping(1000, 100, findMember, phrase));
    Long highestRank = typingRepository.findHighestRankingByMemberId(findMember.getId());

    // then
    assertThat(highestRank).isEqualTo(1L);
  }

  private void updateTypingCreatedDate(LocalDate date, Typing typing) {
    em.createNativeQuery("UPDATE typing SET created_date = :date WHERE id = :id")
        .setParameter("date", date.atStartOfDay())
        .setParameter("id", typing.getId())
        .executeUpdate();
  }

  private Typing createTyping(int cpm, int acc, Member member, Phrase phrase) {
    return Typing.builder()
        .cpm(cpm)
        .acc((double) acc)
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