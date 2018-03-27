package com.rizaldi.hwamin.helper;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Solver24 {
    private final String[] patterns = {"nnonnoo", "nnonono", "nnnoono", "nnnonoo", "nnnnooo"};
    private final String ops = "+-*/^";
    private final Map<String, String> solutions = new ConcurrentHashMap<>();
    private final ExpressionParser parser = new SpelExpressionParser();


    public boolean isCorrect(List<Integer> question, String answer) throws Exception {
        generateSolution(question);
        if (solutions.get(getKey(question)).equals(answer)) return true;
        validateDigits(question, answer);
        return evaluateAnswer(answer);
    }

    public String getSolution(List<Integer> question) {
        generateSolution(question);
        return solutions.get(getKey(question));
    }

    private void generateSolution(List<Integer> question) {
        if (solutions.containsKey(getKey(question))) return;
        Set<List<Integer>> dPerms = new HashSet<>(4 * 3 * 2);
        permute(question, dPerms, 0);
        int total = 4 * 4 * 4;
        List<List<Integer>> oPerms = new ArrayList<>(total);
        permuteOperators(oPerms, 4, total);
        StringBuilder sb = new StringBuilder(4 + 3);
        for (String pattern : patterns) {
            char[] patternChars = pattern.toCharArray();
            for (List<Integer> dig : dPerms) {
                for (List<Integer> opr : oPerms) {
                    int i = 0, j = 0;
                    for (char c : patternChars) {
                        if (c == 'n') sb.append(dig.get(i++)).append(' ');
                        else sb.append(ops.charAt(opr.get(j++))).append(' ');
                    }
                    String candidate = postfixToInfix(sb.toString().trim());
                    try {
                        if (evaluateAnswer(candidate)) {
                            solutions.put(getKey(question), candidate);
                            return;
                        }
                    } catch (Exception ignored) {}
                    sb.setLength(0);
                }
            }
        }
        solutions.put(getKey(question), "tidak ada");
    }

    private String getKey(List<Integer> question) {
        Collections.sort(question);
        return question.toString();
    }

    private boolean evaluateAnswer(String answer) throws Exception {
        Expression expression = parser.parseExpression(formatAnswer(answer));
        Float result = expression.getValue(Float.class);
        if (result == null) throw new Exception("invalid answer");
        return (Math.abs(24.0F - result) < 0.001F);
    }

    private String formatAnswer(String answer) {
        // add .0 to all number so parser will recognize it as double
        StringBuilder newAnswer = new StringBuilder();
        boolean doublify = false;
        for (int i = 0; i < answer.length(); ) {
            while (i < answer.length() && (answer.charAt(i) >= '0' && answer.charAt(i) <= '9')) {
                doublify = true;
                newAnswer.append(answer.charAt(i++));
            }
            if (doublify) {
                doublify = false;
                newAnswer.append(".0");
            }
            if (i < answer.length()) newAnswer.append(answer.charAt(i++));
        }
        return newAnswer.toString();
    }

    private void validateDigits(List<Integer> question, String answer) throws Exception {
        String filter = answer.replaceAll("[^0-9]", " ");
        List<Integer> digits = new ArrayList<>();
        Scanner f = new Scanner(filter);
        while (f.hasNextInt()) digits.add(f.nextInt());
        if (!getKey(question).equals(getKey(digits))) throw new Exception("Not the same digits.");
    }

    private String postfixToInfix(String postfix) {
        class Expression {
            private String op, ex;
            private int prec = 3;

            private Expression(String e) {
                ex = e;
            }

            private Expression(String e1, String e2, String o) {
                ex = String.format("%s %s %s", e1, o, e2);
                op = o;
                prec = ops.indexOf(o) / 2;
            }
        }
        Stack<Expression> expr = new Stack<>();
        for (String s : postfix.split(" ")) {
            int idx = ops.indexOf(s);
            if (idx != -1) {
                Expression r = expr.pop();
                Expression l = expr.pop();
                int opPrec = idx / 2;
                if (l.prec < opPrec)
                    l.ex = '(' + l.ex + ')';
                if (r.prec <= opPrec)
                    r.ex = '(' + r.ex + ')';
                expr.push(new Expression(l.ex, r.ex, "" + s));
            } else {
                expr.push(new Expression("" + s));
            }
        }
        return expr.peek().ex;
    }

    private void permute(List<Integer> lst, Set<List<Integer>> res, int k) {
        for (int i = k; i < lst.size(); i++) {
            Collections.swap(lst, i, k);
            permute(lst, res, k + 1);
            Collections.swap(lst, k, i);
        }
        if (k == lst.size())
            res.add(new ArrayList<>(lst));
    }

    private void permuteOperators(List<List<Integer>> res, int n, int total) {
        for (int i = 0, npow = n * n; i < total; i++)
            res.add(Arrays.asList((i / npow), (i % npow) / n, i % n));
    }
}
