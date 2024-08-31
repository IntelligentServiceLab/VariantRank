package sampling.simrank;
/*
 * This is the implementation of SimRank plugin. 
 * It aims to sample an input large-scale example log and returns a small sample log using.
 * The main difference with LogRank is that it tries to reduce the similarity computation time.
 
 * Step 1: each trace is transformed to a featured vertor, we implement multiple approaches for the transformation, for both lifecycle log and normal one.
 * Step 2: we compute the similarity for each trace (vector) with the rest of traces using the cosine similarity metric. 
 * Step 3: find the most similar traces to get a sub-set of representative event logs. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import equalscale.SampleMethods.SortingHashMapByValues;
import kmean.ConvertTraceToVector;
@Plugin(
		name = "LogRank+-based Event Log Sampling",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log using similarity." 
		)
public class SimRankSamplingPlugin {

	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog SimRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		//select two types of event logs, lifecycle event log and normal event log. 
		//选择两种类型的事件日志，生命周期事件日志和正常事件日志。
		String [] logType = new String[2];
		logType[0]="Normal Event Log";
		logType[1]="Lifecycle Event Log";
		String selectedType =ProMUIHelper.queryForObject(context, "Select the type of event log for sampling", logType);
		context.log("The selected log type is: "+selectedType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedType);
				
		
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		
		
		//return the sample log. 
		return SimRankSamplingTechnique(originalLog,selectedType,samplingRatio);
	}
	
	public static XLog SimRankSamplingTechnique(XLog originalLog, String selectedType, double samplingRatio)
	{
		//create a new log with the same log-level attributes  日志级别属性 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		ConvertTraceToVector ctv = new ConvertTraceToVector();

		//keep an ordered list of traces names.  //保存一份有序的轨迹名称列表。
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace.
		//将log转换成map key是轨迹的名称 value是轨迹
		HashMap<String, XTrace> nameToTrace = new HashMap<>();
		for(XTrace trace: originalLog)
		{
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			nameToTrace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		
		//trace name to vector  
		//轨迹名到向量
		HashMap<String, HashSet<String>> nameToFeatureSet = new HashMap<>();

		if(selectedType=="Normal Event Log")// different trace 2 feature mapping
		{
			for(int i=0;i<TraceIdList.size();i++)
			{
				//get the trace feature vector	
				//获得轨迹的特征向量
				nameToFeatureSet.put(TraceIdList.get(i), ctv.Trace2FeatureSet(nameToTrace.get(TraceIdList.get(i))));
			}
		}
		else{
			for(int i=0;i<TraceIdList.size();i++)
			{
				//get the trace feature vector	
				//获得轨迹特征向量
				nameToFeatureSet.put(TraceIdList.get(i), ctv.Trace2FeatureSetStartComplete(nameToTrace.get(TraceIdList.get(i))));
			}
		}
		
		//trace name to weight, the weight represents the difference, rather than similarity. 
		// 从名称到权重，权重代表的是差异，而不是相似性。
		HashMap<String, Double> nameToWeight = new HashMap<>();
		for(int i=0;i<TraceIdList.size();i++)
		{
			//for i=0, we compute the similarity between 0 and {1...n}
			nameToWeight.put(TraceIdList.get(i), 
					1-ctv.CosineSimilarity(convertHashSet2HashMap(nameToFeatureSet.get(TraceIdList.get(i))),computeFeatureUnion(TraceIdList.get(i), nameToFeatureSet)));
		}

		//select the top n traces. 
		//选择最高的n个轨迹
		int topN=(int)Math.round(samplingRatio*originalLog.size());
		System.out.println("Sample Size: "+ topN);
		//order traces based on the weight
		//基于权重排序轨迹
		HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(nameToWeight,topN);
		System.out.println("Sample Trace Names: "+sampleTraceNameSet);
		//construct the sample log based on the selected top n traces. 
		//构造 样本日志基于选择的n个轨迹
		for(XTrace trace: originalLog)
		{
			if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
			{
				sampleLog.add(trace);
			}
		}
		
		return sampleLog;
				
	}
	
	public static HashMap<String, Boolean> computeFeatureUnion(String currentTraceName, HashMap<String, HashSet<String>> nameToFeatureSet)
	{
		HashSet<String> unionFeatureSet = new HashSet<>();
		for(String traceName : nameToFeatureSet.keySet())
		{
			if(!traceName.equals(currentTraceName))
			{
				unionFeatureSet.addAll(nameToFeatureSet.get(traceName));
			}
		}
		
		//transform feature set to 
		return convertHashSet2HashMap(unionFeatureSet);
	}
	
	public static HashMap<String, Boolean> convertHashSet2HashMap(HashSet<String> hashSet)
	{
		HashMap<String, Boolean> featureMap = new HashMap<>();
		for(String s: hashSet)
		{
			featureMap.put(s, true);
		}
		
		return featureMap;

	}
	
}
