package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * A database used by the application (simulated metadata only, no real connection).
 * The "documented" flag works the same way as in SourceCodeFile.
 */
public class DatabaseElement implements ProjectElement {

    private final String databaseName;
    private final boolean encryptedConnection;
    private final boolean backupEnabled;
    private final boolean exposedCredentials;
    private boolean documented;

    public DatabaseElement(String databaseName, boolean encryptedConnection,
                           boolean backupEnabled, boolean exposedCredentials) {
        this.databaseName = databaseName;
        this.encryptedConnection = encryptedConnection;
        this.backupEnabled = backupEnabled;
        this.exposedCredentials = exposedCredentials;
        this.documented = false;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isEncryptedConnection() {
        return encryptedConnection;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public boolean hasExposedCredentials() {
        return exposedCredentials;
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
