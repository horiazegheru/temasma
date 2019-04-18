package agents.utils;

import java.io.Serializable;

public class Action implements Serializable {
    public String actionName;
    public String arg1;

    public Action(String actionName, String arg1, String arg2) {
        this.actionName = actionName;
        this.arg1 = arg1;
    }

    public String toString() {
        return actionName + " " + arg1;
    }
}
