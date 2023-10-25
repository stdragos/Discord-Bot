package src;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Stack;

class Node{
    String value;
    Node left;
    Node right;
    Node(String value) {
        this.value = value;
        right = null;
        left = null;
    }
}

public class SyntacticTree{
    Node root;
    String equation;
    public SyntacticTree(String eq){
        this.equation = eq;
    }
    //de testat constructorii fara pam
    boolean is_operator(char ch){
        return ch == '-' || ch == '+' || ch == '*' ||
                ch == '/' || ch == '^';
    }

    static private boolean isNumber(String string){
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    int get_priority(char ch)
    {
        switch (ch) {
            case '(' -> {
                return 0;
            }
            case '+', '-' -> {
                return 1;
            }
            case '*', '/' -> {
                return 2;
            }
            case '^' -> {
                return 3;
            }
        }
        return 0;
    }

    double performCalculation(double x, double y, char sign)
    {
        switch(sign)
        {
            case '+' -> {
                return x + y;
            }
            case '-' -> {
                return x - y;
            }
            case '*' -> {
                return x*y;
            }
            case '/' -> {
                return x/y;
            }
            case '^' -> {
                return Math.pow(x,y);
            }
        }
        return 0;
    }

    double calculateExpr(Node root)
    {
        double ans = 0;
        if(root.left == null) {
            ans += Double.parseDouble(root.value);
        }
        else {
            double x = calculateExpr(root.left);
            double y = calculateExpr(root.right);
            ans += performCalculation(x,y, root.value.charAt(0));
        }
        return ans;
    }

    public void generateTree(SlashCommandInteractionEvent event)
    {
        Stack<Character> signs = new Stack<>();
        String[] elements = equation.replaceAll(" ","").split("(?=[-+*/^()])|(?<=[^-+*/^][-+*/^])|(?<=[()])");
        ArrayList<Node> polishNotation = new ArrayList<Node>();

        for(String elem : elements){
            if(isNumber(elem)){
                Node p = new Node(elem);
                polishNotation.add(p);
                continue;
            }
            if(elem.equals("("))
                signs.push(elem.charAt(0));
            else if (elem.equals(")")) {
                while(!signs.empty() && !signs.peek().equals('(')) {
                    Node p = new Node(signs.peek().toString());
                    polishNotation.add(p);
                    signs.pop();
                }
                if(signs.empty())
                {
                    event.reply("The provided operation doesn't have enough signs!").setEphemeral(true).queue();
                    return;
                    //Not enough signs!
                }
                signs.pop();
            } else if (is_operator(elem.charAt(0))) {
                while (!signs.empty() && get_priority(signs.peek()) >= get_priority(elem.charAt(0))) {
                    Node p = new Node(signs.peek().toString());
                    polishNotation.add(p);
                    signs.pop();
                }
                signs.push(elem.charAt(0));
            } else {
                event.reply("The provided operation contains unsupported characters!").setEphemeral(true).queue();
                return;
            }
        }

        while(!signs.empty()) {
            Node p = new Node(signs.peek().toString());
            polishNotation.add(p);
            signs.pop();
        }

        Stack<Node> genTree = new Stack<Node>();

        for(Node p : polishNotation) {
            if (!is_operator(p.value.charAt(0))) {
                genTree.push(p);
            } else {
                char ch = p.value.charAt(0);
                if (genTree.size() < 2) {
                    event.reply("Not enough members provided!").setEphemeral(true).queue();
                    return;
                }
                Node y = genTree.peek();
                genTree.pop();
                Node x = genTree.peek();
                genTree.pop();

                p.left = x;
                p.right = y;
                genTree.push(p);
            }
        }
            if (genTree.size() != 1) {
                event.reply("Not enough operators provided!").setEphemeral(true).queue();
                return;
            }
            this.root = genTree.peek();
            genTree.pop();

            double answer = calculateExpr(this.root);
            event.reply("The answer is: " + answer).setEphemeral(true).queue();
            return;
        }
    }

