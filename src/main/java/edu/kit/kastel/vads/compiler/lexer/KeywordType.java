package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.parser.type.BasicType;

import java.util.Map;

public enum KeywordType {
  STRUCT("struct"),
  IF("if"),
  ELSE("else"),
  WHILE("while"),
  FOR("for"),
  CONTINUE("continue"),
  BREAK("break"),
  RETURN("return"),
  ASSERT("assert"),
  TRUE("true"),
  FALSE("false"),
  NULL("NULL"),
  PRINT("print"),
  READ("read"),
  ALLOC("alloc"),
  ALLOC_ARRAY("alloc_array"),
  INT("int"),
  BOOL("bool"),
  VOID("void"),
  CHAR("char"),
  STRING("string"),
  ;

  public static final Map<KeywordType, BasicType> DATA_TYPES = Map.of(INT, BasicType.INT, BOOL, BasicType.BOOL);

  private final String keyword;

  KeywordType(String keyword) {
    this.keyword = keyword;
  }

  public String keyword() {
    return keyword;
  }

  @Override
  public String toString() {
    return keyword();
  }
}
