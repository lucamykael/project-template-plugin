package com.dev.generator;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class MyProjectModuleBuilder extends ModuleBuilder {

    private String selectedDb;
    private String titleInserted;
    private JComboBox<String> dbCombo;
    private JTextField titleField;

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel model) {
        VirtualFile root = createRootDir(model);

        String db = selectedDb;
        String title = titleInserted;

        String safeTitle = title.replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9]", "");
        String packageName = safeTitle.toLowerCase();
        String className = safeTitle.substring(0, 1).toUpperCase() + safeTitle.substring(1) + "Application";

        if (root != null) {
            try {
                // Estrutura Maven
                VirtualFile src = root.createChildDirectory(this, "src");
                VirtualFile main = src.createChildDirectory(this, "main");
                VirtualFile mainJava = main.createChildDirectory(this, "java");
                VirtualFile pkg = mainJava.createChildDirectory(this, "com")
                        .createChildDirectory(this, "dev")
                        .createChildDirectory(this, packageName);

                VirtualFile mainResources = main.createChildDirectory(this, "resources");

                VirtualFile test = src.createChildDirectory(this, "test");
                VirtualFile testJava = test.createChildDirectory(this, "java")
                        .createChildDirectory(this, "com")
                        .createChildDirectory(this, "dev")
                        .createChildDirectory(this, packageName);

                // pom.xml
                String pom = generatePomXml(db, packageName);
                createFile(root, "pom.xml", pom);

                // README
                createFile(root, "README.md", "# Projeto Spring Boot com Lombok e JPA 🚀");

                // application.properties
                createFile(mainResources, "application.properties",
                        "spring.application.name=" + title + "\nspring.datasource.url=jdbc:h2:mem:testdb\nspring.jpa.hibernate.ddl-auto=update");

                String appJava = """
                        package com.dev.%s;
                        
                        import org.springframework.boot.SpringApplication;
                        import org.springframework.boot.autoconfigure.SpringBootApplication;
                        
                        @SpringBootApplication
                        public class %s {
                            public static void main(String[] args) {
                                SpringApplication.run(%s.class, args);
                            }
                        }
                        """.formatted(packageName, className, className);

                createFile(pkg, className + ".java", appJava);

                VirtualFile domain = pkg.createChildDirectory(this, "domain");
                VirtualFile entities = domain.createChildDirectory(this, "entities");
                VirtualFile dtos = domain.createChildDirectory(this, "dtos");
                VirtualFile servicesDomain = domain.createChildDirectory(this, "services");
                VirtualFile specifications = domain.createChildDirectory(this, "specifications");
                VirtualFile mappers = domain.createChildDirectory(this, "mappers");

                VirtualFile infrastructure = pkg.createChildDirectory(this, "infrastructure");
                VirtualFile repositories = infrastructure.createChildDirectory(this, "repositories");

                VirtualFile controllers = pkg.createChildDirectory(this, "controllers");

                VirtualFile services = pkg.createChildDirectory(this, "services");
                VirtualFile specificationService = services.createChildDirectory(this, "specifications");

                String baseSpecification = "package com.dev."+packageName+".domain.specifications;\n" +
                        "\n" +
                        "import jakarta.persistence.criteria.*;\n" +
                        "import org.springframework.data.domain.Pageable;\n" +
                        "import org.springframework.data.jpa.domain.Specification;\n" +
                        "\n" +
                        "import java.util.ArrayList;\n" +
                        "import java.util.List;\n" +
                        "import java.util.Objects;\n" +
                        "\n" +
                        "public abstract class BaseSpecification<T, F> implements ISpecification<T, F> {\n" +
                        "    protected abstract Predicate generatePredicates(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Join<?,?> joins, List<Predicate> predicates, F filters, String select);\n" +
                        "    protected abstract List<Order> sortByPageable(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Join<?,?> joins, Pageable pageable);\n" +
                        "    protected abstract Predicate applySelectGroupValidations(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Join<?,?> joins, Predicate predicate, String select);\n" +
                        "\n" +
                        "    public Specification<T> getSpecification(F filters, Pageable pageable, String select){\n" +
                        "        return (root, query, cb) ->{\n" +
                        "            List<Predicate> predicates = new ArrayList<>();\n" +
                        "            List<Join<?,?> joins = getJoins(root);\n" +
                        "            Objects.requireNonNull(query).orderBy(sortByPageable(root,query,cb,joins,pageable));\n" +
                        "            Predicate predicate = generatePredicate(root,query,cb,joins,predicates,filter,select);\n" +
                        "            return applySelectGroupValidations(root,query,cb,joins,predicate,select);\n" +
                        "        };\n" +
                        "    }\n" +
                        "\n" +
                        "    @FunctionalInterface\n" +
                        "    protected interface PredicateFunction<T> {\n" +
                        "        Predicate apply(T value);\n" +
                        "    }\n" +
                        "\n" +
                        "    protected <T> void addOrPredicate(List<Predicate> predicates, List<T> values, CriteriaBuilder cb, PredicateFunction<T> predicateFunction) {\n" +
                        "        if (values != null && !values.isEmpty()) {\n" +
                        "            List<Predicate> predicatesList = values.stream()\n" +
                        "                    .map(predicateFunction::apply)\n" +
                        "                    .filter(Objects::nonNull)\n" +
                        "                    .toList();\n" +
                        "            if (!predicates.isEmpty()) {\n" +
                        "                predicatesList.add(cb.or(predicates.toArray(new Predicate[0])));\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public static String formatToLike(String value){\n" +
                        "        return String.format(\"%%%s%%\", value.toLowerCase());\n" +
                        "    }\n" +
                        "}";

                createFile(specifications, "BaseSpecification.java", baseSpecification);

                // Teste
                String testJavaCode = """
                        package com.dev.%s;
                        
                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.context.SpringBootTest;
                        
                        @SpringBootTest
                        class %sTests {
                            @Test
                            void contextLoads() {
                            }
                        }
                        """.formatted(packageName,className);
                createFile(testJava, className+ "Tests.java", testJavaCode);

                String iSpecification = """
                        package com.dev.%s;
                        
                        import org.springframework.data.domain.Pageable;
                        import org.springframework.data.jpa.domain.Specification;
                        
                        public interface ISpecification<T,F> {
                            Specification<T> getSpecification(F filters, Pageable pageable, String select);
                        }
                        """.formatted(packageName);
                createFile(specifications, "ISpecification.java", iSpecification);

                String genericSpecification = """
                        package com.dev.%s.specifications;
                        
                        import jakarta.persistence.criteria.*;
                        import lombok.RequiredArgsConstructor;
                        import org.springframework.data.domain.Pageable;
                        import org.springframework.stereotype.Component;
                        
                        import java.util.List;
                        import java.util.Objects;
                        import java.util.ArrayList;
                        
                        @Component
                        @RequiredArgsConstructor
                        public class GenericSpecification extends BaseSpecification<GenericEntity, GenericFilter> {
                        
                            @Override
                            protected Predicate generatePredicate(Root<GenericEntity> root,
                                                                  CriteriaQuery<?> query,
                                                                  CriteriaBuilder cb,
                                                                  List<Join<?,?>> joins, 
                                                                  List<Predicate> predicates,
                                                                  GenericFilter filter,
                                                                  String select) {
                                List<Predicate> filterApplied = getPredicateByFilter(root, query, cb, joins, filter, select);
                                return cb.and(filterApplied.toArray(new Predicate[0]));
                            }
                            
                            private List<Predicate> getPredicatesByFilter(Root<GenericEntity> root, 
                                                                          CriteriaQuery<?> query,
                                                                          CriteriaBuilder cb,
                                                                          List<Join<?,?>> joins,
                                                                          GenericFilter filter,
                                                                          String select) {
                                for (Field field : filter.getFields()) {
                                    switch(field.getName()) {
                                        case "id" -> {
                                            if(field.getValues().size() > 1) {
                                                List<Predicate> compareValues = field.getValues().stream()
                                                    .map(value -> cb.like(cb.lower(root.get(field.getName()), formatToLike(value)))
                                                    .toList();
                                            } else if(field.getValues().size() == 1) {
                                                Predicate oneValue = cb.and(cb.like(cb.lower(root.get(field.getName())), formatToLike(field.getValues().getFirst())));            
                                                predicates.add(oneValue);
                                            }
                                        }
                                        default -> {
                                            if(field.getValues().size() > 1) {
                                                List<Predicate> compareValues = field.getValues().stream()
                                                    .map(value -> cb.like(cb.lower(root.get(field.getName()), formatToLike(value)))
                                                    .toList();
                                            } else if(field.getValues().size() == 1) {
                                                Predicate oneValue = cb.and(cb.like(cb.lower(root.get(field.getName())), formatToLike(field.getValues().getFirst())));            
                                                predicates.add(oneValue);
                                            }
                                        }
                                    }
                                }
                                return predicates;
                            }
                            
                            @Override
                            protected List<Order> sortByPageable(Root<?> root,
                                                                 CriteriaQuery<?> query,
                                                                 CriteriaBuilder cb,
                                                                 List<Join<?,?>> joins,
                                                                 Pageable pageable) {
                                List<Order> orders = new ArrayList<>();
                                
                                for(var sortBy : pageable.getSort()) {
                                    var direction = sortBy.isAscending() ? "asc" : "desc";
                                    switch(sortBy.getProperty()) {
                                        case "id" -> {
                                            orders.add(direction.equals("asc") ? cb.asc(root.get(sortBy.getProperty()) : cb.desc(root.get(sortBy.getProperty())));
                                        }
                                        default -> {
                                            orders.add(direction.equals("asc") ? cb.asc(root.get(sortBy.getProperty()) : cb.desc(root.get(sortBy.getProperty())));
                                        }
                                    }
                                }
                                return orders;
                            }
                            
                            @Override
                            protected Predicate applySelectGroupValidations(Root<?> root,
                                                                            CriteriaQuery<?> query,
                                                                            CriteriaBuilder cb,
                                                                            List<Join<?,?>> joins,
                                                                            Predicate predicate,
                                                                            String select) {
                                return switch(select) {
                                    case "id" -> {
                                        List<Predicate> predicates = new ArrayList<>();
                                        predicates.add(predicate);
                                        query.multiselect(
                                            root.get("name"),
                                            cb.countDistinct(root.get("id"))
                                        );
                                        
                                        query.groupBy(
                                            root.get("name")
                                        );
                                        
                                        predicates.add(cb.isNotNull(root.get("name")));
                                        
                                        yield cb.and(predicates.toArray(new Predicate[0]));
                                    }
                                    default -> predicate;
                                };
                            }
                        """.formatted(packageName);
                createFile(specificationService, "GenericSpecification.java", genericSpecification);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFile(VirtualFile dir, String name, String content) throws IOException {
        VirtualFile file = dir.createChildData(this, name);
        file.setBinaryContent(content.getBytes());
    }

    private String generatePomXml(String db, String packageName) {
        String dbDependency = switch (db) {
            case "PostgreSQL" ->
                    "\t<groupId>org.postgresql</groupId>\n\t\t\t<artifactId>postgresql</artifactId>\n\t\t\t<scope>runtime</scope>";
            case "MySQL" ->
                    "\t<groupId>mysql</groupId>\n\t\t\t<artifactId>mysql-connector-j</artifactId>\n\t\t\t<scope>runtime</scope>";
            case "Oracle" ->
                    "\t<groupId>com.oracle.database.jdbc</groupId>\n\t\t\t<artifactId>ojdbc11</artifactId>\n\t\t\t<scope>runtime</scope>";
            default ->
                    "\t<groupId>com.h2database</groupId>\n\t\t\t<artifactId>h2</artifactId>\n\t\t\t<scope>runtime</scope>";
        };

        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.dev</groupId>
                    <artifactId>%s</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.3.5</version>
                        <relativePath/> <!-- busca do spring-boot-starter-parent -->
                    </parent>
                
                    <properties>
                        <java.version>21</java.version>
                    </properties>
                
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-jpa</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.34</version>
                            <scope>provided</scope>
                        </dependency>
                        <dependency>
                        %s
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                         	<groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct</artifactId>
                            <version>1.6.3</version>
                        </dependency>
                        <dependency>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>1.6.3</version>
                        </dependency>
                        <dependency>
                            <groupId>org.springdoc</groupId>
                            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                            <version>2.8.5</version>
                        </dependency>
                    </dependencies>
                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(packageName,dbDependency);
    }

    private VirtualFile createRootDir(ModifiableRootModel model) {
        VirtualFile baseDir = model.getProject().getBaseDir();
        if (baseDir == null) {
            return null;
        }
        model.addContentEntry(baseDir);
        return baseDir;
    }

    @Override
    public @NotNull ModuleType<?> getModuleType() {
        return new MyModuleType();
    }

    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        dbCombo = new JComboBox<>(new String[]{"H2", "PostgreSQL", "MySQL", "Oracle"});
        titleField = new JTextField();

        // Aqui você integra com a tela padrão do IntelliJ
        settingsStep.addSettingsField("Project Title:", titleField);
        settingsStep.addSettingsField("Database:", dbCombo);
        return new ModuleWizardStep() {
            @Override
            public JComponent getComponent() {
                return new JPanel();
            }

            @Override
            public void updateDataModel() {
                selectedDb = (String) dbCombo.getSelectedItem();
                titleInserted = titleField.getText();
            }
        };
    }
}
