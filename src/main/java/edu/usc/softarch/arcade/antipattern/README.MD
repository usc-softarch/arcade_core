# Architectural Smell/Anti-pattern

This document presents the smell detection part of ARCADE.
It was structured with four sections:
- [Section 1](./README.MD/#1-overview-of-architectural-smell-detection-in-arcade): Overview of the smell detection in ARCADE
- [Section 2](./README.MD/#2-interface-and-data-structure-to-access-smell-detection-wip): Interfaces to access detection
- [Section 3](./README.MD/#3-smell-detection-wip): Detection of different types of architectural smells

## 1. Overview of Architectural Smell Detection in ARCADE

Different from code smells, architectural smells are at structure level of a system rather than code-level.
In other words, it's the defects in the system's design rather than its implementation.

The code in [antipattern](./) folder detects architectural smells, also often referred to as architectural antipatterns.
The code detects the following types of smells, including three major categories of smells:
- Interface-based smell
- Change-based smell
- Dependency/concern-based smell

Here are the fine-grained catalog for the architectural smells that can be detected by ARCADE:

### 1.1 Table for Architectural Smells in ARCADE

| Type | Definition | Consequences |
| -------- | -------- | -------- |
| Unused Interface | Component’s interface is not linked to other components | Adds unnecessary complexity to the system |
| Unused Brick | Component’s interfaces are all unused | Same as Unused Interface, but more severe |
| Sloppy Delegation | Component delegates functionality it could have performed | Reduces separation of concerns |
| Functionality Overload | Component has an excessive amount of functionality | Reduced modularity |
| Lego Syndrome | Component handles exceedingly small amount of functionality | High coupling |
| Duplicate Functionality | Several components replicate the same functionality | Bugs if changing only one duplicate |
| Logical Coupling | Parts of different components are frequently changed together | Similar to Duplicate Functionality |
| Dependency Cycle (**bdc**) | Set of components whose links form a circular chain | Changes to one component affect the entire cycle |
| Link Overload (**buo**) | Component’s interfaces have too many dependencies | Reduced isolation of changes |
| Scattered Parasitic Funct. (**spf**) | Multiple components responsible for realizing one concern | Changing a feature modifies multiple system parts |
| Concern Overload (**bco**) | Component implements an excessive number of concerns | Violates separation of concerns |

### 1.2 Additional Notes on Architectural Smells

For further information about these architectural smells, please see [here](https://dl.acm.org/doi/10.1145/3368089.3417941).

The auto-run script in [detect_smells.py](../../../../../../../../scripts/detect_smells.py) only give the four types of smells that are bold in the table above.
The reason for this is that the detection for other types of smells only work for Java projects.

## 2. Interface and Data Structure to Access Smell Detection \[WIP\]

The ARCADE architectural smell detection code under directory [antipattern](./) consists of a subdirectory [./detection](./detection/) and two top level interface files to access detection:

- [./Smell.java](./Smell.java)
- [./SmellCollection.java](./SmellCollection.java)

### 2.1 [./Smell.java](./Smell.java)

Class ```Smell``` define the basic data structure of "Smell" with following attributes, and provide methods to serialization/deserialzation from and to JSON format:
- **clusters**: The clusters matches the output of the clusterer. Each one in the output means there is a smell detected in the cluster.
- **smellType**: The smell type can be either of the four bold types in [Section 1.1](./README.MD/#11-table-for-architectural-smells-in-arcade).
- **topicNum**: The topicNum in smells doesn't have a real meaning. It's only indicates that this data entity is a smell, with a fixed value "-1".

### 2.2 [./SmellCollection.java](./SmellCollection.java)

Class ```SmellCollection``` extends ```HashSet``` and provides basic methods and JSON serialization/deserialization methods.

## 2.3 Detection and Analysis

The subdirectory [./detection](./detection/) under [antipattern](.) contains code to detect different types of smells listed in section [1.1](./README.MD/#11-table-for-architectural-smells-in-arcade).

The code consist of two top level interfaces:

- [ArchSmellDetector.java](./detection/ArchSmellDetector.java)
- [SmellToIssuesCorrelation.java](./detection/SmellToIssuesCorrelation.java)


### 2.4 [ArchSmellDetector.java](./detection/ArchSmellDetector.java)

WIP

### 2.5 [SmellToIssuesCorrelation.java](./detection/SmellToIssuesCorrelation.java)

WIP

## 3. Smell Detection \[WIP\]

### 3.1 [Concern-based Smells](./detection/concern/)

WIP

### 3.2 [Coupling Smells](./detection/coupling/)

WIP

### 3.3 [Dependency Smell](./detection/dependency/)

WIP

### 3.4 [Interface-based Smells](./detection/interfacebased/)

WIP