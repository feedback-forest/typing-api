package dasi.typing.api.service.ranking.dto;

import java.time.LocalDateTime;

public record RankingMemberData(
    Long memberId,
    String nickname,
    int score,
    LocalDateTime createdDate
) {

}
