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

import java.util.ArrayList;
import java.util.List;

/**
 * Checks how much of the project is documented.
 *
 * ASSUMPTIONS
 *   - A "component" is a source file, configuration file, database, API endpoint
 *     or deployment configuration. These elements have an "isDocumented()" flag.
 *   - Test files, dependencies and directories are not counted as components.
 *   - Documentation score = documented components * 100 / all components, rounded
 *     (0 when the project has no components).
 *   - The documentation is SUFFICIENT when the score is at least 70.
 */
public class DocumentationVisitor implements ProjectVisitor {

    public static final int SUFFICIENT_SCORE = 70;

    private final List<String> documented = new ArrayList<>();
    private final List<String> missing = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        record(sourceCodeFile.getFileName(), sourceCodeFile.isDocumented());
    }

    @Override
    public void visit(TestFile testFile) {
        // Test files are not counted as components.
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        record(configurationFile.getFileName(), configurationFile.isDocumented());
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        // Dependencies are not counted as components.
    }

    @Override
    public void visit(Directory directory) {
        // Directories are not counted as components.
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        record("database " + databaseElement.getDatabaseName(), databaseElement.isDocumented());
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        record("endpoint " + apiEndpointElement.getLabel(), apiEndpointElement.isDocumented());
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        record("deployment (" + deploymentConfigElement.getEnvironment() + ")",
                deploymentConfigElement.isDocumented());
    }

    private void record(String componentName, boolean isDocumented) {
        if (isDocumented) {
            documented.add(componentName);
        } else {
            missing.add(componentName);
        }
    }

    public List<String> getDocumentedComponents() {
        return new ArrayList<>(documented);
    }

    public List<String> getMissingComponents() {
        return new ArrayList<>(missing);
    }

    public int getComponentCount() {
        return documented.size() + missing.size();
    }

    public int getDocumentationScore() {
        int total = getComponentCount();
        if (total == 0) {
            return 0;
        }
        return (int) Math.round(documented.size() * 100.0 / total);
    }

    public boolean isSufficient() {
        return getDocumentationScore() >= SUFFICIENT_SCORE;
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("7. DOCUMENTATION REPORT"));
        sb.append("Documented components (").append(documented.size()).append("):\n");
        for (String name : documented) {
            sb.append("  [OK]      ").append(name).append("\n");
        }
        sb.append("Components missing documentation (").append(missing.size()).append("):\n");
        for (String name : missing) {
            sb.append("  [MISSING] ").append(name).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("Documentation score: ").append(getDocumentationScore()).append(" / 100\n");
        sb.append("Status             : ").append(isSufficient() ? "SUFFICIENT" : "INSUFFICIENT")
                .append(" (needs at least ").append(SUFFICIENT_SCORE).append(")\n");
        return sb.toString();
    }
}
