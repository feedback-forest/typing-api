package dasi.typing;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
import dasi.typing.filter.GuestPrincipal;
import dasi.typing.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
public abstract class ControllerTestSupport {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

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

  protected RequestPostProcessor guestAuth(String tempToken) {
    GuestPrincipal guest = new GuestPrincipal(tempToken);
    return authentication(new TestingAuthenticationToken(guest, null, "GUEST"));
  }

  protected RequestPostProcessor userAuth(String kakaoId) {
    return authentication(new TestingAuthenticationToken(kakaoId, null, "USER"));
  }

  @BeforeEach
  protected void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .defaultRequest(post("/**").with(csrf()))
        .defaultRequest(patch("/**").with(csrf()))
        .defaultRequest(delete("/**").with(csrf()))
        .build();
  }
}
