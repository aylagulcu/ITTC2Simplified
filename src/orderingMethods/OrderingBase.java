package orderingMethods;

import initializer.CP.CPIndInitializer;

import java.util.List;

public abstract class OrderingBase {

	public abstract int selectEvent(List<Integer> tournament, CPIndInitializer initializer);
	
}
