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

public class AgentBehavior extends CyclicBehaviour {
    String color;
    GridPosition pos;
    AID envAID;

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTPERCEPT_PROTOCOL));

    private MessageTemplate terminateTemplate   = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTTERMINATE_PROTOCOL));

    static final String ENVACTION_PROTOCOL = "sendaction";

    public AgentBehavior(String color, GridPosition pos, AID envAID) {
        this.color = color;
        this.pos = pos;
        this.envAID = envAID;

        System.out.println("my name is " + color);
        System.out.println(color + " is at pos " + pos);
        System.out.println(color + " knows envAID is " + envAID);
    }

    public Action chooseAction() {
        return new Action("Move", "East", null);
    }

    public void sendAction() throws IOException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(ENVACTION_PROTOCOL);
        msg.addReceiver(envAID);

        // CHOOSE ACTION AND SEND IT
        Action chosenAction = chooseAction();

        msg.setContentObject(chosenAction);
        myAgent.send(msg);
    }

    public void receivePerception() throws UnreadableException, IOException {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);
        if (receivedMsg != null) {
            Perception perception = (Perception) receivedMsg.getContentObject();
            System.out.println(color + ": " + perception);
            pos = perception.pos;

            // DO SOMETHING WITH PERCEPTION
            sendAction();
        }
    }

    public void receiveTerminate() {
        ACLMessage terminateMsg = myAgent.receive(terminateTemplate);

        if (terminateMsg != null) {
            System.out.println("AGENT " + color  + " TERMINATING...");
            myAgent.doDelete();
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