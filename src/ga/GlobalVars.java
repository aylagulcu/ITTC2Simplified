package ga;

import java.util.ArrayList;
import java.util.List;

public class GlobalVars {
	public static int runCount;
	
	public static List<String> runDetails= new ArrayList<String>(); // Penalty value of each individual
	
	
	public static List<int[]> popPenaltyStats= new ArrayList<int[]>(); // Best P+ Avg P + Worst P
	public static List<int[]> popPenaltyValues= new ArrayList<int[]>(); // Penalty value of each individual
	
//	public static List<int[]> popDiversityValues= new ArrayList<int[]>();
	public static List<Float> popAvgDiversity= new ArrayList<Float>();
	
	public static int iterCounterWithNoPenaltyImprovement;
	public static int iterCounterWithNoRobustnessImprovement;
	
	public static boolean improvedPrevious;

	public static List<Double> popRobustnessStats= new ArrayList<Double>(); // Best R
	public static List<double[]> popRobustnessValues= new ArrayList<double[]>(); // Robustness value of each individual
	public static List<double[]> popSecondRobustnessValues= new ArrayList<double[]>(); 
	
	public static double initialPopAvgEventP;
	
	public static void initialize(){

		// runDetails= new ArrayList<String>();
		
		popPenaltyStats= new ArrayList<int[]>(); // Best P+ Avg P + Worst P
		popPenaltyValues= new ArrayList<int[]>(); // Penalty value of each individual
		
		popAvgDiversity= new ArrayList<Float>();
		
		iterCounterWithNoPenaltyImprovement= 0;
		iterCounterWithNoRobustnessImprovement= 0;
		improvedPrevious= true;

		popRobustnessStats= new ArrayList<Double>(); // Best R
		popRobustnessValues= new ArrayList<double[]>(); // Robustness value of each individual
		popSecondRobustnessValues= new ArrayList<double[]>(); 
		
		initialPopAvgEventP= 0;

	}
}
