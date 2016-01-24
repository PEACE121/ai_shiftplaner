package de.fit.assistantplaner;

import gac.AStarAdapter;
import gac.ENextVariable;
import gac.GACState;
import gac.IDomainAttribute;
import gac.constraintNetwork.Constraint;
import gac.constraintNetwork.Variable;
import gac.instances.VI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import astarframework.IState;


public class SchichtenplanerAdapter extends AStarAdapter
{
	private final List<Assistant>		assistants;
	private final Map<String, Shift>	shifts;
	
	private float							weekendOpt	= 0f;
	
	
	public SchichtenplanerAdapter(List<Constraint> constraints, List<Variable> vars, ENextVariable next,
			List<Assistant> assistants, Map<String, Shift> shifts)
	{
		super(constraints, vars, next);
		this.assistants = assistants;
		this.shifts = shifts;
		
		for (Shift shift : this.shifts.values())
		{
			if (shift.isWeekend())
				weekendOpt += 1;
		}
		weekendOpt = weekendOpt / this.shifts.size();
	}
	
	
	@Override
	public int getHeuristic(IState state)
	{
		// simplest heuristic, just sum up the amount of domains of all variable instances
		GACState gacState = (GACState) state;
		int h = 0;
		for (VI vi : gacState.getVis().values())
		{
			h += vi.getDomain().size() - 1;
		}
		
		float dist = distanceToSolution(gacState);
		System.out.println(dist);
		return (int) dist + h;
	}
	
	
	@Override
	public boolean isApplicationSolution(GACState state)
	{
		// this is the "solution" for requirement 3 but since this is a check after the algorithm finished then
		// intelligent guessing is not possible... takes for years...
		
		// for every assistant
		HashMap<Integer, Integer> amounts = new HashMap<Integer, Integer>();
		for (Assistant assistant : assistants)
		{
			amounts.put(assistant.getNumericalRepresentation(), 0);
		}
		for (VI vi : state.getVis().values())
		{
			Integer representation = vi.getDomain().get(0).getNumericalRepresentation();
			amounts.put(representation, amounts.get(representation) + 1);
		}
		
		// check
		for (Assistant assistant : assistants)
		{
			if (amounts.get(assistant.getNumericalRepresentation()) > assistant.getMaxAmountOfShifts()
					|| amounts.get(assistant.getNumericalRepresentation()) < assistant.getMinAmountOfShifts())
			{
				System.out.println(assistant.getName() + " has " + amounts.get(assistant.getNumericalRepresentation())
						+ ". min/max: " + assistant.getMinAmountOfShifts() + "/" + assistant.getMaxAmountOfShifts());
				return false;
			}
		}
		return true;
	}
	
	
	private float distanceToSolution(GACState state)
	{
		HashMap<Integer, Float> amounts = new HashMap<Integer, Float>();
		HashMap<Integer, Float> weekendAmounts = new HashMap<Integer, Float>();
		for (Assistant assistant : assistants)
		{
			amounts.put(assistant.getNumericalRepresentation(), 0f);
			weekendAmounts.put(assistant.getNumericalRepresentation(), 0f);
		}
		for (VI vi : state.getVis().values())
		{
			for (IDomainAttribute da : vi.getDomain())
			{
				Integer representation = da.getNumericalRepresentation();
				amounts.put(representation, amounts.get(representation) + ((float) 1 / (float) vi.getDomain().size()));
				if (shifts.get(vi.getVarInCNET().getName()).isWeekend())
				{
					weekendAmounts.put(representation, amounts.get(representation)
							+ ((float) 1 / (float) vi.getDomain().size()));
				}
			}
		}
		
		// check
		float distance = 0;
		for (Assistant assistant : assistants)
		{
			float opt = (assistant.getMaxAmountOfShifts() + assistant.getMinAmountOfShifts()) / 2;
			float dist = (opt - amounts.get(assistant.getNumericalRepresentation()));
			
			float shiftsAmount = amounts.get(assistant.getNumericalRepresentation());
			
			float weekendDist = (weekendOpt - (weekendAmounts.get(assistant.getNumericalRepresentation()) / shiftsAmount));
			distance += dist * dist + weekendDist * weekendDist;
		}
		return distance;
	}
}
