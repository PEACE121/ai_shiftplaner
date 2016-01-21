package de.fit.assistantplaner;

import gac.ENextVariable;
import gac.GACState;
import gac.IDomainAttribute;
import gac.IGACObersvers;
import gac.constraintNetwork.Constraint;
import gac.constraintNetwork.Variable;
import gac.instances.VI;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import algorithms.AStar;
import astarframework.IAStarObersvers;
import astarframework.Node;


public class Schichtplaner implements IGACObersvers, IAStarObersvers
{
	// parameters which need to be configurable in the future
	private static int				ASSISTANTS						= 10;
	private static int				DEFAULT_MAX_SHIFTS_IN_ROW	= 3;
	
	// parameters for logging the update
	private int							counter							= 0;
	private float						lastAvg							= -1;
	
	private final List<Assistant>	assistants						= new ArrayList<Assistant>();
	
	
	/**
	 * @param fieldInfo
	 */
	public Schichtplaner()
	{
		// ####################################################################################
		// ################################ Parse CSV File ####################################
		// ####################################################################################
		super();
		Reader in;
		List<CSVRecord> list = new ArrayList<CSVRecord>();
		try
		{
			in = new FileReader("Dienstplan.csv");
			CSVParser parser;
			try
			{
				parser = new CSVParser(in, CSVFormat.EXCEL);
				list = parser.getRecords();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		
		
		// ####################################################################################
		// ##################### Fill assistant data structure from csv #######################
		// ####################################################################################
		// create list of assistants
		int row = 1;
		for (CSVRecord record : list)
		{
			if (row > ASSISTANTS + 8)
				break;
			if (row > 8)
			{
				assistants.add(new Assistant(record.get(28), Integer.valueOf(record.get(31)) - 1, Integer.valueOf(record
						.get(31)), DEFAULT_MAX_SHIFTS_IN_ROW, new ArrayList<Shift>()));
			}
			row++;
		}
		
		// find available dates
		row = 1;
		int daysOfMonth = 1;
		for (CSVRecord record : list)
		{
			if (record.get(2).equals("") && row > 7)
			{
				daysOfMonth = Integer.valueOf(row - 8);
				break;
			}
			if (row > 7)
			{
				for (int i = 0; i < ASSISTANTS; i++)
				{
					if (record.get(i + 5).equals(""))
					{
						assistants.get(i).addPossibleDay(new Shift(row - 7, true));
						System.out.println(assistants.get(i).getName() + " cannot work the day shift on " + (row - 7));
					}
					
					if (record.get(i + 6 + ASSISTANTS).equals(""))
					{
						assistants.get(i).addPossibleDay(new Shift(row - 7, false));
						System.out.println(assistants.get(i).getName() + " cannot work the night shift on " + (row - 7));
					}
				}
				
			}
			
			
			row++;
		}
		
		// ####################################################################################
		// ############################## Define variables ####################################
		// ####################################################################################
		
		List<Variable> vars = new LinkedList<Variable>();
		
		List<IDomainAttribute> assistantsDomain = new LinkedList<IDomainAttribute>();
		for (int i = 0; i < ASSISTANTS; i++)
		{
			assistantsDomain.add(assistants.get(i));
		}
		// in general every assistant is possible for every day
		for (int i = 1; i < daysOfMonth + 1; i++)
		{
			vars.add(new Variable("day_" + i, assistantsDomain));
			vars.add(new Variable("night_" + i, assistantsDomain));
		}
		
		// ####################################################################################
		// ############################# Define constraints ###################################
		// ####################################################################################
		List<Constraint> constraints = new LinkedList<Constraint>();
		
		// add constraints
		// 1. - Die Anzahl der Gesamtschichten in einem Monat ist endlich - DONE, trivial
		// 2. - Wann ein Assistent überhaupt arbeiten kann (nicht jeder Tag ist für jeden Assistenten möglich) - DONE,
		// über x in exel-Tabelle, represntiert als Constraint
		// 3. - die Anzahl der einzelnen Schichten der Assistenten (z.B. Min 5 Max 7) - DONE in post processing
		// 4. - die Verteilung an Wochenenden und Feiertagen - ?
		// 5. - die Verteilung von Tag und Nachtschichten - ?
		// 6. - die Anzahl an aufeinanderfolgenden Schichten (zum Beispiel nicht mehr als drei) - DONE, über constraint
		// 7. - die Reihenfolge von Schichten (zum Beispiel nicht Tagschicht nach Nachtschicht, dazwischen mindestens zwei
		// Tage frei) - Wie genau? nach einer Nachtschicht erst eine Tag, eine Nacht, eine Tag, eine Nacht frei und dann
		// erst wieder die nächste Tagschicht?
		// 8. - keine Doppelbesetzungen (nicht Tag und Nacht von derselben Person) - DONE, über constraint
		// 9. - Urlaubszeiten - DONE, über x in exel-Tabelle, represntiert als Constraint
		
		// 1 - trivial durch den algorithmus
		// 2 (gleichzeitig auch 9) allgemeine Verfügbarkeit nach x in den excel Liste
		for (int i = 0; i < ASSISTANTS; i++)
		{
			Assistant assistant = assistants.get(i);
			for (Shift notPossibleShift : assistant.getPossibleDays())
			{
				if (notPossibleShift.IsDayShift())
				{
					Map<String, Variable> variables = new HashMap<String, Variable>();
					variables.put("day_" + notPossibleShift.getDay(), vars.get((notPossibleShift.getDay() * 2) - 2));
					constraints.add(new Constraint("day_" + notPossibleShift.getDay() + "!="
							+ assistant.getNumericalRepresentation(), variables));
				} else
				{
					Map<String, Variable> variables = new HashMap<String, Variable>();
					variables.put("night_" + notPossibleShift.getDay(), vars.get((notPossibleShift.getDay() * 2) - 1));
					constraints.add(new Constraint("night_" + notPossibleShift.getDay() + "!="
							+ assistant.getNumericalRepresentation(), variables));
				}
				System.out.println(constraints.get(constraints.size() - 1).getCanonicalFormulation() + ": "
						+ constraints.get(constraints.size() - 1).getVariables().size());
			}
		}
		
		
		// 8 - nicht Tag und danach Nacht von der gleichen Person
		for (int j = 1; j < daysOfMonth + 1; j++)
		{
			// implikation
			// day => not night
			// not (day and not not night)
			Map<String, Variable> variables = new HashMap<String, Variable>();
			variables.put("day_" + j, vars.get((j * 2) - 2));
			variables.put("night_" + j, vars.get((j * 2) - 1));
			constraints.add(new Constraint("!(day_" + j + " == " + "night_" + j + ")", variables));
			System.out.println(constraints.get(constraints.size() - 1).getCanonicalFormulation());
		}
		
		// 6. - die Anzahl an aufeinanderfolgenden Schichten (zum Beispiel nicht mehr als drei)
		int maxShifts = DEFAULT_MAX_SHIFTS_IN_ROW;
		for (int j = 1; j < daysOfMonth - maxShifts + 1; j++)
		{
			// (day_j or night_j) and (day_j+1 or night_j+1) ... and (day_j+MAX_SHIFTS_IN_ROW or
			// night_j+MAX_SHIFTS_IN_ROW) )
			// and (!day_j+MAX_SHIFTS_IN_ROW+1 and !night_j+MAX_SHIFTS_IN_ROW+1 ))
			Map<String, Variable> variables = new HashMap<String, Variable>();
			String constraint = "!(";
			int dayIdx = j;
			variables.put("day_" + dayIdx, vars.get((dayIdx * 2) - 2));
			variables.put("night_" + dayIdx, vars.get((dayIdx * 2) - 1));
			for (int j2 = 0; j2 < maxShifts; j2++)
			{
				dayIdx = j + j2;
				variables.put("day_" + (dayIdx + 1), vars.get(((dayIdx + 1) * 2) - 2));
				variables.put("night_" + (dayIdx + 1), vars.get(((dayIdx + 1) * 2) - 1));
				constraint += "(day_" + dayIdx + " == " + "day_" + (dayIdx + 1) + " || night_" + dayIdx + " == night_"
						+ (dayIdx + 1) + " || day_" + dayIdx + " == " + "night_" + (dayIdx + 1) + " || night_" + dayIdx
						+ " == day_" + (dayIdx + 1) + ") && ";
			}
			constraint = constraint.substring(0, constraint.length() - 4);
			constraint += ")";
			
			
			constraints.add(new Constraint(constraint, variables));
			System.out.println(constraints.get(constraints.size() - 1).getCanonicalFormulation());
		}
		
		// alternative: every assistant has an individual amount of max shifts in row
		// for (int i = 0; i < ASSISTANTS; i++)
		// {
		// Assistant assistant = assistants.get(i);
		// for (int j = 1; j < daysOfMonth - assistant.getMaxShiftsInRow() + 1; j++)
		// {
		// // (day_j or night_j) and (day_j+1 or night_j+1) ... and (day_j+MAX_SHIFTS_IN_ROW or
		// // night_j+MAX_SHIFTS_IN_ROW) )
		// // and (!day_j+MAX_SHIFTS_IN_ROW+1 and !night_j+MAX_SHIFTS_IN_ROW+1 ))
		// Map<String, Variable> variables = new HashMap<String, Variable>();
		// String constraint = "!(";
		// int dayIdx = 0;
		// for (int j2 = 0; j2 < assistant.getMaxShiftsInRow(); j2++)
		// {
		// dayIdx = j + j2;
		// variables.put("day_" + dayIdx, vars.get((dayIdx * 2) - 2));
		// variables.put("night_" + dayIdx, vars.get((dayIdx * 2) - 1));
		// constraint += "(day_" + dayIdx + " == " + assistant.getNumericalRepresentation() + " || night_" + dayIdx
		// + " == " + assistant.getNumericalRepresentation() + ") && ";
		// }
		// dayIdx++;
		// variables.put("day_" + dayIdx, vars.get((dayIdx * 2) - 2));
		// variables.put("night_" + dayIdx, vars.get((dayIdx * 2) - 1));
		// constraint += "(day_" + dayIdx + " == " + assistant.getNumericalRepresentation() + " || night_" + dayIdx
		// + " == " + assistant.getNumericalRepresentation() + "))";
		//
		//
		// constraints.add(new Constraint(constraint, variables));
		// System.out.println(constraints.get(constraints.size() - 1).getCanonicalFormulation());
		// }
		// }
		
		
		System.out.println("Constraints: " + constraints.size());
		
		// ####################################################################################
		// ########################### Invoke Constraint Solver ###############################
		// ####################################################################################
		SchichtenplanerAdapter aStarGAC = new SchichtenplanerAdapter(constraints, vars, ENextVariable.SIMPLE, assistants);
		aStarGAC.register(this);
		AStar aStarInstance = new AStar(aStarGAC);
		aStarInstance.register(this);
		long startTime = System.nanoTime();
		aStarInstance.run();
		System.out.println("Elapsed time: "
				+ TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Schichtplaner();
		return;
	}
	
	
	/**
	 * invoked after each iteration of the algorithm (observer)
	 */
	@Override
	public void update(GACState gacState, boolean force)
	{
		// ####################################################################################
		// ############################### Solution found #####################################
		// ####################################################################################
		if (force)
		{
			PrintWriter writer;
			try
			{
				writer = new PrintWriter("result.csv", "UTF-8");
				System.out.println(" ---------- Result -------------");
				for (VI vi : gacState.getVis().values())
				{
					String var = vi.getVarInCNET().getName();
					String csvLine = var.split("_")[0] + "," + var.split("_")[1] + ","
							+ hashToAssist(vi.getDomain().get(0).getNumericalRepresentation()).getName();
					System.out.println(csvLine);
					writer.println(csvLine);
					
				}
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			
		}
		// ####################################################################################
		// ############################ Intermediate logs #####################################
		// ####################################################################################
		
		counter++;
		int domainSize = 0;
		for (VI vi : gacState.getVis().values())
		{
			domainSize += vi.getDomain().size();
		}
		float averageSize = ((float) domainSize) / ((float) 62);
		if (lastAvg != averageSize)
		{
			System.out.println("Iterations: " + counter + ", last avg: " + lastAvg);
			lastAvg = averageSize;
		}
	}
	
	
	private Assistant hashToAssist(int hash)
	{
		for (Assistant assistant : assistants)
		{
			if (assistant.getNumericalRepresentation() == hash)
			{
				return assistant;
			}
		}
		return null;
	}
	
	
	@Override
	public void update(Node app, boolean force)
	{
		GACState gacState = (GACState) app.getState();
		update(gacState, force);
	}
}
