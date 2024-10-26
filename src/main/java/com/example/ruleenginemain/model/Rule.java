package com.example.ruleenginemain.model;

import javax.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleString;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_node_id", referencedColumnName = "id")
    private Node rootNode;

    // Constructor
    public Rule(String ruleString, Node rootNode) {
        this.ruleString = ruleString;
        this.rootNode = rootNode;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getRuleString() {
        return ruleString;
    }

    public Node getRootNode() {
        return rootNode;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public boolean evaluate(java.util.Map<String, Object> data) {
        return rootNode.evaluate(data);
    }
}