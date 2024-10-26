# rule-engine

A 3-tier rule engine application built with Spring Boot that helps organizations determine user eligibility based on various attributes through a simple web interface. The system uses Abstract Syntax Tree (AST) to represent and evaluate eligibility rules dynamically.

## Features

- Create and manage attributes with different data types (String, Number, Boolean)
- Define rules using a simple expression syntax
- Combine multiple rules using logical operators (AND, OR)
- Evaluate rules against provided data
- Support for custom user-defined functions
- RESTful API endpoints for rule management
- Built-in error handling and validation
- In-memory H2 database for quick testing

## Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- Spring Boot 2.x
- Git

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/vinay-gujjula/rule-engine.git
cd rule-engine
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Attributes

#### Create a New Attribute
```http
POST /api/attributes
```
Parameters:
- `name`: Attribute name
- `type`: Attribute type (STRING, NUMBER, BOOLEAN)

#### Get All Attributes
```http
GET /api/attributes
```

### Rules

#### Create a New Rule
```http
POST /api/rules
```
Request body:
```json
{
    "ruleString": "age > 30 AND department = 'Sales'"
}
```

#### Combine Rules
```http
POST /api/rules/combine
```
Request body:
```json
[1, 2]  // Rule IDs to combine
```

#### Evaluate Rule
```http
POST /api/rules/{ruleId}/evaluate
```
Request body:
```json
{
    "age": 35,
    "department": "Sales"
}
```

## Rule Syntax

Rules can be created using the following operators:
- Comparison: `>`, `<`, `=`, `!=`
- Logical: `AND`, `OR`

Example rule strings:
- `age > 30 AND department = 'Sales'`
- `salary > 50000 OR experience > 5`

## Database Configuration

The application uses an H2 in-memory database by default. Configuration can be found in `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password
```

Access the H2 console at: `http://localhost:8080/h2-console`

## Testing

Run the tests using:

```bash
mvn test
```

## Custom Functions

The rule engine supports registering custom functions for complex evaluations:

```java
ruleService.registerUserDefinedFunction("calculateBonus", data -> {
    double salary = (double) data.get("salary");
    int experience = (int) data.get("experience");
    return salary * 0.1 * experience;
});
```

## Error Handling

The application includes comprehensive error handling for:
- Invalid rule syntax
- Unknown attributes
- Missing data during evaluation
- Database errors

Errors are returned as HTTP responses with appropriate status codes and error messages.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot framework
- H2 Database
- JUnit and Mockito for testing

## Contact

Vinay Gujjula - [GitHub](https://github.com/vinay-gujjula)

Project Link: https://github.com/vinay-gujjula/rule-engine
