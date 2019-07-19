package org.jenkinsci.plugins.codescene.Domain;

import java.net.URL;

public class ConfigurationBuilder {
    private URL codeSceneUrl;
    private CodeSceneUser user;
    private Repository gitRepositoryToAnalyze;
    private int couplingThresholdPercent;
    private boolean useBiomarkers;
    private boolean letBuildPassOnFailedAnalysis;
    private boolean failOnFailedGoal;
    private boolean failOnDecliningCodeHealth;
    private String originUrl;
    private String changeRef;

    public ConfigurationBuilder codeSceneUrl(URL codeSceneUrl) {
        this.codeSceneUrl = codeSceneUrl;
        return this;
    }

    public ConfigurationBuilder user(CodeSceneUser user) {
        this.user = user;
        return this;
    }

    public ConfigurationBuilder gitRepositoryToAnalyze(Repository gitRepositoryToAnalyze) {
        this.gitRepositoryToAnalyze = gitRepositoryToAnalyze;
        return this;
    }

    public ConfigurationBuilder couplingThresholdPercent(int couplingThresholdPercent) {
        this.couplingThresholdPercent = couplingThresholdPercent;
        return this;
    }

    public ConfigurationBuilder useBiomarkers(boolean useBiomarkers) {
        this.useBiomarkers = useBiomarkers;
        return this;
    }

    public ConfigurationBuilder letBuildPassOnFailedAnalysis(boolean letBuildPassOnFailedAnalysis) {
        this.letBuildPassOnFailedAnalysis = letBuildPassOnFailedAnalysis;
        return this;
    }

    public ConfigurationBuilder failOnFailedGoal(boolean failOnFailedGoal) {
        this.failOnFailedGoal = failOnFailedGoal;
        return this;
    }

    public ConfigurationBuilder failOnDecliningCodeHealth(boolean failOnDecliningCodeHealth) {
        this.failOnDecliningCodeHealth = failOnDecliningCodeHealth;
        return this;
    }

    public ConfigurationBuilder originUrl(String originUrl) {
        this.originUrl = originUrl;
        return this;
    }

    public ConfigurationBuilder changeRef(String changeRef) {
        this.changeRef = changeRef;
        return this;
    }

    public Configuration build() {
        return new Configuration(codeSceneUrl, user, gitRepositoryToAnalyze, couplingThresholdPercent, useBiomarkers,
                letBuildPassOnFailedAnalysis, failOnFailedGoal, failOnDecliningCodeHealth, originUrl, changeRef);
    }
}