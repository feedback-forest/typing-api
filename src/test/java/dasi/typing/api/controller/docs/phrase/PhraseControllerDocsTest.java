package dasi.typing.api.controller.docs.phrase;

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
import dasi.typing.api.controller.phrase.PhraseController;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import dasi.typing.exception.ApiResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

class PhraseControllerDocsTest extends RestDocsSupport {

  @Override
  protected Object initController() {
    return new PhraseController(phraseService);
  }

  private final String ACCESS_TOKEN = "Bearer eyJhbGciOi...";

  @Test
  @DisplayName("문장들을 조회하여 정상적으로 응답을 반환한다.")
  void sendPhraseToClientTest() throws Exception {

    // given
    List<PhraseResponse> responses = createPhraseResponse();
    given(phraseService.getRandomPhrases())
        .willReturn(responses);

    // when
    ApiResponse<Map<String, List<PhraseResponse>>> expected
        = ApiResponse.success("phrases", responses);
    String expectedJson = objectMapper.writeValueAsString(expected);

    // then
    mockMvc.perform(
            get("/api/v1/phrases")
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

            // 공통 응답 바디 필드
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
                fieldWithPath("data.phrases")
                    .type(JsonFieldType.ARRAY)
                    .description("문장 리스트"),
                fieldWithPath("data.phrases[].id")
                    .type(JsonFieldType.NUMBER)
                    .description("문장 식별자"),
                fieldWithPath("data.phrases[].sentence")
                    .type(JsonFieldType.STRING)
                    .description("문장 내용"),
                fieldWithPath("data.phrases[].title")
                    .type(JsonFieldType.STRING)
                    .description("문장 제목"),
                fieldWithPath("data.phrases[].author")
                    .type(JsonFieldType.STRING)
                    .description("작성자"),
                fieldWithPath("data.phrases[].lang")
                    .type(JsonFieldType.STRING)
                    .description("언어 코드 (예: KO, EN)"),
                fieldWithPath("data.phrases[].type")
                    .type(JsonFieldType.STRING)
                    .description("문장 타입 (예: QUOTE, POEM)")
            )
        ));
  }

  private List<PhraseResponse> createPhraseResponse() {

    PhraseResponse response1 = PhraseResponse.builder()
        .id(1L)
        .sentence("오늘도 화이팅")
        .title("빛나는 하루")
        .author("dragon")
        .lang(Lang.KO.toString())
        .type(LangType.POEM.toString()).build();

    PhraseResponse response2 = PhraseResponse.builder()
        .id(2L)
        .sentence("Keep going")
        .title("Fighting")
        .author("dragon")
        .lang(Lang.EN.toString())
        .type(LangType.QUOTE.toString()).build();

    return List.of(response1, response2);
  }
}