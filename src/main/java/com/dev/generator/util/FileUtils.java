package com.dev.generator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileUtils {

  public static void deleteRecursively(Path path) throws IOException {
    if (!Files.exists(path)) return;

    Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach(p -> {
              try {
                Files.delete(p);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }
}