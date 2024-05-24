CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    nick_name VARCHAR(255) NOT NULL
    );


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
    id BIGINT PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL,
    token_address VARCHAR(255) NOT NULL,
    network_type varchar(100) REFERENCES network(type),
    contract_type VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet_nft (
    id SERIAL PRIMARY KEY,
    wallet_id BIGINT REFERENCES wallet(id),
    nft_id BIGINT REFERENCES nft(id),
    amount INT
);

CREATE TABLE IF NOT EXISTS account (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    balance DECIMAL(19, 4) NOT NULL
);

CREATE TABLE IF NOT EXISTS accountNft (
    id SERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES account(id),
    nft_id BIGINT REFERENCES nft(id)
);

CREATE TABLE IF NOT EXISTS account_log (
    id SERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES account(id),
    nft_id BIGINT REFERENCES nft(id),
    timestamp BIGINT not null,
    account_type varchar(100) not null,
    transfer_type varchar(100) not null,
    balance DECIMAL(19, 4)
);

