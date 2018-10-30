package org.jenkinsci.plugins.codescene.Domain;

public class RiskDescription {
    private final String d;

    public RiskDescription(final String description) {
        this.d = description;
    }

    public String value() { return d;}

    public String toString() { return value();}
}
