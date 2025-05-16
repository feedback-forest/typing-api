package dasi.typing.api.controller.docs.typing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dasi.typing.api.controller.docs.RestDocsSupport;
import dasi.typing.api.controller.typing.TypingController;
import dasi.typing.api.controller.typing.request.TypingCreateRequest;
import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.domain.member.Role;
import dasi.typing.exception.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.Authentication;

class TypingControllerDocsTest extends RestDocsSupport {

  @Override
  protected Object initController() {
    return new TypingController(typingService);
  }

  @Test
  @DisplayName("비회원이라면 행운의 메시지만 정상적으로 응답한다.")
  void sendGuestTypingResultToClient() throws Exception {

    // given
    TypingCreateRequest request = createTypingCreateRequest();
    TypingResponse typingResponse = createTypingResponse("anonymous", Role.GUEST);

    given(
        typingService.saveTyping(any(Authentication.class), any(TypingCreateServiceRequest.class)))
        .willReturn(typingResponse);

    given(luckyMessageService.generate()).willReturn("Lucky Message");

    // when
    ApiResponse<Map<String, TypingResponse>> response
        = ApiResponse.success("typing", typingResponse);

    String expectedJson = objectMapper.writeValueAsString(response);
    String requestJson = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(
            post("/api/v1/typings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(user("anonymous").roles("GUEST"))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("phraseId").type(JsonFieldType.NUMBER)
                    .description("문구 식별자"),
                fieldWithPath("cpm").type(JsonFieldType.NUMBER)
                    .description("타자 속도 (characters per minute)"),
                fieldWithPath("acc").type(JsonFieldType.NUMBER)
                    .description("정확도 (%)"),
                fieldWithPath("wpm").type(JsonFieldType.NUMBER)
                    .description("타자 속도 (words per minute)"),
                fieldWithPath("maxCpm").type(JsonFieldType.NUMBER)
                    .description("최대 타자 속도 (cpm)")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드 (0=성공, 그 외=에러)"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("응답 메시지"),
                fieldWithPath("data")
                    .type(JsonFieldType.OBJECT)
                    .description("응답 데이터 객체"),
                fieldWithPath("data.typing")
                    .type(JsonFieldType.OBJECT)
                    .description("타이핑 정보를 담고 있는 객체"),
                fieldWithPath("data.typing.role").type(JsonFieldType.STRING)
                    .description("사용자 역할 (예: GUEST, USER)"),
                fieldWithPath("data.typing.nickname").type(JsonFieldType.STRING)
                    .description("사용자 닉네임"),
                fieldWithPath("data.typing.rank").type(JsonFieldType.NUMBER)
                    .description("순위 (회원인 경우에만 값이 있으며, 비회원은 null)"),
                fieldWithPath("data.typing.luckyMessage").type(JsonFieldType.STRING)
                    .description("행운의 메시지")
            )
        ));
  }

  @Test
  @DisplayName("회원이라면 행운의 메시지와 랭킹을 정상적으로 응답한다.")
  void sendUserTypingResultToClient() throws Exception {

    // given
    TypingCreateRequest request = createTypingCreateRequest();
    TypingResponse typingResponse = createTypingResponse("dragon", Role.USER);

    given(
        typingService.saveTyping(any(Authentication.class), any(TypingCreateServiceRequest.class)))
        .willReturn(typingResponse);

    given(luckyMessageService.generate()).willReturn("Lucky Message");

    // when
    ApiResponse<Map<String, TypingResponse>> response
        = ApiResponse.success("typing", typingResponse);

    String expectedJson = objectMapper.writeValueAsString(response);
    String requestJson = objectMapper.writeValueAsString(request);

    // then
    mockMvc.perform(
            post("/api/v1/typings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(user("dragon").roles("USER"))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("phraseId")
                    .type(JsonFieldType.NUMBER)
                    .description("문장 식별자"),
                fieldWithPath("cpm")
                    .type(JsonFieldType.NUMBER)
                    .description("타자 속도"),
                fieldWithPath("acc")
                    .type(JsonFieldType.NUMBER)
                    .description("정확도"),
                fieldWithPath("wpm")
                    .type(JsonFieldType.NUMBER)
                    .description("타자 속도"),
                fieldWithPath("maxCpm")
                    .type(JsonFieldType.NUMBER)
                    .description("최대 타자 속도 (cpm)")
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
                fieldWithPath("data.typing")
                    .type(JsonFieldType.OBJECT)
                    .description("타자 정보를 담고 있는 객체"),
                fieldWithPath("data.typing.role")
                    .type(JsonFieldType.STRING)
                    .description("사용자 역할 (예: GUEST, USER)"),
                fieldWithPath("data.typing.nickname")
                    .type(JsonFieldType.STRING)
                    .description("사용자 닉네임"),
                fieldWithPath("data.typing.rank")
                    .type(JsonFieldType.NUMBER)
                    .description("순위 (회원인 경우에만 값이 있으며, 비회원은 null)"),
                fieldWithPath("data.typing.luckyMessage")
                    .type(JsonFieldType.STRING)
                    .description("행운의 메시지")
            )
        ));
  }

  private static TypingCreateRequest createTypingCreateRequest() {
    return TypingCreateRequest.builder()
        .phraseId(1L)
        .cpm(100)
        .acc(100)
        .wpm(100)
        .maxCpm(150).build();
  }

  private static TypingResponse createTypingResponse(String username, Role expectedRole) {
    return TypingResponse.builder()
        .role(expectedRole)
        .nickname(username)
        .rank(1L)
        .luckyMessage("Lucky Message").build();
  }

}
