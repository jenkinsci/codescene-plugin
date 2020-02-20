**Small change to trigger delta analysis**
 1

# codescene-jenkins-plugin

[![Build Status](https://travis-ci.org/empear-analytics/codescene-jenkins-plugin.svg)](https://travis-ci.org/empear-analytics/codescene-jenkins-plugin)
[![Latest release](https://img.shields.io/github/release/empear-analytics/codescene-jenkins-plugin.svg)](https://github.com/empear-analytics/codescene-jenkins-plugin/releases/latest)

A jenkins plugin for
[CodeScene](https://empear.com/how-it-works/) by Empear.

Developers, check [internal jenkins plugin documentation](https://github.com/empear-analytics/codescene-docs/blob/master/codescene-jenkins-plugin.md) (private repo).


CodeScene identifies and prioritizes technical debt, while at the same time uncovering and measuring social factors of the organization behind
the system. The earlier you can react to any potential finding, the better. That’s why
CodeScene offers integration points that let you incorporate the analysis
results into your build pipeline.

## Capabilities, Use Cases, and Examples

This plugin lets you use CodeScene’s Delta Analysis to:
* Prioritize code reviews based on the risk of the commits.
* Specify quality gates for the goals specified on identified hotspots (see [Managing Technical Debt](https://empear.com/blog/manage-technical-debt-with-augmented-code-analysis/)).
* Specify quality gates that trigger in case the Code Health of a hotspot declines.

### Prioritize Pull Requests and Commits for Code Reviews

The risk classification is described in detail in CodeScene's documentation. The value goes from 1 (lowest risk) to 10 (a high risk change).
The plugin can be run on either individual commits or a complete branch:

![Screenshot](screenshot.png)

### Enable Quality Gates to Supervise Technical Debt

CodeScene's Intelligent Notes concept lets you augment the analysis with your goals and contextual information. For example, you could
decide that a hotspot might indeed have a high degree of technical debt, but chose to live with it for now. In that case you would tell
CodeScene about your decision but ask the tool to supervise the hotspot so that it doesn't get worse.
With the Jenkins plugin, you could then enforce that goal through a quality gate.

As long as everything evolves according to plan, the plugin will report an OK in its green Quality Gate (QG) indication:

![Pass Quality Gate](pass-quality-gate.png)

However, should one of your goals fail -- e.g. the supervised hotspot grows worse -- then we notify directly in the CI/CD pipeline and
mark the build as unstable:

![Failed Quality Gate](failed-quality-gate.png)

If you don't yet use the Intelligent Notes feature then, well, you really should -- it's a game changer that lets you take
on technical debt in a pro-active rather than reactive way. However, there might be other unsupervised hotspots. You can
catch a decline in Code Health in that case too by using the second quality gate, "Mark Build as Unstable on Code Health Decline".
The two quality gates work well together, and we recommend you enable both.

### Get Early Warnings from a Temporal Perspective

In addition to the risk classification and quality gates, CodeScene also runs its set of early warning analyses:

![EarlyWarning](earlywarning.png)

The early warnings and risk classification let you prioritize your code reviews and
focus your time where (and when) it’s likely to be needed the most.
Code reviewer fatigue is a real thing, so let’s use our review efforts wisely.

## Installation

The recommended way is to install the plugin via Jenkins Update Center (Jenkins -> Manage Jenkins -> Manage Plugins). You can find the latest version in [Jenkins plugin repository](https://plugins.jenkins.io/codescene).

### Manual build and installation

You can build the latest version of plugin by running `mvn clean package` and installing `target/codescene.hpi`
to your Jenkins manually.

### Configure Jenkins for CodeScene

Enable the CodeScene integration by adding a new build step in your Jenkins configuration. Select the option *Run CodeScene Delta Analysis*.

![Add Buildstep](add-buildstep.png)

Enter the required information in the CodeScene Jenkins configuration:

![Buildstep](buildstep.png)

####  Delta Analysis Settings

CodeScene gives you a number of options that controls the scope of the delta analysis:

* *Individual Commits*: Check this option to run a delta analysis on each individual commit. When in doubt, make this your default setting.
* *By Branch*: With this option, CodeScene runs a delta analysis on the difference between the branch you build and the one you provide as reference. Use this option if you want to analyse a whole pull request.

You can also change the defaults for risk threshold and temporal coupling threshold

* *Risk Threshold*: commits with risk equal or higher than this value will make a build unstable
* *Temporal Coupling Threshold*: minimum temporal coupling for "Absence of Expected Change Pattern" warning

By checking the _**Use Biomarkers**_ option, CodeScene warns about files that seem to degrade in quality through issues introduced in the current changeset.

Finally, you enable the Quality Gates in the configuration too.

#### CodeScene API Configuration

The CodeScene API configuration section has to match the information specified inside CodeScene itself and retrievable from the analysis configuration (Project configuration -> Delta Analysis):

![Project Configuration - Delta Analysis](project-config-delta-analysis.png)

API Credentials should be added via [jenkins credentials plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin).
Check [Injecting Secrets into Jenkins Build Jobs](https://support.cloudbees.com/hc/en-us/articles/203802500-Injecting-Secrets-into-Jenkins-Build-Jobs) for more details.


#### Gerrit Configuration

This is an advanced config useful if you use [Gerrit Code Review](https://www.gerritcodereview.com/) tool.

In the simplest case, you don't need to set  anything in the "Gerrit's origin URL" field:

- If you use the [Gerrit Trigger](https://wiki.jenkins.io/display/JENKINS/Gerrit+Trigger) plugin,
it sets the `GERRIT_REFSPEC` job env variable used by the CodeScene plugin as the `change_ref` parameter's value
sent to the CodeScene Delta Analysis API.
- The API also requires the `origin_url` parameter which is by default set 
to the value of the _Repository URL_ field (see Jenkins job Git plugin config). 

In more complex scenarios you can:
- add the `GERRIT_REFSPEC` "parameter" to your job config and set it manually when triggering a "Build with parameters". 
- provide a custom value for the `origin_url` by setting the **_Gerrit's origin URL_** field in the job configuration (see the _Gerrit Configuration_ section).

See also the [DELTA ANALYSIS WITH GERRIT](https://docs.enterprise.codescene.io/versions/3.2.5/guides/delta/automated-delta-analyses.html#delta-analysis-with-gerrit)
section in CodeScene on-prem documentation.




## Changelog

* 1.2.3
 * Introduce optional quality gates based on CodeScene's [Intelligent Notes](https://empear.com/blog/manage-technical-debt-with-augmented-code-analysis/).
* 1.1.4
 * Reverse the previously added configuration option so that a user explicitly has to allow an analysis failure to pass the build.
* 1.1.3
 * Introduce a configuration option to mark builds as UNSTABLE rather than FAILED if a remote CodeScene analysis couldn't be performed.
* 1.1.2
  * Add Biomarkers support to auto-detect files that seem to degrade in quality through issues introduced in a changeset - requires CodeScene version 2.4.0 or higher.
* 1.1.1
  * [BUGFIX: skip delta analysis if there are no different commits between analyzed branch and base revision](https://github.com/jenkinsci/codescene-plugin/pull/1)
* 1.1.0
  * Use [credentials plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin)
    for storing CodeScene API credentials instead of hardcoded username and password.
    Existing jobs will continue to work, but you won't be able to update job configuration
    (e.g. change username or password) until you switch to the credentials plugin.
    Check [Injecting Secrets into Jenkins Build Jobs](https://support.cloudbees.com/hc/en-us/articles/203802500-Injecting-Secrets-into-Jenkins-Build-Jobs)
    for more details.
  * Only required parameters are now set in constructor.
    Optional parameters are set with setters.
    Check https://github.com/jenkinsci/pipeline-plugin/blob/c84a9af/DEVGUIDE.md#constructor-vs-setters.
    **Original constructor has been removed**!
  * Specify mnemonic extension name "codescene" via `@Symbol` in `CodeSceneBuilder`.
    Check https://github.com/jenkinsci/pipeline-plugin/blob/c84a9af/DEVGUIDE.md#defining-symbols
    and https://wiki.jenkins-ci.org/display/JENKINS/Structs+plugin for more details.
  * Add new configuration field for temporal coupling threshold. The new field is set to 80 percent by default. Requires CodeScene 2.0+, otherwise the new settings doesn't have any effect.
* 1.0.1
  * Add checksums to built artifacts in GitHub releases
* 1.0.0
  * Delta analysis by branch, based on _base revision_
  * Delta analysis by individual commits (new commits not seen in previous jobs)
  * "Mark build as unstable" based on risk threshold

## Contributing

You're encouraged to submit [pull
requests](https://github.com/empear-analytics/codescene-jenkins-plugin/pulls),
and to [propose features and discuss
issues](https://github.com/empear-analytics/codescene-jenkins-plugin/issues).

## License

Licensed under the [MIT License](LICENSE).
