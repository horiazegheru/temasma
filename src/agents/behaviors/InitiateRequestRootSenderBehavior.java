package agents.behaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;

public class InitiateRequestRootSenderBehavior extends WakerBehaviour {
    /**
     * The serial UID.
     */
    private static final long	serialVersionUID	= -8741441435805457781L;

    /**
     * The name of the registration protocol.
     */
    static final String INFORMDOWN_PROTOCOL = "inform-child";

    /**
     * The ID of the parent.
     */
    AID parentAID;
    public List<AID> childAgents;

    /**
     * @param agent
     *            - the agent containing this behavior.
     * @param timeout
     *            - the time after which to send the registration message.
     * @param parentAID
     *            - the agent to send the registration message to.
     */
    public InitiateRequestRootSenderBehavior(Agent agent, long timeout, AID parentAID, List<AID> childAgents)
    {
        super(agent, timeout);
        this.parentAID = parentAID;
        this.childAgents = childAgents;
    }

    @Override
    protected void onWake()
    {
        // Create the registration message as a simple INFORM message
        // with th protocol "register-child"

        for (AID agentAID: childAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(INFORMDOWN_PROTOCOL);
            msg.setConversationId("informdown-" + agentAID.getName());

            msg.addReceiver(agentAID);
            msg.setContent(myAgent.getAID().getLocalName() + " 's request message");

            myAgent.send(msg);
        }
    }
}
