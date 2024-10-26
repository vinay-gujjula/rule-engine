/*package com.example.ruleenginemain.model;

import java.util.function.Function;

public class UserDefinedFunction {
    private String name;
    private Function<Object, Object> function;

    public UserDefinedFunction(String name, Function<Object, Object> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Object apply(Object input) {
        return function.apply(input);
    }
}*/
package com.example.ruleenginemain.model;

import java.util.Map;
import java.util.function.Function;

public class UserDefinedFunction {
    private String name;
    private Function<Map<String, Object>, Object> function; // Adjusted type

    public UserDefinedFunction(String name, Function<Map<String, Object>, Object> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Object apply(Map<String, Object> input) { // Adjusted parameter
        return function.apply(input);
    }
}

