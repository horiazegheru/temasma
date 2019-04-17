package agents;

import agents.utils.GridPosition;
import jade.core.Agent;

/**
 * The Agent.
 */
public class MyAgent extends Agent
{
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 2081456560111009192L;

	String color;
	GridPosition pos;

	@Override
	protected void setup()
	{
		color = getAID().getLocalName();
		pos = (GridPosition) getArguments()[0];

		System.out.println("my name is " + color);
		System.out.println(color + " is at pos " + pos);
	}
	
	@Override
	protected void takeDown()
	{
	}
}
