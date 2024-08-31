package sampling.sigrank;
/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
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

@Plugin(
		name = "SigRank-based Event Log Sampling2",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces." 
		)
public class SigRankSamplingPlugin2 {
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
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> TraceID2Trace = new HashMap<>();
		
				
		
		//trace to activity set
		HashMap<String, ArrayList<String>> traceIDToActivityList = new HashMap<>();
		//trace to direct-follow relation set
		
		HashMap<String, ArrayList<String>> traceIDToDFRList = new HashMap<>();
		
		//遍历原始日志中的每个轨迹，提取活动和直接后继关系，并存储到对应的HashSet中：
		//对于每条轨迹，代码创建了两个HashSet，分别用来存储轨迹中的活动（activitySet）和直接后继关系对（dfrSet）。通过嵌套的for循环，遍历轨迹中的每个活动，并提取活动名称和直接后继关系对。
		//轨迹变体 eventSet
		
		
		for(XTrace trace:originalLog)
		{
			
			ArrayList<String> activityList = new ArrayList<>();
		
			ArrayList<String> dfrList = new ArrayList<>();
			ArrayList<String> eventList = new ArrayList<>();
			
			for(XEvent event:trace) {
				eventList.add(XConceptExtension.instance().extractName(event));
			}
			
			
			if(selectedType=="Normal Event Log")// different trace 2 feature mapping
			{
				
					for(int i =0;i<trace.size();i++)
					{
						//add activity
					
					
						activityList.add(XConceptExtension.instance().extractName(trace.get(i)));
						
					}
					for(int i =0;i<trace.size()-1;i++)
					{
					
						//add directly follow pair
						
						dfrList.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
						
					}
					
					
					traceIDToActivityList.put(XConceptExtension.instance().extractName(trace), activityList);
					
					
					traceIDToDFRList.put(XConceptExtension.instance().extractName(trace), dfrList);
					
					TraceIdList.add(trace.getAttributes().get("concept:name").toString());
					TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
				
				
			}
			else{
				
					for(int i =0;i<trace.size();i+=2)
					{
						//add activity
					
				
//						System.out.println(XConceptExtension.instance().extractName(trace.get(i)) );
						activityList.add(XConceptExtension.instance().extractName(trace.get(i)));
						
					}
					for(int i =1;i<trace.size()-1;i+=2)
					{
					
						//add directly follow pair
						
						dfrList.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
						
					}
					
					
					traceIDToActivityList.put(XConceptExtension.instance().extractName(trace), activityList);
					
					
					traceIDToDFRList.put(XConceptExtension.instance().extractName(trace), dfrList);
					
					TraceIdList.add(trace.getAttributes().get("concept:name").toString());
					TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
					
				
			
			
		}
		
	
		}
	
        
				
		int traceNumber = traceIDToActivityList.size();
		
		//activity set of the log
		HashSet<String> activitySetLog = new HashSet<>();
		for(String traceID: traceIDToActivityList.keySet())
		{
			activitySetLog.addAll(traceIDToActivityList.get(traceID));
		}
	   
		//direct-follow relation set of the log
		HashSet<String> dfrListLog = new HashSet<>();
		for(String traceID: traceIDToDFRList.keySet())
		{
			dfrListLog.addAll(traceIDToDFRList.get(traceID));
		}
	
		
		
		//后面初始化两个HashMap来存储活动和直接后继关系的重要性： 分别是activity2Sig dfr2Sig 
		//然后计算活动重要性和直接关系重要性
		//activity to significance
		HashMap<String, Double> activity2Sig = new HashMap<>();
		for(String act: activitySetLog)
		{
			//count the number of traces that contains act
			int count =0;
			Collection<XTrace> trace3 = TraceID2Trace.values();
			for(XTrace trace: trace3)
			{
				for(int i =0;i<trace.size();i++)
				{
					if(act.equals(XConceptExtension.instance().extractName(trace.get(i))))
					{
						count++;
						break;
					}
				}
			}
			  System.out.println(count + " " + traceNumber);
			activity2Sig.put(act,(double) count/traceNumber);
		}

	
		
		  // 获取 HashMap 的值的集合
     
		
		//direct-follow relation to significance. the number of traces contain the drf divided by the total number of traces. 
		HashMap<String, Double> dfr2Sig = new HashMap<>();
		for(String dfr: dfrListLog)
		{
			//count the number of traces that contains dfr
			int count =0;
			Collection<XTrace> trace3 = TraceID2Trace.values();
			for(XTrace trace: trace3)
			{
				for(int i =0;i<trace.size()-1;i++)
				{
					if(dfr.equals(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1))))
					{
						count++;
						break;
					}
				}
			}
			dfr2Sig.put(dfr, (double) count/traceNumber);

		}
		
		System.out.println("成功获取轨迹变体DFR重要性 " );

		
		
		
		//trace to significance	
		HashMap<String, Double> traceToSignificance = new HashMap<>();
		for(String traceID: TraceID2Trace.keySet())
		{

			int ActNumber= traceIDToActivityList.get(traceID).size();
			double ActSigSum = 0;
			
			for(String act: traceIDToActivityList.get(traceID))
			{
				ActSigSum=ActSigSum+activity2Sig.get(act);

			}
			// the average activity significance of current trace
			double AverageActSig = ActSigSum/ActNumber;

			
			
			//dfrs significance
			// number of dfr in this trace
			int DFRNumber= ActNumber-1;
			double DFRSigSum =0;

			for(String dfr: traceIDToDFRList.get(traceID))
			{

				DFRSigSum=DFRSigSum+dfr2Sig.get(dfr);

			}
			// the average dfr significance of current trace
			double AverageDFRSig = DFRSigSum/DFRNumber;
		

			
			traceToSignificance.put(traceID, 1-(AverageActSig+AverageDFRSig)/2);//1- the insignificance... used for ordering 
		}

	
		
		
		
		//select the top n traces. 
		int topN=(int)Math.round(samplingRatio*originalLog.size());
		System.out.println("Sample Size: "+ topN);
		
		//order traces based on the weight
		HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceToSignificance,topN);
		
		System.out.println("Sample Trace Names: "+sampleTraceNameSet);
		//construct the sample log based on the selected top n traces. 
		for(XTrace trace: originalLog)
		{
			if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
			{
				sampleLog.add(trace);
			}
		}
		
		//return the sample log. 
		return sampleLog;	
	}
}
