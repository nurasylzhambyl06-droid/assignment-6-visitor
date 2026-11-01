package com.inspection;

import com.inspection.demo.FintechProjectFactory;
import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.ProjectElement;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;

/** Small helper methods that build project trees for the tests. */
public final class TestProjects {

    private TestProjects() {
    }

    /** A directory that contains the given elements. */
    public static Directory dirWith(ProjectElement... elements) {
        Directory directory = new Directory("root");
        for (ProjectElement element : elements) {
            directory.add(element);
        }
        return directory;
    }

    public static SourceCodeFile source(String name, int lines, int complexity, boolean credentials, boolean documented) {
        SourceCodeFile file = new SourceCodeFile(name, lines, complexity, credentials);
        file.setDocumented(documented);
        return file;
    }

    /** Simple clean source file: 50 lines, complexity 2, documented. */
    public static SourceCodeFile cleanSource(String name) {
        return source(name, 50, 2, false, true);
    }

    /** A project where every visitor finds nothing to complain about. */
    public static Directory secureProject() {
        SourceCodeFile orderService = source("OrderService.java", 100, 4, false, true);
        ConfigurationFile config = new ConfigurationFile("app.properties", false, false);
        config.setDocumented(true);
        DatabaseElement database = new DatabaseElement("orders-db", true, true, false);
        database.setDocumented(true);
        APIEndpointElement endpoint = new APIEndpointElement("/api/orders", "GET", true, true);
        endpoint.setDocumented(true);
        DeploymentConfigElement production = new DeploymentConfigElement("production", false, true, false);
        production.setDocumented(true);

        Directory nested = new Directory("nested");
        nested.add(new TestFile("OrderServiceTest.java", 5));

        Directory root = new Directory("secure-project");
        root.add(orderService);
        root.add(nested);
        root.add(config);
        root.add(new DependencyFile("junit", "5.10.2", false, false));
        root.add(database);
        root.add(endpoint);
        root.add(production);
        return root;
    }

    /** The demo project that Main inspects. */
    public static Directory demoProject() {
        return FintechProjectFactory.createProject();
    }
}
