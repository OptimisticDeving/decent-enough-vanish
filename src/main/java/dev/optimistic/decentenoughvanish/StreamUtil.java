package dev.optimistic.decentenoughvanish;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public final class StreamUtil {
  private StreamUtil() {

  }

  public static <T, U> Stream<U> filterMap(
    Stream<T> stream,
    Function<T, @Nullable U> filterMapper
  ) {
    return stream.flatMap(input -> Stream.ofNullable(filterMapper.apply(input)));
  }
}
