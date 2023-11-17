# Scripts

The python scripts in this directory provide the functionalities to run the analysis in batch.

This document explains, **in the order of execution**, how do each file functions. 

## Available Python Scripts

- [constants.py](scripts\constants.py)
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

## 2. dir_cleaner.py

[dir_cleaner.py](scripts\dir_cleaner.py) will delete any subdirectories that contain the word "test" in their name. 
There are three arguments for the main function:
```python
run_dir_cleaner(system_name: str, safe_mode: str, ignore_patterns: List[str])
```
- ```system_name```: The string for naming the subject system.
- ```safe_mode```: Safe mode will create a list of directories that would be deleted, but will not perform the deletion. Users may check the output in safe mode to ensure there is no unexpected removal.
- ```ignore_patterns```: Determine the patterns that should be ignored during the removal process. The argument will be passed to the java class file.

### 2.1 Command

```console
python dir_cleaner.py <system_name> <safe_mode> <ignore_patterns>
```

### 2.2 Example

```console
python dir_cleaner.py “cassandra” “off”
```


## 3. extract_facts.py

[extract_facts.py](scripts\extract_facts.py) 
- Runs [Understand](https://scitools.com/) to extract facts to CSV files: ```edu.usc.softarch.arcade.facts.dependencies.UnderstandCsvToRsf```
- Run Mallet to obtain vectors and topic model files: ```edu.usc.softarch.arcade.topics.MalletRunner```. Default setting: 50 topics and 250 iterations.
- Generate DocTopics file: ```edu.usc.softarch.arcade.topics.DocTopics```



### 3.1 Prerequisite External Tool

[SciTools Understand](https://scitools.com/), license required, free trial available.

### 3.2 Command

```console
python extract_facts.py <system_name> <programming_language>
```

### 3.3 Example

```console
python extract_facts.py “cassandra” “java”
```

## 4. run_clustering.py

[run_clustering.py](scripts\run_clustering.py) runs **ACDC**, **ARC**, **PKG**, and **Limbo** clustering, calling different components in ARCADE java code:

- **ARC**, **WCA**, **Limbo**: edu.usc.softarch.arcade.clustering.Clusterer
- **PKG**: edu.usc.softarch.arcade.clustering.Pkg
- **ACDC**: edu.usc.softarch.arcade.clustering.acdc.ACDC

### 4.1 Command

```console
python run_clustering.py <system_name> <programming_language> <assigned_memory (GB)>
```

### 4.2 Example

```console
python run_clustering.py “cassandra” “java” 32
```

## detect_smells.py

Detects architectural smells for **ARC**, **ACDC**, **PKG**, and **Limbo** through
```edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector```

### Command

```console
python detect_smells.py <system_name> 
```

### Example

```console
python detect_smells.py “cassandra”
```

## extract_metrics.py

## plotter.py
