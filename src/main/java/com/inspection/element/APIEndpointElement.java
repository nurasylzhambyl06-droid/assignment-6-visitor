package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * One API endpoint, for example "POST /api/payments".
 * The "documented" flag works the same way as in SourceCodeFile.
 */
public class APIEndpointElement implements ProjectElement {

    private final String endpointPath;
    private final String httpMethod;
    private final boolean authenticationRequired;
    private final boolean rateLimitingEnabled;
    private boolean documented;

    public APIEndpointElement(String endpointPath, String httpMethod,
                              boolean authenticationRequired, boolean rateLimitingEnabled) {
        this.endpointPath = endpointPath;
        this.httpMethod = httpMethod;
        this.authenticationRequired = authenticationRequired;
        this.rateLimitingEnabled = rateLimitingEnabled;
        this.documented = false;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public boolean isRateLimitingEnabled() {
        return rateLimitingEnabled;
    }

    /** Short label used in reports, for example "POST /api/payments". */
    public String getLabel() {
        return httpMethod + " " + endpointPath;
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
