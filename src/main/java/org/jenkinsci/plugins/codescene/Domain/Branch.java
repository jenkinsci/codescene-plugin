package org.jenkinsci.plugins.codescene.Domain;

public class Branch {
    private final String v;

    public Branch(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("A branch name cannot be null - just don't do that");
        }

        v = name;
    }

    public String value() {
        return v;
    }

    @Override
    public String toString() {
        return v;
    }
}
