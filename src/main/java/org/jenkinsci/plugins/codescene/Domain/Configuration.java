package org.jenkinsci.plugins.codescene.Domain;

import java.net.URL;

public class Configuration {

    private final URL url;
    private final CodeSceneUser user;
    private final Repository repo;
    private final int couplingThresholdPercent;
    private final boolean useBiomarkers;
    private final boolean letBuildPassOnFailedAnalysis;
    private final boolean failOnFailedGoal;
    private final boolean failOnDecliningCodeHealth;
    private final String originUrl;
    private final String changeRef;
    private final Commit currentCommit; // aka head-ref, delta-merge-head


    public Configuration(final URL codeSceneUrl, final CodeSceneUser user, final Repository gitRepositoryToAnalyze,
                         int couplingThresholdPercent, boolean useBiomarkers,
                         boolean letBuildPassOnFailedAnalysis,
                         boolean failOnFailedGoal,
                         boolean failOnDecliningCodeHealth,
                         String originUrl, String changeRef, Commit currentCommit) {
        this.url = codeSceneUrl;
        this.user = user;
        this.repo = gitRepositoryToAnalyze;
        this.couplingThresholdPercent = couplingThresholdPercent;
        this.useBiomarkers = useBiomarkers;
        this.letBuildPassOnFailedAnalysis = letBuildPassOnFailedAnalysis;
        this.failOnFailedGoal = failOnFailedGoal;
        this.failOnDecliningCodeHealth = failOnDecliningCodeHealth;
        this.originUrl = originUrl;
        this.changeRef = changeRef;
        this.currentCommit = currentCommit;
    }

    public URL codeSceneUrl() {
        return url;
    }

    public CodeSceneUser user() {
        return user;
    }

    public Repository gitRepositoryToAnalyze() {
        return repo;
    }

    public int couplingThresholdPercent() {
        return couplingThresholdPercent;
    }

    public boolean useBiomarkers() {
        return useBiomarkers;
    }

    public boolean letBuildPassOnFailedAnalysis() { return letBuildPassOnFailedAnalysis; }

    public boolean failOnFailedGoal() { return failOnFailedGoal; }

    public boolean failOnDecliningCodeHealth() { return failOnDecliningCodeHealth; }

    public String originUrl() { return originUrl; }

    public String changeRef() { return changeRef; }

    public Commit currentCommit() {
        return currentCommit;
    }
}
