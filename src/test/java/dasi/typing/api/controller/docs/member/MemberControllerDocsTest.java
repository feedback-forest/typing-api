package dasi.typing.api.controller.docs.member;

import static dasi.typing.exception.Code.ALREADY_EXIST_MEMBER;
import static dasi.typing.exception.Code.ALREADY_EXIST_NICKNAME;
import static dasi.typing.exception.Code.EXPIRED_REFRESH_TOKEN;
import static dasi.typing.exception.Code.INVALID_ACCESS_TOKEN;
import static dasi.typing.exception.Code.INVALID_CHARACTER_NICKNAME;
import static dasi.typing.exception.Code.INVALID_CV_NICKNAME;
import static dasi.typing.exception.Code.INVALID_LENGTH_NICKNAME;
import static dasi.typing.exception.Code.INVALID_TEMP_TOKEN;
import static dasi.typing.exception.Code.KAKAO_ACCOUNT_NOT_REGISTERED;
import static dasi.typing.utils.CommonConstant.BEARER_PREFIX;
import static dasi.typing.utils.CommonConstant.REDIS_KEY_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import dasi.typing.api.controller.docs.RestDocsSupport;
import dasi.typing.api.controller.member.MemberController;
import dasi.typing.api.controller.member.request.MemberCreateRequest;
import dasi.typing.api.controller.member.response.NicknameResponse;
import dasi.typing.api.service.member.request.MemberCreateServiceRequest;
import dasi.typing.api.service.member.request.MemberNicknameServiceRequest;
import dasi.typing.domain.consent.ConsentType;
import dasi.typing.exception.ApiResponse;
import dasi.typing.exception.Code;
import dasi.typing.exception.CustomException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

class MemberControllerDocsTest extends RestDocsSupport {

  private final String KAKAO_ID = "1234567890";
  private final String NICKNAME = "dragon";
  private final String TEMP_TOKEN = "457e32c9-9780-41f7-923c-bfee04a44057";
  private final String ACCESS_TOKEN = "Bearer eyJhbGciOi...";
  private final String RANDOM_NICKNAME = "아름다운취준생";

  @Override
  protected Object initController() {
    return new MemberController(memberService, nicknameService);
  }

  @Test
  @DisplayName("회원은 성공적으로 로그인하여 JWT 토큰을 발급받고, 헤더에 담아 정상적으로 응답을 반환한다.")
  void signInSuccessTest() throws Exception {

    // given
    given(memberService.signIn(any(String.class)))
        .willReturn(ACCESS_TOKEN);

    // when & then
    String expectedJson = createExpectedSuccessJson(true);

    mockMvc.perform(
            get("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, REDIS_KEY_PREFIX + TEMP_TOKEN)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + ACCESS_TOKEN))
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("임시 인증 토큰 -> `auth:temp-token:{tempToken}`")
            ),

            responseHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 ACCESS TOKEN")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("회원이 아닌 유저가 로그인 했을 때, KAKAO_ACCOUNT_NOT_REGISTERED 예외를 반환한다.")
  void signInFailureTest() throws Exception {

    // given
    given(memberService.signIn(any(String.class)))
        .willThrow(new CustomException(KAKAO_ACCOUNT_NOT_REGISTERED));

    // when & then
    String expectedJson = createExpectedErrorJson(KAKAO_ACCOUNT_NOT_REGISTERED);

    mockMvc.perform(
            get("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, REDIS_KEY_PREFIX + TEMP_TOKEN)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("임시 인증 토큰 -> `auth:temp-token:{tempToken}`")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("회원이 아닌 유저가 이미 존재하는 회원의 식별값을 통해 회원가입을 시도하면 ALREADY_EXIST_MEMBER 예외를 반환한다.")
  void signUpAlreadyExistMemberFailureTest() throws Exception {

    // given
    given(memberService.signUp(any(String.class), any(MemberCreateServiceRequest.class)))
        .willThrow(new CustomException(ALREADY_EXIST_MEMBER));

    MemberCreateRequest request = new MemberCreateRequest(NICKNAME,
        List.of(ConsentType.TERMS_OF_SERVICE, ConsentType.PRIVACY_POLICY));

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(ALREADY_EXIST_MEMBER);

    // then
    mockMvc.perform(
            post("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("카카오 로그인으로 발급된 임시 토큰 (3분 유효)")
            ),

            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임"),
                fieldWithPath("agreements")
                    .type(JsonFieldType.ARRAY)
                    .description("필수 동의 항목 리스트"),
                fieldWithPath("agreements[]")
                    .type(JsonFieldType.ARRAY)
                    .description("동의 항목 (TERMS_OF_SERVICE, PRIVACY_POLICY)")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("임시 토큰이 없는 유저가 회원가입을 시도하면 INVALID_TEMP_TOKEN 예외를 반환한다.")
  void signUpInvalidTempTokenFailureTest() throws Exception {

    // given
    given(memberService.signUp(any(String.class), any(MemberCreateServiceRequest.class)))
        .willThrow(new CustomException(INVALID_TEMP_TOKEN));

    MemberCreateRequest request = new MemberCreateRequest(NICKNAME,
        List.of(ConsentType.TERMS_OF_SERVICE, ConsentType.PRIVACY_POLICY));

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(INVALID_TEMP_TOKEN);

    // then
    mockMvc.perform(
            post("/api/v1/members")
                .header(HttpHeaders.AUTHORIZATION, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("카카오 로그인으로 발급된 임시 토큰 (3분 유효)")
            ),

            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임"),
                fieldWithPath("agreements")
                    .type(JsonFieldType.ARRAY)
                    .description("필수 동의 항목 리스트"),
                fieldWithPath("agreements[]")
                    .type(JsonFieldType.ARRAY)
                    .description("동의 항목 (TERMS_OF_SERVICE, PRIVACY_POLICY)")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("모든 닉네임 형식에 맞게 작성한 경우 정상적으로 응답을 반환한다.")
  void validateNicknameSuccessTest() throws Exception {

    // given
    willDoNothing()
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = MemberNicknameServiceRequest.builder()
        .nickname(NICKNAME).build();

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
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("2자 ~ 12자의 닉네임 길이 조건을 만족하지 못하면 INVALID_LENGTH_NICKNAME 예외를 반환한다.")
  void validateNicknameInvalidLengthNicknameFailureTest() throws Exception {

    // given
    willThrow(new CustomException(INVALID_LENGTH_NICKNAME))
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = MemberNicknameServiceRequest.builder()
        .nickname("abcdefghijklmnop").build();

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(INVALID_LENGTH_NICKNAME);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("자음 또는 모음이 포함된 닉네임은 INVALID_CV_NICKNAME 예외를 반환한다.")
  void validateNicknameInvalidCVNicknameFailureTest() throws Exception {

    // given
    willThrow(new CustomException(INVALID_CV_NICKNAME))
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = MemberNicknameServiceRequest.builder()
        .nickname("abcdeㄹㅇ").build();

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(INVALID_CV_NICKNAME);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("특수 문자가 포함된 닉네임은 INVALID_CHARACTER_NICKNAME 예외를 반환한다.")
  void validateNicknameInvalidCharacterNicknameFailureTest() throws Exception {

    // given
    willThrow(new CustomException(INVALID_CHARACTER_NICKNAME))
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = MemberNicknameServiceRequest.builder()
        .nickname("abcde*&^").build();

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(INVALID_CHARACTER_NICKNAME);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("이미 사용 중인 닉네임은 ALREADY_EXIST_NICKNAME 예외를 반환한다.")
  void validateNicknameAlreadyExistNicknameFailureTest() throws Exception {

    // given
    willThrow(new CustomException(ALREADY_EXIST_NICKNAME))
        .given(memberService)
        .validateNickname(any(MemberNicknameServiceRequest.class));

    MemberNicknameServiceRequest request = MemberNicknameServiceRequest.builder()
        .nickname("dragon").build();

    // when
    String requestJson = objectMapper.writeValueAsString(request);
    String expectedJson = createExpectedErrorJson(ALREADY_EXIST_NICKNAME);

    // then
    mockMvc.perform(
            post("/api/v1/members/nickname/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(guestAuth(TEMP_TOKEN))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestFields(
                fieldWithPath("nickname")
                    .type(JsonFieldType.STRING)
                    .description("검증 대상 닉네임")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
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
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
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
                fieldWithPath("data.nickname")
                    .type(JsonFieldType.STRING)
                    .description("생성된 랜덤 닉네임")
            )
        ));
  }

  @Test
  @DisplayName("회원 ACCESS 토큰이 만료되면 REFRESH 토큰을 통해 재발급받고, 헤더에 담아 정상적으로 응답을 반환한다.")
  void reissueSuccessTest() throws Exception {

    // given
    given(memberService.reissue(any(String.class)))
        .willReturn(ACCESS_TOKEN);

    // when
    String expectedJson = createExpectedSuccessJson(true);

    // then
    mockMvc.perform(
            post("/api/v1/members/reissue")
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)
                .with(userAuth(KAKAO_ID))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + ACCESS_TOKEN))
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 ACCESS TOKEN")
            ),

            responseHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("발급된 JWT ACCESS TOKEN")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("회원 ACCESS 토큰이 유효하지 않은 경우 INVALID_ACCESS_TOKEN 예외를 반환한다.")
  void reissueInvalidAccessTokenFailureTest() throws Exception {
    // given
    given(memberService.reissue(any(String.class)))
        .willThrow(new CustomException(INVALID_ACCESS_TOKEN));

    // when
    String expectedJson = createExpectedErrorJson(INVALID_ACCESS_TOKEN);

    // then
    mockMvc.perform(
            post("/api/v1/members/reissue")
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)

                .with(userAuth(KAKAO_ID))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 Access Token")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
  }

  @Test
  @DisplayName("회원 ACCESS 토큰이 만료된 경우 EXPIRED_REFRESH_TOKEN 예외를 반환한다.")
  void reissueFailureTest() throws Exception {
    // given
    given(memberService.reissue(any(String.class)))
        .willThrow(new CustomException(EXPIRED_REFRESH_TOKEN));

    // when
    String expectedJson = createExpectedErrorJson(EXPIRED_REFRESH_TOKEN);

    // then
    mockMvc.perform(
            post("/api/v1/members/reissue")
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN)
                .with(userAuth(KAKAO_ID))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andDo(restDocs.document(
            requestHeaders(
                headerWithName(HttpHeaders.AUTHORIZATION)
                    .description("회원 Access Token")
            ),

            responseFields(
                fieldWithPath("code")
                    .type(JsonFieldType.NUMBER)
                    .description("응답 코드"),
                fieldWithPath("message")
                    .type(JsonFieldType.STRING)
                    .description("성공 여부"),
                fieldWithPath("data")
                    .type(JsonFieldType.BOOLEAN)
                    .description("응답 컨텐츠")
            )
        ));
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
