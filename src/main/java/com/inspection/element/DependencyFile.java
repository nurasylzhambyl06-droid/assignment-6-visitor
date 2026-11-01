package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * One declared dependency (for example a library listed in pom.xml).
 * The version may be null or empty when it was not specified.
 */
public class DependencyFile implements ProjectElement {

    private final String dependencyName;
    private final String version;
    private final boolean vulnerable;
    private final boolean prohibited;

    public DependencyFile(String dependencyName, String version, boolean vulnerable, boolean prohibited) {
        this.dependencyName = dependencyName;
        this.version = version;
        this.vulnerable = vulnerable;
        this.prohibited = prohibited;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public boolean isProhibited() {
        return prohibited;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
