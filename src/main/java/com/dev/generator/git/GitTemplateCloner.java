package com.dev.generator.git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class GitTemplateCloner {

  private static final String TEMPLATE_URL =
          "https://github.com/lucamykael/template-project";

  private static final String TEMPLATE_BRANCH = "master";

  public void cloneTemplate(String destinationPath) {

    try {
      Path destination = Path.of(destinationPath);

      Path tempDir = Files.createTempDirectory("template-clone");

      try (Git git = Git.cloneRepository()
              .setURI(TEMPLATE_URL)
              .setBranch(TEMPLATE_BRANCH)
              .setDirectory(tempDir.toFile())
              .call()) {
      }

      FileUtils.deleteDirectory(new File(tempDir.toFile(), ".git"));

      copyRecursively(tempDir, Path.of(destinationPath));
      removeGitkeepFiles(destination);

      try (Git ignored = Git.init()
              .setDirectory(destination.toFile())
              .call()) {
      }

      FileUtils.deleteDirectory(tempDir.toFile());

    } catch (Exception e) {
      throw new RuntimeException("Failed to clone template", e);
    }
  }

  public static void copyRecursively(Path source, Path target) throws IOException {
    Files.walkFileTree(source, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path targetDir = target.resolve(source.relativize(dir).toString());
        Files.createDirectories(targetDir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, target.resolve(source.relativize(file).toString()),
                StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private static void removeGitkeepFiles(Path root) throws IOException {
    Files.walk(root)
            .filter(path -> path.getFileName().toString().equals(".gitkeep"))
            .forEach(path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException e) {
                throw new RuntimeException("Failed to delete .gitkeep: " + path, e);
              }
            });
  }
}