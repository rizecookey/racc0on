package net.rizecookey.racc0on.lexer.keyword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public sealed interface KeywordType permits AllocKeywordType, BasicTypeKeywordType, BoolLiteralKeywordType, BuiltinFunctionsKeywordType, ComposedTypeKeywordType, ControlKeywordType, PointerLiteralKeywordType, ReservedKeywordType {
  Set<KeywordType> VALUES = join(Set.of(
          AllocKeywordType.values(),
          BoolLiteralKeywordType.values(),
          BuiltinFunctionsKeywordType.values(),
          ComposedTypeKeywordType.values(),
          ControlKeywordType.values(),
          PointerLiteralKeywordType.values(),
          ReservedKeywordType.values(),
          BasicTypeKeywordType.values()
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
