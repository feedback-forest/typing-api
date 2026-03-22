package dasi.typing.api.service.oauth.request;

import static lombok.AccessLevel.PROTECTED;

import com.fasterxml.jackson.annotation.JsonProperty;
import dasi.typing.api.service.oauth.info.KakaoUserInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class KakaoUserCreateServiceRequest {

  private String sub;

  private String name;

  private String nickname;

  private String email;

  @JsonProperty(value = "email_verified")
  private boolean emailVerified;

  public KakaoUserInfo toKakaoUserInfo() {
    return new KakaoUserInfo(sub, name, nickname);
  }

  public static KakaoUserCreateServiceRequest of(
      String sub,
      String name,
      String nickname,
      String email,
      boolean verified
  ) {
    KakaoUserCreateServiceRequest request = new KakaoUserCreateServiceRequest();
    request.sub = sub;
    request.name = name;
    request.nickname = nickname;
    request.email = email;
    request.emailVerified = verified;
    return request;
  }
}
