package dasi.typing.domain.member;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {

  GUEST("비회원"), USER("회원");

  private final String text;

}
