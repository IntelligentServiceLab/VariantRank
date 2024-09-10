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
		name = "VarientRank-Plugin",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "sampling via varientrank." 
		)
public class VarientRankPlugin {
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
		//初始化一个ArrayList来存储所有轨迹的ID：
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//将原始日志转换为一个HashMap，其中键是轨迹ID，值是对应的轨迹对象：
		HashMap<String, XTrace> TraceID2Trace = new HashMap<>();

		
		
		//初始化两个List来分别存储每个轨迹的活动集和直接后继关系集：		
		HashMap<String, ArrayList<String>> traceIDToActivityList = new HashMap<>();
		HashMap<String, ArrayList<String>> traceIDToDFRList = new HashMap<>();
		
		//遍历原始日志中的每个轨迹，提取活动和直接后继关系，并存储到对应的Listt中：
		//对于每条轨迹，代码创建了两个HashSet，分别用来存储轨迹中的活动（activitySet）和直接后继关系对（dfrSet）。通过嵌套的for循环，遍历轨迹中的每个活动，并提取活动名称和直接后继关系对。
		//轨迹变体 eventSet
		HashSet<ArrayList> eventSet = new HashSet<>();
		HashMap<String, XTrace> varienttrace = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			
			ArrayList<String> activityList = new ArrayList<>();
		
			ArrayList<String> dfrList = new ArrayList<>();
			ArrayList<String> eventList = new ArrayList<>();
			
			for(XEvent event:trace) {
				eventList.add(XConceptExtension.instance().extractName(event));
			}
			
			//正常日志
			if(selectedType=="Normal Event Log")// different trace 2 feature mapping
			{
				if(eventSet.add(eventList)) {
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
			}
			else{
				if(eventSet.add(eventList)) {
					for(int i =0;i<trace.size();i+=2)
					{
						//add activity

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
		
	
		}
		

		//轨迹变体数量
		int traceNumber = traceIDToActivityList.size();
	
		//将所有不重复活动放到activitySetLog中，方便后续计算重要性
		HashSet<String> activitySetLog = new HashSet<>();
		for(String traceID: traceIDToActivityList.keySet())
		{
			activitySetLog.addAll(traceIDToActivityList.get(traceID));
		}
	   
		//将所有DFR关系放到dfrListLog中，方便后续计算重要性
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
				  
				//构造一个包含原始日志所有traceid和 活动list的 hashmap   maplog
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
				//如果轨迹变体小于采样数量的情况
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
//						System.out.println(key+" "+eventset);
					}
					//计算每个轨迹变体在原日志中的数量
					HashMap<String,Integer> trace2number  = new HashMap<String,Integer>();
					for(XTrace trace4:originalLog)
					{
						//获取原日志轨迹的list
						List list3 = maplog.get(trace4.getAttributes().get("concept:name").toString());
						
						//与轨迹变体进行比较，相等就加一
						for(String key: sortedKeys)
						{
							List list4 = sampletraceset.get(key);
							//如果key已经在trace2number里就返回1 
							if(list4.equals(list3)) {
								if(trace2number.containsKey(key)) {
									trace2number.put(key,(trace2number.get(key)+1));
//									System.out.println(key+" "+trace2number.get(key));
									
								}
								else {
									trace2number.put(key,1);
//									System.out.println(key+" 1");
									
								}
								break;
							}
							
							
						}
						
					}	
					
					 for (String value5: trace2number.keySet()) {
				            System.out.println(value5+" "+trace2number.get(value5));
				           
				        }
					
					while(count<samplecount) {
						
						
						for (String traceid : sortedKeys) {
						   
							//采样数量
							Integer number = (int) Math.ceil(trace2number.get(traceid)*samplingRatio);
							 // 获取对应的 活动list
						    ArrayList value = sampletraceset.get(traceid);
						   
						    
						   
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
										
										maplog.remove(XConceptExtension.instance().extractName(logtrace));
										count++;
										number--;
//										System.out.println(XConceptExtension.instance().extractName(logtrace)+" count="+count+" 轨迹重要性="+traceToSignificance.get(traceid));
										
										logeventset.clear();
										if(number<=0)
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

				System.out.println(samplecount);	
				
		//return the sample log. 
		return sampleLog;	
	}
}
