package de.fit.assistantplaner;

public class Shift
{
	private int			day;
	private boolean	isDayShift;
	
	
	/**
	 * @param day
	 * @param isDayShift
	 */
	public Shift(int day, boolean isDayShift)
	{
		super();
		this.day = day;
		this.isDayShift = isDayShift;
	}
	
	
	/**
	 * @return the day
	 */
	public int getDay()
	{
		return day;
	}
	
	
	/**
	 * @param day the day to set
	 */
	public void setDay(int day)
	{
		this.day = day;
	}
	
	
	/**
	 * @return the isDayShift
	 */
	public boolean IsDayShift()
	{
		return isDayShift;
	}
	
	
	/**
	 * @param isDayShift the isDayShift to set
	 */
	public void setIsDayShift(boolean isDayShift)
	{
		this.isDayShift = isDayShift;
	}
	
	
}
