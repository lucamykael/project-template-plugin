package com.dev.generator.module;

import com.dev.generator.service.ProjectGenerationService;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ProjectModuleBuilder extends ModuleBuilder {

  private WizardContext wizardContext;

  private String selectedDb;
  private String dbUrl;
  private String dbUsername;
  private String dbPassword;
  private String titleInserted;
  private boolean includeSamplesFlag;

  @Override
  public void setupRootModel(@NotNull ModifiableRootModel model) {

    String destinationPath = getContentEntryPath();
    if (destinationPath == null) return;

    String projectName = model.getProject().getName();
    String basePackage = "com.dev." + projectName.toLowerCase();

    Map<String, String> variables = Map.of(
            "GROUP_ID", basePackage,
            "ARTIFACT_ID", projectName.toLowerCase(),
            "PROJECT_NAME", projectName,
            "BASE_PACKAGE", basePackage,
            "DATABASE", selectedDb,
            "DATABASE_URL", dbUrl,
            "DATABASE_USERNAME", dbUsername,
            "DATABASE_PASSWORD", dbPassword
    );

    ProgressManager.getInstance()
            .runProcessWithProgressSynchronously(() -> {
              new ProjectGenerationService()
                      .generate(destinationPath, variables, basePackage);
            }, "Generating project...", true, model.getProject());

    String basePath = model.getProject().getBasePath();
    if (basePath != null) {
      VirtualFile baseDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath);
      if (baseDir != null) {
        baseDir.refresh(false, true);
        model.addContentEntry(baseDir);
      }
    }

    MavenProjectsManager mavenManager = MavenProjectsManager.getInstance(model.getProject());
    File pomFile = Path.of(destinationPath, "pom.xml").toFile();

    VirtualFile pomVFile = LocalFileSystem.getInstance()
            .refreshAndFindFileByIoFile(pomFile);

    if (pomVFile != null) {
      mavenManager.addManagedFilesOrUnignore(List.of(pomVFile));
    }
  }

  @Override
  public ModuleType<?> getModuleType() {
    return ModuleTypeManager.getInstance().findByID("MY_SPRING_TEMPLATE");
  }

  @Override
  public String getBuilderId() {
    return "my-spring-template";
  }

  @Override
  public boolean isTemplateBased() {
    return true;
  }

  @Override
  public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {

    JLabel greeting = new JLabel("Percebi que tens bom gosto para plugins");
    greeting.setFont(greeting.getFont().deriveFont(Font.BOLD, 26f));
    greeting.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    settingsStep.addSettingsComponent(greeting);

    JLabel dbTitle = new JLabel("Database Settings:");
    dbTitle.setFont(dbTitle.getFont().deriveFont(Font.BOLD, 14f));
    dbTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

    settingsStep.addSettingsComponent(dbTitle);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = JBUI.insets(4, 0);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;

    ComboBox<String> dbCombo = new ComboBox<>(new String[]{"H2", "PostgreSQL", "MySQL", "Oracle"});
    JTextField urlField = new JTextField();
    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();

    gbc.gridx = 0; gbc.gridy = 0;
    panel.add(new JLabel("Database:"), gbc);
    gbc.gridx = 1;
    panel.add(dbCombo, gbc);

    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(new JLabel("URL:"), gbc);
    gbc.gridx = 1;
    panel.add(urlField, gbc);

    gbc.gridx = 0; gbc.gridy = 2;
    panel.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    panel.add(usernameField, gbc);

    gbc.gridx = 0; gbc.gridy = 3;
    panel.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    panel.add(passwordField, gbc);

    settingsStep.addSettingsComponent(panel);

    JCheckBox includeSamplesCheck = new JCheckBox("Include Code Samples");

    gbc.gridx = 0; gbc.gridy = 4;
    gbc.gridwidth = 2;
    panel.add(includeSamplesCheck, gbc);

    return new ModuleWizardStep() {
      @Override
      public JComponent getComponent() {
        return panel;
      }

      @Override
      public void updateDataModel() {
        selectedDb = (String) dbCombo.getSelectedItem();
        dbUrl = urlField.getText();
        dbUsername = usernameField.getText();
        dbPassword = new String(passwordField.getPassword());

        includeSamplesFlag = includeSamplesCheck.isSelected();
      }
    };
  }
}