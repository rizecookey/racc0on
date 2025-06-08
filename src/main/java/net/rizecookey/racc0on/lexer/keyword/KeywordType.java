package net.rizecookey.racc0on.lexer.keyword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public sealed interface KeywordType permits BoolLiteralKeywordType, ControlKeywordType, ReservedKeywordType, TypeKeywordType {
  Set<KeywordType> VALUES = join(Set.of(
          BoolLiteralKeywordType.values(),
          ControlKeywordType.values(),
          ReservedKeywordType.values(),
          TypeKeywordType.values()
  ));

  Map<String, KeywordType> STRING_TO_KEYWORD = VALUES.stream()
          .collect(Collectors.toMap(KeywordType::keyword, Function.identity()));

  String keyword();

  String toString();

  private static <T> Set<T> join(Set<T[]> arrays) {
      return arrays.stream()
              .flatMap(Arrays::stream)
              .collect(Collectors.toCollection(HashSet::new));
  }
}
