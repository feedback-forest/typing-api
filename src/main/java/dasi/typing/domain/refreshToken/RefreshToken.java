package dasi.typing.domain.refreshToken;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "kakaoId", timeToLive = 60 * 60 * 24 * 7)
public class RefreshToken {

  @Id
  @Indexed
  private String kakaoId;

  private String token;

  @Builder
  private RefreshToken(String kakaoId, String token) {
    this.kakaoId = kakaoId;
    this.token = token;
  }

  public RefreshToken updateValue(String refreshToken) {
    this.token = refreshToken;
    return this;
  }
}
