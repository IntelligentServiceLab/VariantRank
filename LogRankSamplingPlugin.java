package sampling.logrank;
/*
 * This is the implementation of LogRank plugin. 
 * It aims to sample an input large-scale example log and returns a small sample log using PageRank algorithm. 
 * Step 1: each trace is transformed to a featured vertor, we implement multiple approaches to the transformation.
 * Step 2: we compute the similarity for each two traces (vectors) using the cosine similarity metric. 
 * Step 3: using text ranking /PageRank algorithm to get a sub-set of representative event logs. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
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
import org.jblas.util.*;
import org.jblas.benchmark.*;
import org.jblas.exceptions.*;
import org.jblas.ranges.*;
import equalscale.SampleMethods.SortingHashMapByValues;
import kmean.ConvertTraceToVector;

@Plugin(
		name = "LogRank-based Event Log Sampling",// plugin name
		
		returnLabels = {"An Event Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log using the socalled LogRank model." 
		)
public class LogRankSamplingPlugin {
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
	public static XLog LogRankSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
			
//		//create a new log with the same log-level attributes. 
//		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
				
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="Normal Event Log";
		logType[1]="Lifecycle Event Log";
		String selectedType =ProMUIHelper.queryForObject(context, "Select the type of event log for sampling", logType);
		context.log("The selected log type is: "+selectedType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedType);
				
		
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
			
		return LogRankSamplingTechnique(originalLog,  selectedType, samplingRatio);
//		
//		
//		ConvertTraceToVector ctv = new ConvertTraceToVector();
//
//		//keep an ordered list of traces names. 
//		ArrayList<String> TraceIdList = new ArrayList<>();
//		
//		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
//		HashMap<String, XTrace> nameToTrace = new HashMap<>();
//		for(XTrace trace: originalLog)
//		{
//			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
//			nameToTrace.put(trace.getAttributes().get("concept:name").toString(), trace);
//		}
//		
//		// the similarity matrix of the log
//		double[][] matrix = new double[TraceIdList.size()][TraceIdList.size()];
//		if(selectedType=="Normal Event Log")// different trace 2 feature mapping
//		{
//			for(int i=0;i<TraceIdList.size();i++)
//			{
//				for(int j =0;j<TraceIdList.size();j++)
//				{
//					//get the trace similarity	
//					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(i))),
//							ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(j))));
//				}
//			}
//		}
//		else{
//			for(int i=0;i<TraceIdList.size();i++)
//			{
//				for(int j =0;j<TraceIdList.size();j++)
//				{
//					//get the trace similarity	
//					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(i))),
//							ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(j))));
//				}
//			}
//		}
//		
//		//use the jblas-1.2.4.jar for matrix computation 
//		// 0.3 is not used at the current stage. 
//		PageRankSampling prs = new PageRankSampling(matrix, 0.3);
//		prs.GenerateMarkovTransitionMatrix();
//		prs.CalculatePageRank(0.85, 100, 1e-8);
//
//		double[] pr = prs.OutputPageRank();
//		for (int i = 0; i < pr.length; i++) {
//			System.out.println(pr[i]);
//		}
//		
//		
//		//create a mapping from TraceID to the pagerank resutls. 
//		HashMap<String, Double> traceTovalues = new HashMap();
//		for(int i =0;i<TraceIdList.size();i++)
//		{
//			traceTovalues.put(TraceIdList.get(i), pr[i]);
//		}
//		    
//		//select the top n traces. 
//		int topN=(int)Math.round(samplingRatio*originalLog.size());
//		System.out.println("Sample Size: "+ topN);
//		
//		//order traces based on the weight
//		HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceTovalues,topN);
//		
//		System.out.println("Sample Trace Names: "+sampleTraceNameSet);
//		
//		//construct the sample log based on the selected top n traces. 
//		for(XTrace trace: originalLog)
//		{
//			if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
//			{
//				sampleLog.add(trace);
//			}
//		}
//		
//		//return the sample log. 
//		XConceptExtension.instance().assignName(sampleLog, "Sample Log");
//		return sampleLog;	
		
		
		
		
//		//output the similarity matrix as a text file
//		BufferedWriter bw = null;
//		try {
//			//Specify the file name and path here
//			
//			File file = new File("D:/My Papers/Sampling Matrix/"+originalLog.getAttributes().get("concept:name").toString()+".txt");
//			
//			// This logic will make sure that the file gets created if it is not present at the specified location
//			if(!file.exists()) 
//			{
//			  file.createNewFile();
//			}
//			FileWriter fw = new FileWriter(file);
//			bw = new BufferedWriter(fw);
//			
//			//output the activity
//			for(int i =0;i<TraceIdList.size();i++)
//			{
//				bw.write("\t"+TraceIdList.get(i));
//			}
//			
//			for(int i = 0;i<TraceIdList.size();i++)
//			{
//				bw.newLine();
//				bw.write(TraceIdList.get(i));
//				for(int j =0;j<TraceIdList.size();j++)
//				{
//					bw.write("\t"+matrix[i][j]);
//				}
//			}
//			if(bw!=null)
//			bw.close();
//		} catch (IOException ioe) {
//			   ioe.printStackTrace();
//			}
		
	}	 
	
	
	public static XLog LogRankSamplingTechnique(XLog originalLog, String selectedType, double samplingRatio)
	{
		//create a new log with the same log-level attributes. 
		XLog sampleLog = new XLogImpl(originalLog.getAttributes());
		ConvertTraceToVector ctv = new ConvertTraceToVector();

		//keep an ordered list of traces names. 
		ArrayList<String> TraceIdList = new ArrayList<>();
		
		//convert the log to a map, the key is the name of the trace, and the value is the trace. 
		HashMap<String, XTrace> nameToTrace = new HashMap<>();
		for(XTrace trace: originalLog)
		{
			TraceIdList.add(trace.getAttributes().get("concept:name").toString());
			nameToTrace.put(trace.getAttributes().get("concept:name").toString(), trace);
		}
		
		// the similarity matrix of the log
		double[][] matrix = new double[TraceIdList.size()][TraceIdList.size()];
		if(selectedType=="Normal Event Log")// different trace 2 feature mapping
		{
			for(int i=0;i<TraceIdList.size();i++)
			{
				for(int j =0;j<TraceIdList.size();j++)
				{
					//get the trace similarity	
					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(i))),
							ctv.Trace2FeatureMap(nameToTrace.get(TraceIdList.get(j))));
				}
			}
		}
		else{
			for(int i=0;i<TraceIdList.size();i++)
			{
				for(int j =0;j<TraceIdList.size();j++)
				{
					//get the trace similarity	
					matrix[i][j]=ctv.CosineSimilarity(ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(i))),
							ctv.Trace2FeatureMapStartComplete(nameToTrace.get(TraceIdList.get(j))));
				}
			}
		}
		
		//use the jblas-1.2.4.jar for matrix computation 
		// 0.3 is not used at the current stage. 
		PageRankSampling prs = new PageRankSampling(matrix, 0.3);
		prs.GenerateMarkovTransitionMatrix();
		prs.CalculatePageRank(0.85, 100, 1e-8);

		double[] pr = prs.OutputPageRank();
		for (int i = 0; i < pr.length; i++) {
			System.out.println(pr[i]);
		}
		
		int count=0;
		//create a mapping from TraceID to the pagerank results. 
		HashMap<String, Double> traceTovalues = new HashMap();
		for(int i =0;i<TraceIdList.size();i++)
		{
			traceTovalues.put(TraceIdList.get(i), pr[i]);
		}
		    
		//select the top n traces. 
		int topN=(int)Math.round(samplingRatio*originalLog.size());
		System.out.println("Sample Size: "+ topN);
		
		//order traces based on the weight
		HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceTovalues,topN);
		
		System.out.println("Sample Trace Names: "+sampleTraceNameSet);
	
		//construct the sample log based on the selected top n traces. 
		for(XTrace trace: originalLog)
		{
			if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
			{
				
				sampleLog.add(trace);
				count++;
				System.out.println(count);
			}
		}
		
		//return the sample log. 
		XConceptExtension.instance().assignName(sampleLog, "Sample Log");
		return sampleLog;	
	}
	
}
