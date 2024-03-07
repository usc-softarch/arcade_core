# Architectural Design Decisions

This document presents the **Architectural Design Decisions Analysis** part of ARCADE.
It was structured with four sections:
- [Section 1](./README.MD/#1-overview-of-architectural-smell-detection-in-arcade): Overview of the Architectural Design Decisions Analysis in ARCADE
- [Section 2](./README.MD/#2-interface-and-data-structure-to-access-smell-detection-wip): Interfaces to access detection
- [Section 3](./README.MD/#3-smell-detection-wip): Detection of different types of architectural smells

## 1. Overview of Architectural Design Decisions Analysis

Architectural Design Decisions are decisions made during the development that have impact on software architecture.
In this folder, we introduce a tool named [RecovAr](./RecovArEngine.java), which analyzes issue history and extract design decisions from issue and components history.
More specifically, it output information that contains the issue descriptions and respect architecture-related components (files) that get impacted for all versions of the given software system.

## 2. Interface and Data Structure to Access Smell Detection \[WIP\]

The ARCADE architectural smell detection code under directory [antipattern](./) consists of a subdirectory [./detection](./detection/) and two top level interface files to access detection:

- [RecovAr Engine](./RecovArEngine.java)
- [Cluster Decision](./ClusterDecision.java)
- [Code Element Decision](./CodeElementDecision.java)
- [Decision Analyzer](./DecisionAnalyzer.java)
- [Decision Ranking Engine](./DecisionRankingEngine.java)
- [Element Change Analyzer](./ElementChangeAnalyzer.java)
- [Version Tree Generator](./VersionTreeGenerator.java)

### 2.1 [RecovAr Engine](./RecovArEngine.java)
### 2.2 [Cluster Decision](./ClusterDecision.java)
### 2.3 [Code Element Decision](./CodeElementDecision.java)
### 2.4 [Decision Analyzer](./DecisionAnalyzer.java)

The Decision Analyzer gets added and removed elements between versions and return decisions in CodeElementDecision format with following information:
- issue.description
- issue.id
- version
- addedElements
- removedElements

Please **NOTE**: in method getDecisionList which is used to get the list of design decisions requires the issues to have fixed version information. 
In the code, you can find "issue.getFixVersions().contains(version)" as the precondition. 
Thus, if the target software system does not have this information in its issues, RecovAr won't give any results. E.g., MKVToolNix.

### 2.5 [Decision Ranking Engine](./DecisionRankingEngine.java)
### 2.6 [Element Change Analyzer](./ElementChangeAnalyzer.java)
### 2.7 [Version Tree Generator](./VersionTreeGenerator.java)