package vnSearchers;

import ga.Individual;
import ga.Population;
import ga.PopulationParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;
import constraints.ConstraintBase;
import data.parameters;

public class VNSManager {
	
	public List<ConstraintBase> constraints;
	private Random myRandom;
	
	public VNS[] VNSList;
	public Float[] VNSShares;
	public VNS selectedVNS;
	int[] VNSCount;
	ArrayList<Individual> selected;

	ArrayList<Individual> frontIndivs= new ArrayList<Individual>();
	ArrayList<Float> frontDistances= new ArrayList<Float>();
	ArrayList<Integer> tempIndivIndices= new ArrayList<Integer>();
	 
	public VNSManager(List<ConstraintBase> constr) {
		this.constraints=constr;
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
		
		VNSList= new VNS[3];
		VNSList[0]= new VNSSingleObjP(this.constraints);
		VNSList[1]= new VNSSingleObjR(this.constraints);
		VNSList[2]= new VNSMultipleObjPR(this.constraints);
		VNSShares= new Float[3];
		VNSCount= new int[3]; // Keeps count of each VNS applied
		VNSCount[0]= 0; VNSCount[1]= 0; VNSCount[2]= 0;
		
		selected= new ArrayList<Individual>();
	}
	
	public void applyVNS(int iteration, Population pop) {
		// Select individuals for VNS
		// Choose which VNS will be applied (Improve P / Improve R / Improve both)
		// Population P and R values should be up to date
		
//		float Pmin= pop.bestPIndividual.totalPenalty;
//		float Pmax= getWorstFeasibleP(pop); // Worst feasible individual is found
//		
//		float Rmin= pop.bestRIndividual.robustValueMin;
//		float Rmax= pop.worstRIndividual.robustValueMin;
//		
//		float distanceP;
//		float distanceR;
//		float shareP;
//		float shareR;
		
//		System.out.println("VNSearchers:");
//		System.out.println(VNSList[0].getClass().getSimpleName()+"\t"+ VNSList[1].getClass().getSimpleName()+"\t"+ VNSList[2].getClass().getSimpleName()+"\t");
//		System.out.println("Penalty"+"\t"+"maxP"+"\t"+ "minP"+ "\t"+ "Pdistance"+"\t"+"PShare"+"\t"+ "RobustnessVal"+"\t"+"maxR"+ "\t"+"minR"+ "\t"+"Rdistance"+"\t"+"RShare"+"\t"+"Selected searcher");

		// Select the individuals for VNS op:
		// First select the best P individual
		// Then select the best R individual
	
		// Create a list of individuals for VNS:
		// Selection rule: 
//		ArrayList<Individual> indivsForLS= selectIndivsForLS(pop);
		Individual ind= pop.individuals[pop.bestPIndIndex];
//		for (Individual ind: pop.individuals){
//		for (Individual ind: indivsForLS){
//			if (!ind.isFeasible)
//				continue; // VNS will not be applied to infeasible individuals
//			else{
//				// calculate the relative position of the current solution w.r.t. the objective boundaries for the current stage of the research
//				distanceP= 1- (ind.totalPenalty - Pmin)/ (Pmax - Pmin); // normalized distance between current P and maxP
//				distanceR= 1- (ind.robustValueMin - Rmin) / (Rmax - Rmin); // normalized distance between current R and maselectedVNSxR
//
//				if (distanceP == 0) shareP= 0;
//				else shareP= distanceP / (distanceP + distanceR); // penalty share
//				if (distanceR == 0) shareR= 0;
//				else shareR= distanceR / (distanceP + distanceR); // robustness share
//
//				// The shares of the local search operators on the roulette wheel:
//				VNSShares[0]= shareP / 2; // shareVNSP
//				VNSShares[1]= shareR / 2; // shareVNSR
//				VNSShares[2]= (shareP / 2) + (shareR / 2); // shareVNSPR

				this.selectedVNS= selectVNS();
//				System.out.println(ind.totalPenalty+"\t"+Pmax +"\t"+ Pmin+ "\t"+ distanceP+"\t"+ shareP+ "\t"+ ind.robustValueMin+"\t"+Rmax+ "\t"+ Rmin+"\t" +distanceR+"\t"+shareR+"\t"+ this.selectedVNS.getClass().getSimpleName() + "\t"+ VNSShares[2]);			
//			}
			this.selectedVNS.applyVNS(iteration, ind);
//		} // end individual for
		
//		System.out.println("Number of individuals: "+ pop.individuals.length);
//		System.out.println("VNS selection counts:...");
//		for(int i=0; i< VNSCount.length; i++)
//			System.out.println(VNSList[i].getClass()+ "\t"+ VNSCount[i]);
//		System.out.println();
	}

	private ArrayList<Individual> selectIndivsForLS(Population pop) {
		 /* Both individual “rank” and “distance” values are used. 
		  * For each front set (starting with rank 1) the individuals for LS is determined as:
			 Find the distance of each individual from its closest two neighbors. 
			 Take the sum of these two distances. 
			 Sort individuals in decreasing order of their distances. 
			 Take the best, the worst and the upper (front size/2)-2 individuals.
		*/
		
		selected.clear();		
		float min1; float min2; int min1Indiv;

		// Find the biggest rank in the pop:
		int biggestRNK= 0;
		for (int i= 0; i< pop.individuals.length; i++){
			if (pop.individuals[i].rank> biggestRNK)
				biggestRNK= pop.individuals[i].rank;
		} // end for
		
		int[] sortedIndividuals;
		float[] sortedTotalDistance;
		int tempI; float tempD;

		int RNK= 0;
		while(RNK<= biggestRNK){	
			RNK+= 1;
			frontIndivs.clear();
			frontDistances.clear();
			tempIndivIndices.clear();
			
			for (int i=0; i< pop.individuals.length; i++ )
				if (pop.individuals[i].rank== RNK){
					frontIndivs.add(pop.individuals[i]);
					tempIndivIndices.add(i);
				} // end if
			if (frontIndivs.size()==0) continue;
			
			// Now evaluate distance values in the current front:
			int indivIndex1;  int indivIndex2;
			for (int i1= 0; i1< tempIndivIndices.size();i1++){
				indivIndex1= tempIndivIndices.get(i1);

				min1= parameters.numCourses; 
				min1Indiv = parameters.UNUSED_EVENT; // initialized with this!
				// find the closest individual for this ind:
				for (int i2= 0; i2< tempIndivIndices.size();i2++){
					if (i1== i2) continue;
					indivIndex2= tempIndivIndices.get(i2);
					if (pop.indIndDiff[indivIndex1][indivIndex2] < min1){
						min1= pop.indIndDiff[indivIndex1][indivIndex2];
						min1Indiv= indivIndex2;
					} // end if
				} // end i2 for

				min2= parameters.numCourses; // maximum possible value
				// find the second closest individual for this ind:
				for (int i2= 0; i2< tempIndivIndices.size();i2++){
					if (i1== i2) continue;
					indivIndex2= tempIndivIndices.get(i2);
					if (indivIndex2== min1Indiv) continue;
					if (pop.indIndDiff[indivIndex1][indivIndex2] < min2){
						min2= pop.indIndDiff[indivIndex1][indivIndex2];
					} // end if
				} // end i2 for
				// Now the distance to the closest two individuals have been found. Take sum of these values:
				frontDistances.add(min1 + min2);
			} // end i1 for

			// Individual indices sorted in descending order of their distance values:
			// 0th index refers to the most different individual
			sortedIndividuals= new int[frontIndivs.size()];
			for (int i= 0; i< sortedIndividuals.length; i++)
				sortedIndividuals[i]= i;
			// Corresponding individual distances are kept in below:
			sortedTotalDistance= new float[frontIndivs.size()];
			for (int i= 0; i< sortedTotalDistance.length; i++)
				sortedTotalDistance[i]= frontDistances.get(i);
			
			for (int i= 0; i< frontIndivs.size()-1; i++){
				for (int j= i+1; j< frontIndivs.size(); j++){
					if (sortedTotalDistance[j] > sortedTotalDistance[i]){
						tempI= sortedIndividuals[i];
						sortedIndividuals[i]= sortedIndividuals[j];
						sortedIndividuals[j]= tempI;
						
						tempD= sortedTotalDistance[i];
						sortedTotalDistance[i]= sortedTotalDistance[j];
						sortedTotalDistance[j]= tempD;
					} // end if	
				} // end j for
			} // end i for
			
			selected.add(frontIndivs.get(sortedIndividuals[0]));	
			if (frontIndivs.size()> 1){
				selected.add(frontIndivs.get(sortedIndividuals[sortedIndividuals.length-1]));
			}
			if (frontIndivs.size()> 2){
				for (int i= 1; i< (frontIndivs.size()/2)-1; i++){
					selected.add(frontIndivs.get(sortedIndividuals[i]));
				}
			} // end if
			if (selected.size()>= PopulationParameters.populationSize/2)
				break;
		}; // end while
		
		return selected;
	}

	private VNS selectVNS() {
//		if (VNSShares[2] == 0){ // This means that shareP and shareR are both zero!
//			VNSCount[2]+= 1;
//			return VNSList[2]; // Improve both!
//		}
//		
//		float totalShare= 0;
//		float rand= this.myRandom.nextFloat(); // 0<=r<1
//		float randShare;
//
//		for (int i=0; i< VNSList.length; i++)
//			totalShare+= VNSShares[i];
//		randShare= rand * totalShare;
//		
//		// VNS shares are sorted in desc order
//		// Larger the share, the larger the selection probability
//		int[] shareIndexSorted= new int[3];
//		for (int i=0; i< shareIndexSorted.length; i++)
//			shareIndexSorted[i]= i;
//		int temp;
//		for (int i=0; i< shareIndexSorted.length; i++)
//			for (int j=i+1; j< shareIndexSorted.length; j++){
//				if (VNSShares[shareIndexSorted[j]]> VNSShares[shareIndexSorted[i]]){
//					temp= shareIndexSorted[i];
//					shareIndexSorted[i]= shareIndexSorted[j];
//					shareIndexSorted[j]= temp;
//				}
//			}
//		
//		float partialTotal= 0;
//		for (int i=0; i< VNSList.length; i++){
//			partialTotal+= VNSShares[shareIndexSorted[i]];
//			if (partialTotal >= randShare){
//				VNSCount[shareIndexSorted[i]]+= 1;
//				return VNSList[shareIndexSorted[i]];
//			} // end if
//		} // end i for
		return VNSList[0];  // this is added but will not be reached. Because the above if statement will be satisfied	
	}

	private float getWorstFeasibleP(Population pop) {
		int worstP = 0;
		int startIndex = 0;
		for (int i=0; i< pop.individuals.length; i++){
			if (pop.individuals[0].isFeasible){
				worstP= pop.individuals[i].totalPenalty;
				startIndex= i+1;
				break;
			} // end if
		} // end i for
		
		for (int i=startIndex; i < pop.individuals.length; i++){
			if (pop.individuals[i].isFeasible){
				if(pop.individuals[i].totalPenalty > worstP)		
					worstP= pop.individuals[i].totalPenalty;
			}
		}
		return worstP;
	}

}

