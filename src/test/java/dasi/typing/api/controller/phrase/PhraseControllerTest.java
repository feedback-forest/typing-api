package dasi.typing.api.controller.phrase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dasi.typing.ControllerTestSupport;
import dasi.typing.api.controller.phrase.response.PhraseResponse;
import dasi.typing.domain.phrase.Lang;
import dasi.typing.domain.phrase.LangType;
import dasi.typing.domain.phrase.Phrase;
import dasi.typing.exception.ApiResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
class PhraseControllerTest extends ControllerTestSupport {

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
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(phraseService).getRandomPhrases();
  }

  private List<PhraseResponse> createPhraseResponse() {
    return List.of(PhraseResponse.from(
        Phrase.builder()
            .sentence("문장")
            .title("제목")
            .author("저자")
            .lang(Lang.KO)
            .type(LangType.POEM).build()
    ));
  }
}