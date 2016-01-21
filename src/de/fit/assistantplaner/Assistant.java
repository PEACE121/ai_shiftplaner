package de.fit.assistantplaner;

import gac.IDomainAttribute;

import java.util.List;


public class Assistant implements IDomainAttribute
{
	private String			name;
	private int				minAmountOfShifts;
	private int				maxAmountOfShifts;
	private final int		maxShiftsInRow;
	private List<Shift>	possibleDays;
	
	
	/**
	 * @param name
	 * @param minAmountOfShifts
	 * @param maxAmountOfShifts
	 * @param possibleDays
	 */
	public Assistant(String name, int minAmountOfShifts, int maxAmountOfShifts, int maxShiftsInRow,
			List<Shift> possibleDays)
	{
		super();
		this.name = name;
		this.minAmountOfShifts = minAmountOfShifts;
		this.maxAmountOfShifts = maxAmountOfShifts;
		this.maxShiftsInRow = maxShiftsInRow;
		this.possibleDays = possibleDays;
	}
	
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	
	/**
	 * @return the minAmountOfShifts
	 */
	public int getMinAmountOfShifts()
	{
		return minAmountOfShifts;
	}
	
	
	/**
	 * @param minAmountOfShifts the minAmountOfShifts to set
	 */
	public void setMinAmountOfShifts(int minAmountOfShifts)
	{
		this.minAmountOfShifts = minAmountOfShifts;
	}
	
	
	/**
	 * @return the maxAmountOfShifts
	 */
	public int getMaxAmountOfShifts()
	{
		return maxAmountOfShifts;
	}
	
	
	/**
	 * @param maxAmountOfShifts the maxAmountOfShifts to set
	 */
	public void setMaxAmountOfShifts(int maxAmountOfShifts)
	{
		this.maxAmountOfShifts = maxAmountOfShifts;
	}
	
	
	/**
	 * @return the possibleDays
	 */
	public List<Shift> getPossibleDays()
	{
		return possibleDays;
	}
	
	
	/**
	 * @param possibleDays the possibleDays to set
	 */
	public void setPossibleDays(List<Shift> possibleDays)
	{
		this.possibleDays = possibleDays;
	}
	
	
	public void addPossibleDay(Shift shift)
	{
		possibleDays.add(shift);
	}
	
	
	@Override
	public int getNumericalRepresentation()
	{
		return name.hashCode();
	}
	
	
	/**
	 * @return the maxShiftsInRow
	 */
	public int getMaxShiftsInRow()
	{
		return maxShiftsInRow;
	}
	
	
}
