package com.startupsurveys.util

import com.google.common.primitives.UnsignedInteger
import com.google.common.primitives.UnsignedLong
import com.startupsurveys.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.xrpl.xrpl4j.client.XrplClient
import org.xrpl.xrpl4j.crypto.keys.Seed
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult
import org.xrpl.xrpl4j.model.transactions.*


class PaymentHelper {

    // https://github.com/XRPLF/xrpl4j
    // https://xrpl.org/xrp-testnet-faucet.html
    companion object {
        fun completePayment(address: String, amountXrp: String, callback: (SubmitResult<ImmutablePayment>?, error: Exception?) -> Unit) {
            // Construct a SignatureService that uses in-memory Keys (see SignatureService.java for alternatives).
            try {
                val signatureService = BcSignatureService();

//            val senderSeed = Seed.ed25519Seed(); // Random sender.

                val senderSeed = Seed.fromBase58EncodedSecret { BuildConfig.SENDER_SECRET }
                val senderPair = senderSeed.deriveKeyPair()
                val senderPublicKey = senderPair.publicKey();
                val senderPrivateKey = senderPair.privateKey();
                val senderAddress = senderPublicKey.deriveAddress();

                // Receiver (using secp256k1 key)
                val receiverAddress = Address.of(address);

                val payment = Payment.builder()
                    .account(senderAddress)
                    .amount(xrpStringToDrops(amountXrp))
                    .destination(receiverAddress)
                    .sequence(UnsignedInteger.ONE)
                    .fee(xrpStringToDrops("0.00001"))
                    .signingPublicKey(senderPublicKey)
                    .build();

                val signedTransaction = signatureService.sign(senderPrivateKey, payment);
                val result = xrplClient.submit(signedTransaction);

                callback(result, null)
            } catch (e: Exception) {
                callback(null, e)
            }
        }

        private fun xrpStringToDrops(xrp: String): XrpCurrencyAmount {
            val xrpDrops = xrp.toDouble() * CurrencyAmount.ONE_XRP_IN_DROPS
            // Convert to google common unsigned long.
            val xrpLong = UnsignedLong.valueOf(xrpDrops.toLong())
            return XrpCurrencyAmount.of(xrpLong)
        }


        val xrplClient = XrplClient("https://s.altnet.rippletest.net:51234".toHttpUrl())

    }
}