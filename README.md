# VariantRank

## Sampling Methods and Plugins
### LogRank
- **Plugin**: LogRankSamplingPlugin.java
- **Required Package**: PageRankSampling.java, ConvertTraceToVector.java
### LogRank+
- **Plugin**: SimRankSamplingPlugin.java
- **Required Package**: SortingHashMapByValues.java, ConvertTraceToVector.java
### LogRank++
- **Plugin**: SigRankSamplingPlugin.java
- **Required Package**: SortingHashMapByValues.java
### VarientRank
- **Plugin**: VarientRankPlugin.java
- **Required Package**: SortingHashMapByValues.java

## How to Use Sampling Plugins
- **Step 1**: Download the required proM version from https://github.com/promworkbench

- **Step 2**: Download Eclipse and Ivy plugins, latest version of Java SE.

- **Step 3**: After importing the project into Eclipse, build the project repository using ivy.xml.

- **Step 4**: Right click on ProM Package Manager, select ProM Package Manager as the running mode, start the ProM plugin manager, install necessary startup plugins and commonly used mining algorithms.

- **Step 5**: Select ProM with UITopia, right-click, choose ProM with UITopia as the running mode, and start ProM Tool.

- **Step 6**: Import the event set like BPIC_2012_O.xes, select the algorithm like VarientRankPlugin, choose the parameters, and complete the event set sampling.

The operation of LogRankSamplingPlugin(LogRank) requires  PageRankSampling.java and ConvertTraceToVector.

The operation of SigRankSamplingPlugin(LogRank++) requires  SortingHashMapByValues.java.

The operation of SimRankSamplingPlugin(LogRank+) requires  SortingHashMapByValues.java and ConvertTraceToVector.java.
