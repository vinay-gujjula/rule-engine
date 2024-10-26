package com.example.ruleenginemain.model;

import javax.persistence.*;

@Entity
@Table(name = "nodes")
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "operator" for AND/OR, "operand" for conditions

    @ManyToOne
    @JoinColumn(name = "left_node_id")
    private Node left; // Reference to the left child

    @ManyToOne
    @JoinColumn(name = "right_node_id")
    private Node right; // Reference to the right child (for operators)

    private String value; // Optional value for operand nodes (e.g., number for comparisons)

    // Constructor
    public Node(String type, Node left, Node right, String value) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.value = value;
    }

    // Default constructor (required by JPA)
    public Node() {}

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // Method to evaluate the node
    public boolean evaluate(java.util.Map<String, Object> data) {
        if ("operator".equals(type)) {
            if ("AND".equals(value)) {
                return left.evaluate(data) && right.evaluate(data);
            } else if ("OR".equals(value)) {
                return left.evaluate(data) || right.evaluate(data);
            }
        } else if ("operand".equals(type)) {
            String[] parts = value.split(" ");
            String attribute = parts[0];
            String operator = parts[1];
            String compareValue = parts[2];

            Object attributeValue = data.get(attribute);
            if (attributeValue == null) {
                return false;
            }

            if (">".equals(operator)) {
                return Double.parseDouble(attributeValue.toString()) > Double.parseDouble(compareValue);
            }
            // Add more operators as needed
        }
        return false;
    }
}
