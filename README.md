# VariantRank

## Sampling Methods and Plugins
### LogRank
- **Plugin**: LogRankSamplingPlugin.java
- **Required Package**: PageRankSampling.java, ConvertTraceToVector.java
- **Reference**: Cong Liu, Yulong Pei, Long Cheng, Qingtian Zeng, Hua Duan. Sampling Business Process Event Logs Using Graph‐Based Ranking Model. Concurrency Computation: Practice Experience, Vol. 33, No. 5, pp. e5974, 2021.
### LogRank+
- **Plugin**: SimRankSamplingPlugin.java
- **Required Package**: SortingHashMapByValues.java, ConvertTraceToVector.java
- **Reference**: Cong Liu, Yulong Pei, Qingtian Zeng, Hua Duan, Feng Zhang. Logrank+: A Novel Approach to Support Business Process Event Log Sampling. International Conference on Web Information Systems Engineering. Springer, pp. 417-430, 2020.
### LogRank++
- **Plugin**: SigRankSamplingPlugin.java
- **Required Package**: SortingHashMapByValues.java
- **Reference**: 刘聪, 张帅鹏, 李会玲, 何华, 曾庆田. Logrank++: 一种高效的业务过程事件日志采样方法. 计算机集成制造系统, Vol. 30, No. 2, pp. 623, 2024.
### RevisedLogRank++
- **Plugin**: RevisedSigRankSamplingPlugin.java
- **Required Package**: SortingHashMapByValues.java
- **Reference**: 刘聪, 张帅鹏, 李会玲, 何华, 曾庆田. Logrank++: 一种高效的业务过程事件日志采样方法. 计算机集成制造系统, Vol. 30, No. 2, pp. 623, 2024.
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
