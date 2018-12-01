package me.wordcount;

import java.io.EOFException;
import java.util.Random;

public class DelayedCharacterReader implements CharacterReader {

  private final String text;

  public DelayedCharacterReader(String text) {
    this.text = text;
  }

  private int pos = -1;

  @Override
  public char nextCharacter() throws EOFException, InterruptedException {
    int delay = new Random().nextInt(500);
    Thread.sleep(delay);

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
