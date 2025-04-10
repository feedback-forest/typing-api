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
    return KakaoUserInfo.builder()
        .sub(sub)
        .name(name)
        .nickname(nickname).build();
  }
}
