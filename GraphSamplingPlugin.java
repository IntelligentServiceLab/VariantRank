package sampling.samplingwithguarantees;

/*
 * A fast algorithm to sample log. The original idea is referred to
 * "The automatic creation of literature abstracts".
 * 
 * rank a trace by its significance; the significance of a trace is determined
 * by the combination of its activity significance and dfr significance.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

public class GraphSamplingPlugin {
	//@Plugin(name = "Business Process Event Log Sampling Plugin", // plugin name
	@Plugin(name = "DFR-equivalence-based Event Log Sampling Technique", // plugin name
			returnLabels = { "Sample Log" }, //return labels
			returnTypes = { XLog.class }, //return class

			//input parameter labels, corresponding with the second parameter of main function
			parameterLabels = { "Large Event Log" },

			userAccessible = true,
			help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces.")
	@UITopiaVariant(affiliation = "TU/e", author = "Xuan Su", email = "15715325632@163.com")
	@PluginVariant(variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = { 0 })
	public static XLog SimRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException {
		String[] GraphSamplingTechnique = new String[9];
		GraphSamplingTechnique[0] = "Brute Force Sampling-";
		GraphSamplingTechnique[1] = "Brute Force Sampling+";
		GraphSamplingTechnique[2] = "Set Covering Sampling";
		GraphSamplingTechnique[3] = "Trace Length-based Sampling-";
		GraphSamplingTechnique[4] = "Trace Length-based Sampling+";
		GraphSamplingTechnique[5] = "Variant Occurrence Count-based Sampling-";
		GraphSamplingTechnique[6] = "Variant Occurrence Count-based Sampling+";

		String selectedTechnique = ProMUIHelper.queryForObject(context, "Select the sampling techniques",
				GraphSamplingTechnique);
		context.log("The selected graph sampling technique is: " + selectedTechnique, MessageLevel.NORMAL);
		System.out.println("Selected selected graph sampling technique is: " + selectedTechnique);

		//select two types of event logs, lifecycle event log and normal event log.  
		String[] logType = new String[2];
		logType[0] = "Normal Event Log";
		logType[1] = "Lifecycle Event Log";
		String selectedLogType = ProMUIHelper.queryForObject(context, "Select the type of event log for sampling",
				logType);
		context.log("The selected log type is: " + selectedLogType, MessageLevel.NORMAL);
		System.out.println("Selected log type is: " + selectedLogType);

		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();

		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> TraceID2Trace = new HashMap<>();
		for (XTrace trace : originalLog) {
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		//	System.out.println("///////"+TraceIdList+"//////////////");
		//	System.out.println("/////////TraceID2Trace//////////////////");
		for (int i = 0; i < TraceIdList.size(); i++) {
			System.out.println("TraceIdList" + i + ":" + TraceIdList.get(i));
			System.out.println(TraceID2Trace.get(TraceIdList.get(i)));
		}
		//TraceIdList.get(0)
		
		HashMap<String, HashSet<String>> traceIDToActivitySet = new HashMap<>();
		
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for (XTrace trace : originalLog) {
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			for (int i = 0; i < trace.size(); i++) {
				
				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			}
			for (int i = 0; i < trace.size() - 1; i++) {
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i)) + ","
						+ XConceptExtension.instance().extractName(trace.get(i + 1)));
			}
			traceIDToActivitySet.put(XConceptExtension.instance().extractName(trace), activitySet);
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
		System.out.print("traceIDToActivitySet=================>" + traceIDToActivitySet);
		
		HashSet<String> dfrSetLog = new HashSet<>();
		for (String traceID : traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		//add initial set and finally set
		HashSet<String> StartSet = new HashSet<>();
		HashSet<String> EndSet = new HashSet<>();
		for (XTrace trace : originalLog) {
			int num = 0;
			HashMap<Integer, String> word_list = new HashMap<>();
			for (XEvent event : trace) {
				num += 1;
				word_list.put(num, event.getAttributes().get("concept:name").toString());
			}
			StartSet.add(word_list.get(1));
			EndSet.add(word_list.get(trace.size()));
		}
		//System.out.println("StartSet==========="+StartSet);
		//System.out.println("EndSet================="+EndSet);

		/**
		 * 一、
		 * 
		 */
				if(selectedTechnique.equals("Brute Force Sampling-")) {
					for(int ijk=0;ijk<=4;ijk++) {
					double startTime_total=0;
					double endTime_total=0;
					startTime_total=System.currentTimeMillis();
					HashSet<String> StartSet1 = new HashSet<>();
					HashSet<String> EndSet1 = new HashSet<>();
					for(XTrace trace:originalLog)
					{   
						int count=0;
						HashMap<Integer, String> word_list1 = new HashMap<>(); 
						for(XEvent event: trace) {
							count+=1;
							word_list1.put(count,event.getAttributes().get("concept:name").toString());
						}
						StartSet1.add(word_list1.get(1));
						EndSet1.add(word_list1.get(trace.size()));
						if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
							int a=0;
							int b=0;
							int c=0;
							if(StartSet.remove(word_list1.get(1)))
							{
								a=1;
							}
							if(EndSet.remove(word_list1.get(trace.size())))
							{
								b=1;
							}
							if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
							{
								c=1;
							}
							if((a==1)||(b==1)||(c==1))
							{
								sampleLog.add(trace);
							}
						}
						}
					endTime_total=System.currentTimeMillis();
					System.out.println("***************");
					System.out.println("Brute Force Sampling- Time:"+(endTime_total-startTime_total));
					System.out.println("***************");
			         }
					
				}
		/**
		 * 一、
		 * 
		 */
		if (selectedTechnique.equals("Brute Force Sampling+")) {
				double startTime_total = 0;
				double endTime_total = 0;
				startTime_total = System.currentTimeMillis();
				HashSet<String> StartSet1 = new HashSet<>();
				HashSet<String> EndSet1 = new HashSet<>();
				HashSet<String> dfrSetLog1 = new HashSet<>();
				for (XTrace trace : originalLog) {
					int count = 0;
					HashMap<Integer, String> word_list1 = new HashMap<>();
					for (XEvent event : trace) {
						count += 1;
						word_list1.put(count, event.getAttributes().get("concept:name").toString());
					}
					StartSet1.add(word_list1.get(1));
					EndSet1.add(word_list1.get(trace.size()));
					int a = 0;
					int b = 0;
					int c = 0;
					if (StartSet.remove(word_list1.get(1))) {
						a = 1;
					}
					if (EndSet.remove(word_list1.get(trace.size()))) {
						b = 1;
					}
					if (dfrSetLog1.addAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace)))) {
						c = 1;
					}
					if ((a == 1) || (b == 1) || (c == 1)) {
						sampleLog.add(trace);
					}
				}
				endTime_total = System.currentTimeMillis();
				System.out.println("***************");
				System.out.println("Brute Force Sampling+ Time:" + (endTime_total - startTime_total));
				System.out.println("***************");
			
		}
		/**
		 * 二、
		 * 
		 */
		//		for(int i=0;i<TraceIdList.size();i++)
		//		{

		//			if(dfrSetLog.removeAll(traceIDToDFRSet.get(TraceIdList.get(i)))) {
		//				sampleLog.add(TraceID2Trace.get(TraceIdList.get(i)));
		//			}
		//			if(dfrSetLog.size()==0) break;
		//		}

		//		return sampleLog;	

		/**
		 * 三、
		 */

		if (selectedTechnique.equals("Set Covering Sampling")) {
				double startTime_total = 0;
				double endTime_total = 0;
				startTime_total = System.currentTimeMillis();
				
				ArrayList<String> selects = new ArrayList<String>();
				
				HashSet<String> tempSet = new HashSet<String>();
				
				HashSet<XTrace> traceSet = new HashSet<XTrace>();
				
				String maxKey = null;
				
				while (dfrSetLog.size() != 0) {
					
					maxKey = null;
					
					for (String key : traceIDToDFRSet.keySet()) {
						
						tempSet.clear();
						
						HashSet<String> areas = traceIDToDFRSet.get(key);
						tempSet.addAll(areas);
						
						tempSet.retainAll(dfrSetLog);
						
						if (tempSet.size() > 0
								&& (maxKey == null || tempSet.size() > traceIDToDFRSet.get(maxKey).size())) {
							maxKey = key;
						}
					}
					
					if (maxKey != null) {
						
						sampleLog.add(TraceID2Trace.get(maxKey));
						
						traceSet.add(TraceID2Trace.get(maxKey));
						selects.add(maxKey);
						
						dfrSetLog.removeAll(traceIDToDFRSet.get(maxKey));
					}
				}
				
				for (XTrace trace : traceSet) {
					String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
					String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size() - 1));
					StartSet.remove(StartEvent);
					EndSet.remove(EndEvent);
				}

				for (XTrace trace : originalLog) {
					if (traceSet.contains(trace)) {
						continue;
					}
					String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
					String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size() - 1));
					if ((StartSet.size() != 0) || (EndSet.size() != 0)) {

						int flag1 = 0;
						int flag2 = 0;
						//System.out.println("StartSet："+StartSet);
						//System.out.println("EndSet："+EndSet);
						//System.out.println("StartEvent:"+StartEvent);
						//System.out.println("EndEvent:"+EndEvent);
						if (StartSet.remove(StartEvent)) {
							flag1 = 1;
						}
						if (EndSet.remove(EndEvent)) {
							flag2 = 1;
						}
						if ((flag1 == 1) || (flag2 == 1)) {
							sampleLog.add(trace);
						}
						//System.out.println("StartSet.size():"+StartSet.size());
						//System.out.println("EndSet.size():"+EndSet.size());
						//System.out.println("dfrSetLog.size():"+dfrSetLog.size());
					}
				}
				// System.out.println(selects);	
				endTime_total = System.currentTimeMillis();
				System.out.println("***************");
				System.out.println("Inductive MIner Time:" + (endTime_total - startTime_total));
				System.out.println("***************");
			
		}

		/**
		 * 四、
		 */
		
		if (selectedTechnique.equals("Trace Length-based Sampling+")) {
				double startTime_total = 0;
				double endTime_total = 0;
				startTime_total = System.currentTimeMillis();
				
				HashMap<String, Integer> TraceLength = new HashMap<>();
				for (XTrace trace : originalLog) {
					int num = 0;
					for (XEvent event : trace) {
						num += 1;
					}
					TraceLength.put(trace.getAttributes().get("concept:name").toString(), num);
				}
				//System.out.println("TraceLength:---------------->"+TraceLength);
					
				ArrayList<String> SortedTraceLengthID = new ArrayList<>();
				List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(
						TraceLength.entrySet());
				
				Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return (o2.getValue() - o1.getValue());
					}
				});
				
				for (int i = 0; i < infoIds.size(); i++) {
					String id = infoIds.get(i).getKey();
					SortedTraceLengthID.add(id);
				}
				
				HashSet<String> dfrSetLog3 = new HashSet<>();
				for (int i = 0; i < SortedTraceLengthID.size(); i++) {
					int count = 0;
					HashMap<Integer, String> word_list1 = new HashMap<>();
					for (XEvent event : TraceID2Trace.get(SortedTraceLengthID.get(i))) {
						count += 1;
						word_list1.put(count, event.getAttributes().get("concept:name").toString());
					}
					int a = 0;
					int b = 0;
					int c = 0;
					if (StartSet.remove(word_list1.get(1))) {
						a = 1;
					}
					if (EndSet.remove(word_list1.get(TraceID2Trace.get(SortedTraceLengthID.get(i)).size()))) {
						b = 1;
					}
					if (dfrSetLog3.addAll(traceIDToDFRSet.get(SortedTraceLengthID.get(i)))) {
						c = 1;
					}
					if ((a == 1) || (b == 1) || (c == 1)) {
						sampleLog.add(TraceID2Trace.get(SortedTraceLengthID.get(i)));
					}
				}
				endTime_total = System.currentTimeMillis();
				System.out.println("***************");
				System.out.println("Inductive MIner Time:" + (endTime_total - startTime_total));
				System.out.println("***************");
			
		}

				/**
				 *  四、
				 */
				
				if(selectedTechnique.equals("Variant Occurrence Count-based Sampling-")) {
			        	double startTime_total=0;
						double endTime_total=0;
						startTime_total=System.currentTimeMillis();
		        	
		    		HashMap<String, Integer> TraceLength = new HashMap<>();
		    		for(XTrace trace:originalLog) {
		    			int num=0;
		    			for(XEvent event: trace) {
		    				num+=1;
		    			}
		    			TraceLength.put(trace.getAttributes().get("concept:name").toString(),num);
		    		}
		    		//System.out.println("TraceLength:---------------->"+TraceLength);
		    		
		           	
		    		ArrayList<String> SortedTraceLengthID = new ArrayList<>();
		    		List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(TraceLength.entrySet());
		    		
		    		
		    		Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
		    		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
		    		        return (o2.getValue() - o1.getValue()); 
		    		    }
		    		}); 
		    	
		    		for (int i = 0; i < infoIds.size(); i++) {
		    			//System.out.println("infoIds.get("+i+"):"+infoIds.get(i));
		    			//System.out.println("infoIds.get("+i+").getKey():"+infoIds.get(i).getKey());
		    		    String id = infoIds.get(i).getKey();
		    		    SortedTraceLengthID.add(id);
		    		  }
		    	    //System.out.println("****************SortedTraceLengthID:"+SortedTraceLengthID);
		    	   
		    	    for(int i=0;i<SortedTraceLengthID.size();i++) {
		    	    	int count=0;
						HashMap<Integer, String> word_list1 = new HashMap<>(); 
		    	    	for(XEvent event : TraceID2Trace.get(SortedTraceLengthID.get(i))) {
		    	    		count+=1;
							word_list1.put(count,event.getAttributes().get("concept:name").toString());
		    	    	}
		    	    	if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
							int a=0;
							int b=0;
							int c=0;
							if(StartSet.remove(word_list1.get(1)))
							{
								a=1;
							}
							if(EndSet.remove(word_list1.get(TraceID2Trace.get(SortedTraceLengthID.get(i)).size())))
							{
								b=1;
							}
				    	    if(dfrSetLog.removeAll(traceIDToDFRSet.get(SortedTraceLengthID.get(i)))) {
				    			c=1;
				    		}
				    	    if((a==1)||(b==1)||(c==1))
				    	    {
				    			sampleLog.add(TraceID2Trace.get(SortedTraceLengthID.get(i)));
				    	    }
				    	    
						}
						else
						{
							break;
						}
		    	     
		    }
		    	    endTime_total=System.currentTimeMillis();
			        System.out.println("***************");
			        System.out.println("Inductive MIner Time:"+(endTime_total-startTime_total));
			        System.out.println("***************");	}
		//		//old
		//        if(selectedTechnique.equals("Trace Length-based Graph Sampling")) {
		//        		
		//    		HashMap<XTrace, Integer> TraceLength = new HashMap<>();
		//    		for(XTrace trace:originalLog) {
		//    			int num=0;
		//    			for(XEvent event: trace) {
		//    				num+=1;
		//    			}
		//    			TraceLength.put(trace,num);
		//    		}
		//    		System.out.println("TraceLength:---------------->"+TraceLength);
		//    		
		//           	
		//    		ArrayList<XTrace> SortedTraceLengthID = new ArrayList<>();
		//    		List<Map.Entry<XTrace, Integer>> infoIds = new ArrayList<Map.Entry<XTrace, Integer>>(TraceLength.entrySet());
		//    		
		//    		
		//    		Collections.sort(infoIds, new Comparator<Map.Entry<XTrace, Integer>>() {   
		//    		    public int compare(Map.Entry<XTrace, Integer> o1, Map.Entry<XTrace, Integer> o2) {      
		//    		        return (o2.getValue() - o1.getValue()); 
		//    		    }
		//    		}); 
		//    		//排序后
		//    		for (int i = 0; i < infoIds.size(); i++) {
		//    			System.out.println("infoIds.get("+i+"):"+infoIds.get(i));
		//    			System.out.println("infoIds.get("+i+").getKey():"+infoIds.get(i).getKey());
		//    		    XTrace id = infoIds.get(i).getKey();
		//    		    SortedTraceLengthID.add(id);
		//    		  }
		//    	    System.out.println("****************SortedTraceLengthID:"+SortedTraceLengthID);
		//    	    
		//            
		//    	    for(int i=0;i<SortedTraceLengthID.size();i++) {
		//    	    	int count=0;
		//				HashMap<Integer, String> word_list1 = new HashMap<>(); 
		//    	    	for(XEvent event : SortedTraceLengthID.get(i)) {
		//    	    		count+=1;
		//					word_list1.put(count,event.getAttributes().get("concept:name").toString());
		//    	    	}
		//    	    	if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
		//					int a=0;
		//					int b=0;
		//					int c=0;
		//					if(StartSet.remove(word_list1.get(1)))
		//					{
		//						a=1;
		//					}
		//					if(EndSet.remove(word_list1.get(SortedTraceLengthID.get(i).size())))
		//					{
		//						b=1;
		//					}
		//		    	    if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(SortedTraceLengthID.get(i))))) {
		//		    			c=1;
		//		    		}
		//		    	    if((a==1)||(b==1)||(c==1))
		//		    	    {
		//		    			sampleLog.add(SortedTraceLengthID.get(i));
		//		    	    }
		//		    	    
		//				}
		//				else
		//				{
		//					break;
		//				}
		//    	    }
		//    	    for(int i=0;i<SortedTraceLengthID.size();i++)
		//    	    {
		//    	    	if(dfrSetLog.removeAll(traceIDToDFRSet.get(SortedTraceLengthID.get(i)))) {
		//    				sampleLog.add(TraceID2Trace.get(SortedTraceLengthID.get(i)));
		//    			}
		//    			if(dfrSetLog.size()==0) break;
		//    	    System.out.println("SortedTraceLengthID.get(i):++++++++"+SortedTraceLengthID.get(i));
		//    	    }}

				/**
				 * 五、
				 */
		
		        if(selectedTechnique.equals("Trace Frequency-based Sampling-")) {
		        	for(int ijk=0;ijk<=4;ijk++) {
		        	double startTime_total=0;
					double endTime_total=0;
					startTime_total=System.currentTimeMillis();
					//System.out.println("startTime_total["+ijk+"]:"+startTime_total);
		        	
		    			HashMap<String, String> TraceString = new HashMap<>();
		    			for(XTrace trace:originalLog) {
		    				String transition="";
		    				String tracestr="";
		    				for(XEvent event: trace) {
		    					transition = event.getAttributes().get("concept:name").toString();
		    					tracestr = tracestr+transition;
		    				}
		    				TraceString.put(trace.getAttributes().get("concept:name").toString(),tracestr);
		    			}
		    			//System.out.println("TraceString:---------------->"+TraceString);
		
		    		
		    			HashMap<String, Integer> TraceRate = new HashMap<>();
		
		    			for(int i=0;i<TraceString.size();i++)
		    			{
		    				int count = 0; 
		    				for(int j=i;j<TraceString.size();j++)
		    				{
		    					String firstTrace = TraceString.get(TraceIdList.get(i));
		    					String secondTrace = TraceString.get(TraceIdList.get(j));
		    					if(firstTrace.equals(secondTrace))
		    					{
		    						count+=1;
		    					}
		    					else
		    					{
		    						break;
		    					}
		    					
		    				}
		    					TraceRate.put(TraceIdList.get(i), count);
		    			    
		    				//System.out.println("TraceIdList.get(i)---------->"+TraceIdList.get(i)+"----------->"+count);
		    			}
		    		
		    			HashSet<String> compareString=new HashSet<>();
		    			for(int i=0;i<TraceRate.size();i++)
		    			{
		    			compareString.add(TraceString.get(TraceIdList.get(i)));
		    			}
		    			//System.out.println("compareString---------->"+compareString);
		    			for(int i=0;i<TraceString.size();i++) {
		    				String m1=TraceString.get(TraceIdList.get(i));
		    				if(compareString.contains(m1)) {
		    					compareString.remove(m1);
		    				}else {
		    					TraceRate.remove(TraceIdList.get(i));
		    				}
		    			}
		    			//System.out.println("TraceRate---------->"+TraceRate);
		            			
		    			ArrayList<String> SortedTraceRateID = new ArrayList<>();
		    			List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(TraceRate.entrySet());
		    			
		    			Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
		    			    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
		    			        return (o2.getValue() - o1.getValue()); 
		    			    }
		    			}); 
		    			
		    			for (int i = 0; i < infoIds.size(); i++) {
		    			    String id = infoIds.get(i).getKey();
		    			    SortedTraceRateID.add(id);
		    			  }
		    	       
						for(int i=0;i<SortedTraceRateID.size();i++) {
		    	    	int count=0;
						HashMap<Integer, String> word_list1 = new HashMap<>(); 
		    	    	for(XEvent event : TraceID2Trace.get(SortedTraceRateID.get(i))) {
		    	    		count+=1;
							word_list1.put(count,event.getAttributes().get("concept:name").toString());
		    	    	}
		    	    	if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
							int a=0;
							int b=0;
							int c=0;
							if(StartSet.remove(word_list1.get(1)))
							{
								a=1;
							}
							if(EndSet.remove(word_list1.get(TraceID2Trace.get(SortedTraceRateID.get(i)).size())))
							{
								b=1;
							}
				    	    if(dfrSetLog.removeAll(traceIDToDFRSet.get(SortedTraceRateID.get(i)))) {
				    			c=1;
				    		}
				    	    if((a==1)||(b==1)||(c==1))
				    	    {
				    			sampleLog.add(TraceID2Trace.get(SortedTraceRateID.get(i)));
				    	    }
				    	    
						}
						else
						{
							break;
						}
		    	    }
						endTime_total=System.currentTimeMillis();
						//System.out.println("endTime_total["+ijk+"]:"+endTime_total);
						System.out.println("***************");
						System.out.println("Inductive MIner Time["+ijk+"]:"+(endTime_total-startTime_total));
						System.out.println("***************");	 
		        }
		        }
		/**
		 * 五、
		 */
		if (selectedTechnique.equals("Variant Occurrence Count-based Sampling+")) {
				double startTime_total = 0;
				double endTime_total = 0;
				startTime_total = System.currentTimeMillis();
				//step1：
				
				HashMap<String, String> TraceString = new HashMap<>();
				for (XTrace trace : originalLog) {
					String transition = "";
					String tracestr = "";
					for (XEvent event : trace) {
						transition = event.getAttributes().get("concept:name").toString();
						tracestr = tracestr + transition;
					}
					TraceString.put(trace.getAttributes().get("concept:name").toString(), tracestr);
				}
			
				HashMap<String, Integer> TraceRate = new HashMap<>();
				for (int i = 0; i < TraceString.size(); i++) {
					int count = 0;
					for (int j = i; j < TraceString.size(); j++) {
						String firstTrace = TraceString.get(TraceIdList.get(i));
						String secondTrace = TraceString.get(TraceIdList.get(j));
						if (firstTrace.equals(secondTrace)) {
							count += 1;
						} else {
							break;
						}
					}
					TraceRate.put(TraceIdList.get(i), count);
				}
				
				HashSet<String> compareString = new HashSet<>();
				for (int i = 0; i < TraceRate.size(); i++) {
					compareString.add(TraceString.get(TraceIdList.get(i)));
				}
				for (int i = 0; i < TraceString.size(); i++) {
					String m1 = TraceString.get(TraceIdList.get(i));
					if (compareString.contains(m1)) {
						compareString.remove(m1);
					} else {
						TraceRate.remove(TraceIdList.get(i));
					}
				}
			
				ArrayList<String> SortedTraceRateID = new ArrayList<>();
				List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(
						TraceRate.entrySet());
				
				Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return (o2.getValue() - o1.getValue());
					}
				});
				
				for (int i = 0; i < infoIds.size(); i++) {
					String id = infoIds.get(i).getKey();
					SortedTraceRateID.add(id);
				}
				System.out.println("**********-------->");
				System.out.println("SortedTraceRateID**********-------->"+SortedTraceRateID);
				System.out.println("**********-------->");
				
				HashSet<String> dfrSetLog4 = new HashSet<>();
				for (int i = 0; i < SortedTraceRateID.size(); i++) {
					int count = 0;
					HashMap<Integer, String> word_list1 = new HashMap<>();
					for (XEvent event : TraceID2Trace.get(SortedTraceRateID.get(i))) {
						count += 1;
						word_list1.put(count, event.getAttributes().get("concept:name").toString());
					}
					int a = 0;
					int b = 0;
					int c = 0;
					if (StartSet.remove(word_list1.get(1))) {
						a = 1;
					}
					if (EndSet.remove(word_list1.get(TraceID2Trace.get(SortedTraceRateID.get(i)).size()))) {
						b = 1;
					}
					if (dfrSetLog4.addAll(traceIDToDFRSet.get(SortedTraceRateID.get(i)))) {
						c = 1;
					}
					if ((a == 1) || (b == 1) || (c == 1)) {
						sampleLog.add(TraceID2Trace.get(SortedTraceRateID.get(i)));
					}
				}
				endTime_total = System.currentTimeMillis();
				System.out.println("***************");
				System.out.println("Inductive MIner Time:" + (endTime_total - startTime_total));
				System.out.println("***************");
			
		}

		return sampleLog;
	}

}
