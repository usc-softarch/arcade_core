# Architecture Recovery, Change, And Decay Evaluator (ARCADE)

## Are you using ARCADE? Please let us know!

We'd love to know more about the project you use ARCADE for. This information allows us to apply for more funding that can support future development and maintenance of ARCADE. Feel free to contact either [Nenad Medvidović](mailto:neno@usc.edu), [Joshua Garcia](mailto:joshug4@uci.edu) or [Marcelo Schmitt Laser](mailto:schmittl@usc.edu) to let us know more about how you use ARCADE.

## What is ARCADE?

Architecture Recovery, Change, And Decay Evaluator (ARCADE) is a software workbench that employs (1) a suite of architecture-recovery techniques, (2) a catalogue of architectural smell definitions, (3) accompanying smell-detection algorithms, and (4) a set of metrics for measuring different aspects of architectural change and decay. ARCADE combines these elements to investigate a variety of questions regarding architectural change and decay.

ARCADE’s foundational element is its suite of architecture-recovery techniques. The architectures produced by the Recovery Techniques are used for studying change and decay. ARCADE currently provides access to eight recovery techniques. This allows an engineer to (1) extract multiple architectural views and to (2) ensure maximum accuracy of extracted architectures by highlighting their different aspects.

## ARCADE Core

ARCADE Core is a fork of [ARCADE](https://bitbucket.org/joshuaga/arcade) containing all of its primary functionalities, updated with modern programming practices for ease of use. ARCADE Core uses a minimal set of external dependencies and is packaged using Maven and Java 11. All functions described in the [manual](https://tiny.cc/arcademanual) are present and have been thoroughly tested and debugged to ensure correctness. We welcome any suggestions and bug reports you may have, which can be posted in the form of Issues in this repository. You may also e-mail the primary ARCADE Core maintainer, [Marcelo Schmitt Laser](mailto:schmittl@usc.edu), for any questions or concerns regarding how to use ARCADE Core.

## Contributors

[**Prof. Joshua Garcia**](https://jgarcia.ics.uci.edu/) is the original creator of ARCADE, and author of the ARC clustering technique. He also co-designed and implemented ARCADE's architectural decay prediction tools.

[**Prof. Nenad Medvidović**](https://viterbi.usc.edu/directory/faculty/Medvidovic/Nenad) is the head of the [Software Architecture Research Group](https://softarch.usc.edu/), where ARCADE was created and is maintained.

[**Dr. Duc Minh Le**](https://lemduc.github.io/) is a major contributor to ARCADE, and is responsible for much of its smell detection components.

**Marcelo Schmitt Laser** is the current maintainer of ARCADE, and creator of ARCADE Core.

[**Daye Nam**](https://dayenam.com/) is the creator of [EVA](https://github.com/namdy0429/EVA), a visualization tool that utilizes ARCADE's results to create graphical presentations of architectural information.

[**Dr. Arman Shahbazian**](http://shahbazian.me/) created several extensions to ARCADE for the recovery of architectural design decision information.

[**Dr. Ehsan Kouroshfar**](https://www.linkedin.com/in/ekouroshfar/) and [**Prof. Sam Malek**](https://malek.ics.uci.edu/) co-designed and implemented architectural decay prediction tools based on ARCADE.

[**Prof. Igor Ivkovic**](https://uwaterloo.ca/systems-design-engineering/profile/iivkovic) is a co-author of the first-ever publication involving ARCADE, and contributed significantly to its original vision.

[**Sylvia Wong**](https://www.linkedin.com/in/sylvia-wong) contributed towards the modernization effort in ARCADE Core, being largely responsible for its CI and test suite.

[**Gina Yang**](https://www.linkedin.com/in/gina-yang861) developed a large part of the ARCADE Core test suite and was responsible for generating the majority of its test oracles.

**Khoi Pham** developed much of the infrastructure that enabled the integration of ARCADE into SAIN, the Software Architecture Instrument.

## A Brief Manual for ARCADE and RecovAr

### Building ARCADE with Maven

- ```mvn clean``` first
- ```mvn package -Dmaven.test.skip=true ```(This Should be run in pure CMD instead of PowerShell. It will fail in PowerShell, but the reason is not clear.)
- Rename the built jar file to **ARCADE_Core.jar**.

### Get System Source Code from Repositories

Get project sources for different versions: <https://github.com/milvio123/Scraping-Tool>. The script doesn't require any CLI/Bash parameters. Instead, it reads project URL input on running and grab zip files for all versions of the target project.

### Set Environment Variables

The project folder should be in the data folder, which should be consistent with the one in the **constant.py**. 
A top-level folder should be named after the project and the sub-directories should be different versions of that project. 
E.g., An component in a project should be in "data/projects/project_name/version_tag/component_name". 
And again, the version tags should match the regular expression in the code below.

### Rename the Version Folders

**renamer.py**: Rename the version folders to match the regex. This is for source code. This file is [here](\scripts\ARCADE\renamer.py).

### Clean Up Testing Modules 

**dir_cleaner.py**: This deletes test-related code. It uses string comparison. In the safe mode, we get the list that will be deleted. It's for double check. After that, we can turn off safe mode. If there is something we don't want to delete, there is another parameter for ignore regex. **Parameters**: system_name (E.g., "graphviz"), safe_mode or not ("on" to check OK or "off" to directly remove the files), and ignored_patterns.

### Extract Facts (Dependencies)

**extract_facts.py**: This requires an external tool https://scitools.com/. It will extract the dependencies for the project. Use it with the language. **Parameters**: system_name ("graphviz") and the language (c/java/python).

### Run Clustering for the Architecture

**run_clustering.py**: This generates input for the most following analysis, including smell detection, metrics generation, and design decisions extraction. Give it as much memory as possible. **Parameters**: system_name ("graphviz"), language (c/java/python) and an integer (GBs) to indicate assigned memory.

### Extract Architectural Design Decisions with RecovAr 

Generate a version tree, as required for RecovAr, to extract design decisions from systems.

**Command:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.VersionTreeGenerator <PATH_TO_CLUSTERS_INPUT> <PATH_TO_VERSIONTREE_OUTPUT>
```
- <PATH_TO_CLUSTERS_INPUT>: project directory "/clusters/project_name/algorithm_name"
- <PATH_TO_VERSIONTREE_OUTPUT>: version tree file (.rsf)


**Example:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.VersionTreeGenerator "gitlab_subjects/clusters/inkscape/acdc" "gitlab_subjects/clusters/inkscape/versionTree.rsf"
```
**Example 2:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.VersionTreeGenerator ./clusters/mkvtoolnix/acdc ./versionTree_mkvtoolnix_acdc.rsf
```


Call RecovAr engine to extract design decisions

```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.RecovArEngine <PATH_TO_CLUSTER_DIR> <PATH_TO_VERSION_TREE> <PROJECT_ID> <CHECKPOINT_FILE_PATH> <VERSION_SCHEME_REGEX> <OUTPUT_PATH>
```
- <PATH_TO_CLUSTER_DIR>: project directory
- <PATH_TO_VERSION_TREE>: version tree file (.rsf)
- <PROJECT_ID> 
- <CHECKPOINT_FILE_PATH>: checkpoint file, if it breaks, we will be able to run from the checkpoint next time 
- <VERSION_SCHEME_REGEX>: regex for how versions are organized, "(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))"
- <OUTPUT_PATH>: final output file

**Example for Inkscape:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.RecovArEngine "gitlab_subjects/clusters/inkscape/acdc" "gitlab_subjects/clusters/inkscape/versionTree.rsf" 3472737 "gitlab_subjects/clusters/inkscape/issuesCheckpoint.json" "(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))*" "gitlab_subjects/clusters/inkscape/recovArOutput.json"
```

**Example for GraphViz:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.RecovArEngine "D:\Workspace\arc-sum\scripts\ARCADE\clusters\graphviz\arc" "D:\Workspace\arc-sum\scripts\ARCADE\versionTree_graphviz.rsf" 4207231 "D:\Workspace\arc-sum\scripts\ARCADE\issuesCheckpoint_graphviz_arc.json" "(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))*" "D:\Workspace\arc-sum\scripts\ARCADE\recovArOutput_graphviz_arc.json"
```

**Example for MKVToolNix:**
```console
java -cp ARCADE_Core.jar edu.usc.softarch.arcade.facts.design.RecovArEngine "D:\Workspace\arc-sum\scripts\ARCADE\clusters\mkvtoolnix\acdc" "D:\Workspace\arc-sum\scripts\ARCADE\versionTree_mkvtoolnix.rsf" 4910441 "D:\Workspace\arc-sum\scripts\ARCADE\issuesCheckpoint_mkvtoolnix_acdc.json" "(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))*" "D:\Workspace\arc-sum\scripts\ARCADE\recovArOutput_mkvtoolnix_acdc.json"
```
