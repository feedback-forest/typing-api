package dasi.typing.api.controller.ranking;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dasi.typing.ControllerTestSupport;
import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.exception.ApiResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
class RankingControllerTest extends ControllerTestSupport {

  @Test
  @DisplayName("실시간 랭킹 데이터를 조회하여 정상적으로 응답을 반환한다.")
  void sendRealTimeRankingToClientTest() throws Exception {

    // given
    List<RankingResponse> responses = createRankingResponse();
    given(rankingService.getRealTimeRanking())
        .willReturn(responses);

    // when
    ApiResponse<Map<String, List<RankingResponse>>> expected
        = ApiResponse.success("rankings", responses);
    String expectedJson = objectMapper.writeValueAsString(expected);

    // then
    mockMvc.perform(
            get("/api/v1/rankings/realtime")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(rankingService).getRealTimeRanking();
  }

  @Test
  @DisplayName("현재 날짜에 해당하는 연월 랭킹 데이터를 조회하여 정상적으로 응답을 반환한다.")
  void sendMonthlyRankingToClientTest() throws Exception {

    // given
    List<RankingResponse> responses = createRankingResponse();
    given(rankingService.getMonthlyRanking())
        .willReturn(responses);

    // when
    ApiResponse<Map<String, List<RankingResponse>>> expected
        = ApiResponse.success("rankings", responses);
    String expectedJson = objectMapper.writeValueAsString(expected);
    System.out.println(expectedJson);

    // then
    mockMvc.perform(
            get("/api/v1/rankings/monthly")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(rankingService).getMonthlyRanking();
  }

  private static List<RankingResponse> createRankingResponse() {
    return List.of(RankingResponse.builder()
        .memberId(1L)
        .nickname("dragon")
        .score(180)
        .createdDate(Timestamp.valueOf(LocalDateTime.now()))
        .ranking(1L).build());
  }
}