package dasi.typing.domain.phrase;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LangType {
  QUOTE("인용문"),
  POEM("시");

  private final String text;
}
