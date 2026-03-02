package com.dev.generator.wizard;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NewProjectWizard implements DirectoryProjectGenerator<Void> {

  @NotNull
  @Override
  public String getName() {
    return "Spring Boot Maven Template";
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return null;
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull VirtualFile baseDir,
                              @NotNull Void settings,
                              @NotNull Module module) {

    try {
      String rootPath = baseDir.getPath();

      Files.writeString(
              Path.of(rootPath, "TEST_PLUGIN_WORKING.txt"),
              "Plugin está funcionando 🚀"
      );

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
    return ValidationResult.OK;
  }
}