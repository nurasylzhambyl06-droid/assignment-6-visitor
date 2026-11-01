package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * Deployment settings for one environment.
 * The environment is "development", "testing" or "production".
 * The "documented" flag works the same way as in SourceCodeFile.
 */
public class DeploymentConfigElement implements ProjectElement {

    public static final String DEVELOPMENT = "development";
    public static final String TESTING = "testing";
    public static final String PRODUCTION = "production";

    private final String environment;
    private final boolean debugMode;
    private final boolean httpsEnabled;
    private final boolean autoDeploymentEnabled;
    private boolean documented;

    public DeploymentConfigElement(String environment, boolean debugMode,
                                   boolean httpsEnabled, boolean autoDeploymentEnabled) {
        this.environment = environment;
        this.debugMode = debugMode;
        this.httpsEnabled = httpsEnabled;
        this.autoDeploymentEnabled = autoDeploymentEnabled;
        this.documented = false;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isProduction() {
        return PRODUCTION.equalsIgnoreCase(environment);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public boolean isAutoDeploymentEnabled() {
        return autoDeploymentEnabled;
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
