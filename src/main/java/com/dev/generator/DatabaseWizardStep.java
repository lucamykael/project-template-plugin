package com.dev.generator;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import javax.swing.*;
import java.awt.*;

public class DatabaseWizardStep extends ModuleWizardStep {

    private JComboBox<String> dbCombo;
    private String selectedDb;

    @Override
    public JComponent getComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dbCombo = new JComboBox<>(new String[]{"H2", "PostgreSQL", "MySQL", "Oracle"});

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Database:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(dbCombo, gbc);

        return panel;
    }

    @Override
    public void updateDataModel() {
        selectedDb = (String) dbCombo.getSelectedItem();
    }

    public String getSelectedDb() {
        return selectedDb;
    }
}
