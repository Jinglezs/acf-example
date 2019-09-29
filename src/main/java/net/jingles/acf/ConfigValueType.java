package net.jingles.acf;

import java.util.function.Function;

/**
 * An enum of different values a config may contain. I only included those that
 * can be easily edited through a command in-game.
 */
public enum ConfigValueType {

  DELETE(input -> null), // If the player wants to delete the value
  BOOLEAN(Boolean::parseBoolean),
  STRING(input -> input),
  INTEGER(Integer::valueOf),
  DOUBLE(Double::valueOf),
  FLOAT(Float::valueOf);

  private Function<String, ?> conversionFunction;

  ConfigValueType(Function<String, ?> conversionFunction) {
    this.conversionFunction = conversionFunction;
  }

  public Object convert(String input) {
    return conversionFunction.apply(input);
  }

}
