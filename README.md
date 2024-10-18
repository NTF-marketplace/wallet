# Wallet

### 주요 API

- 로그인과 회원가입
- 사용자 지갑 NFT를 조회
- CloseSea에서 사용하는 계좌 조회
- CloseSea에서 사용하는 계좌의 NFT 조회
- CloseSea에서 사용하는 계좌 로그 조회
- CloseSea에서의 계좌 입출금

### ERD
![image](https://github.com/user-attachments/assets/761c0227-7878-433c-b2a0-61561ea5477b)


### API Flow
![image](https://github.com/user-attachments/assets/88f1ef3e-b2f5-4e3a-b78a-20f6a76f9b19)


## Client API 명세 ( API-Gateway: 8443)

- 회원 가입 →  POST:  **/v1/signin**
    
    **Request :  클라이언트(프론트)에서 서명 된 message와** signature를 받아 검증 
    
    ```kotlin
    POST http://localhost:8443/v1/signin
    Content-Type: application/json
    
    {
      "address": "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
      "message": "Hello, MetaMask!",
      "signature": "0x5714c3d6a6773a614091a9ac81dc8f4f6a0219349ddb4010edb6595c47b158814a9265e2c17aa7f7cfe479636a96c9f93cd665b213cc76b005c4b742edb6b27c1c",
      "chain": "POLYGON_MAINNET"
    }
    
    ```
    
    **Response :**
    
    ```kotlin
    HTTP/1.1 200 
    {
      "data": {
        "wallet": {
          "id": 6,
          "address": "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
          "userId": 4,
          "chainType": "POLYGON_MAINNET",
          "balance": 0,
          "createdAt": 1727149712663,
          "updatedAt": 1727149713728
        },
        "tokens": {
          "accessToken": "eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcxNDk3MTQsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjcyMzYxMTQsImlhdCI6MTcyNzE0OTcxNH0.JNeReZQEqFOGIQ7DnLDI2iyR7dBLPc1ocoGqAGEAZPqheDRguvHKmhOOvj6tRZ1dJCAiDNwAuzUy1PBt7bQNg9xQmfexzWSRG2N9MVY2hA6HrA24PgdwPP13AGtJaBt86G4sFR4bE4MQ1hCaB-vCM45Wyoykh5rxbdRTKrDitN5Ploy7rgwUOWghUl_dpV1EPxVxbnkb6oDBQhgMUCc2lrGe1HKT-yB6euts16yilK5pratUIaDRcL4WmVP90YkU72FFmjwV9q6annTOKv7rD69pAzzypoGXNOi_BTIA070IYCaPFafoFKlp4DbR9e2iqLfqHwlkBsqIv5xOgn4uPw",
          "refreshToken": "eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcxNDk3MTQsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3Mjc1ODE3MTQsImlhdCI6MTcyNzE0OTcxNH0.P0fxkX_yMf2aDNqJYPDr2EOTWHVnNTSLWYuKfoqqkWBhfdrpoEl_OuQ_i-9Aqb0p4rqYVS658oRS1dNyguuzS0GHZcBKnpt-hdlBYK_2AlL16m54Y-6Iug_uuqtOQoTxOOSAorE56RNpp76OP6rnDlh4a1N7lKFGYfYu3p_z2txxPT61JTSVlVrJlnxljKcXhzT7uDUCHpHlpXlcv7PdXPWauzcfPB3VKb0N9o1t-1E3R0r0lJni56upgqR9zno_5xI4j4hgTLVW6KDvb26dOypLz8mTQdTQLGmJ4VwXje2sfIeho4zPgSni6f5rH6gqPPcWKadp7HR_CMHB_IzJUg"
        }
      },
      "error": null
    }
    ```
    
- 지갑조회 → GET:  **/v1/wallet/{chainType}**
    
    Request:
    
    ```kotlin
    GET http://localhost:8443/v1/wallet/POLYGON_MAINNET?address=0x01b72b4aa3f66f213d62d53e829bc172a6a72867
    Content-Type: application/json
    ```
    
    Response:
    
    ```kotlin
    {
      "balance": 0.0000,
      "account": {
        "chainType": "POLYGON_MAINNET",
        "balance": 0.0,
        "balanceToUsdt": 0
      }
    }
    ```
    
- 지갑 NFT 조회 → GET: /v1/wallet/nft?{chainType}&address&page&size
    
    Request:
    
    ```kotlin
    
    GET http://localhost:8443/v1/wallet/nft?chainType=POLYGON_MAINNET&address=0x01b72b4aa3f66f213d62d53e829bc172a6a72867&page=0&size=3
    Content-Type: application/json
    
    ```
    
    Response:
    
    ```kotlin
    {
      "data": {
        "content": [
          {
            "id": 11,
            "tokenId": "0",
            "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
            "contractType": "ERC721",
            "chainType": "POLYGON_MAINNET",
            "nftName": "NFT #3",
            "collectionName": "picasoo",
            "image": "https://ipfs.io/ipfs/Qma4ew7LmWiirCMh1UNAPKMnZdiGpEjJkmM9qy5YoLoCDd",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmYjY4Kqj2CDXkBkDz7ryy69tbKzUZDwTmwCGTFDoked6C"
          },
          {
            "id": 4,
            "tokenId": "0",
            "tokenAddress": "0xd3af8188ad25cacbf0aba62f7f4f2c5d22008cf5",
            "contractType": "ERC721",
            "chainType": "POLYGON_MAINNET",
            "nftName": "NFT #1",
            "collectionName": "Collection-7wtf1udep",
            "image": "https://ipfs.io/ipfs/QmYQThbqcB5KSwiKb3qMTyA31A4j2xi58YcupwDB3PxWoR",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmYQThbqcB5KSwiKb3qMTyA31A4j2xi58YcupwDB3PxWoR"
          },
          {
            "id": 1,
            "tokenId": "2",
            "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
            "contractType": "ERC721",
            "chainType": "POLYGON_MAINNET",
            "nftName": "NFT #4",
            "collectionName": "picasoo",
            "image": "https://ipfs.io/ipfs/QmYjY4Kqj2CDXkBkDz7ryy69tbKzUZDwTmwCGTFDoked6C",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmYjY4Kqj2CDXkBkDz7ryy69tbKzUZDwTmwCGTFDoked6C"
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 3,
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "paged": true,
          "unpaged": false
        },
        "last": false,
        "totalElements": 15,
        "totalPages": 5,
        "first": true,
        "size": 3,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "numberOfElements": 3,
        "empty": false
      },
      "error": null
    }
    ```
    
- 계좌 입금 → POST: /v1/account/auth/deposit
    
    Requset:
    
    ```kotlin
    POST http://localhost:8443/v1/account/auth/deposit
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    
    {
      "chainType": "POLYGON_AMOY",
      "transactionHash": "0xb1c4782e93c4d44c343b10b1e980f880cc36983dbfa11c55a2f56b53f5a79ffb"
    }
    ```
    
- 계좌 출금 ERC20 → POST: /v1/account/auth/withdraw/ERC20
    
    Request:
    
    ```kotlin
    
    POST http://localhost:8443/v1/account/auth/withdraw/ERC20
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    
    {
      "chainType": "POLYGON_AMOY",
      "amount": 1.2
    }
    ```
    
- 계좌 출금 ERC721 → POST: /v1/account/auth/withdraw/ERC721
    
    Request:
    
    ```kotlin
    POST http://localhost:8443/v1/account/auth/withdraw/ERC721
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    
    {
      "nftId": 18
    }
    ```
    
- 계좌 입출금 기록 조회: → GET: /v1/account/auth/logs
    
    Request:
    
    ```kotlin
    GET http://localhost:8443/v1/account/auth/logs
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    ```
    
    Response:
    
    ```kotlin
    {
      "data": {
        "content": [
          {
            "timestamp": 1727245616961,
            "accountType": "DEPOSIT",
            "transactionStatusType": "SUCCESS",
            "detail": {
              "nftResponse": null,
              "balance": 2.0,
              "transferType": "ERC20"
            }
          },
          {
            "timestamp": 1727247081382,
            "accountType": "WITHDRAW",
            "transactionStatusType": "SUCCESS",
            "detail": {
              "nftResponse": null,
              "balance": 1.2,
              "transferType": "ERC20"
            }
          },
          {
            "timestamp": 1727247511937,
            "accountType": "DEPOSIT",
            "transactionStatusType": "SUCCESS",
            "detail": {
              "nftResponse": {
                "id": 18,
                "tokenId": "10",
                "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
                "contractType": "ERC721",
                "chainType": "POLYGON_AMOY",
                "nftName": "NFT #du918uyzf1",
                "collectionName": "Collection-nrom9nvvu",
                "image": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H",
                "lastPrice": null,
                "collectionLogo": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H"
              },
              "balance": 0,
              "transferType": "ERC721"
            }
          },
          {
            "timestamp": 1727247663479,
            "accountType": "WITHDRAW",
            "transactionStatusType": "SUCCESS",
            "detail": {
              "nftResponse": {
                "id": 18,
                "tokenId": "10",
                "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
                "contractType": "ERC721",
                "chainType": "POLYGON_AMOY",
                "nftName": "NFT #du918uyzf1",
                "collectionName": "Collection-nrom9nvvu",
                "image": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H",
                "lastPrice": null,
                "collectionLogo": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H"
              },
              "balance": 0,
              "transferType": "ERC721"
            }
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 50,
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "unpaged": false,
          "paged": true
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 4,
        "first": true,
        "size": 50,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "numberOfElements": 4,
        "empty": false
      },
      "error": null
    }
    ```
    
- 계좌 조회 : → GET: /v1/account/auth
    
    Request:
    
    ```kotlin
    GET http://localhost:8443/v1/account/auth
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    ```
    
    Response:
    
    ```kotlin
    {
      "data": [
        {
          "chainType": "POLYGON_AMOY",
          "balance": 0.8,
          "balanceToUsdt": 0.30352
        },
        {
          "chainType": "POLYGON_MAINNET",
          "balance": 0.0,
          "balanceToUsdt": 0
        }
      ],
      "error": null
    }
    ```
    
- 
    
    Request:
    
    ```kotlin
    GET http://localhost:8443/v1/account/auth/nft
    Authorization: Bearer eyJraWQiOiIxOWJkMDRmOC1jNGRkLTQwZGYtYTc4NC1iY2Y5NTU5NmU2OTYiLCJhbGciOiJSUzI1NiJ9.eyJuYmYiOjE3MjcyNDQ5MTUsImFkZHJlc3MiOiIweDAxYjcyYjRhYTNmNjZmMjEzZDYyZDUzZTgyOWJjMTcyYTZhNzI4NjciLCJleHAiOjE3MjczMzEzMTUsImlhdCI6MTcyNzI0NDkxNX0.NZqqlru18_aYpvEvkTxL5kbjo-lMtKHeD8DFEcyH8tfUWom4TCMofwPtDMV5507gUY8zY3hABNW6UzDXG1qAw4QhIhEciO8dm9OvXJPxDYEQ5hzbwZXJ22k7Qugnsgu6MJ5l3AmoSDmefYv9DMF7V36nHsTLyxO4VDqlItCUPM-PQFMaXqv0CZOpvWsPcg_3GSNYj96zVhe1_gFa02EK7dAXEeB7wWYEzA83_Kkm7jd8Nxpcc2akjBV--Cg6r-ND46wbG62_vKrD_pSuSMvZLSwSec55VWLkEoGof6QTsGfqxQ9N0WCd5KfPnhqAiO8FzGPmem22yNxRvZ2zN_2CJA
    Content-Type: application/json
    ```
    
    Response:
    
    ```kotlin
    {
      "data": {
        "content": [
          {
            "id": 28,
            "tokenId": "6",
            "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
            "contractType": "ERC721",
            "chainType": "POLYGON_AMOY",
            "nftName": "NFT #13zqb6rd41",
            "collectionName": "Collection-nrom9nvvu",
            "image": "https://ipfs.io/ipfs/QmYDaHCkzrc4oWpXxfKrtLHj3VXWQjCuC68SmLvTPh3iye",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H"
          },
          {
            "id": 34,
            "tokenId": "1",
            "tokenAddress": "0xe96432772cb106a3541dc41426fa779b87319afe",
            "contractType": "ERC721",
            "chainType": "POLYGON_AMOY",
            "nftName": "NFT #92ilfdsxi1",
            "collectionName": "Collection-83hfexo9j",
            "image": "https://ipfs.io/ipfs/QmdjHmZ82WX39CYuGJ89x9AkgxXNmFMa1AbV57JYZc5FyW",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmbPjeDoM2LFoDSV1Zm1xpWPRqCjpHthQ337T9uqX1uiH3"
          },
          {
            "id": 21,
            "tokenId": "4",
            "tokenAddress": "0xa3784fe9104fdc0b988769fba7459ece2fb36eea",
            "contractType": "ERC721",
            "chainType": "POLYGON_AMOY",
            "nftName": "NFT #ixh73rsx11",
            "collectionName": "Collection-nrom9nvvu",
            "image": "https://ipfs.io/ipfs/QmSzHXuGwKEy1HiUYp3aHaZV9j1fm3F3i5nXsvrEQTma7S",
            "lastPrice": null,
            "collectionLogo": "https://ipfs.io/ipfs/QmaFJaoAtRAcbWWBsbT4yEBwFfAQ8DJWiL2XixoQmXsU9H"
          }
        ],
        "pageable": {
          "pageNumber": 0,
          "pageSize": 50,
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "paged": true,
          "unpaged": false
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 3,
        "first": true,
        "size": 50,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "numberOfElements": 3,
        "empty": false
      },
      "error": null
    }
    ```
