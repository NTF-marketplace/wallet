package com.api.wallet.enums

enum class ChainType{
        ETHEREUM_MAINNET,
        LINEA_MAINNET,
        LINEA_SEPOLIA,
        POLYGON_MAINNET,
        ETHEREUM_HOLESKY,
        ETHEREUM_SEPOLIA,
        POLYGON_AMOY,

}

enum class ContractType{
        ERC1155,
        ERC721
}

enum class StatusType { RESERVATION, LISTING, RESERVATION_CANCEL, CANCEL, EXPIRED, NONE }
enum class AccountType{ DEPOSIT, WITHDRAW }

enum class TransferType {
        ERC20,ERC721
}

enum class MyEnum { ORANGE,APPLE }

enum class TokenType {
        MATIC, ETH
}


