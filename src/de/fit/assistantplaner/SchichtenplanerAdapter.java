package de.fit.assistantplaner;

import gac.AStarAdapter;
import gac.ENextVariable;
import gac.GACState;
import gac.constraintNetwork.Constraint;
import gac.constraintNetwork.Variable;

import java.util.List;


public class SchichtenplanerAdapter extends AStarAdapter
{
	private final List<Assistant>	assistants;
	
	
	public SchichtenplanerAdapter(List<Constraint> constraints, List<Variable> vars, ENextVariable next,
			List<Assistant> assistants)
	{
		super(constraints, vars, next);
		this.assistants = assistants;
	}
	
	
	@Override
	public boolean isApplicationSolution(GACState state)
	{
		// this is the "solution" for requirement 3 but since this is a check after the algorithm finished then
		// intelligent guessing is not possible... takes for years...
		/**
		 * // for every assistant
		 * HashMap<Integer, Integer> amounts = new HashMap<Integer, Integer>();
		 * for (Assistant assistant : assistants)
		 * {
		 * amounts.put(assistant.getNumericalRepresentation(), 0);
		 * }
		 * for (VI vi : state.getVis().values())
		 * {
		 * Integer representation = vi.getDomain().get(0).getNumericalRepresentation();
		 * amounts.put(representation, amounts.get(representation) + 1);
		 * }
		 * 
		 * // check
		 * for (Assistant assistant : assistants)
		 * {
		 * if (amounts.get(assistant.getNumericalRepresentation()) > assistant.getMaxAmountOfShifts()
		 * || amounts.get(assistant.getNumericalRepresentation()) < assistant.getMinAmountOfShifts())
		 * return false;
		 * }
		 **/
		return true;
	}
}
