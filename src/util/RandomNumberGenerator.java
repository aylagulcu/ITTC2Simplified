package util;

import java.util.Random;

public class RandomNumberGenerator {

	// All random operations are thread-safe.
	public static volatile Random myRandom;
	public static volatile int mySeed;
	
	public static synchronized float getRandomFloat() {
		return myRandom.nextFloat();
	}
	public static synchronized double getRandomDouble() {
		return myRandom.nextDouble();
	}
	public static synchronized boolean getRandomBoolean(){
		return myRandom.nextBoolean();
	}
	
	public static synchronized int getNewSeed(){
//		mySeed++;
//		return mySeed;
		
		return myRandom.nextInt();
	}

}
