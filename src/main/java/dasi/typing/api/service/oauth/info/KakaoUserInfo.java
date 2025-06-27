package dasi.typing.api.service.oauth.info;

import static lombok.AccessLevel.PROTECTED;

import dasi.typing.domain.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class KakaoUserInfo {

  private String sub;

  private String name;

  private String nickname;

  public KakaoUserInfo(String sub, String name, String nickname) {
    this.sub = sub;
    this.name = name;
    this.nickname = nickname;
  }

  public Member toEntity() {
    return new Member(sub, nickname);
  }
}
