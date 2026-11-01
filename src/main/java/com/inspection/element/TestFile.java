package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/** A test file that contains a number of test methods. */
public class TestFile implements ProjectElement {

    private final String fileName;
    private final int numberOfTests;

    public TestFile(String fileName, int numberOfTests) {
        this.fileName = fileName;
        this.numberOfTests = numberOfTests;
    }

    public String getFileName() {
        return fileName;
    }

    public int getNumberOfTests() {
        return numberOfTests;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
