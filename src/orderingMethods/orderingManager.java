package orderingMethods;

import initializer.CP.CPIndInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;

public class orderingManager {

	public CPIndInitializer init;
	public Random myRand;
	public List<OrderingBase> orderings;
	
	public orderingManager(){
		myRand= new Random(RandomNumberGenerator.getNewSeed()); // The same random object will be used for the selection of strategy for all individuals
		orderings= new ArrayList<OrderingBase>();
		orderings.add(new LargestDegree()); //0
		orderings.add(new ColorDegree()); //1
		orderings.add(new SaturationDegreeTime()); //2
		orderings.add(new RandomOrdering()); //3
		
//		orderings.add(new SaturationDegreeRooms());  
	}
	
	// returns new ordering method
	public OrderingBase selectOrdering(){
		int strategyNo= this.myRand.nextInt(this.orderings.size()); // upper bound is excluded!
//		int strategyNo= this.myRand.nextInt(3); // 0-1-2 is selected randomly
		return this.orderings.get(2);
	}
}
