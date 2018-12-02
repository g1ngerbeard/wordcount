package me.wordcount;

import java.io.EOFException;

public class SimpleCharacterReader implements CharacterReader {

  private final String text;

  private int pos = -1;

  public SimpleCharacterReader(String text) {
    this.text = text;
  }

  @Override
  public char nextCharacter() throws EOFException {

    pos += 1;

    if (pos >= text.length()) {
      throw new EOFException();
    }

    return text.charAt(pos);
  }

  @Override
  public void close() {

  }
}
