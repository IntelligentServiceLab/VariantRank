package sampling.logrank;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jblas.DoubleMatrix;

public class PageRankSampling {
	private double[][] data;
	private int length;

	private double ratio;

	private DoubleMatrix MarkovTransitionMatrix;
	private DoubleMatrix PageRankValues;

	private double[] PageRankArray;
	private ArrayList<Integer> SelectedIndices;

	public PageRankSampling(double[][] data, double ratio) {
		this.data = data;
		this.length = data.length;
		this.ratio = ratio;

		this.MarkovTransitionMatrix = DoubleMatrix.zeros(this.length, this.length);
		this.PageRankValues = DoubleMatrix.ones(this.length, 1).div(this.length);

		this.PageRankArray = new double[this.length];
		this.SelectedIndices = new ArrayList<Integer>();
	}

	public void GenerateMarkovTransitionMatrix() {
		DoubleMatrix NormalizedMatrix = new DoubleMatrix(this.data);
		DoubleMatrix NormalizedVector = DoubleMatrix.ones(1, this.length).div(this.length);

		DoubleMatrix SumOfRows = MarkovTransitionMatrix.rowSums();

		for (int i = 0; i < this.length; i++) {
			if (SumOfRows.get(i, 0) < 1e-10) {
				this.MarkovTransitionMatrix.putRow(i, NormalizedVector);
			} else {
				this.MarkovTransitionMatrix.putRow(i, NormalizedMatrix.getRow(i).div(SumOfRows.get(i)));
			}
		}
		this.MarkovTransitionMatrix = NormalizedMatrix.transpose();
	}

	public void CalculatePageRank(double alpha, int maxiter, double err) {
		// DoubleMatrix PageRankValues = DoubleMatrix.ones(1,
		// this.length).div(this.length);
		DoubleMatrix PreviousPageRankValues = this.PageRankValues.dup();

		double difference = 1e5;
		DoubleMatrix TeleportMatrix = DoubleMatrix.ones(1, this.length).mul(1 - alpha).div(this.length);

		int iter = 0;
		while (iter < maxiter && difference > err) {
			iter++;

			this.PageRankValues = this.MarkovTransitionMatrix.mmul(PreviousPageRankValues).mul(alpha)
					.add(TeleportMatrix);
			this.PageRankValues = this.PageRankValues.div(this.PageRankValues.columnSums());
			difference = this.PageRankValues.sub(PreviousPageRankValues).norm1();
			PreviousPageRankValues = this.PageRankValues.dup();

			System.out.println("Iteration " + iter + " " + difference);
		}

		this.PageRankArray = this.PageRankValues.toArray();
	}

	public double[] OutputPageRank() {
		return this.PageRankValues.toArray();
	}

	public void GenerateSummary() {
		int numberOfItem = (int) (this.length * ratio);
		int index = this.FindMinimalIndex();
		this.SelectedIndices.add(index);
		for (int i = 1; i < numberOfItem; i++) {
			index = this.FindMinimalIndex();
			this.SelectedIndices.add(index);
		}
	}

	public int FindMinimalIndex() {
		int index = 0;
		double minValue = 100;

		for (int i = 0; i < this.length; i++) {
			if (this.PageRankArray[i] < minValue) {
				minValue = this.PageRankArray[i];
				index = i;
			}
		}
		this.PageRankArray[index] = 1.0;
		return index;
	}

	public static void main(String[] args) {
		double[][] data = new double[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				data[i][j] = Math.random();
			}
		}

		
		PageRankSampling prs = new PageRankSampling(data, 0.3);
		prs.GenerateMarkovTransitionMatrix();
		prs.CalculatePageRank(0.85, 100, 1e-8);

		//before sorting
		double[] pr = prs.OutputPageRank();
//		for (int i = 0; i < pr.length; i++) {
//			System.out.println(pr[i]);
//		}
		

		ArrayList<String> TraceIdList = new ArrayList<>();
		TraceIdList.add("A");
		TraceIdList.add("B");
		TraceIdList.add("C");
		TraceIdList.add("D");
		TraceIdList.add("E");
		
		//create a mapping from TraceID to the pagerank resutls. 
		HashMap<String, Double> IDtoValue = new HashMap();
		for(int i =0;i<TraceIdList.size();i++)
		{
			IDtoValue.put(TraceIdList.get(i), pr[i]);
		}
		
		System.out.println("before sorting");
		System.out.print(IDtoValue);
		 // let's sort this map by values 
	    Map<String, Double> sorted =IDtoValue
	            .entrySet()
	            .stream()
	            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
	                    LinkedHashMap::new));
	    
	    System.out.println("after sorting");
	    System.out.print(sorted);
	}
}
