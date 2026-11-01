package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.TestFile;
import com.inspection.visitor.ProjectStatisticsVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.dirWith;
import static com.inspection.TestProjects.source;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectStatisticsVisitorTest {

    private ProjectStatisticsVisitor measure(Directory root) {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        root.accept(visitor);
        return visitor;
    }

    /** One tree with every element type, including a nested directory. */
    private Directory mixedProject() {
        Directory nested = new Directory("nested");
        nested.add(source("C.java", 50, 6, false, true));

        Directory root = new Directory("root");
        root.add(source("A.java", 100, 4, false, true));
        root.add(source("B.java", 200, 8, false, true));
        root.add(new TestFile("ATest.java", 3));
        root.add(new ConfigurationFile("a.properties", false, false));
        root.add(new DependencyFile("lib1", "1.0", false, false));
        root.add(new DependencyFile("lib2", "2.0", false, false));
        root.add(new DatabaseElement("db", true, true, false));
        root.add(new APIEndpointElement("/a", "GET", true, true));
        root.add(new DeploymentConfigElement("production", false, true, false));
        root.add(nested);
        return root;
    }

    @Test
    void countsEveryElementType() {
        ProjectStatisticsVisitor stats = measure(mixedProject());

        assertEquals(3, stats.getSourceFileCount());
        assertEquals(1, stats.getTestFileCount());
        assertEquals(1, stats.getConfigFileCount());
        assertEquals(2, stats.getDependencyCount());
        assertEquals(1, stats.getDatabaseCount());
        assertEquals(1, stats.getApiEndpointCount());
        assertEquals(1, stats.getDeploymentConfigCount());
        assertEquals(2, stats.getDirectoryCount());
        assertEquals(7, stats.getTotalFiles(), "directories and infrastructure elements are not files");
    }

    @Test
    void sumsLinesOfCodeAndTestMethods() {
        ProjectStatisticsVisitor stats = measure(mixedProject());

        assertEquals(350, stats.getTotalLinesOfCode());
        assertEquals(3, stats.getTotalTestMethods());
    }

    @Test
    void calculatesAverageComplexityAndTestSourceRatio() {
        ProjectStatisticsVisitor stats = measure(mixedProject());

        assertEquals(6.0, stats.getAverageComplexity(), 0.001);   // (4 + 8 + 6) / 3
        assertEquals(1.0 / 3.0, stats.getTestSourceRatio(), 0.001);
    }

    @Test
    void emptyDirectoryGivesZerosWithoutDividingByZero() {
        ProjectStatisticsVisitor stats = measure(new Directory("empty"));

        assertEquals(1, stats.getDirectoryCount());
        assertEquals(0, stats.getTotalFiles());
        assertEquals(0, stats.getTotalLinesOfCode());
        assertEquals(0.0, stats.getAverageComplexity(), 0.0);
        assertEquals(0.0, stats.getTestSourceRatio(), 0.0);
    }

    @Test
    void zeroSourceFilesWithTestsKeepsRatioAtZero() {
        ProjectStatisticsVisitor stats = measure(dirWith(new TestFile("OnlyTest.java", 4)));

        assertEquals(0, stats.getSourceFileCount());
        assertEquals(1, stats.getTestFileCount());
        assertEquals(0.0, stats.getTestSourceRatio(), 0.0);
        assertEquals(0.0, stats.getAverageComplexity(), 0.0);
    }

    @Test
    void demoProjectStatistics() {
        ProjectStatisticsVisitor stats = measure(TestProjects.demoProject());

        assertEquals(11, stats.getDirectoryCount());
        assertEquals(11, stats.getTotalFiles());
        assertEquals(500, stats.getTotalLinesOfCode());
        assertEquals(10.0, stats.getAverageComplexity(), 0.001);
        assertEquals(0.5, stats.getTestSourceRatio(), 0.001);
        assertEquals(2, stats.getDatabaseCount());
        assertEquals(3, stats.getApiEndpointCount());
        assertEquals(3, stats.getDeploymentConfigCount());
    }
}
