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
    id SERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
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
    id SERIAL PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL,
    token_address VARCHAR(255) NOT NULL,
    network_type varchar(100) REFERENCES network(type)
);

CREATE TABLE IF NOT EXISTS transaction (
    id SERIAL PRIMARY KEY,
    nft_id BIGINT REFERENCES nft(id),
    to_address VARCHAR(255) NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    amount INT,
    value NUMERIC,
    hash VARCHAR(255),
    block_timestamp BIGINT,
    wallet_id BIGINT REFERENCES wallet(id)
);

CREATE TABLE IF NOT EXISTS wallet_nft (
    id SERIAL PRIMARY KEY,
    wallet_id BIGINT REFERENCES wallet(id),
    nft_id BIGINT REFERENCES nft(id),
    amount INT
);

