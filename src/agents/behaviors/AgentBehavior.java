package agents.behaviors;

import agents.utils.Action;
import agents.utils.GridPosition;
import agents.utils.Perception;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentBehavior extends CyclicBehaviour {
    String color;
    GridPosition pos;
    AID envAID;
    ArrayList<AID> otherAgents;
    boolean gotFirstEnvMessage = false;
    int aliveAgents = 0;

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTPERCEPT_PROTOCOL));

    private MessageTemplate terminateTemplate   = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTTERMINATE_PROTOCOL));

    private MessageTemplate negotiateTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(AGNEGOTIATE_PROTOCOL));

    static final String ENVACTION_PROTOCOL = "sendaction";
    static final String AGNEGOTIATE_PROTOCOL = "negotiate";

    public AgentBehavior(String color, GridPosition pos, AID envAID, int agentsNr) {
        this.color = color;
        this.pos = pos;
        this.envAID = envAID;

        System.out.println("my name is " + color);
        System.out.println(color + " is at pos " + pos);
        System.out.println(color + " knows envAID is " + envAID);
        aliveAgents = agentsNr;
    }

    public Action chooseAction(Perception perception) {

        // EXAMPLE WITH ACTIONS THAT MAKE GREEN AGENT GRAB A BLUE TILE AND PUT IT IN A BLUE HOLE
        // BLUE WILL DIE BECAUSE IT WILL GET AN ERROR (ACTIONS DO NOT MATCH)

        if (perception.currentTime == 0)
            return new Action("Move", "East");

        if (perception.currentTime == 300)
            return new Action("Move", "East");

        if (perception.currentTime == 600)
            return new Action("Pick", "blue");

        if (perception.currentTime == 900)
            return new Action("Move", "West");

        if (perception.currentTime == 1200)
            return new Action("Use_tile", "South");

        return new Action("Move", "South");
    }

    public void sendAction(Perception perception) throws IOException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(ENVACTION_PROTOCOL);
        msg.addReceiver(envAID);

        // CHOOSE ACTION AND SEND IT
        Action chosenAction = chooseAction(perception);

        msg.setContentObject(chosenAction);
        myAgent.send(msg);
    }

    public void receivePerception() throws UnreadableException, IOException {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);
        if (receivedMsg != null) {
            gotFirstEnvMessage = true;
            Perception perception = (Perception) receivedMsg.getContentObject();
            System.out.println(color + ": " + perception);
            System.out.println(color + " ERROR: " + perception.error);
            pos = perception.pos;
            otherAgents = perception.otherAgentAIDs;
            negotiate();

            if (perception.error != null) {
                System.out.println("AGENT " + color  + " TERMINATING...");
                aliveAgents--;
                myAgent.doDelete();
            }

            // DO SOMETHING WITH PERCEPTION
            sendAction(perception);
        }
    }

    public void receiveTerminate() {
        ACLMessage terminateMsg = myAgent.receive(terminateTemplate);

        if (terminateMsg != null) {
            System.out.println("AGENT " + color  + " TERMINATING...");
            myAgent.doDelete();
        }
    }

    public void negotiate() {
        if (gotFirstEnvMessage) {
            for (int i = 0; i < aliveAgents - 1; ++i) {
                ACLMessage negotiateMsg = myAgent.receive(negotiateTemplate);
//                if (negotiateMsg != null)
//                    System.out.println("Agent " + color  + " received negotiation stuff from " +
//                            negotiateMsg.getSender().getLocalName() + " with content " + negotiateMsg.getContent());
            }

            for (AID aid: otherAgents) {
                if (!aid.getLocalName().equals(myAgent.getAID().getLocalName())) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol(AGNEGOTIATE_PROTOCOL);
                    msg.addReceiver(aid);

                    msg.setContent("salut" + LocalDateTime.now());
                    myAgent.send(msg);
                }
            }

//            System.out.println("NEGOTIATION PHASE COMPLETED");
        }
    }

    @Override
    public void action() {
        try {
            receivePerception();
        } catch (UnreadableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiveTerminate();
    }
}