# VariantRank

## Sampling Methods and Plugins
### LogRank
### LogRank+
### LogRank++
### VarientRank


Download the required proM version from https://github.com/promworkbench

Download Eclipse and Ivy plugins, latest version of Java SE.

After importing the project into Eclipse, build the project repository using ivy.xml.

Right click on ProM Package Manager, select ProM Package Manager as the running mode, start the ProM plugin manager, install necessary startup plugins and commonly used mining algorithms.

Select ProM with UITopia, right-click, choose ProM with UITopia as the running mode, and start ProM Tool.

Import the event set like BPIC_2012_O.xes, select the algorithm like VarientRankPlugin, choose the parameters, and complete the event set sampling.

The operation of LogRankSamplingPlugin(LogRank) requires  PageRankSampling.java and ConvertTraceToVector.

The operation of SigRankSamplingPlugin(LogRank++) requires  SortingHashMapByValues.java.

The operation of SimRankSamplingPlugin(LogRank+) requires  SortingHashMapByValues.java and ConvertTraceToVector.java.
