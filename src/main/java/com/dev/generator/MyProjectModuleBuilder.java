package com.dev.generator;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MyProjectModuleBuilder extends ModuleBuilder {

    private DatabaseWizardStep dbStep;

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel model) {
        VirtualFile root = createRootDir(model);

        String db = dbStep.getSelectedDb();

        if (root != null) {
            try {
                // Estrutura Maven
                VirtualFile src = root.createChildDirectory(this, "src");
                VirtualFile main = src.createChildDirectory(this, "main");
                VirtualFile mainJava = main.createChildDirectory(this, "java");
                VirtualFile pkg = mainJava.createChildDirectory(this, "com")
                        .createChildDirectory(this, "dev")
                        .createChildDirectory(this, "demo");

                VirtualFile mainResources = main.createChildDirectory(this, "resources");

                VirtualFile test = src.createChildDirectory(this, "test");
                VirtualFile testJava = test.createChildDirectory(this, "java")
                        .createChildDirectory(this, "com")
                        .createChildDirectory(this, "dev")
                        .createChildDirectory(this, "demo");

                // pom.xml
                String pom = generatePomXml(db);
                createFile(root, "pom.xml", pom);

                // README
                createFile(root, "README.md", "# Projeto Spring Boot com Lombok e JPA 🚀");

                // application.properties
                createFile(mainResources, "application.properties",
                        "spring.application.name=demo\nspring.datasource.url=jdbc:h2:mem:testdb\nspring.jpa.hibernate.ddl-auto=update");

                // Classe principal
                String appJava = """
                        package com.dev.demo;

                        import org.springframework.boot.SpringApplication;
                        import org.springframework.boot.autoconfigure.SpringBootApplication;

                        @SpringBootApplication
                        public class DemoApplication {
                            public static void main(String[] args) {
                                SpringApplication.run(DemoApplication.class, args);
                            }
                        }
                        """;
                createFile(pkg, "DemoApplication.java", appJava);

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

                String baseSpecification = "package com.study.crud_api.domain.specifications;\n" +
                        "\n" +
                        "import jakarta.persistence.criteria.CriteriaBuilder;\n" +
                        "import jakarta.persistence.criteria.CriteriaQuery;\n" +
                        "import jakarta.persistence.criteria.Predicate;\n" +
                        "import jakarta.persistence.criteria.Root;\n" +
                        "import org.springframework.data.jpa.domain.Specification;\n" +
                        "\n" +
                        "import java.util.ArrayList;\n" +
                        "import java.util.List;\n" +
                        "import java.util.Objects;\n" +
                        "\n" +
                        "public abstract class BaseSpecification<T, F> implements ISpecification<T, F> {\n" +
                        "    protected abstract Predicate generatePredicates(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> predicates, F filters);\n" +
                        "\n" +
                        "    public Specification<T> getSpecification(F filters){\n" +
                        "        return (root, query, cb) ->{\n" +
                        "            List<Predicate> predicates = new ArrayList<>();\n" +
                        "            return generatePredicates(root, query, cb, predicates, filters);\n" +
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

                createFile(specifications, "BaseSpecification.java",  baseSpecification);

                // Teste
                String testJavaCode = """
                        package com.dev.demo;

                        import org.junit.jupiter.api.Test;
                        import org.springframework.boot.test.context.SpringBootTest;

                        @SpringBootTest
                        class DemoApplicationTests {
                            @Test
                            void contextLoads() {
                            }
                        }
                        """;
                createFile(testJava, "DemoApplicationTests.java", testJavaCode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFile(VirtualFile dir, String name, String content) throws IOException {
        VirtualFile file = dir.createChildData(this, name);
        file.setBinaryContent(content.getBytes());
    }

    private String generatePomXml(String db) {
        String dbDependency = switch (db) {
            case "PostgreSQL" -> "\t<groupId>org.postgresql</groupId>\n\t\t\t<artifactId>postgresql</artifactId>\n\t\t\t<scope>runtime</scope>";
            case "MySQL" -> "\t<groupId>mysql</groupId>\n\t\t\t<artifactId>mysql-connector-j</artifactId>\n\t\t\t<scope>runtime</scope>";
            case "Oracle" -> "\t<groupId>com.oracle.database.jdbc</groupId>\n\t\t\t<artifactId>ojdbc11</artifactId>\n\t\t\t<scope>runtime</scope>";
            default -> "\t<groupId>com.h2database</groupId>\n\t\t\t<artifactId>h2</artifactId>\n\t\t\t<scope>runtime</scope>";
        };

        return """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                         http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.dev</groupId>
                <artifactId>my-spring-project</artifactId>
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
            """.formatted(dbDependency);
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
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                          @NotNull ModulesProvider modulesProvider) {
        dbStep = new DatabaseWizardStep();
        return new ModuleWizardStep[]{ dbStep };
    }
}
