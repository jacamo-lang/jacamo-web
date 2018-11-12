package jacamo.web.plugins;

import java.util.ArrayList;
import java.util.List;

public class GoalNode {
    private List<String> skills = new ArrayList<String>();
    private List<GoalNode> successors = new ArrayList<GoalNode>();
    private String goalName;
    private GoalNode parent;
    private String operator;

    public GoalNode(GoalNode p, String name) {
        goalName = name;
        parent = p;
        operator = "sequence";
        if (parent != null) {
            parent.addSuccessors(this);
        }
    }

    public void addSkill(String newSkill) {
        skills.add(newSkill);
    }

    public List<String> getSkills() {
        return skills;
    }

    private void addSuccessors(GoalNode newSuccessor) {
        successors.add(newSuccessor);
        if (parent != null)
            parent.addSuccessors(newSuccessor);
    }

    public List<GoalNode> getSuccessors() {
        return successors;
    }

    public String getGoalName() {
        return goalName;
    }

    public GoalNode getParent() {
        return parent;
    }

    public void setOperator(String op) {
        this.operator = op;
    }

    public String getOperator() {
        return operator;
    }

    public String toString() {
        return goalName;
    }
}
