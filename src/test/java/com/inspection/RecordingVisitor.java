package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.visitor.ProjectVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny visitor used only in tests. It records which visit method was called.
 * It also shows that a brand-new visitor can be added without touching any element class.
 */
public class RecordingVisitor implements ProjectVisitor {

    private final List<String> calls = new ArrayList<>();

    public List<String> getCalls() {
        return calls;
    }

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        calls.add("SourceCodeFile");
    }

    @Override
    public void visit(TestFile testFile) {
        calls.add("TestFile");
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        calls.add("ConfigurationFile");
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        calls.add("DependencyFile");
    }

    @Override
    public void visit(Directory directory) {
        calls.add("Directory");
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        calls.add("DatabaseElement");
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        calls.add("APIEndpointElement");
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        calls.add("DeploymentConfigElement");
    }
}
