package com.example.ruleenginemain.repository;

import com.example.ruleenginemain.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    // You can add custom query methods here if needed
}
