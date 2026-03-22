package dasi.typing.api.service.mypage;

import static dasi.typing.utils.ConstantUtil.RANKING_MEMBER_KEY;
import static dasi.typing.utils.ConstantUtil.RANKING_REALTIME_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dasi.typing.api.controller.mypage.response.MyPageResponse;
import dasi.typing.api.controller.mypage.response.TypingHistoryResponse;
import dasi.typing.api.controller.mypage.response.WeeklyScoreResponse;
import dasi.typing.api.service.ranking.RankingCacheService;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.domain.phrase.PhraseRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.exception.CustomException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class MyPageServiceTest {

  @Autowired
  private MyPageService myPageService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PhraseRepository phraseRepository;

  @Autowired
  private TypingRepository typingRepository;

  @Autowired
  private RankingCacheService rankingCacheService;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @BeforeEach
  void setUp() {
    redisTemplate.delete(RANKING_REALTIME_KEY);
    redisTemplate.delete(RANKING_MEMBER_KEY);
  }

  @AfterEach
  void tearDown() {
    typingRepository.deleteAllInBatch();
    phraseRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();
    redisTemplate.delete(RANKING_REALTIME_KEY);
    redisTemplate.delete(RANKING_MEMBER_KEY);
  }

  @Test
  @DisplayName("마이페이지 조회 시 닉네임, 총 타이핑 수, 최고 점수, 현재 랭킹을 반환한다.")
  void getMyPageBasicInfoTest() {
    // given
    Member member = memberRepository.save(new Member("kakao123", "테스트유저"));
    Phrase phrase = phraseRepository.save(createPhrase("테스트 문장입니다."));

    typingRepository.save(createTyping(200, 0.95, 40, 220, member, phrase));
    typingRepository.save(createTyping(300, 0.90, 60, 320, member, phrase));
    typingRepository.save(createTyping(250, 0.88, 50, 270, member, phrase));

    // Redis 랭킹 등록
    rankingCacheService.addOrUpdateIfBetter(
        member.getId(), member.getNickname(), 300, 320, 0.90, LocalDateTime.now());

    // when
    MyPageResponse response = myPageService.getMyPage("kakao123");

    // then
    assertThat(response.nickname()).isEqualTo("테스트유저");
    assertThat(response.totalTypingCount()).isEqualTo(3);
    assertThat(response.highestScore()).isNotNull();
    assertThat(response.currentRanking()).isEqualTo(1L);
  }

  @Test
  @DisplayName("마이페이지 조회 시 타이핑 이력이 최신순으로 반환된다.")
  void getMyPageTypingHistoriesTest() {
    // given
    Member member = memberRepository.save(new Member("kakao456", "이력테스트"));
    Phrase phrase1 = phraseRepository.save(createPhrase("첫 번째 문장"));
    Phrase phrase2 = phraseRepository.save(createPhrase("두 번째 문장"));

    typingRepository.save(createTyping(100, 0.85, 20, 120, member, phrase1));
    typingRepository.save(createTyping(200, 0.90, 40, 220, member, phrase2));

    // when
    MyPageResponse response = myPageService.getMyPage("kakao456");
    List<TypingHistoryResponse> histories = response.typingHistories();

    // then
    assertThat(histories).hasSize(2);
    assertThat(histories.getFirst().getCreatedDate())
        .isAfterOrEqualTo(histories.getLast().getCreatedDate());
    assertThat(histories).extracting(TypingHistoryResponse::getSentence)
        .containsExactlyInAnyOrder("첫 번째 문장", "두 번째 문장");
  }

  @Test
  @DisplayName("타이핑 기록이 없는 회원의 마이페이지를 조회할 수 있다.")
  void getMyPageNoTypingDataTest() {
    // given
    memberRepository.save(new Member("kakao789", "빈데이터유저"));

    // when
    MyPageResponse response = myPageService.getMyPage("kakao789");

    // then
    assertThat(response.nickname()).isEqualTo("빈데이터유저");
    assertThat(response.totalTypingCount()).isZero();
    assertThat(response.highestScore()).isNull();
    assertThat(response.currentRanking()).isNull();
    assertThat(response.typingHistories()).isEmpty();
    assertThat(response.weeklyScores()).isEmpty();
  }

  @Test
  @DisplayName("존재하지 않는 kakaoId로 마이페이지 조회 시 NOT_EXIST_MEMBER 예외가 발생한다.")
  void getMyPageNotExistMemberTest() {
    // when & then
    assertThatThrownBy(() -> myPageService.getMyPage("nonexistent"))
        .isInstanceOf(CustomException.class);
  }

  @Test
  @DisplayName("주간 최고 점수 데이터가 반환된다.")
  void getMyPageWeeklyScoresTest() {
    // given
    Member member = memberRepository.save(new Member("kakao_weekly", "주간테스트"));
    Phrase phrase = phraseRepository.save(createPhrase("주간 점수 테스트 문장"));

    typingRepository.save(createTyping(100, 0.90, 20, 120, member, phrase));
    typingRepository.save(createTyping(300, 0.95, 60, 320, member, phrase));
    typingRepository.save(createTyping(200, 0.88, 40, 220, member, phrase));

    // when
    MyPageResponse response = myPageService.getMyPage("kakao_weekly");
    List<WeeklyScoreResponse> weeklyScores = response.weeklyScores();

    // then
    assertThat(weeklyScores).isNotEmpty();
    assertThat(weeklyScores).extracting(WeeklyScoreResponse::getHighestScore)
        .allMatch(score -> score > 0);
  }

  private Typing createTyping(int cpm, double acc, int wpm, int maxCpm,
      Member member, Phrase phrase) {
    return Typing.builder()
        .cpm(cpm)
        .acc(acc)
        .wpm(wpm)
        .maxCpm(maxCpm)
        .member(member)
        .phrase(phrase)
        .build();
  }

  private Phrase createPhrase(String sentence) {
    return Phrase.builder()
        .sentence(sentence)
        .title("테스트")
        .author("테스트 작가")
        .lang(Lang.KO)
        .type(LangType.QUOTE)
        .build();
  }
}
