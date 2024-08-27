CREATE TYPE  chain_type AS ENUM (
    'ETHEREUM_MAINNET',
    'LINEA_MAINNET',
    'LINEA_SEPOLIA',
    'POLYGON_MAINNET',
    'ETHEREUM_HOLESKY',
    'ETHEREUM_SEPOLIA',
    'POLYGON_AMOY'
    );

CREATE TYPE account_type AS ENUM(
    'DEPOSIT',
    'WITHDRAW'
    );

CREATE TYPE transfer_type AS ENUM(
    'ERC20',
    'ERC721'
    );

CREATE TYPE my_enum AS ENUM(
    'ORANGE', 'APPLE'
    );


CREATE TYPE status_type AS ENUM(
    'RESERVATION', 'ACTIVED', 'NONE','LISTING','AUCTION', 'LEDGER'
    );


CREATE TABLE IF NOT EXISTS test (
    id SERIAL PRIMARY KEY,
    type my_enum not null
);


CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    nick_name VARCHAR(255) NOT NULL
    );

-- CREATE TABLE IF NOT EXISTS chain (
-- --     id SERIAL PRIMARY KEY,
--     type chain_type PRIMARY KEY
-- );

CREATE TABLE IF NOT EXISTS wallet (
    id SERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    created_At BIGINT,
    updated_At BIGINT,
    user_id BIGINT NOT NULL,
    chain_type chain_type,
    CONSTRAINT fk_users
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS nft (
    id BIGINT PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL,
    token_address VARCHAR(255) NOT NULL,
    chain_type chain_type
);

CREATE TABLE IF NOT EXISTS wallet_nft (
    id SERIAL PRIMARY KEY,
    wallet_id BIGINT REFERENCES wallet(id),
    nft_id BIGINT REFERENCES nft(id),
    amount INT
);

CREATE TABLE IF NOT EXISTS account (
    id SERIAL PRIMARY KEY,
    wallet_id BIGINT REFERENCES wallet(id),
    balance DECIMAL(19, 4) NOT NULL
);

CREATE TABLE IF NOT EXISTS account_nft (
    id SERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES account(id),
    nft_id BIGINT REFERENCES nft(id),
    status status_type not null
);

CREATE TABLE IF NOT EXISTS account_log (
    id SERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES account(id),
    nft_id BIGINT REFERENCES nft(id),
    timestamp BIGINT not null,
    account_type account_type not null,
    transfer_type transfer_type not null,
    balance DECIMAL(19, 4)
);


