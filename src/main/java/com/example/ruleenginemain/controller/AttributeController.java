package com.example.ruleenginemain.controller;

import com.example.ruleenginemain.exception.RuleEngineException;
import com.example.ruleenginemain.model.Attribute;
import com.example.ruleenginemain.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attributes")
public class AttributeController {
    private final AttributeService attributeService;

    @Autowired
    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @PostMapping
    public ResponseEntity<?> createAttribute(@RequestParam String name, @RequestParam Attribute.AttributeType type) {
        try {
            Attribute attribute = attributeService.createAttribute(name, type);
            return ResponseEntity.ok(attribute);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Attribute>> getAllAttributes() {
        return ResponseEntity.ok(attributeService.getAllAttributes());
    }
}
