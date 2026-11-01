package com.inspection.visitor;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.report.ReportFormat;

/**
 * Counts elements and calculates simple project metrics.
 *
 * DEFINITIONS
 *   Total files        = source files + test files + configuration files + dependency files.
 *                        Directories are not files. Databases, API endpoints and deployment
 *                        configurations are infrastructure elements, not files, so they are
 *                        counted separately.
 *   Average complexity = total complexity / number of source files (0 if there are none).
 *   Test/source ratio  = number of test files / number of source files (0 if there are no source files).
 */
public class ProjectStatisticsVisitor implements ProjectVisitor {

    private int directoryCount;
    private int sourceFileCount;
    private int testFileCount;
    private int configFileCount;
    private int dependencyCount;
    private int databaseCount;
    private int apiEndpointCount;
    private int deploymentConfigCount;
    private int totalLinesOfCode;
    private int totalComplexity;
    private int totalTestMethods;

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        sourceFileCount++;
        totalLinesOfCode += sourceCodeFile.getLinesOfCode();
        totalComplexity += sourceCodeFile.getComplexity();
    }

    @Override
    public void visit(TestFile testFile) {
        testFileCount++;
        totalTestMethods += testFile.getNumberOfTests();
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        configFileCount++;
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        dependencyCount++;
    }

    @Override
    public void visit(Directory directory) {
        directoryCount++;
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        databaseCount++;
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        apiEndpointCount++;
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        deploymentConfigCount++;
    }

    public int getDatabaseCount() {
        return databaseCount;
    }

    public int getApiEndpointCount() {
        return apiEndpointCount;
    }

    public int getDeploymentConfigCount() {
        return deploymentConfigCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getTotalFiles() {
        return sourceFileCount + testFileCount + configFileCount + dependencyCount;
    }

    public int getSourceFileCount() {
        return sourceFileCount;
    }

    public int getTestFileCount() {
        return testFileCount;
    }

    public int getConfigFileCount() {
        return configFileCount;
    }

    public int getDependencyCount() {
        return dependencyCount;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public int getTotalTestMethods() {
        return totalTestMethods;
    }

    public double getAverageComplexity() {
        if (sourceFileCount == 0) {
            return 0.0;
        }
        return (double) totalComplexity / sourceFileCount;
    }

    public double getTestSourceRatio() {
        if (sourceFileCount == 0) {
            return 0.0;
        }
        return (double) testFileCount / sourceFileCount;
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("2. PROJECT STATISTICS REPORT"));
        sb.append("Directories        : ").append(directoryCount).append("\n");
        sb.append("Total files        : ").append(getTotalFiles()).append("\n");
        sb.append("  Source files     : ").append(sourceFileCount).append("\n");
        sb.append("  Test files       : ").append(testFileCount).append("\n");
        sb.append("  Config files     : ").append(configFileCount).append("\n");
        sb.append("  Dependencies     : ").append(dependencyCount).append("\n");
        sb.append("Databases          : ").append(databaseCount).append("\n");
        sb.append("API endpoints      : ").append(apiEndpointCount).append("\n");
        sb.append("Deployment configs : ").append(deploymentConfigCount).append("\n");
        sb.append("Total lines of code: ").append(totalLinesOfCode).append("\n");
        sb.append("Total test methods : ").append(totalTestMethods).append("\n");
        sb.append("Average complexity : ").append(ReportFormat.number(getAverageComplexity())).append("\n");
        sb.append("Test/source ratio  : ").append(ReportFormat.number(getTestSourceRatio())).append("\n");
        return sb.toString();
    }
}
