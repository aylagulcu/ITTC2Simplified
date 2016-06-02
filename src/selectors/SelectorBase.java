package selectors;

import java.util.Random;

import util.RandomNumberGenerator;
import ga.Individual;
import ga.Population;

public abstract class SelectorBase {

	Random myRandom;
	
	public SelectorBase() {
		this.myRandom = new Random(RandomNumberGenerator.getNewSeed());
	}
	
	public abstract Individual[] selectIndividual(Population population);
	
	public abstract int[] selectIndividualIndices(Population population);
}

