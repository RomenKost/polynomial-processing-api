CREATE TABLE IF NOT EXISTS polynomials (
    polynomial_id INT PRIMARY KEY,
    polynomial_request VARCHAR UNIQUE,
    simplified_polynomial VARCHAR,
    error_code VARCHAR
);

CREATE TABLE IF NOT EXISTS evaluations (
    evaluation_id INT PRIMARY KEY,
    polynomial_id INT REFERENCES polynomials,
    evaluation_request VARCHAR,
    evaluation_result INT,
    UNIQUE (polynomial_id, evaluation_request)
);

create sequence polynomial_seq start 1 increment 50;
create sequence evaluation_seq start 1 increment 50;
