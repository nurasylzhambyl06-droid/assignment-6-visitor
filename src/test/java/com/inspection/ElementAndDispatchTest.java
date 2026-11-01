package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.ProjectElement;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the eight element types, double dispatch and the nested Directory. */
class ElementAndDispatchTest {

    @Test
    void sourceCodeFileStoresItsData() {
        SourceCodeFile file = new SourceCodeFile("PaymentService.java", 320, 14, true);
        assertEquals("PaymentService.java", file.getFileName());
        assertEquals(320, file.getLinesOfCode());
        assertEquals(14, file.getComplexity());
        assertTrue(file.hasHardcodedCredentials());
        assertFalse(file.isDocumented(), "documentation flag must default to false");
    }

    @Test
    void testFileStoresItsData() {
        TestFile file = new TestFile("PaymentServiceTest.java", 8);
        assertEquals("PaymentServiceTest.java", file.getFileName());
        assertEquals(8, file.getNumberOfTests());
    }

    @Test
    void configurationFileStoresItsData() {
        ConfigurationFile file = new ConfigurationFile("application.properties", true, false);
        assertEquals("application.properties", file.getFileName());
        assertTrue(file.hasExposedCredentials());
        assertFalse(file.isInsecureConfiguration());
    }

    @Test
    void dependencyFileStoresItsData() {
        DependencyFile dependency = new DependencyFile("log4j-core", "2.14.0", true, false);
        assertEquals("log4j-core", dependency.getDependencyName());
        assertEquals("2.14.0", dependency.getVersion());
        assertTrue(dependency.isVulnerable());
        assertFalse(dependency.isProhibited());
    }

    @Test
    void databaseElementStoresItsData() {
        DatabaseElement database = new DatabaseElement("payments-db", true, false, true);
        assertEquals("payments-db", database.getDatabaseName());
        assertTrue(database.isEncryptedConnection());
        assertFalse(database.isBackupEnabled());
        assertTrue(database.hasExposedCredentials());
    }

    @Test
    void apiEndpointElementStoresItsData() {
        APIEndpointElement endpoint = new APIEndpointElement("/api/payments", "POST", true, false);
        assertEquals("/api/payments", endpoint.getEndpointPath());
        assertEquals("POST", endpoint.getHttpMethod());
        assertTrue(endpoint.isAuthenticationRequired());
        assertFalse(endpoint.isRateLimitingEnabled());
        assertEquals("POST /api/payments", endpoint.getLabel());
    }

    @Test
    void deploymentConfigElementStoresItsData() {
        DeploymentConfigElement production = new DeploymentConfigElement("production", true, true, false);
        assertEquals("production", production.getEnvironment());
        assertTrue(production.isProduction());
        assertTrue(production.isDebugMode());
        assertTrue(production.isHttpsEnabled());
        assertFalse(production.isAutoDeploymentEnabled());
        assertFalse(new DeploymentConfigElement("development", false, false, false).isProduction());
    }

    @Test
    void doubleDispatchCallsTheMatchingVisitMethodForAllEightElements() {
        // The variable type is the interface ProjectElement, so only accept() can find the right overload.
        ProjectElement[] elements = {
                new SourceCodeFile("A.java", 1, 1, false),
                new TestFile("ATest.java", 1),
                new ConfigurationFile("a.properties", false, false),
                new DependencyFile("lib", "1.0", false, false),
                new Directory("dir"),
                new DatabaseElement("db", true, true, false),
                new APIEndpointElement("/a", "GET", true, true),
                new DeploymentConfigElement("testing", false, true, false)
        };
        String[] expected = {
                "SourceCodeFile", "TestFile", "ConfigurationFile", "DependencyFile",
                "Directory", "DatabaseElement", "APIEndpointElement", "DeploymentConfigElement"
        };

        for (int i = 0; i < elements.length; i++) {
            RecordingVisitor visitor = new RecordingVisitor();
            elements[i].accept(visitor);
            assertEquals(Arrays.asList(expected[i]), visitor.getCalls(),
                    "wrong visit method for " + expected[i]);
        }
    }

    @Test
    void directoryPassesTheVisitorToItsChildrenInOrder() {
        Directory directory = new Directory("root");
        directory.add(new SourceCodeFile("A.java", 1, 1, false));
        directory.add(new TestFile("ATest.java", 1));
        directory.add(new DatabaseElement("db", true, true, false));

        RecordingVisitor visitor = new RecordingVisitor();
        directory.accept(visitor);

        assertEquals(Arrays.asList("Directory", "SourceCodeFile", "TestFile", "DatabaseElement"),
                visitor.getCalls());
    }

    @Test
    void nestedDirectoriesAreVisitedRecursively() {
        Directory level3 = new Directory("level3");
        level3.add(new APIEndpointElement("/deep", "GET", true, true));
        Directory level2 = new Directory("level2");
        level2.add(level3);
        level2.add(new TestFile("BTest.java", 2));
        Directory level1 = new Directory("level1");
        level1.add(level2);
        level1.add(new SourceCodeFile("B.java", 10, 1, false));

        RecordingVisitor visitor = new RecordingVisitor();
        level1.accept(visitor);

        assertEquals(Arrays.asList("Directory", "Directory", "Directory", "APIEndpointElement",
                "TestFile", "SourceCodeFile"), visitor.getCalls());
    }

    @Test
    void emptyDirectoryOnlyVisitsItself() {
        Directory empty = new Directory("empty");
        RecordingVisitor visitor = new RecordingVisitor();
        empty.accept(visitor);

        assertEquals(Arrays.asList("Directory"), visitor.getCalls());
        assertTrue(empty.getElements().isEmpty());
    }

    @Test
    void getElementsReturnsACopy() {
        Directory directory = new Directory("root");
        directory.add(new TestFile("ATest.java", 1));
        directory.getElements().clear();
        assertEquals(1, directory.getElements().size());
    }

    @Test
    void demoTreeContainsAllEightElementTypes() {
        RecordingVisitor visitor = new RecordingVisitor();
        TestProjects.demoProject().accept(visitor);

        Set<String> types = new HashSet<>(visitor.getCalls());
        assertEquals(8, types.size(), "demo project should use every element type, found " + types);
    }
}
