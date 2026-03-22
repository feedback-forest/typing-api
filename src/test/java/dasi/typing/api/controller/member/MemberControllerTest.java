package dasi.typing.api.controller.member;

import static dasi.typing.exception.Code.ALREADY_EXIST_MEMBER;
import static dasi.typing.exception.Code.ALREADY_EXIST_NICKNAME;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_ACCESS_TOKEN;
import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CV_NICKNAME;
import static dasi.typing.exception.Code.INVALID_LENGTH_NICKNAME;
import static dasi.typing.exception.Code.INVALID_TEMP_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import dasi.typing.ControllerTestSupport;
import dasi.typing.api.controller.member.request.MemberCreateRequest;
import dasi.typing.api.controller.member.response.NicknameResponse;
import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.exception.ApiResponse;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import dasi.typing.jwt.JwtToken;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

class MemberControllerTest extends ControllerTestSupport {

  private final String KAKAO_ID = "kakaoId";
  private final String NICKNAME = "nickname";
  private final String TEMP_TOKEN = "tempToken";
  private final String RANDOM_NICKNAME = "RandomNickname";

  @Test
  @DisplayName("회원이 아닌 유저가 회원가입에 성공하면 JWT 토큰을 HTTP-only 쿠키로 발급받고, 정상적으로 응답을 반환한다.")
  void signUpSuccessTest() throws Exception {

    // given
    JwtToken jwtToken = new JwtToken("Bearer", "testAccessToken", "testRefreshToken");
    given(memberService.signUp(any(String.class), any(MemberCreateServiceRequest.class)))
        .willReturn(jwtToken);

    MemberCreateRequest request = new MemberCreateRequest(NICKNAME,
        List.of(ConsentType.TERMS_OF_SERVICE, ConsentType.PRIVACY_POLICY));

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedSuccessJson(true);

    // then
    mockMvc.perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  private static Stream<Arguments> singUpScenarios() {
    return Stream.of(
        Arguments.of(ALREADY_EXIST_MEMBER),
        Arguments.of(INVALID_TEMP_TOKEN)
    );
  }

  @DisplayName("회원이 아닌 유저가 회원가입에 실패하면 상황에 맞는 예외를 반환한다.")
  @ParameterizedTest(name = "[{index}] -> {0}")
  @MethodSource("singUpScenarios")
  void signUpFailureTest(Code code) throws Exception {

    // given
    given(memberService.signUp(any(String.class), any(MemberCreateServiceRequest.class)))
        .willThrow(new CustomException(code));

    MemberCreateRequest request = new MemberCreateRequest(NICKNAME,
        List.of(ConsentType.TERMS_OF_SERVICE, ConsentType.PRIVACY_POLICY));

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(code);

    // then
    mockMvc.perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().is(code.getHttpStatus().value()))
        .andExpect(content().json(expectedJson));
  }

  @Test
  @DisplayName("모든 닉네임 형식에 맞게 작성한 경우 정상적으로 응답을 반환한다.")
  void validateNicknameSuccessTest() throws Exception {

    // given
    willDoNothing()
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = new MemberNicknameServiceRequest("nickname");

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedSuccessJson(true);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(memberService).validateNickname(any(MemberNicknameServiceRequest.class));
  }

  private static Stream<Arguments> nicknameScenarios() {
    return Stream.of(
        Arguments.of("abcdefghijklmnop", INVALID_LENGTH_NICKNAME),
        Arguments.of("abcdeㄹㅇ", INVALID_CV_NICKNAME),
        Arguments.of("abcde*&^", INVALID_CHARACTER_NICKNAME),
        Arguments.of("abcde", ALREADY_EXIST_NICKNAME)
    );
  }

  @DisplayName("옳지 않은 닉네임은 상황에 맞는 예외를 반환한다.")
  @ParameterizedTest(name = "[{index}] -> {1} 예외 상황 검증")
  @MethodSource("nicknameScenarios")
  void validateNicknameFailureTest(String nickname, Code code) throws Exception {

    // given
    willThrow(new CustomException(code))
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = new MemberNicknameServiceRequest(nickname);

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(code);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().is(code.getHttpStatus().value()))
        .andExpect(content().json(expectedJson));

    verify(memberService).validateNickname(any(MemberNicknameServiceRequest.class));
  }

  @Test
  @DisplayName("랜덤 닉네임을 생성하여 정상적으로 응답을 반환한다.")
  void generateRandomNicknameTest() throws Exception {

    // given
    given(nicknameService.generate()).willReturn(RANDOM_NICKNAME);

    // when
    String expectedJson = createExpectedSuccessJson(new NicknameResponse(RANDOM_NICKNAME));

    // then
    mockMvc.perform(
            get("/api/v1/members/nickname/random")
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(nicknameService).generate();
  }

  @Test
  @DisplayName("회원 ACCESS 토큰이 만료되면 REFRESH 토큰을 통해 재발급받고, 쿠키로 정상적으로 응답을 반환한다.")
  void reissueSuccessTest() throws Exception {
    // given
    JwtToken jwtToken = new JwtToken("Bearer", "newAccessToken", "newRefreshToken");
    given(memberService.reissue(any(String.class)))
        .willReturn(jwtToken);

    // when
    String expectedJson = createExpectedSuccessJson(true);

    // then
    mockMvc.perform(
            post("/api/v1/members/reissue")
                .with(userAuth(KAKAO_ID))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));

    verify(memberService).reissue(any(String.class));
  }

  private static Stream<Arguments> reissueScenarios() {
    return Stream.of(
        Arguments.of(INVALID_ACCESS_TOKEN),
        Arguments.of(EXPIRED_REFRESH_TOKEN)
    );
  }

  @DisplayName("회원 ACCESS 토큰 재발급이 실패하면 상황에 맞는 예외를 반환한다.")
  @ParameterizedTest(name = "[{index}] -> {0}")
  @MethodSource("reissueScenarios")
  void reissueFailureTest(Code code) throws Exception {
    // given
    given(memberService.reissue(any(String.class)))
        .willThrow(new CustomException(code));

    // when
    String expectedJson = createExpectedErrorJson(code);

    // then
    mockMvc.perform(
            post("/api/v1/members/reissue")
                .with(userAuth(KAKAO_ID))
        )
        .andDo(print())
        .andExpect(status().is(code.getHttpStatus().value()))
        .andExpect(content().json(expectedJson));

    verify(memberService).reissue(any(String.class));
  }

  private <T> String createExpectedSuccessJson(T data) throws JsonProcessingException {
    ApiResponse<T> response = ApiResponse.success(data);
    return objectMapper.writeValueAsString(response);
  }

  private String createExpectedErrorJson(Code code) throws JsonProcessingException {
    ApiResponse<Boolean> response = ApiResponse.error(code);
    return objectMapper.writeValueAsString(response);
  }
}
