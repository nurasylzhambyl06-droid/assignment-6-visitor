package com.inspection.element;

import com.inspection.visitor.ProjectVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A directory is a composite element: it contains other ProjectElements,
 * including other directories, so the tree can be nested as deeply as needed.
 *
 * accept() first lets the visitor visit the directory itself and then passes
 * the same visitor to every child. This is how the recursion happens.
 */
public class Directory implements ProjectElement {

    private final String name;
    private final List<ProjectElement> elements = new ArrayList<>();

    public Directory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(ProjectElement element) {
        elements.add(element);
    }

    /** Returns a copy so that callers cannot change the directory by accident. */
    public List<ProjectElement> getElements() {
        return new ArrayList<>(elements);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
        for (ProjectElement element : elements) {
            element.accept(visitor);
        }
    }
}
