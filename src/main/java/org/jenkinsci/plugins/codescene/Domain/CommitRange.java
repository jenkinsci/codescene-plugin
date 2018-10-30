package org.jenkinsci.plugins.codescene.Domain;

public class CommitRange {

    private final Commit from;
    private final Commit to;
    private final Branch branchBase;

    public CommitRange(Commit from, Commit to) {
        this.from = from;
        this.to = to;
        this.branchBase = null;
    }

    public CommitRange(Branch branchBase, Commit to) {
        this.from = null;
        this.to = to;
        this.branchBase = branchBase;
    }

    public String from() {
        if (from != null) {
            return from.value();
        }

        if (branchBase != null) {
            return branchBase.value();
        }

        throw new IllegalArgumentException("We lack either a commit hash or a branch reference");
    }

    public Commit to() { return to;}
}
