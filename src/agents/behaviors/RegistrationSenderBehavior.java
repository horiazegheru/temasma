package agents.behaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behavior that sends a registration message to the parent agent after a given timeout.
 */
public class RegistrationSenderBehavior extends WakerBehaviour
{
	
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= -8741441435805457781L;

	/**
	 * The name of the registration protocol.
	 */
	static final String REGISTRATION_PROTOCOL = "register-child";
	
	/**
	 * The ID of the parent.
	 */
	AID							parentAID;

	/**
	 * @param agent
	 *            - the agent containing this behavior.
	 * @param timeout
	 *            - the time after which to send the registration message.
	 * @param parentAID
	 *            - the agent to send the registration message to.
	 */
	public RegistrationSenderBehavior(Agent agent, long timeout, AID parentAID)
	{
		super(agent, timeout);
		this.parentAID = parentAID;
	}
	
	@Override
	protected void onWake()
	{
		// Create the registration message as a simple INFORM message
		// with th protocol "register-child"
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(REGISTRATION_PROTOCOL);
		msg.setConversationId("registration-" + myAgent.getName());
		msg.addReceiver(parentAID);

		myAgent.send(msg);
	}
	
	@Override
	public int onEnd()
	{
		System.out.println(
				"Agent " + myAgent.getAID().getLocalName() + " has sent registration message to " + parentAID.getLocalName());
		return super.onEnd();
	}
	
}
