package com.dev.generator.template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class TemplateProcessor {

  private static final Set<String> BINARY_EXTENSIONS = Set.of(
          ".png", ".jpg", ".jpeg", ".gif", ".ico",
          ".jar", ".class", ".zip", ".gz"
  );

  public void process(Path projectRoot,
                      Map<String, String> variables,
                      String oldBasePackage,
                      String newBasePackage) {

    try {
      replaceVariables(projectRoot, variables);
      renamePackageStructure(projectRoot, oldBasePackage, newBasePackage);
    } catch (IOException e) {
      throw new RuntimeException("Error processing template", e);
    }
  }

  private void replaceVariables(Path root,
                                Map<String, String> variables) throws IOException {

    Files.walk(root)
            .filter(Files::isRegularFile)
            .filter(this::shouldProcess)
            .forEach(path -> replaceInFile(path, variables));
  }

  private boolean shouldProcess(Path path) {

    String fileName = path.getFileName().toString().toLowerCase();

    if (path.toString().contains(".git")) return false;

    return BINARY_EXTENSIONS.stream()
            .noneMatch(fileName::endsWith);
  }

  private void replaceInFile(Path file,
                             Map<String, String> variables) {

    try {
      String content = Files.readString(file, StandardCharsets.UTF_8);

      for (Map.Entry<String, String> entry : variables.entrySet()) {
        content = content.replace(
                "${" + entry.getKey() + "}",
                entry.getValue()
        );
      }

      Files.writeString(file, content, StandardCharsets.UTF_8);

    } catch (IOException e) {
      throw new RuntimeException("Failed processing file: " + file, e);
    }
  }

  private void renamePackageStructure(Path root,
                                      String oldPackage,
                                      String newPackage) throws IOException {

    if (oldPackage.equals(newPackage)) return;

    Path oldPath = root.resolve("src/main/java")
            .resolve(oldPackage.replace(".", "/"));

    Path newPath = root.resolve("src/main/java")
            .resolve(newPackage.replace(".", "/"));

    if (!Files.exists(oldPath)) return;

    Files.createDirectories(newPath.getParent());

    Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

    cleanupEmptyDirectories(root.resolve("src/main/java"));
  }

  private void cleanupEmptyDirectories(Path root) throws IOException {

    Files.walk(root)
            .sorted(Comparator.reverseOrder())
            .filter(Files::isDirectory)
            .forEach(path -> {
              try {
                if (Files.list(path).findAny().isEmpty()) {
                  Files.delete(path);
                }
              } catch (IOException ignored) {
              }
            });
  }
}