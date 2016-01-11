package orderingMethods;

import initializer.CP.CPIndInitializer;

import java.util.List;
import java.util.Random;

import util.RandomNumberGenerator;
import crosser.crosserBase;

public class RandomOrdering extends OrderingBase  {
	// Static
	// Used in initializer
	
	Random myRandom;
	public RandomOrdering() {
		myRandom= new Random(RandomNumberGenerator.getNewSeed());
	}

	public int selectEvent(List<Integer> tournament, CPIndInitializer initializer) {
		int rndEvent= tournament.get(initializer.myRandom.nextInt(tournament.size()));
		return rndEvent;
	}
	
	// Used in CX:
	public int selectEvent(List<Integer> tournament, crosserBase crosser) {
		int rndEvent= tournament.get(myRandom.nextInt(tournament.size()));
		return rndEvent;
	}

}
