package edu.kit.kastel.vads.compiler;

public sealed interface Position extends Comparable<Position> {
  int line();
  int column();

  record SimplePosition(int line, int column) implements Position {
    @Override
    public String toString() {
      return line() + ":" + column();
    }

    @Override
    public int compareTo(Position position) {
      int lineCompare = Integer.compare(line(), position.line());
      if (lineCompare == 0) {
        return Integer.compare(column(), position.column());
      }
      return lineCompare;
    }
  }
}
