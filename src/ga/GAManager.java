package ga;

import java.io.IOException;
import java.util.ArrayList;

import constraints.ClashConstraint;
import constraints.CompletenessConstraint;
import constraints.ConstraintBase;
import constraints.CurriculumCompactnessConstraint;
import constraints.InstructorTimeAvailabilityConstraint;
import constraints.MinimumWorkingDaysConstraint;
import constraints.RoomCapacityConstraint;
import constraints.RoomStabilityConstraint;
import constraints.RoomUniquenessConstraint;


public class GAManager {

	public void runGA() throws InterruptedException, IOException {
		ArrayList<ConstraintBase> listOfConst1 = loadList1(); 

		GABase GA = new Deme(listOfConst1);
		GA.run();
	}
	
	private ArrayList<ConstraintBase> loadList1() {
		ArrayList<ConstraintBase> constr= new ArrayList<ConstraintBase>();
		constr = new ArrayList<ConstraintBase>();
		constr.add(new ClashConstraint(100));
		constr.add(new RoomUniquenessConstraint(100));
		constr.add(new InstructorTimeAvailabilityConstraint(100));
		constr.add(new CompletenessConstraint(100));
		constr.add(new CurriculumCompactnessConstraint());
		constr.add(new MinimumWorkingDaysConstraint());
		constr.add(new RoomCapacityConstraint());	
		constr.add(new RoomStabilityConstraint());
		return constr;
	}
	
}
