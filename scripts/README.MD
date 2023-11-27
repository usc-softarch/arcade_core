# Scripts

The python scripts in this directory provide the functionalities to run the analysis in batch.

This document explains how do each file functions and how to use them in a correct order. 

## Available Python Scripts

- [constants.py](scripts\constants.py)
- [renamer.py](scripts\renamer.py)
- [dir_cleaner.py](scripts\dir_cleaner.py)
- [extract_facts.py](scripts\extract_facts.py)
- [run_clustering.py](scripts\run_clustering.py)
- [detect_smells.py](scripts\constants.py)
- [extract_metrics.py](scripts\extract_metrics.py)
- [plotter.py](scripts\plotter.py)

## 1. constants.py

[constants.py](scripts\constants.py) provides the global variables for the following analysis.
The default settings are:
- The directory for extracted facts: ```FACTS_ROOT = "facts"```
- The directory for generated cluster files: ```CLUSTERS_ROOT = "clusters"```
- The directory for subject systems: ```SUBJECT_SYSTEMS_ROOT = "subject_systems"```
- The directory for programming language stop words: ```STOPWORDS_DIR_PATH = "stopwords"```
- The directory for generated metrics: ```METRICS_ROOT = "metrics"```
- The directory for detected smells: ```SMELLS_ROOT = "smells"```

## 2. renamer.py

[renamer.py](scripts\renamer.py) will rename the version folders under a given system folder based on the given source and target regular expressions.
The purpose is to ensure the version folder names match the regular expression that can be recognized by following analyses, including design decision extraction.
The current (2023/11) target regular expression is:

```console
(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))*
```

**NOTE: There wasn't an official script for renaming the folders, and this functionality still need testing and improvement.**

### 2.1 Command

```console
python renamer.py <regular_expression_source> <regular_expression_target>
```

### 2.2 Example

```console
python renamer.py "*" "(\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-?(alpha(\\d+)?|beta(\\d+)?|rc(\\d+)?|pre(\\d+)?)?))*" 
```

## 3. dir_cleaner.py

[dir_cleaner.py](scripts\dir_cleaner.py) will delete any subdirectories that contain the word "test" in their name. 
There are three arguments for the main function:
```python
run_dir_cleaner(system_name: str, safe_mode: str, ignore_patterns: List[str])
```
- ```system_name```: The string for naming the subject system.
- ```safe_mode```: Safe mode will create a list of directories that would be deleted, but will not perform the deletion. Users may check the output in safe mode to ensure there is no unexpected removal.
- ```ignore_patterns```: Determine the patterns that should be ignored during the removal process. The argument will be passed to the java class file.

### 3.1 Command

```console
python dir_cleaner.py <system_name> <safe_mode> <ignore_patterns>
```

### 3.2 Example

```console
python dir_cleaner.py “cassandra” “off”
```


## 4. extract_facts.py

[extract_facts.py](scripts\extract_facts.py) 
- Runs [Understand](https://scitools.com/) to extract facts to CSV files: ```edu.usc.softarch.arcade.facts.dependencies.UnderstandCsvToRsf```
- Run Mallet to obtain vectors and topic model files: ```edu.usc.softarch.arcade.topics.MalletRunner```. Default setting: 50 topics and 250 iterations.
- Generate DocTopics file: ```edu.usc.softarch.arcade.topics.DocTopics```



### 4.1 Prerequisite External Tool

[SciTools Understand](https://scitools.com/), license required, free trial available.

### 4.2 Command

```console
python extract_facts.py <system_name> <programming_language>
```

### 4.3 Example

```console
python extract_facts.py “cassandra” “java”
```

## 5. run_clustering.py

[run_clustering.py](scripts\run_clustering.py) runs **ACDC**, **ARC**, **PKG**, and **Limbo** clustering, calling different components in ARCADE java code:

- **ARC**, **WCA**, **Limbo**: edu.usc.softarch.arcade.clustering.Clusterer
- **PKG**: edu.usc.softarch.arcade.clustering.Pkg
- **ACDC**: edu.usc.softarch.arcade.clustering.acdc.ACDC

### 5.1 Command

```console
python run_clustering.py <system_name> <programming_language> <assigned_memory (GB)>
```

### 5.2 Example

```console
python run_clustering.py “cassandra” “java” 32
```

## 6. detect_smells.py

Detects architectural smells for **ARC**, **ACDC**, **PKG**, and **Limbo** through
```edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector```

### 6.1 Command

```console
python detect_smells.py <system_name> 
```

### 6.2 Example

```console
python detect_smells.py “cassandra”
```

## 7. extract_metrics.py

**NOTE: This script has been outdated for a while and lack maintenance. It will be correctly documented after it's updated appropriately.**


[extract_metrics.py](scripts\extract_metrics.py) runs **ACDC**, **ARC**, **PKG**, and **Limbo** metric extraction, calling the same java component ```edu.usc.softarch.arcade.metrics.data.SystemMetrics```.

### 7.1 Command

To be added.

### 7.2 Example

To be added.

## 8. plotter.py


[plotter.py](scripts\plotter.py) runs visualization to generate first .dot data files and then .svg and .png files. 
For **ACDC**, **ARC**, **PKG**, and **Limbo**, the script calls the same java component ```edu.usc.softarch.arcade.clustering.RsfToDot```, uses the package ```pydot``` to generate charts.
However, with **Limbo** and **ARC**, it will additionally call ```ARCADE_Core.jar edu.usc.softarch.arcade.clustering.ClusterMatcher``` to match clusters.

### 8.1 Command

```console
python plotter.py <system_name>
```

### 8.2 Example

```console
python plotter.py “cassandra” 
```
