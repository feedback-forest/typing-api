package dasi.typing.api.controller.mypage.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dasi.typing.domain.typing.Typing;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TypingHistoryResponse {

  private final Long typingId;
  private final String sentence;
  private final Integer cpm;
  private final Integer wpm;
  private final Integer maxCpm;
  private final Double acc;
  private final Integer score;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private final LocalDateTime createdDate;

  @Builder
  private TypingHistoryResponse(Long typingId, String sentence, Integer cpm, Integer wpm,
      Integer maxCpm, Double acc, Integer score, LocalDateTime createdDate) {
    this.typingId = typingId;
    this.sentence = sentence;
    this.cpm = cpm;
    this.wpm = wpm;
    this.maxCpm = maxCpm;
    this.acc = acc;
    this.score = score;
    this.createdDate = createdDate;
  }

  public static TypingHistoryResponse from(Typing typing) {
    return TypingHistoryResponse.builder()
        .typingId(typing.getId())
        .sentence(typing.getPhrase().getSentence())
        .cpm(typing.getCpm())
        .wpm(typing.getWpm())
        .maxCpm(typing.getMaxCpm())
        .acc(typing.getAcc())
        .score(typing.getScore())
        .createdDate(typing.getCreatedDate())
        .build();
  }
}
