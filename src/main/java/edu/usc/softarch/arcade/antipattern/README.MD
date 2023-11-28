# Architectural Smell/Anti-pattern

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


### 1.2 Additional Notes

For further information about these architectural smells, please see [here](https://dl.acm.org/doi/10.1145/3368089.3417941).

The auto-run script in [detect_smells.py](../../../../../../../../scripts/detect_smells.py) only give the four types of smells that are bold in the table above.
The reason for this is that the detection for other types of smells only work for Java projects.

## 2. Detection Code Structure

- [./detection](./detection/)
- [./Smell.java](./Smell.java)
- [./SmellCollection.java](./SmellCollection.java)

### 2.1 [./detection](./detection/)

WIP

### 2.2 [./Smell.java](./Smell.java)

This class define the basic data structure of "Smell" with following attributes, and provide methods to serialization/deserialzation from and to JSON format:
- **clusters**: The clusters matches the output of the clusterer. Each one in the output means there is a smell detected in the cluster.
- **smellType**: The smell type can be either of the four bold types in [Section 1.1](./README.MD/#11-table-for-architectural-smells-in-arcade).
- **topicNum**: The topicNum in smells doesn't have a real meaning. It's only indicates that this data entity is a smell, with a fixed value "-1".

### 2.3 [./SmellCollection.java](./SmellCollection.java)

WIP

# 3.

WIP