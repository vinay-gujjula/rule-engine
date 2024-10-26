package com.example.ruleenginemain.service;

import com.example.ruleenginemain.exception.RuleEngineException;
import com.example.ruleenginemain.model.Node;
import com.example.ruleenginemain.model.Rule;
import com.example.ruleenginemain.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.example.ruleenginemain.model.UserDefinedFunction;


@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final AttributeService attributeService;

    @Autowired
    public RuleService(RuleRepository ruleRepository, AttributeService attributeService) {
        this.ruleRepository = ruleRepository;
        this.attributeService = attributeService;
    }

    public Rule createRule(String ruleString) {
        if (ruleString == null || ruleString.trim().isEmpty()) {
            throw new RuleEngineException("Rule string cannot be null or empty");
        }
        try {
            Node ast = parseRule(ruleString);
            Rule rule = new Rule(ruleString, ast);
            return ruleRepository.save(rule);
        } catch (Exception e) {
            throw new RuleEngineException("Error creating rule: " + e.getMessage(), e);
        }
    }

    public Rule combineRules(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.size() < 2) {
            throw new RuleEngineException("At least two rule IDs are required for combination");
        }
        try {
            List<Rule> rules = ruleRepository.findAllById(ruleIds);
            if (rules.size() != ruleIds.size()) {
                throw new RuleEngineException("One or more rules not found");
            }

            Node combinedAst = rules.get(0).getRootNode();
            for (int i = 1; i < rules.size(); i++) {
                combinedAst = new Node("operator", combinedAst, rules.get(i).getRootNode(), "AND");
            }

            String combinedRuleString = rules.stream()
                    .map(Rule::getRuleString)
                    .collect(Collectors.joining(" AND "));
            Rule combinedRule = new Rule(combinedRuleString, combinedAst);
            return ruleRepository.save(combinedRule);
        } catch (Exception e) {
            throw new RuleEngineException("Error combining rules: " + e.getMessage(), e);
        }
    }

    public boolean evaluateRule(Long ruleId, Map<String, Object> data) {
        if (ruleId == null) {
            throw new RuleEngineException("Rule ID cannot be null");
        }
        if (data == null || data.isEmpty()) {
            throw new RuleEngineException("Data for evaluation cannot be null or empty");
        }
        try {
            Rule rule = ruleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuleEngineException("Rule not found with ID: " + ruleId));
            return evaluateNode(rule.getRootNode(), data);
        } catch (Exception e) {
            throw new RuleEngineException("Error evaluating rule: " + e.getMessage(), e);
        }
    }

    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if ("operator".equals(node.getType())) {
            switch (node.getValue()) {
                case "AND":
                    return evaluateNode(node.getLeft(), data) && evaluateNode(node.getRight(), data);
                case "OR":
                    return evaluateNode(node.getLeft(), data) || evaluateNode(node.getRight(), data);
                case ">":
                    return compareValues(node.getLeft(), node.getRight(), data, (a, b) -> a > b);
                case "<":
                    return compareValues(node.getLeft(), node.getRight(), data, (a, b) -> a < b);
                case "=":
                    return compareValues(node.getLeft(), node.getRight(), data, Object::equals);
                case "!=":
                    return compareValues(node.getLeft(), node.getRight(), data, (a, b) -> !a.equals(b));
                default:
                    throw new RuleEngineException("Unknown operator: " + node.getValue());
            }
        } else {
            return true; // Leaf node (attribute or value) always evaluates to true
        }
    }

    private boolean compareValues(Node left, Node right, Map<String, Object> data, BiFunction<Double, Double, Boolean> comparator) {
        Object leftValue = getValue(left, data);
        Object rightValue = getValue(right, data);
        if (leftValue instanceof Number && rightValue instanceof Number) {
            return comparator.apply(((Number) leftValue).doubleValue(), ((Number) rightValue).doubleValue());
        }
        return comparator.apply(Double.parseDouble(leftValue.toString()), Double.parseDouble(rightValue.toString()));
    }

    /*private Object getValue(Node node, Map<String, Object> data) {
        if ("operand".equals(node.getType())) {
            if (data.containsKey(node.getValue())) {
                return data.get(node.getValue());
            } else {
                try {
                    return Double.parseDouble(node.getValue());
                } catch (NumberFormatException e) {
                    return node.getValue();
                }
            }
        }
        throw new RuleEngineException("Invalid node type for value extraction");
    }*/

    private Node parseRule(String rule) {
        Stack<Node> stack = new Stack<>();
        String[] tokens = rule.split("\\s+");

        for (String token : tokens) {
            if (token.equals("AND") || token.equals("OR")) {
                if (stack.size() < 2) {
                    throw new RuleEngineException("Invalid rule syntax: not enough operands for operator " + token);
                }
                Node right = stack.pop();
                Node left = stack.pop();
                stack.push(new Node("operator", left, right, token));
            } else if (token.equals(">") || token.equals("<") || token.equals("=") || token.equals("!=")) {
                stack.push(new Node("operator", null, null, token));
            } else {
                if (!attributeService.isValidAttribute(token) && !isNumeric(token)) {
                    throw new RuleEngineException("Invalid attribute or value: " + token);
                }
                stack.push(new Node("operand", null, null, token));
            }
        }

        if (stack.size() != 1) {
            throw new RuleEngineException("Invalid rule syntax: unbalanced expression");
        }

        return stack.pop();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

        public Rule modifyRule(Long ruleId, String newRuleString) {
            Rule existingRule = ruleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuleEngineException("Rule not found with ID: " + ruleId));
            
            Node newAst = parseRule(newRuleString);
            existingRule.setRuleString(newRuleString);
            existingRule.setRootNode(newAst);
            
            return ruleRepository.save(existingRule);
        }

        public Rule addCondition(Long ruleId, String condition, String operator) {
            Rule existingRule = ruleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuleEngineException("Rule not found with ID: " + ruleId));
            
            Node newCondition = parseRule(condition);
            Node newRoot = new Node("operator", existingRule.getRootNode(), newCondition, operator);
            
            existingRule.setRootNode(newRoot);
            existingRule.setRuleString("(" + existingRule.getRuleString() + ") " + operator + " " + condition);
            
            return ruleRepository.save(existingRule);
        }

        public Rule removeCondition(Long ruleId, String condition) {
            Rule existingRule = ruleRepository.findById(ruleId)
                    .orElseThrow(() -> new RuleEngineException("Rule not found with ID: " + ruleId));
            
            Node newRoot = removeNodeFromAST(existingRule.getRootNode(), condition);
            existingRule.setRootNode(newRoot);
            existingRule.setRuleString(generateRuleString(newRoot));
            
            return ruleRepository.save(existingRule);
        }

        private Node removeNodeFromAST(Node node, String condition) {
            if (node == null) return null;
            
            if (node.getType().equals("operand") && node.getValue().equals(condition)) {
                return null;
            }
            
            node.setLeft(removeNodeFromAST(node.getLeft(), condition));
            node.setRight(removeNodeFromAST(node.getRight(), condition));
            
            if (node.getLeft() == null && node.getRight() == null) {
                return null;
            }
            
            if (node.getLeft() == null) return node.getRight();
            if (node.getRight() == null) return node.getLeft();
            
            return node;
        }

        private String generateRuleString(Node node) {
            if (node == null) return "";
            if (node.getType().equals("operand")) return node.getValue();
            return "(" + generateRuleString(node.getLeft()) + " " + node.getValue() + " " + generateRuleString(node.getRight()) + ")";
        }
        
     // Add this method to RuleService
        private void validateRuleString(String ruleString) {
            String[] tokens = ruleString.split("\\s+");
            Stack<String> operatorStack = new Stack<>();
            Stack<String> operandStack = new Stack<>();

            for (String token : tokens) {
                if (token.equals("AND") || token.equals("OR")) {
                    if (operandStack.size() < 2) {
                        throw new RuleEngineException("Invalid rule syntax: not enough operands for operator " + token);
                    }
                    operandStack.pop();
                    operandStack.push("expression");
                    operatorStack.push(token);
                } else if (token.equals(">") || token.equals("<") || token.equals("=") || token.equals("!=")) {
                    if (operandStack.isEmpty()) {
                        throw new RuleEngineException("Invalid rule syntax: operator " + token + " missing left operand");
                    }
                    operatorStack.push(token);
                } else {
                    if (!attributeService.isValidAttribute(token) && !isNumeric(token)) {
                        throw new RuleEngineException("Invalid attribute or value: " + token);
                    }
                    operandStack.push(token);
                }
            }

            if (operandStack.size() != 1 || !operatorStack.isEmpty()) {
                throw new RuleEngineException("Invalid rule syntax: unbalanced expression");
            }
        }

        // Update createRule method to use validateRuleString
       /* public Rule createRule(String ruleString) {
            if (ruleString == null || ruleString.trim().isEmpty()) {
                throw new RuleEngineException("Rule string cannot be null or empty");
            }
            try {
                validateRuleString(ruleString);
                Node ast = parseRule(ruleString);
                Rule rule = new Rule(ruleString, ast);
                return ruleRepository.save(rule);
            } catch (Exception e) {
                throw new RuleEngineException("Error creating rule: " + e.getMessage(), e);
            }
        }*/
     // Add to RuleService class
       /* private Map<String, UserDefinedFunction> userDefinedFunctions = new HashMap<>();

        public void registerUserDefinedFunction(String name, Function<Object, Object> function) {
            userDefinedFunctions.put(name, new UserDefinedFunction(name, function));
        }

        // Update getValue method in RuleService
        private Object getValue(Node node, Map<String, Object> data) {
            if ("operand".equals(node.getType())) {
                if (data.containsKey(node.getValue())) {
                    return data.get(node.getValue());
                } else if (userDefinedFunctions.containsKey(node.getValue())) {
                    return userDefinedFunctions.get(node.getValue()).apply(data);
                } else {
                    try {
                        return Double.parseDouble(node.getValue());
                    } catch (NumberFormatException e) {
                        return node.getValue();
                    }
                }
            }
            throw new RuleEngineException("Invalid node type for value extraction");
        }*/
        private Map<String, UserDefinedFunction> userDefinedFunctions = new HashMap<>();

        public void registerUserDefinedFunction(String name, Function<Map<String, Object>, Object> function) {
            userDefinedFunctions.put(name, new UserDefinedFunction(name, function));
        }

        private Object getValue(Node node, Map<String, Object> data) {
            if ("operand".equals(node.getType())) {
                if (data.containsKey(node.getValue())) {
                    return data.get(node.getValue());
                } else if (userDefinedFunctions.containsKey(node.getValue())) {
                    return userDefinedFunctions.get(node.getValue()).apply(data); // Pass data map
                } else {
                    try {
                        return Double.parseDouble(node.getValue());
                    } catch (NumberFormatException e) {
                        return node.getValue();
                    }
                }
            }
            throw new RuleEngineException("Invalid node type for value extraction");
        }
  
}
