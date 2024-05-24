package com.api.wallet.enums


enum class ChainType{
        ETHEREUM_MAINNET,
        POLYGON_MAINNET,
        ETHREUM_GOERLI,
        ETHREUM_SEPOLIA,
        POLYGON_MUMBAI,
}

// ChainType과 NetwordType 통일하기,
enum class NetworkType{
        ETHEREUM,
        POLYGON,
}

enum class ContractType{
        ERC1155,
        ERC721
}

enum class StatusType { ACTIVE, DEACTIVE }
enum class AccountType{ DEPOSIT, WITHDRAW }

enum class TransferType {
        ERC20,ERC721
}
