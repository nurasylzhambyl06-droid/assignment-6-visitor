package com.inspection.visitor;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;

/**
 * The "Visitor" role of the Visitor pattern.
 * There is one overloaded visit method for every concrete element type.
 * The compiler picks the right method from the static type of the argument,
 * and that static type is known inside each element's accept() method.
 */
public interface ProjectVisitor {

    void visit(SourceCodeFile sourceCodeFile);

    void visit(TestFile testFile);

    void visit(ConfigurationFile configurationFile);

    void visit(DependencyFile dependencyFile);

    void visit(Directory directory);

    void visit(DatabaseElement databaseElement);

    void visit(APIEndpointElement apiEndpointElement);

    void visit(DeploymentConfigElement deploymentConfigElement);
}
