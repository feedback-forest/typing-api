package dasi.typing.api.service.mypage;

import dasi.typing.api.controller.mypage.response.MyPageResponse;
import dasi.typing.api.controller.mypage.response.TypingHistoryResponse;
import dasi.typing.api.controller.mypage.response.WeeklyScoreResponse;
import dasi.typing.api.service.ranking.RankingCacheService;
import dasi.typing.domain.member.Member;
import dasi.typing.domain.member.MemberRepository;
import dasi.typing.domain.typing.Typing;
import dasi.typing.domain.typing.TypingRepository;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

  private static final int WEEKLY_SCORE_WEEKS = 12;

  private final MemberRepository memberRepository;
  private final TypingRepository typingRepository;
  private final RankingCacheService rankingCacheService;

  public MyPageResponse getMyPage(String kakaoId) {
    Member member = memberRepository.findByKakaoId(kakaoId)
        .orElseThrow(() -> new CustomException(Code.NOT_EXIST_MEMBER));

    long totalTypingCount = typingRepository.countByMember(member);
    Integer highestScore = typingRepository.findHighestScoreByMember(member);
    Long currentRanking = getCurrentRanking(member.getId());

    List<TypingHistoryResponse> typingHistories = getTypingHistories(member);
    List<WeeklyScoreResponse> weeklyScores = getWeeklyScores(member);

    return MyPageResponse.builder()
        .nickname(member.getNickname())
        .totalTypingCount(totalTypingCount)
        .highestScore(highestScore)
        .currentRanking(currentRanking)
        .typingHistories(typingHistories)
        .weeklyScores(weeklyScores)
        .build();
  }

  private Long getCurrentRanking(Long memberId) {
    try {
      Long rank = rankingCacheService.getMemberRealtimeRank(memberId);
      if (rank != null) {
        return rank;
      }
    } catch (Exception e) {
      log.warn("[MyPage] Redis 랭킹 조회 실패. DB fallback 실행.", e);
    }

    return typingRepository.findHighestRankingByMemberId(memberId);
  }

  private List<TypingHistoryResponse> getTypingHistories(Member member) {
    List<Typing> typings = typingRepository.findByMemberOrderByCreatedDateDesc(member);
    return typings.stream()
        .map(TypingHistoryResponse::from)
        .toList();
  }

  private List<WeeklyScoreResponse> getWeeklyScores(Member member) {
    LocalDateTime since = LocalDate.now()
        .minusWeeks(WEEKLY_SCORE_WEEKS)
        .atStartOfDay();

    List<Typing> typings = typingRepository.findByMemberAndCreatedDateAfter(member, since);

    if (typings.isEmpty()) {
      return List.of();
    }

    Map<LocalDate, Integer> weeklyMaxScores = typings.stream()
        .collect(Collectors.groupingBy(
            typing -> typing.getCreatedDate().toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            Collectors.collectingAndThen(
                Collectors.maxBy(Comparator.comparingInt(Typing::getScore)),
                opt -> opt.map(Typing::getScore).orElse(0)
            )
        ));

    return weeklyMaxScores.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> WeeklyScoreResponse.builder()
            .weekStartDate(entry.getKey())
            .highestScore(entry.getValue())
            .build())
        .toList();
  }
}
