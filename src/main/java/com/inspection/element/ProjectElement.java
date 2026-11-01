package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

/**
 * The "Element" role of the Visitor pattern.
 * Every part of the project that can be inspected implements this interface.
 */
public interface ProjectElement {

    void accept(ProjectVisitor visitor);
}
