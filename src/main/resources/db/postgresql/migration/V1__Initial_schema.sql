CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    nick_name VARCHAR(255) NOT NULL
    );


-- CREATE TYPE network_type AS ENUM ('ETHEREUM', 'POLYGON');


CREATE TABLE IF NOT EXISTS network (
--     id SERIAL PRIMARY KEY,
    type varchar(100) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS wallet (
    address VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(19, 4) NOT NULL,
    created_At BIGINT,
    updated_At BIGINT,
    user_id BIGINT NOT NULL,
    network_type varchar(100)  NOT NULL,
    CONSTRAINT fk_users
    FOREIGN KEY (user_id)
    REFERENCES users(id),
    CONSTRAINT fk_network
    FOREIGN KEY (network_type)
    REFERENCES network(type)
    );

CREATE TABLE IF NOT EXISTS nft (
    token_address VARCHAR(255) PRIMARY KEY,
    network_type varchar(100) REFERENCES network(type)
);

CREATE TABLE IF NOT EXISTS transaction (
    id SERIAL PRIMARY KEY,
    nft_id VARCHAR(255) REFERENCES nft(token_address),
    to_address VARCHAR(255) NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    amount INT,
    value NUMERIC,
    hash VARCHAR(255),
    block_timestamp BIGINT,
    wallet_id VARCHAR(255) REFERENCES wallet(address)
);

