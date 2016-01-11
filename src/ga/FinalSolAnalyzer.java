package ga;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import constraints.ConstraintBase;

public class FinalSolAnalyzer {
	public static List<String> finalSolutionAnalysis= new ArrayList<String>();
	
	public static void Analyze(List<ConstraintBase> constraints, Individual ind) throws IOException{
		for(ConstraintBase cstr: constraints){
			finalSolutionAnalysis.addAll(cstr.AnayzeFinalSol(ind.Data));
		}
		util.FileOperations.printFinalSolutionAnalysis(finalSolutionAnalysis);	
	}

}
