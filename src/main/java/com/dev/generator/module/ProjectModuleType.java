package com.dev.generator.module;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectModuleType extends ModuleType<ProjectModuleBuilder> {

  public static final String ID = "MY_SPRING_TEMPLATE";

  public ProjectModuleType() {
    super(ID);
  }

  public static ProjectModuleType getInstance() {
    return (ProjectModuleType) ModuleTypeManager.getInstance().findByID(ID);
  }

  @Override
  public @NotNull ProjectModuleBuilder createModuleBuilder() {
    return new ProjectModuleBuilder();
  }

  @Override
  public @NotNull String getName() {
    return "Spring Boot Maven Template";
  }

  @Override
  public @NotNull String getDescription() {
    return "Template Spring Boot Maven com Lombok e JPA";
  }

  @Override
  public Icon getNodeIcon(boolean isOpened) {
    return IconLoader.getIcon("/icons/spring-icon.svg", ProjectModuleType.class);
  }
}