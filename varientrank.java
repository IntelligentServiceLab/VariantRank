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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XEvent;
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
		name = "varientrank-plugin",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "get sample by varient-trace." 
		)
public class varientrank {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Jiawei chen", 
	        email = "jiaweichen0031@gmail.com"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog SimRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		
		
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		
		return SimRankSamplingTechnique(originalLog, samplingRatio);
	}
	
	public static XLog SimRankSamplingTechnique(XLog originalLog, double samplingRatio)
	{
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		//初始化一个ArrayList来存储所有轨迹的ID：
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//将原始日志转换为一个HashMap，其中键是轨迹ID，值是对应的轨迹对象：
		HashMap<String, XTrace> TraceID2Trace = new HashMap<>();
//		for(XTrace trace: originalLog)
//		{
//			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
//			TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
//		}
		//这里需要TraceID2Trace变成轨迹变体
		//构建轨迹变体日志
		
		
		
		//初始化两个HashMap来分别存储每个轨迹的活动集和直接后继关系集：		
		//trace to activity set
		HashMap<String, HashSet<String>> traceIDToActivitySet = new HashMap<>();
		
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		
		
		//遍历原始日志中的每个轨迹，提取活动和直接后继关系，并存储到对应的HashSet中：
		//对于每条轨迹，代码创建了两个HashSet，分别用来存储轨迹中的活动（activitySet）和直接后继关系对（dfrSet）。通过嵌套的for循环，遍历轨迹中的每个活动，并提取活动名称和直接后继关系对。
		//轨迹变体 eventSet
		HashSet<ArrayList> eventSet = new HashSet<>();
		HashMap<String, XTrace> varienttrace = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			ArrayList<String> eventList = new ArrayList<>();
			
			for(XEvent event:trace) {
				eventList.add(XConceptExtension.instance().extractName(event));
			}
			
			
			if(eventSet.add(eventList)) {
				for(int i =0;i<trace.size();i++)
				{
					//add activity
				
					activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
					
					
				}
				for(int i =0;i<trace.size()-1;i++)
				{
				
					//add directly follow pair
					dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
					
					
				}
				traceIDToActivitySet.put(XConceptExtension.instance().extractName(trace), activitySet);
				traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
				TraceIdList.add(trace.getAttributes().get("concept:name").toString());
				TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
				
			}
			
		}
		
	
		
	    

		
		
		
//		for(XTrace trace:originalLog)
//		{
//			System.out.print("轨迹"+XConceptExtension.instance().extractName(trace)+" 包含活动 ");
//			
//			for(XEvent event:trace) {
//				System.out.print(XConceptExtension.instance().extractName(event)+" ");
//			}
//			System.out.println();
//		}

		
		
	
		
		
		
		//计算日志中活动和直接后继关系的总数，以便后续计算重要性：
		//the number of traces
		int traceNumber = traceIDToActivitySet.size();
		
		//activity set of the log
		HashSet<String> activitySetLog = new HashSet<>();
		for(String traceID: traceIDToActivitySet.keySet())
		{
			activitySetLog.addAll(traceIDToActivitySet.get(traceID));
		}
	   
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
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
		for(String dfr: dfrSetLog)
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
		
		Collection<Double> values4 = dfr2Sig.values();
		 for (String value: dfr2Sig.keySet()) {
	            System.out.println(value+" ");
	        }
	        // 使用迭代器遍历集合
	        for (Double value : values4) {
	            System.out.println(value+" ");
	        }
		
		
		
		
		//trace to significance	
		HashMap<String, Double> traceToSignificance = new HashMap<>();
		for(String traceID: TraceID2Trace.keySet())
		{
			//activities significance
			// number of activities in this trace
			int ActNumber= traceIDToActivitySet.get(traceID).size();
			double ActSigSum = 0;
			for(String act: traceIDToActivitySet.get(traceID))
			{
				ActSigSum=ActSigSum+activity2Sig.get(act);
			}
			// the average activity significance of current trace
			double AverageActSig = ActSigSum/ActNumber;
			
			//dfrs significance
			// number of dfr in this trace
			int DFRNumber= ActNumber-1;
			double DFRSigSum =0;
			for(String dfr: traceIDToDFRSet.get(traceID))
			{
				DFRSigSum=DFRSigSum+dfr2Sig.get(dfr);
			}
			// the average dfr significance of current trace
			double AverageDFRSig = DFRSigSum/DFRNumber;
			
			//set the significance of trace as: the average of activity and dfr significance
			
			traceToSignificance.put(traceID, (AverageActSig+AverageDFRSig)/2);//1- the insignificance... used for ordering 
		}
	
		
		
	       
	        
		
	        
	        
	        
		//得到了traceToSignificance 键是轨迹变体的traceid value是轨迹重要性
		//下面进行采样
		//首先将原始日志转换为一个HashMap log，其中键是轨迹ID，值是对应的轨迹对象：
				HashMap<String, XTrace> log = new HashMap<>();
				for(XTrace trace: originalLog)
				{
					
					log.put(trace.getAttributes().get("concept:name").toString(), trace);
				}
				
				HashMap<String, ArrayList> maplog = new HashMap<>();
				//构造一个包含原始日志所有traceid和 活动list的 hashmap
				for(XTrace trace:originalLog) {
					ArrayList<String> logeventset = new ArrayList<String>();
					for(XEvent event:trace) {
						logeventset.add(XConceptExtension.instance().extractName(event));
				}
					maplog.put(trace.getAttributes().get("concept:name").toString(), logeventset);
				}
		
		//接下来根据轨迹变体traceToSignificance对TraceID2Trace2进行采样。
				//采样数量
				int samplecount=(int)Math.round(samplingRatio*originalLog.size());
				//获得排序后的traceid list
				List<String> sortedKeys = traceToSignificance.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed()) // 降序排序
                        .map(Map.Entry::getKey) // 提取键
                        .collect(Collectors.toList());
			
				//如果采样数量小于等于轨迹变体数量就直接取轨迹变体前n个
				if(traceToSignificance.size()>=samplecount) {
//					HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceToSignificance,samplecount);
//					System.out.println("Sample Trace Names: "+sampleTraceNameSet);
//					//construct the sample log based on the selected top n traces. 
//					for(XTrace trace: originalLog)
//					{
//						if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
//						{
//							sampleLog.add(trace);
//						}
//	               }
				
					
					for (int i = 0; i < samplecount; i++) {
					    String key = sortedKeys.get(i);
					    for(XTrace trace: originalLog)
						{
							if(key.equals(trace.getAttributes().get("concept:name").toString()))
							{
								sampleLog.add(trace);
							}
						}
					}
					 System.out.println("策略1");
					 return sampleLog;	
				}
				else {
					ArrayList logeventset = new ArrayList();
					int count = 0;
				
					
					System.out.println("Sample Trace Names: ");
					//sampletraceset键是traceid 值是一个hashset里面包含了他的活动
					HashMap<String,ArrayList> sampletraceset = new HashMap<String,ArrayList>();
					for(String key: sortedKeys)
					{
//						获得轨迹变体中traceid对应的trace
						XTrace trace = TraceID2Trace.get(key);
						//把trace变成事件集eventset
						
						
						ArrayList<String> eventset = new ArrayList<>();
						for(XEvent event:trace) {
						eventset.add(XConceptExtension.instance().extractName(event));
						
						}
						sampletraceset.put(key, eventset);
						System.out.println(key+" "+eventset);
					}
						
					
					while(count<samplecount) {
						
						
						for (String traceid : sortedKeys) {
						   
						    ArrayList value = sampletraceset.get(traceid); // 获取对应的 HashSet
						   
						    
						   
						    // 迭代当前键对应的 HashSet
						    
						
							
							for(XTrace logtrace: originalLog)
							{
//								ArrayList<String> logeventset = new ArrayList<String>();
//									for(XEvent event:logtrace) {
//										logeventset.add(XConceptExtension.instance().extractName(event));
//								}
								logeventset = maplog.get(XConceptExtension.instance().extractName(logtrace));
									if(value.equals(logeventset)  ) {
										
										sampleLog.add(logtrace);
										
										maplog.remove(logtrace.getAttributes().get("concept:name").toString());
										count++;
										
										System.out.println(XConceptExtension.instance().extractName(logtrace)+" count="+count);
										
										logeventset.clear();
										break;
									}
									if(count>=samplecount)
										break;
							}
							
							if(count>=samplecount)
								break;
							
						}
						if(count>=samplecount)
							break;
					
					}
					
					 System.out.println("策略2");
				}
//				for(XTrace trace:originalLog)
//				{
//					System.out.print("轨迹"+XConceptExtension.instance().extractName(trace)+" 包含活动 ");
//					
//					for(XEvent event:trace) {
//						System.out.print(XConceptExtension.instance().extractName(event)+" ");
//					}
//					System.out.println();
//				}
		
				System.out.println(samplecount);	
				
		//return the sample log. 
		return sampleLog;	
	}
}
