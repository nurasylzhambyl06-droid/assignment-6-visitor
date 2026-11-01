package com.inspection.demo;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;

/**
 * Builds the simulated "smart-fintech-platform" project as a tree of Java objects.
 * All data is invented for this demo. Nothing is read from disk or the network.
 *
 * smart-fintech-platform
 *   src/main            PaymentService.java, UserService.java
 *   tests/unit          PaymentServiceTest.java
 *   config              application.properties
 *   config/environments application-prod.properties
 *   dependencies        five dependencies (the "pom.xml" content)
 *   infrastructure      two databases, three deployment configurations
 *   api                 three endpoints
 *   documentation       openapi-spec.yaml
 *
 * Intentional problems: hardcoded and exposed credentials, a vulnerable and a prohibited
 * dependency, a dependency without version, a missing UserServiceTest, an unencrypted
 * database without backup, an unauthenticated endpoint, debug mode in production,
 * and missing documentation.
 */
public final class FintechProjectFactory {

    private FintechProjectFactory() {
    }

    public static Directory createProject() {
        Directory root = new Directory("smart-fintech-platform");
        root.add(createSourceDirectory());
        root.add(createTestsDirectory());
        root.add(createConfigDirectory());
        root.add(createDependenciesDirectory());
        root.add(createInfrastructureDirectory());
        root.add(createApiDirectory());
        root.add(createDocumentationDirectory());
        return root;
    }

    private static Directory createSourceDirectory() {
        SourceCodeFile paymentService = new SourceCodeFile("PaymentService.java", 320, 14, true);
        paymentService.setDocumented(true);

        // UserService is clean but has no documentation.
        SourceCodeFile userService = new SourceCodeFile("UserService.java", 180, 6, false);

        Directory main = new Directory("main");
        main.add(paymentService);
        main.add(userService);

        Directory src = new Directory("src");
        src.add(main);
        return src;
    }

    private static Directory createTestsDirectory() {
        Directory unit = new Directory("unit");
        unit.add(new TestFile("PaymentServiceTest.java", 8));

        Directory tests = new Directory("tests");
        tests.add(unit);
        return tests;
    }

    private static Directory createConfigDirectory() {
        // Insecure: password written in the file and debug options left on.
        ConfigurationFile applicationProperties = new ConfigurationFile("application.properties", true, true);
        applicationProperties.setDocumented(true);

        // Secure: secrets come from the environment. Not documented.
        ConfigurationFile prodProperties = new ConfigurationFile("application-prod.properties", false, false);

        Directory environments = new Directory("environments");
        environments.add(prodProperties);

        Directory config = new Directory("config");
        config.add(applicationProperties);
        config.add(environments);
        return config;
    }

    private static Directory createDependenciesDirectory() {
        Directory dependencies = new Directory("dependencies");
        dependencies.add(new DependencyFile("spring-boot-starter-web", "3.2.1", false, false));
        dependencies.add(new DependencyFile("commons-lang3", "3.14.0", false, false));
        dependencies.add(new DependencyFile("log4j-core", "2.14.0", true, false));
        dependencies.add(new DependencyFile("old-crypto-lib", "0.9", false, true));
        dependencies.add(new DependencyFile("internal-utils", "", false, false));
        return dependencies;
    }

    private static Directory createInfrastructureDirectory() {
        // Safe database.
        DatabaseElement paymentsDb = new DatabaseElement("payments-db", true, true, false);
        paymentsDb.setDocumented(true);

        // Unsafe database: no encryption and no backups.
        DatabaseElement reportingDb = new DatabaseElement("legacy-reporting-db", false, false, false);

        DeploymentConfigElement development = new DeploymentConfigElement(
                DeploymentConfigElement.DEVELOPMENT, true, false, true);
        DeploymentConfigElement testing = new DeploymentConfigElement(
                DeploymentConfigElement.TESTING, false, true, true);
        // Production uses HTTPS but debug mode was left on.
        DeploymentConfigElement production = new DeploymentConfigElement(
                DeploymentConfigElement.PRODUCTION, true, true, false);
        production.setDocumented(true);

        Directory infrastructure = new Directory("infrastructure");
        infrastructure.add(paymentsDb);
        infrastructure.add(reportingDb);
        infrastructure.add(development);
        infrastructure.add(testing);
        infrastructure.add(production);
        return infrastructure;
    }

    private static Directory createApiDirectory() {
        // Secure endpoint.
        APIEndpointElement payments = new APIEndpointElement("/api/payments", "POST", true, true);
        payments.setDocumented(true);

        // Authenticated but without rate limiting.
        APIEndpointElement users = new APIEndpointElement("/api/users", "GET", true, false);
        users.setDocumented(true);

        // Dangerous: no authentication and no rate limiting.
        APIEndpointElement clearCache = new APIEndpointElement("/api/admin/cache", "DELETE", false, false);

        Directory api = new Directory("api");
        api.add(payments);
        api.add(users);
        api.add(clearCache);
        return api;
    }

    private static Directory createDocumentationDirectory() {
        // Documentation is represented with an existing element: a documented specification file.
        ConfigurationFile openApiSpec = new ConfigurationFile("openapi-spec.yaml", false, false);
        openApiSpec.setDocumented(true);

        Directory documentation = new Directory("documentation");
        documentation.add(openApiSpec);
        return documentation;
    }
}
