package dasi.typing.api.service.oauth.info;

import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = PUBLIC)
@NoArgsConstructor(access = PROTECTED)
public class KakaoUserInfo {

  private String sub;

  private String profileNickname;
}
