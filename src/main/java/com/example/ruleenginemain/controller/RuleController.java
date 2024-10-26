package com.example.ruleenginemain.controller;

import com.example.ruleenginemain.exception.RuleEngineException;
import com.example.ruleenginemain.model.Rule;
import com.example.ruleenginemain.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final RuleService ruleService;

    @Autowired
    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRule(@RequestBody String ruleString) {
        try {
            Rule rule = ruleService.createRule(ruleString);
            return ResponseEntity.ok(rule);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> createRule(@RequestBody Map<String, String> request) {
        try {
            Rule rule = ruleService.createRule(request.get("ruleString"));
            return ResponseEntity.ok(rule);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/combine")
    public ResponseEntity<?> combineRules(@RequestBody List<Long> ruleIds) {
        try {
            Rule combinedRule = ruleService.combineRules(ruleIds);
            return ResponseEntity.ok(combinedRule);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{ruleId}/evaluate")
    public ResponseEntity<?> evaluateRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> data) {
        try {
            boolean result = ruleService.evaluateRule(ruleId, data);
            return ResponseEntity.ok(result);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}