package me.wordcount;

import java.io.EOFException;
import java.util.Random;

public class DelayedCharacterReader implements CharacterReader {

  private final String text;

  private final int delayMs;

  public DelayedCharacterReader(String text, int delayMs) {
    this.text = text;
    this.delayMs = delayMs;
  }

  private int pos = -1;

  @Override
  public char nextCharacter() throws EOFException, InterruptedException {
    int delay = new Random().nextInt(delayMs);
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
