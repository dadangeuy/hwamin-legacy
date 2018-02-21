package com.rizaldi.hwamin.game.duaempat;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DuaEmpatLogicService {
    private final String[] patterns = {"nnonnoo", "nnonono", "nnnoono", "nnnonoo", "nnnnooo"};
    private final String ops = "+-*/^";
    private Random random = new Random();
    private Map<String, String> solutions = new ConcurrentHashMap<>();

    public List<Integer> getQuestion() {
        List<Integer> question = Arrays.asList(
                random.nextInt(9) + 1,
                random.nextInt(9) + 1,
                random.nextInt(9) + 1,
                random.nextInt(9) + 1);
        findSolution(question);
        return question;
    }

    private void findSolution(List<Integer> question) {
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
                        if (c == 'n')
                            sb.append(dig.get(i++));
                        else
                            sb.append(ops.charAt(opr.get(j++)));
                    }
                    String candidate = sb.toString();
                    try {
                        if (evaluateAnswer(candidate)) {
                            solutions.put(getKey(question), postfixToInfix(candidate));
                            return;
                        }
                    } catch (Exception ignored) {}
                    sb.setLength(0);
                }
            }
        }
    }

    private String getKey(List<Integer> question) {
        Collections.sort(question);
        return question.toString();
    }

    public boolean isCorrectAnswer(List<Integer> question, String answer) throws Exception {
        if (isEqualsToGeneratedSolution(question, answer)) return true;
        else if (answer.equals("tidak ada") && solutions.containsKey(getKey(question))) return false;
        validateAnswer(question, answer);
        return evaluateAnswer(infixToPostfix(answer));
    }

    private boolean isEqualsToGeneratedSolution(List<Integer> question, String answer) {
        return answer.equals(solutions.getOrDefault(getKey(question), "tidak ada"));
    }

    private boolean evaluateAnswer(String answer) throws Exception {
        Stack<Float> s = new Stack<>();
        try {
            for (char c : answer.toCharArray()) {
                if (Character.isDigit(c)) s.push((float) c - '0');
                else s.push(applyOperator(s.pop(), s.pop(), c));
            }
        } catch (EmptyStackException e) {
            throw new Exception("Invalid entry.");
        }
        return (Math.abs(24 - s.peek()) < 0.001F);
    }

    private void validateAnswer(List<Integer> question, String answer) throws Exception {
        int total1 = 0, parens = 0, opsCount = 0;
        for (char c : answer.toCharArray()) {
            if (Character.isDigit(c)) total1 += 1 << (c - '0') * 4;
            else if (c == '(') parens++;
            else if (c == ')') parens--;
            else if (ops.indexOf(c) != -1) opsCount++;
            if (parens < 0) throw new Exception("Parentheses mismatch.");
        }
        if (parens != 0) throw new Exception("Parentheses mismatch.");
        if (opsCount != 3) throw new Exception("Wrong number of operators.");
        int total2 = 0;
        for (int d : question) total2 += 1 << d * 4;
        if (total1 != total2) throw new Exception("Not the same digits.");
    }

    private String infixToPostfix(String infix) throws Exception {
        StringBuilder sb = new StringBuilder();
        Stack<Integer> s = new Stack<>();
        try {
            for (char c : infix.toCharArray()) {
                int idx = ops.indexOf(c);
                if (idx != -1) {
                    if (s.isEmpty())
                        s.push(idx);
                    else {
                        while (!s.isEmpty()) {
                            int prec2 = s.peek() / 2;
                            int prec1 = idx / 2;
                            if (prec2 >= prec1)
                                sb.append(ops.charAt(s.pop()));
                            else
                                break;
                        }
                        s.push(idx);
                    }
                } else if (c == '(') {
                    s.push(-2);
                } else if (c == ')') {
                    while (s.peek() != -2)
                        sb.append(ops.charAt(s.pop()));
                    s.pop();
                } else {
                    sb.append(c);
                }
            }
            while (!s.isEmpty())
                sb.append(ops.charAt(s.pop()));

        } catch (EmptyStackException e) {
            throw new Exception("Invalid entry.");
        }
        return sb.toString();
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
        for (char c : postfix.toCharArray()) {
            int idx = ops.indexOf(c);
            if (idx != -1) {
                Expression r = expr.pop();
                Expression l = expr.pop();
                int opPrec = idx / 2;
                if (l.prec < opPrec)
                    l.ex = '(' + l.ex + ')';
                if (r.prec <= opPrec)
                    r.ex = '(' + r.ex + ')';
                expr.push(new Expression(l.ex, r.ex, "" + c));
            } else {
                expr.push(new Expression("" + c));
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

    private float applyOperator(float a, float b, char c) {
        switch (c) {
            case '+':
                return a + b;
            case '-':
                return b - a;
            case '*':
                return a * b;
            case '/':
                return b / a;
            default:
                return Float.NaN;
        }
    }
}
