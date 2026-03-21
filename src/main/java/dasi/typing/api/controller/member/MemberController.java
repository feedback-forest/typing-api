package dasi.typing.api.controller.member;

import dasi.typing.api.controller.member.request.MemberCreateRequest;
import dasi.typing.api.controller.member.request.MemberNicknameRequest;
import dasi.typing.api.controller.member.response.NicknameResponse;
import dasi.typing.api.service.member.MemberService;
import dasi.typing.api.service.member.NicknameService;
import dasi.typing.exception.ApiResponse;
import dasi.typing.filter.GuestPrincipal;
import dasi.typing.jwt.JwtToken;
import dasi.typing.utils.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('GUEST')")
public class MemberController {

  private final MemberService memberService;
  private final NicknameService nicknameService;

  @PostMapping("/api/v1/members")
  public ApiResponse<Boolean> signUp(
      @AuthenticationPrincipal GuestPrincipal guest,
      @RequestBody MemberCreateRequest request,
      HttpServletResponse response) {

    JwtToken jwtToken = memberService.signUp(guest.id(), request.toServiceRequest());
    CookieUtil.addTokenCookies(response, jwtToken);
    return ApiResponse.success(true);
  }

  @PostMapping("/api/v1/members/nickname/validate")
  public ApiResponse<Boolean> validateNickname(@RequestBody MemberNicknameRequest request) {
    memberService.validateNickname(request.toServiceRequest());
    return ApiResponse.success(true);
  }

  @GetMapping("/api/v1/members/nickname/random")
  public ApiResponse<NicknameResponse> generateRandomNickname() {
    String nickname = nicknameService.generate();
    return ApiResponse.success(new NicknameResponse(nickname));
  }

  @PostMapping("/api/v1/members/reissue")
  @PreAuthorize("hasRole('USER')")
  public ApiResponse<Boolean> reissue(HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String kakaoId = (String) authentication.getPrincipal();

    CookieUtil.clearTokenCookies(response);
    JwtToken jwtToken = memberService.reissue(kakaoId);
    CookieUtil.addTokenCookies(response, jwtToken);
    return ApiResponse.success(true);
  }
}
