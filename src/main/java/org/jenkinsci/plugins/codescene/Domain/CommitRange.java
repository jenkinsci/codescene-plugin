package org.jenkinsci.plugins.codescene.Domain;

public class CommitRange {

    private final Commit from;
    private final Commit to;

    public CommitRange(Commit from, Commit to) {
        this.from = from;
        this.to = to;
    }

    public Commit from() { return from;}

    public Commit to() { return to;}
}
