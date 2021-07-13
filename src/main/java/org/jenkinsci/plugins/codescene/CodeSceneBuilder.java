package org.jenkinsci.plugins.codescene;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.util.concurrent.ListenableFuture;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.HyperlinkNote;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.codescene.Domain.*;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CodeSceneBuilder extends Builder implements SimpleBuildStep {

    private static final Logger logger = Logger.getLogger(CodeSceneBuilder.class.getName());

    private static final int DEFAULT_RISK_THRESHOLD = 7;
    // default is the same as in codescene rest api and shouldn't be changed
    private static final int DEFAULT_COUPLING_THRESHOLD_PERCENT = 80;

    // required params
    private final String credentialsId;
    private final String deltaAnalysisUrl;
    private final String repository;

    // optional params
    private boolean analyzeLatestIndividually;
    private boolean analyzeBranchDiff;
    private String baseRevision;
    private boolean markBuildAsUnstable;
    private int riskThreshold = DEFAULT_RISK_THRESHOLD;
    private int couplingThresholdPercent = DEFAULT_COUPLING_THRESHOLD_PERCENT;
    // CodeScene 2.4.0+ supports biomarkers as a separate risk category - default: true
    private boolean useBiomarkers = true;
    // Some users prefer their builds to continue even if CodeScene -- for some reason -- fail to
    // execute the delta analysis. By default we fail the build, but this can be overriden:
    private boolean letBuildPassOnFailedAnalysis = false;

    // Quality Gates
    private boolean failOnFailedGoal = true;
    private boolean failOnDecliningCodeHealth = true;

    /* Gerrit support - alternative origin_url (e.g. "ssh://admin@localhost:29418/poptavka")
       for CodeScene to fetch changes before running an analysis;
       this is needed because Gerrit's "Changes" are special and are not fetched during a regular `git fetch`.
       note: change_ref is only set via GERRIT_REFSPEC env var/parameter
       */
    private String originUrl;

    // deprecated authentication params - use credentialsId instead
    @Deprecated private transient String username;
    @Deprecated private transient String password;



    @DataBoundConstructor
    public CodeSceneBuilder(String credentialsId, String deltaAnalysisUrl, String repository) {
        this.credentialsId = credentialsId;
        this.deltaAnalysisUrl = deltaAnalysisUrl;
        this.repository = repository;
    }

    public boolean isAnalyzeLatestIndividually() {
        return analyzeLatestIndividually;
    }

    public boolean isAnalyzeBranchDiff() {
        return analyzeBranchDiff;
    }

    public String getBaseRevision() {
        return baseRevision;
    }

    public boolean isMarkBuildAsUnstable() {
        return markBuildAsUnstable;
    }

    public int getRiskThreshold() {
        return riskThreshold;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getDeltaAnalysisUrl() {
        return deltaAnalysisUrl;
    }

    public String getRepository() {
        return repository;
    }

    public int getCouplingThresholdPercent() {
        return couplingThresholdPercent;
    }

    public boolean isUseBiomarkers() {
        return useBiomarkers;
    }

    public boolean isLetBuildPassOnFailedAnalysis() {
        return letBuildPassOnFailedAnalysis;
    }

    public boolean isFailOnFailedGoal() {
        return failOnFailedGoal;
    }

    public boolean isFailOnDecliningCodeHealth() {
        return failOnDecliningCodeHealth;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    @DataBoundSetter
    public void setAnalyzeLatestIndividually(boolean analyzeLatestIndividually) {
        this.analyzeLatestIndividually = analyzeLatestIndividually;
    }

    @DataBoundSetter
    public void setAnalyzeBranchDiff(boolean analyzeBranchDiff) {
        this.analyzeBranchDiff = analyzeBranchDiff;
    }

    @DataBoundSetter
    public void setBaseRevision(String baseRevision) {
        this.baseRevision = baseRevision;
    }

    @DataBoundSetter
    public void setMarkBuildAsUnstable(boolean markBuildAsUnstable) {
        this.markBuildAsUnstable = markBuildAsUnstable;
    }

    @DataBoundSetter
    public void setRiskThreshold(int riskThreshold) {
        this.riskThreshold = riskThreshold < 1 || riskThreshold > 10 ? DEFAULT_RISK_THRESHOLD : riskThreshold;
    }

    @DataBoundSetter
    public void setCouplingThresholdPercent(int couplingThresholdPercent) {
        this.couplingThresholdPercent = couplingThresholdPercent < 1 || couplingThresholdPercent > 100
                ? DEFAULT_COUPLING_THRESHOLD_PERCENT
                : couplingThresholdPercent;
    }

    @DataBoundSetter
    public void setUseBiomarkers(boolean useBiomarkers) {
        this.useBiomarkers = useBiomarkers;
    }

    @DataBoundSetter
    public void setLetBuildPassOnFailedAnalysis(boolean letBuildPassOnFailedAnalysis) { this.letBuildPassOnFailedAnalysis = letBuildPassOnFailedAnalysis; }

    @DataBoundSetter
    public void setFailOnFailedGoal(boolean failOnFailedGoal) { this.failOnFailedGoal = failOnFailedGoal; }

    @DataBoundSetter
    public void setFailOnDecliningCodeHealth(boolean failOnDecliningCodeHealth) { this.failOnDecliningCodeHealth = failOnDecliningCodeHealth; }

    @DataBoundSetter
    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    // handle default values for new fields with regards to existing jobs (backward compatibility)
    // check https://wiki.jenkins-ci.org/display/JENKINS/Hint+on+retaining+backward+compatibility
    protected Object readResolve() {
        if (couplingThresholdPercent == 0) {
            couplingThresholdPercent = DEFAULT_COUPLING_THRESHOLD_PERCENT;
        }
        return this;
    }

    private Commits revisionsAsCommitSet(List<String> revisions) {
        ArrayList<Commit> commits = new ArrayList<>();
        for (String revision : revisions) {
            commits.add(new Commit(revision));
        }
        return new Commits(commits);
    }

    private ArrayList<Commits> revisionsAsIndividualCommitSets(List<String> revisions) {
        ArrayList<Commits> commitSets = new ArrayList<>();
        for (String revision : revisions) {
            commitSets.add(Commits.from(new Commit(revision)));
        }
        return commitSets;
    }

    private ArrayList<CodeSceneBuildActionEntry> runDeltaAnalysesOnIndividualCommits(Configuration config, List<String> revisions, TaskListener listener)
            throws RemoteAnalysisException, MalformedURLException {
        List<Commits> commitSets = revisionsAsIndividualCommitSets(revisions);
        ArrayList<CodeSceneBuildActionEntry> entries = new ArrayList<>(commitSets.size());

        if (!commitSets.isEmpty()) {
            listener.getLogger().format("Starting delta analysis on %d commit(s)...%n", commitSets.size());
            for (Commits commits : commitSets) {
                DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
                listener.getLogger().format("Running delta analysis on commits (%s) in repository %s.%n", commits.value(), config.gitRepositoryToAnalyze().value());
                DeltaAnalysisResult result = deltaAnalysis.runOn(commits);

                URL detailsUrl = new URL(
                        config.codeSceneUrl().getProtocol(),
                        config.codeSceneUrl().getHost(),
                        config.codeSceneUrl().getPort(),
                        result.getViewUrl());

                entries.add(new CodeSceneBuildActionEntry(
                        commits.value().get(0).value(),
                        false,
                        commits.value(),
                        result.getRisk(),
                        result.getWarnings().value(),
                        detailsUrl,
                        riskThreshold,
                        result.getRiskDescription(),
                        result.qualityGatesState(),
                        result.improvements(),
                        result.describeCodeHealthDelta(),
                        result.getNewFilesFeedback()));
            }
        } else {
            listener.getLogger().format("No commits to run delta analysis on.%n");
        }

        return entries;
    }

    private CodeSceneBuildActionEntry runDeltaAnalysisOnBranchDiff(Configuration config, List<String> revisions, String branchName, TaskListener listener)
            throws RemoteAnalysisException, MalformedURLException {
        Commits commitSet = revisionsAsCommitSet(revisions);
        DeltaAnalysis deltaAnalysis = new DeltaAnalysis(config);
        listener.getLogger().format("Running delta analysis on branch %s in repository %s.%n", branchName, config.gitRepositoryToAnalyze().value());
        DeltaAnalysisResult result = deltaAnalysis.runOn(commitSet);

        URL detailsUrl = new URL(
                config.codeSceneUrl().getProtocol(),
                config.codeSceneUrl().getHost(),
                config.codeSceneUrl().getPort(),
                result.getViewUrl());

        return new CodeSceneBuildActionEntry(
                branchName,
                true,
                commitSet.value(),
                result.getRisk(),
                result.getWarnings().value(),
                detailsUrl,
                riskThreshold,
                result.getRiskDescription(),
                result.qualityGatesState(),
                result.improvements(),
                result.describeCodeHealthDelta(),
                result.getNewFilesFeedback());
    }

    /**
     * Find the previous successful commit in the environment,
     * or use the base revision (usually reference to the base branch like 'origin/master')
     * if there's no previous commit set.
     * This is important in case we are executing the first build of of a branch.
     */
    private CommitRange getRangeFromPreviousSuccessfulCommit(EnvVars env, Commit currentCommit) {
        // This can be null: see https://issues.jenkins-ci.org/browse/JENKINS-51324
        final String previousCommitFromEnv = env.get("GIT_PREVIOUS_SUCCESSFUL_COMMIT");
        if (previousCommitFromEnv != null) {
            return new CommitRange(new Commit(previousCommitFromEnv), currentCommit);
        } else {
            // fallback to the base revision
            return new CommitRange(new Branch(getBaseRevision()), currentCommit);
        }
    }

    private EnvVars getWorkFlowEnvVars(FlowExecution execution) throws ExecutionException, InterruptedException, IOException {
        EnvVars envVars = new EnvVars();
        if(execution != null) {
            ListenableFuture<List<StepExecution>> executions = execution.getCurrentExecutions(true);
            List<StepExecution> stepExecutions = executions.get();
            for(StepExecution stepExecution : stepExecutions) {
                EnvVars stepEnvVars = stepExecution.getContext().get(EnvVars.class);
                if (stepEnvVars != null) {
                    envVars.putAll(stepEnvVars);
                }
            }
        }
        return envVars;
    }

    private boolean isPipelineRun(Run<?, ?> build){
        // when runnin as stage in pipeline the build is an instance of WorkflowRun
        return build instanceof WorkflowRun;
    }

    private void setBuildResult(Run<?, ?> build, Result result, String message){
        build.setResult(result);
        if(isPipelineRun(build)){
            //when running as stage in pipeline we throw a runtime exception to signal the pipeline the failing of the stage
            throw new RuntimeException(message);
        }
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (isDeltaAnalysisConfigured()) {
            return;
        }
        try {
            URL url = new URL(deltaAnalysisUrl);
            EnvVars env = build.getEnvironment(listener);
            if(isPipelineRun(build)){
                env.putAll(this.getWorkFlowEnvVars(((WorkflowRun)build).getExecution()));
            }

            final Commit currentCommit = new Commit(env.get("GIT_COMMIT"));
            Configuration codesceneConfig = new ConfigurationBuilder()
                    .codeSceneUrl(url)
                    .user(userConfig())
                    .gitRepositoryToAnalyze(new Repository(repository))
                    .couplingThresholdPercent(couplingThresholdPercent)
                    .useBiomarkers(useBiomarkers)
                    .letBuildPassOnFailedAnalysis(letBuildPassOnFailedAnalysis)
                    .failOnFailedGoal(failOnFailedGoal)
                    .failOnDecliningCodeHealth(failOnDecliningCodeHealth)
                    // In most cases, we want to re-use the repository URL.
                    // If that's different from the Gerrit repo url, then the user can provide a custom value via the originUrl config field
                    .originUrl(StringUtils.isNotBlank(originUrl) ? originUrl : env.get("GIT_URL"))
                    // This should be set by the Gerrit Trigger plugin
                    // Alternatively, user can set this parameter explicitly/
                    // However, it doesn't make sense to provide a job config field for this since it's different for every job
                    .changeRef(env.get("GERRIT_REFSPEC"))
                    .currentCommit(currentCommit)
                    .build();

            if (isAnalyzeLatestIndividually()) {
                analyzeLatestIndividualCommitFor(build, workspace, launcher, listener, codesceneConfig,
                        getRangeFromPreviousSuccessfulCommit(env, currentCommit));
            }
            final String branch = env.get("GIT_BRANCH");
            if (isAnalyzeBranchDiff() && getBaseRevision() != null) {
                analyzeWorkOnBranchFor(build, workspace, launcher, listener, codesceneConfig, currentCommit, branch);
            }
        } catch (RemoteAnalysisException e) {
            String message = String.format("Remote failure as CodeScene couldn't perform the delta analysis: %s", e);
            listener.error(message);
            // This is necessary to log the complete stacktrace - listener only shows exception message
            // Note: this log is visible only in the jenkins logs, not in the job's console log.
            logger.log(Level.WARNING, message);
            setBuildResult(build, buildResultForFailedAnalysisDependsOn(letBuildPassOnFailedAnalysis), message);
        } catch (InterruptedException | IOException | ExecutionException e) {
            String message = String.format("Failed to run delta analysis: %s", e);
            listener.error(message);
            setBuildResult(build, Result.FAILURE, message);
        }
    }

    private void analyzeWorkOnBranchFor(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
                                        Configuration codesceneConfig, Commit currentCommit, String branch)
            throws IOException, InterruptedException, RemoteAnalysisException {
        final Branch branchBase = new Branch(getBaseRevision());
        final CommitRange rangeToAnalyse = new CommitRange(branchBase, currentCommit);
        List<String> revisions = getCommitRange(build, workspace, launcher, listener, rangeToAnalyse);
        if (revisions.isEmpty()) {
            listener.getLogger().println(format("No new commits to analyze between the branch '%s' " +
                    "and base revision '%s'.", branch, baseRevision));
        } else {
            CodeSceneBuildActionEntry entry = runDeltaAnalysisOnBranchDiff(codesceneConfig, revisions, branch, listener);

            markAsUnstableWhenFailedGoal(entry, build, listener);
            markAsUnstableWhenCodeHealthDeclines(entry, build, listener);
            markAsUnstableWhenAtRiskThreshold(riskThreshold, entry, build, listener);

            build.addAction(new CodeSceneBuildAction("Delta - By Branch", singletonList(entry)));
        }
    }

    private void analyzeLatestIndividualCommitFor(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
                                                  Configuration codesceneConfig, CommitRange rangeToAnalyse)
            throws IOException, InterruptedException, RemoteAnalysisException {
        List<String> revisions = getCommitRange(build, workspace, launcher, listener, rangeToAnalyse);
        if (revisions.isEmpty()) {
            listener.getLogger().println("No new commits to analyze individually for this build.");
        } else {
            ArrayList<CodeSceneBuildActionEntry> entries = runDeltaAnalysesOnIndividualCommits(codesceneConfig, revisions, listener);
            for (CodeSceneBuildActionEntry entry : entries) {
                markAsUnstableWhenFailedGoal(entry, build, listener);
                markAsUnstableWhenCodeHealthDeclines(entry, build, listener);
                markAsUnstableWhenAtRiskThreshold(riskThreshold, entry, build, listener);
            }
            build.addAction(new CodeSceneBuildAction("Delta - Individual Commits", entries));
        }
    }

    private boolean isDeltaAnalysisConfigured() {
        return !isAnalyzeLatestIndividually() && !isAnalyzeBranchDiff();
    }

    private static Result buildResultForFailedAnalysisDependsOn(final boolean passOnFailedAnalysis) {
        if (passOnFailedAnalysis) {
            return Result.SUCCESS;
        }

        return Result.FAILURE;
    }

    private CodeSceneUser userConfig() {
        if (credentialsId == null) {
            // fallback to the deprecated username and password due the backward compatibility.
            return new CodeSceneUser(username, password);
        }

        final UsernamePasswordCredentials credentials = lookupCredentials(credentialsId);
        if (credentials == null) {
            throw new IllegalStateException("No CodeScene credentials found for id=" + credentialsId);
        }
        return new CodeSceneUser(credentials.getUsername(), credentials.getPassword().getPlainText());
    }


    private UsernamePasswordCredentials lookupCredentials(String credentialId) {
        List<UsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(UsernamePasswordCredentials.class,
                Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }


    private static void marktBuildAsUnstable(Run<?, ?> build) {
        final Result newResult = Result.UNSTABLE;

        final Result result = build.getResult();
        if (result != null) {
            build.setResult(result.combine(newResult));
        } else {
            build.setResult(newResult);
        }
    }

    private static void markAsUnstableWhenFailedGoal(CodeSceneBuildActionEntry entry, Run<?, ?> build, TaskListener listener) {
        final QualityGates gates = entry.gates();

        if (gates.goalHasFailed()) {
            String link = HyperlinkNote.encodeTo(entry.getViewUrl().toExternalForm(), "Failed Quality Gate");
            listener.error("%s : the analysis detects a failed goal. Marking build as unstable.", link);
            marktBuildAsUnstable(build);
        }
    }


    private static void markAsUnstableWhenCodeHealthDeclines(CodeSceneBuildActionEntry entry, Run<?, ?> build, TaskListener listener) {
        final QualityGates gates = entry.gates();

        if (gates.codeHealthDeclined()) {
            String link = HyperlinkNote.encodeTo(entry.getViewUrl().toExternalForm(), "Failed Quality Gate");
            listener.error("%s : the analysis detects a decline in Code Health. Marking build as unstable.", link);
            marktBuildAsUnstable(build);
        }
    }

    private void markAsUnstableWhenAtRiskThreshold(int threshold, CodeSceneBuildActionEntry entry, Run<?, ?> build, TaskListener listener) {
        if (isMarkBuildAsUnstable() && entry.getHitsRiskThreshold()) {
            String link = HyperlinkNote.encodeTo(entry.getViewUrl().toExternalForm(), format("Delta analysis result with risk %d", entry.getRisk().getValue()));
            listener.error("%s hits the risk threshold (%d). Marking build as unstable.", link, threshold);
            marktBuildAsUnstable(build);
        }
    }

    private List<String> getCommitRange(
            Run<?, ?> build,
            FilePath workspace,
            Launcher launcher,
            TaskListener listener,
            CommitRange revisionSpan) throws IOException, InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        launcher.launch()
                .cmdAsSingleString(format("git log --pretty='%%H' %s..%s", revisionSpan.from(), revisionSpan.to()))
                .pwd(workspace)
                .envs(build.getEnvironment(listener))
                .stdout(out)
                .join();

        ArrayList<String> revisions = new ArrayList<>();
        for (String line : out.toString("UTF8").split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                revisions.add(trimmed);
            }
        }
        return revisions;
    }

    @Symbol("codescene")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Run CodeScene Delta Analysis";
        }

        /**
         * Applicable to any kind of project.
         */
        @Override
        public boolean isApplicable(Class type) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) {
            save();
            return true; // indicate that everything is good so far
        }

        public FormValidation doCheckBaseRevision(@QueryParameter boolean analyzeBranchDiff,
                                                  @QueryParameter String baseRevision) {
            if (analyzeBranchDiff && (baseRevision == null || baseRevision.isEmpty())) {
                return FormValidation.error("Base revision cannot be empty.");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckRiskThreshold(@QueryParameter int riskThreshold, @QueryParameter boolean markBuildAsUnstable) {
            if (markBuildAsUnstable && (riskThreshold < 1 || riskThreshold > 10)) {
                return FormValidation.error("Risk threshold must be a number between 1 and 10. The value %d is invalid.", riskThreshold);
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String credentialsId) {
            if (credentialsId == null || credentialsId.isEmpty()) {
                return FormValidation.error("CodeScene API credentials must be set.");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckDeltaAnalysisUrl(@QueryParameter String deltaAnalysisUrl) {
            if (deltaAnalysisUrl == null || deltaAnalysisUrl.isEmpty()) {
                return FormValidation.error("CodeScene delta analysis URL cannot be blank.");
            } else {
                try {
                    new URL(deltaAnalysisUrl);
                    return FormValidation.ok();
                } catch (MalformedURLException e) {
                    return FormValidation.error("Invalid URL");
                }
            }
        }

        public FormValidation doCheckRepository(@QueryParameter String repository) {
            if (repository == null || repository.isEmpty()) {
                return FormValidation.error("CodeScene repository cannot be blank.");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckCouplingThresholdPercent(@QueryParameter int couplingThresholdPercent) {
            if (couplingThresholdPercent < 1 || couplingThresholdPercent > 100) {
                return FormValidation.error("Temporal coupling threshold is percentage and must be a number between 1 and 100." +
                        "The value %d is invalid.", couplingThresholdPercent);
            } else {
                return FormValidation.ok();
            }
        }


        /**
         * Populates the list of credentials in the select box in CodeScene API configuration section
         * Inspired by git plugin:
         * https://github.com/jenkinsci/git-plugin/blob/f58648e9005293ab07b2389212603ff9a460b80a/src/main/java/jenkins/plugins/git/GitSCMSource.java#L239
         */
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Jenkins context, @QueryParameter String credentialsId) {
            if (context == null || !context.hasPermission(Item.CONFIGURE)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            context instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task)context) : ACL.SYSTEM,
                            context,
                            StandardUsernameCredentials.class,
                            Collections.<DomainRequirement>emptyList(),
                            CredentialsMatchers.always())
                    .includeCurrentValue(credentialsId);
        }

    }
}

