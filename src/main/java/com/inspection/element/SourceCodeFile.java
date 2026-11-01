package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * A source code file. This class only stores data; all analysis is done by visitors.
 *
 * The "documented" flag is a simple documentation property: true means
 * the component has documentation (for example Javadoc or a README section).
 * It is read by DocumentationVisitor and defaults to false.
 */
public class SourceCodeFile implements ProjectElement {

    private final String fileName;
    private final int linesOfCode;
    private final int complexity;
    private final boolean hardcodedCredentials;
    private boolean documented;

    public SourceCodeFile(String fileName, int linesOfCode, int complexity, boolean hardcodedCredentials) {
        this.fileName = fileName;
        this.linesOfCode = linesOfCode;
        this.complexity = complexity;
        this.hardcodedCredentials = hardcodedCredentials;
        this.documented = false;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public int getComplexity() {
        return complexity;
    }

    public boolean hasHardcodedCredentials() {
        return hardcodedCredentials;
    }

    public boolean isDocumented() {
        return documented;
    }

    public void setDocumented(boolean documented) {
        this.documented = documented;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
