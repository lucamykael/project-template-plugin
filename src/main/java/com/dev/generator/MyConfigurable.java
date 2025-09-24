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

    // Recebe o projeto automaticamente se for ProjectConfigurable
    public MyConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public JComponent createComponent() {
        panel = new JPanel();
        uiDisposable = Disposer.newDisposable("MyConfigurableUI");

        // Exemplo: pegar editor existente do projeto (não liberar!)
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        // Se precisar usar recursos descartáveis, registre aqui
        // Mas nunca chame releaseEditor no editor que veio do IDE
        // Exemplo de disposable custom:
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
        // aplicar alterações se houver
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