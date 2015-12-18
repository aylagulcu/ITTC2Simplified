package initializer;

import java.io.IOException;
import ga.Population;

public interface iPopulationInitializer {

	void initialize(Population popObject) throws IOException;

}
