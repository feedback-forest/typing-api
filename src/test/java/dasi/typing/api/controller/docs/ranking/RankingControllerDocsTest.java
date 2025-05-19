package dasi.typing.api.controller.docs.ranking;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dasi.typing.api.controller.docs.RestDocsSupport;
import dasi.typing.api.controller.ranking.RankingController;
import dasi.typing.api.controller.ranking.response.RankingResponse;
import dasi.typing.exception.ApiResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

class RankingControllerDocsTest extends RestDocsSupport {

  @Override
  protected Object initController() {
    return new RankingController(rankingService);
  }

  private final String ACCESS_TOKEN = "Bearer eyJhbGciOi...";

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
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(

            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 ACCESS TOKEN")
                    .optional()
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.OBJECT)
                    .description("응답 컨텐츠"),
                fieldWithPath("data.rankings")
                    .type(JsonFieldType.ARRAY)
                    .description("랭킹 리스트"),
                fieldWithPath("data.rankings[].memberId")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 식별자"),
                fieldWithPath("data.rankings[].nickname")
                    .type(JsonFieldType.STRING)
                    .description("회원 닉네임"),
                fieldWithPath("data.rankings[].score")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 점수"),
                fieldWithPath("data.rankings[].ranking")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 순위")
            )
        ));
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
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 ACCESS TOKEN")
                    .optional()
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.OBJECT)
                    .description("응답 컨텐츠"),
                fieldWithPath("data.rankings")
                    .type(JsonFieldType.ARRAY)
                    .description("랭킹 리스트"),
                fieldWithPath("data.rankings[].memberId")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 식별자"),
                fieldWithPath("data.rankings[].nickname")
                    .type(JsonFieldType.STRING)
                    .description("회원 닉네임"),
                fieldWithPath("data.rankings[].score")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 점수"),
                fieldWithPath("data.rankings[].ranking")
                    .type(JsonFieldType.NUMBER)
                    .description("회원 순위")
            )
        ));

  }

  private static List<RankingResponse> createRankingResponse() {

    RankingResponse response1 = RankingResponse.builder()
        .memberId(1L)
        .nickname("dragon")
        .score(180)
        .createdDate(Timestamp.valueOf(LocalDateTime.now()))
        .ranking(1L).build();

    RankingResponse response2 = RankingResponse.builder()
        .memberId(2L)
        .nickname("tiger")
        .score(170)
        .createdDate(Timestamp.valueOf(LocalDateTime.now()))
        .ranking(2L).build();

    return List.of(response1, response2);
  }
}
