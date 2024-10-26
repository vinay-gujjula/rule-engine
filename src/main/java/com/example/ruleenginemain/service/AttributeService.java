package com.example.ruleenginemain.service;

import com.example.ruleenginemain.exception.RuleEngineException;
import com.example.ruleenginemain.model.Attribute;
import com.example.ruleenginemain.repository.AttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeService {

    private final AttributeRepository attributeRepository;

    @Autowired
    public AttributeService(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public Attribute createAttribute(String name, Attribute.AttributeType type) {
        if (attributeRepository.findByName(name).isPresent()) {
            throw new RuleEngineException("Attribute with name " + name + " already exists");
        }
        return attributeRepository.save(new Attribute(name, type));
    }

    public List<Attribute> getAllAttributes() {
        return attributeRepository.findAll();
    }

    /*public boolean isValidAttribute(String name) {
        return attributeRepository.findByName(name).isPresent();
    }*/
 // Update isValidAttribute method in AttributeService
    public boolean isValidAttribute(String name) {
        boolean isValid = attributeRepository.findByName(name).isPresent();
        if (!isValid) {
            throw new RuleEngineException("Invalid attribute: " + name + ". This attribute is not in the catalog.");
        }
        return true;
    }
}
