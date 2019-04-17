package agents.behaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;

public class PropagateRequestNonRootBehavior extends CyclicBehaviour {

    boolean sendOnce = false;

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(InitiateRequestRootSenderBehavior.INFORMDOWN_PROTOCOL));

    static final String PROPAGATEUP_PROTOCOL = "propagate-parent";

    public List<AID> childAgents;
    public AID parentAID;
    public Integer agentValue;

    public PropagateRequestNonRootBehavior(Agent a, List<AID> childAgents, AID parentAID, Integer agentValue)
    {
        super(a);
        this.childAgents = childAgents;
        this.parentAID = parentAID;
        this.agentValue = agentValue;
    }

    @Override
    public void action() {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);

        // register the agent if message received
        if(receivedMsg != null)
        {
            System.out.println(myAgent.getAID().getLocalName() + " received " + receivedMsg.getContent());
            if (!sendOnce) {
                if (!childAgents.isEmpty()) {
                    for (AID agentAID : childAgents) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setProtocol(InitiateRequestRootSenderBehavior.INFORMDOWN_PROTOCOL);
                        msg.setConversationId("informdown-" + agentAID.getName());

                        msg.addReceiver(agentAID);
                        msg.setContent(myAgent.getAID().getLocalName() + " 's request message");

                        myAgent.send(msg);
                    }

                    sendOnce = true;
                } else {
                    ACLMessage msg = new ACLMessage((ACLMessage.INFORM));
                    msg.setProtocol(PROPAGATEUP_PROTOCOL);
                    msg.setConversationId("passvalue-" + parentAID);

                    msg.addReceiver(parentAID);
                    msg.setContent(agentValue.toString());
                    System.out.println(myAgent.getAID().getLocalName() + " sent value " + agentValue + " to " + parentAID.getLocalName());

                    myAgent.send(msg);
                }
            }
        }
    }
}
