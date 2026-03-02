package com.dev.generator;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;

public class MyConfigurable implements Configurable {

    private final Project project;
    private JPanel panel;
    private Disposable uiDisposable;

    public MyConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public JComponent createComponent() {
        panel = new JPanel();
        uiDisposable = Disposer.newDisposable("MyConfigurableUI");

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        Disposable child = () -> System.out.println("Disposed child!");
        Disposer.register(uiDisposable, child);

        return panel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }

    @Override
    public void disposeUIResources() {
        if (uiDisposable != null) {
            Disposer.dispose(uiDisposable);
            uiDisposable = null;
        }
        panel = null;
    }

    @Override
    public String getDisplayName() {
        return "My Config";
    }
}