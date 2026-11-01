package com.inspection;

import com.inspection.element.DependencyFile;
import com.inspection.element.Directory;
import com.inspection.element.TestFile;
import com.inspection.visitor.DocumentationVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.dirWith;
import static com.inspection.TestProjects.source;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentationVisitorTest {

    private DocumentationVisitor document(Directory root) {
        DocumentationVisitor visitor = new DocumentationVisitor();
        root.accept(visitor);
        return visitor;
    }

    @Test
    void demoProjectDocumentationReport() {
        DocumentationVisitor visitor = document(TestProjects.demoProject());

        assertEquals(7, visitor.getDocumentedComponents().size());
        assertEquals(6, visitor.getMissingComponents().size());
        assertEquals(13, visitor.getComponentCount());
        assertEquals(54, visitor.getDocumentationScore()); // 7 of 13 = 53.8
        assertFalse(visitor.isSufficient());
        assertTrue(visitor.getMissingComponents().contains("UserService.java"));
        assertTrue(visitor.getMissingComponents().contains("endpoint DELETE /api/admin/cache"));
        assertTrue(visitor.getDocumentedComponents().contains("database payments-db"));
    }

    @Test
    void fullyDocumentedProjectScoresOneHundred() {
        DocumentationVisitor visitor = document(TestProjects.secureProject());

        assertEquals(5, visitor.getComponentCount());
        assertTrue(visitor.getMissingComponents().isEmpty());
        assertEquals(100, visitor.getDocumentationScore());
        assertTrue(visitor.isSufficient());
    }

    @Test
    void emptyProjectHasScoreZero() {
        DocumentationVisitor visitor = document(new Directory("empty"));

        assertEquals(0, visitor.getComponentCount());
        assertEquals(0, visitor.getDocumentationScore());
        assertFalse(visitor.isSufficient());
    }

    @Test
    void testFilesAndDependenciesAreNotComponents() {
        DocumentationVisitor visitor = document(dirWith(
                new TestFile("ATest.java", 1),
                new DependencyFile("lib", "1.0", false, false)));

        assertEquals(0, visitor.getComponentCount());
    }

    @Test
    void sufficientLimitIsSeventyPercent() {
        // 7 documented + 3 undocumented = 70 %
        Directory root = new Directory("root");
        for (int i = 0; i < 7; i++) {
            root.add(source("Doc" + i + ".java", 10, 1, false, true));
        }
        for (int i = 0; i < 3; i++) {
            root.add(source("Missing" + i + ".java", 10, 1, false, false));
        }
        DocumentationVisitor visitor = document(root);

        assertEquals(70, visitor.getDocumentationScore());
        assertTrue(visitor.isSufficient());
    }
}
