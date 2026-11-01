package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * A configuration file such as application.properties.
 * The "documented" flag works the same way as in SourceCodeFile.
 */
public class ConfigurationFile implements ProjectElement {

    private final String fileName;
    private final boolean exposedCredentials;
    private final boolean insecureConfiguration;
    private boolean documented;

    public ConfigurationFile(String fileName, boolean exposedCredentials, boolean insecureConfiguration) {
        this.fileName = fileName;
        this.exposedCredentials = exposedCredentials;
        this.insecureConfiguration = insecureConfiguration;
        this.documented = false;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean hasExposedCredentials() {
        return exposedCredentials;
    }

    public boolean isInsecureConfiguration() {
        return insecureConfiguration;
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
