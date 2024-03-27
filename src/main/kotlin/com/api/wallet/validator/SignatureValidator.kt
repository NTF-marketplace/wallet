package com.api.wallet.validator

import com.api.wallet.controller.dto.request.ValidateRequest
import org.springframework.stereotype.Component
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.nio.charset.StandardCharsets

@Component
class SignatureValidator {

    fun verifySignature(request: ValidateRequest): Boolean {
        val signatureBytes = request.signature.removePrefix("0x").let {
            Numeric.hexStringToByteArray(it).takeIf { bytes -> bytes.size == 65 } ?: return false
        }
        val msgHash = Hash.sha3(("\u0019Ethereum Signed Message:\n${request.message.length}${request.message}")
            .toByteArray(StandardCharsets.UTF_8))

        val signatureData = Sign.SignatureData(signatureBytes[64], signatureBytes.sliceArray(0..31), signatureBytes.sliceArray(32..63))

        val publicKey = Sign.signedMessageHashToKey(msgHash, signatureData)
        val recoveredAddress = "0x${Keys.getAddress(publicKey)}"
        if(!recoveredAddress.equals(request.address, ignoreCase = true)) {
            return false
        }

        val recoveredMsgHash = Hash.sha3(("\u0019Ethereum Signed Message:\n${request.message.length}${request.message}")
            .toByteArray(StandardCharsets.UTF_8))
        val recoveredMessage = Numeric.toHexString(recoveredMsgHash)
        val actualMessageHash = Numeric.toHexString(msgHash)

        return recoveredMessage.equals(actualMessageHash, ignoreCase = true)
    }
}