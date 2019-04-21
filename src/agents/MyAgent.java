package agents;

import agents.behaviors.AgentBehavior;
import agents.utils.GridPosition;
import jade.core.AID;
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
	AID envAID;

	@Override
	protected void setup()
	{
		color = getAID().getLocalName();
		pos = (GridPosition) getArguments()[0];
		envAID = (AID) getArguments()[1];
		int agentsNr = (int) getArguments()[2];

		addBehaviour(new AgentBehavior(color, pos, envAID, agentsNr));

	}
	
	@Override
	protected void takeDown()
	{
	}
}
