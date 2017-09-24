drop table user if exists;

CREATE TABLE USER(
  id BIGINT GENERATED by default as identity,
  username VARCHAR(40),
  name VARCHAR(20),
  age int(3),
  balance DECIMAL(10, 2),
  PRIMARY KEY(id)
);