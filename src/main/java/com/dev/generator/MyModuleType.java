package com.dev.generator;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class MyModuleType extends ModuleType<MyProjectModuleBuilder> {
    public MyModuleType() {
        super("MY_SPRING_TEMPLATE");
    }

    @Override
    public MyProjectModuleBuilder createModuleBuilder() {
        return new MyProjectModuleBuilder();
    }

    @Override
    public String getName() {
        return "Spring Boot Template";
    }

    @Override
    public String getDescription() {
        return "Template Spring Boot Maven com Lombok e JPA";
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return IconLoader.getIcon("/icons/spring-icon.svg", MyModuleType.class);
    }
}

