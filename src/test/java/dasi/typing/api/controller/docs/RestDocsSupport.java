package dasi.typing.api.controller.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import dasi.typing.api.controller.member.MemberController;
import dasi.typing.api.controller.phrase.PhraseController;
import dasi.typing.api.controller.ranking.RankingController;
import dasi.typing.api.controller.typing.TypingController;
import dasi.typing.api.service.member.MemberService;
import dasi.typing.api.service.member.NicknameService;
import dasi.typing.api.service.phrase.LuckyMessageService;
import dasi.typing.api.service.phrase.PhraseService;
import dasi.typing.api.service.ranking.RankingService;
import dasi.typing.api.service.typing.TypingService;
import dasi.typing.config.RestDocsConfig;
import dasi.typing.config.TestSecurityConfig;
import dasi.typing.jwt.GuestPrincipal;
import dasi.typing.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@WebMvcTest(controllers = {
    MemberController.class,
    PhraseController.class,
    RankingController.class,
    TypingController.class
})
@ExtendWith(RestDocumentationExtension.class)
@Import({RestDocsConfig.class, TestSecurityConfig.class})
public abstract class RestDocsSupport {

  protected MockMvc mockMvc;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  protected RestDocumentationResultHandler restDocs;

  @MockitoBean
  protected MemberService memberService;

  @MockitoBean
  protected TypingService typingService;

  @MockitoBean
  protected PhraseService phraseService;

  @MockitoBean
  protected RankingService rankingService;

  @MockitoBean
  protected NicknameService nicknameService;

  @MockitoBean
  protected LuckyMessageService luckyMessageService;

  @MockitoBean
  protected JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp(
      final WebApplicationContext webApplicationContext,
      final RestDocumentationContextProvider provider) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(provider))
        .apply(springSecurity())
        .alwaysDo(print())
        .alwaysDo(restDocs)
        .build();
  }

  protected static Attributes.Attribute constraints(final String value) {
    return new Attributes.Attribute("constraints", value);
  }

  protected RequestPostProcessor guestAuth(String tempToken) {
    GuestPrincipal guest = new GuestPrincipal(tempToken);
    return authentication(new TestingAuthenticationToken(guest, null, "GUEST"));
  }

  protected RequestPostProcessor userAuth(String kakaoId) {
    return authentication(new TestingAuthenticationToken(kakaoId, null, "USER"));
  }

  protected abstract Object initController();
}
