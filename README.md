# Polynomial Processing API

This project demonstrates skills in API design, object-oriented programming (OOP), data structures, database integration, and testing. The API simplifies polynomial expressions, evaluates them for a given value of x, and caches results in a database to avoid redundant computations.

## Features

1. **Simplify Polynomial Expressions**  
   Accepts a polynomial string, simplifies it by combining like terms, and handles polynomial multiplication.
    - Example:
        - Input: `2*x^2 + 3*x - 5 + x^2 + x`
        - Output: `3*x^2 + 4*x - 5`

2. **Evaluate Simplified Polynomials**  
   Evaluates a simplified polynomial at a specific value of \(x\).
    - Example:
        - Input: Polynomial: `3*x^2 + 4*x - 5`, \(x = 2\)
        - Output: `15`

3. **Database Caching**  
   Stores all polynomial simplifications and evaluations in a PostgreSQL database.
    - Reuses cached results for identical inputs to improve efficiency.

## Requirements

- **Java**: 17+
- **Gradle**: 7.6.0+
- **PostgreSQL**: 14+ (Dockerized)
- **Docker**: 20.10+

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/RomenKost/polynomial-processing-api.git
cd polynomial-processing-api 
```
### 2. Run the Project
```bash
docker-compose up 
```
The API will be available at: http://localhost:8089
### 3. Endpoints
#### Simplify Polynomial
##### POST /api/polynomials/simplify

Simplifies a polynomial expression.

###### Request Body:
```json
{
   "polynomial": "2*x^2 + 3*x - 5 + x^2 + x"
}
```
###### Response:
```json
{
    "polynomial": "3*x^2 + 4*x - 5"
}
```
The API also handle polynomial multiplication as well.
###### Request Body:
```json
{
   "polynomial": "(x + 2) * (x - 1)"
}
```
###### Response:
```json
{
    "polynomial": "x^2 + x - 2"
}
```
#### Evaluate Polynomial
##### POST /api/polynomials/evaluate

Evaluate a polynomial expression.

###### Request Body:
```json
{
   "polynomial": "3*x^2 + 4*x - 5",
   "x": 2
}
```
###### Response:
```json
{
    "result": 15
}
```
### 4. Run Tests
```bash
./gradlew clean test
```
### 5. Stopping the Application
Stop the application and remove Docker containers:
```bash
docker-compose down
```



