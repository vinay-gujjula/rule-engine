package com.example.ruleenginemain.service;

import com.example.ruleenginemain.exception.RuleEngineException;
import com.example.ruleenginemain.model.Attribute;
import com.example.ruleenginemain.model.Node;
import com.example.ruleenginemain.model.Rule;
import com.example.ruleenginemain.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private AttributeService attributeService;

    @InjectMocks
    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRule() {
        String ruleString = "age > 30 AND department = 'Sales'";
        when(attributeService.isValidAttribute(anyString())).thenReturn(true);
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArguments()[0]);

        Rule result = ruleService.createRule(ruleString);

        assertNotNull(result);
        assertEquals(ruleString, result.getRuleString());
        assertNotNull(result.getRootNode());
        verify(ruleRepository).save(any(Rule.class));
    }

    @Test
    void testCreateRuleWithInvalidAttribute() {
        String ruleString = "invalidAttr > 30";
        when(attributeService.isValidAttribute("invalidAttr")).thenReturn(false);

        assertThrows(RuleEngineException.class, () -> ruleService.createRule(ruleString));
    }

    @Test
    void testCombineRules() {
        Rule rule1 = new Rule("age > 30", new Node("operator", new Node("operand", null, null, "age"), new Node("operand", null, null, "30"), ">"));
        Rule rule2 = new Rule("department = 'Sales'", new Node("operator", new Node("operand", null, null, "department"), new Node("operand", null, null, "Sales"), "="));
        
        when(ruleRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(rule1, rule2));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArguments()[0]);

        Rule result = ruleService.combineRules(Arrays.asList(1L, 2L));

        assertNotNull(result);
        assertEquals("age > 30 AND department = 'Sales'", result.getRuleString());
        assertNotNull(result.getRootNode());
        assertEquals("operator", result.getRootNode().getType());
        assertEquals("AND", result.getRootNode().getValue());
    }

    @Test
    void testEvaluateRule() {
        Rule rule = new Rule("age > 30 AND department = 'Sales'", 
            new Node("operator", 
                new Node("operator", new Node("operand", null, null, "age"), new Node("operand", null, null, "30"), ">"),
                new Node("operator", new Node("operand", null, null, "department"), new Node("operand", null, null, "Sales"), "="),
                "AND"
            )
        );
        
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        Map<String, Object> data = new HashMap<>();
        data.put("age", 35);
        data.put("department", "Sales");

        boolean result = ruleService.evaluateRule(1L, data);

        assertTrue(result);
    }

    @Test
    void testEvaluateRuleWithInvalidData() {
        Rule rule = new Rule("age > 30", new Node("operator", new Node("operand", null, null, "age"), new Node("operand", null, null, "30"), ">"));
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "John"); // Missing 'age' attribute

        assertThrows(RuleEngineException.class, () -> ruleService.evaluateRule(1L, data));
    }
}