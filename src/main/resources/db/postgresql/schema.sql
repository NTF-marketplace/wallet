CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    nickName VARCHAR(255) NOT NULL
);


CREATE TYPE NetworkType AS ENUM ('ETHEREUM', 'POLYGON');


CREATE TABLE IF NOT EXISTS network (
    id SERIAL PRIMARY KEY,
    type NetworkType NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet (
    address VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(19, 4) NOT NULL,
    createdAt BIGINT,
    updatedAt BIGINT,
    userId BIGINT NOT NULL,
    networkId BIGINT NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    updatedAt BIGINT DEFAULT EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    CONSTRAINT fk_user
        FOREIGN KEY (userId)
        REFERENCES users(id),
    CONSTRAINT fk_network
        FOREIGN KEY (networkId)
        REFERENCES network(id)
    );
