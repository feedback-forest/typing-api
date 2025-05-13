package dasi.typing.api.controller.typing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dasi.typing.ControllerTestSupport;
import dasi.typing.api.controller.typing.request.TypingCreateRequest;
import dasi.typing.api.service.typing.request.TypingCreateServiceRequest;
import dasi.typing.api.service.typing.response.TypingResponse;
import dasi.typing.domain.member.Role;
import dasi.typing.exception.ApiResponse;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
class TypingControllerTest extends ControllerTestSupport {

  private static Stream<Arguments> userScenarios() {
    return Stream.of(
        Arguments.of("anonymous", "GUEST", Role.GUEST),
        Arguments.of("user", "USER", Role.USER)
    );
  }

  @DisplayName("회원 유형에 따라 타자 수행 결과 정보를 전달하고, 행운의 메시지와 랭킹을 정상적으로 응답한다.")
  @ParameterizedTest(name = "[{index}] username={0}, userRole={1} → expectedRole={2}")
  @MethodSource("userScenarios")
  void sendVariousUserTypingResultToClient(String username, String userRole, Role expectedRole)
      throws Exception {

    // given
    TypingCreateRequest request = createTypingCreateRequest();
    TypingResponse typingResponse = createTypingResponse(username, expectedRole);

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
                .with(user(username).roles(userRole))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(typingService)
        .saveTyping(any(Authentication.class), any(TypingCreateServiceRequest.class));
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