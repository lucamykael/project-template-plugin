package com.dev.generator.service;

import com.dev.generator.git.GitTemplateCloner;
import com.dev.generator.template.TemplateProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ProjectGenerationService {

  private static final String TEMPLATE_BASE_PACKAGE = "com.dev";

  public void generate(String destinationPath,
                       Map<String, String> variables,
                       String newBasePackage) {

    try {

      Path destination = Path.of(destinationPath);
      Files.createDirectories(destination);

      try{
        new GitTemplateCloner().cloneTemplate(destinationPath);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Project generation failed", e);
      }

      new TemplateProcessor().process(
              destination,
              variables,
              TEMPLATE_BASE_PACKAGE,
              newBasePackage
      );

    } catch (Exception e) {
      throw new RuntimeException("Project generation failed", e);
    }
  }
}