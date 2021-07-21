package npo.kib.odc_demo.data

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Deferred
import npo.kib.odc_demo.core.ISO_4217_CODE
import java.security.PublicKey
import java.sql.Time
import java.util.*

data class BokResponse(
    @SerializedName("bok")
    val bok: String,

    @SerializedName("code")
    val code: Int
)

data class WalletResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("sok_signature")
    val sokSignature: String,

    @SerializedName("wid")
    val wid: String,

    @SerializedName("message")
    val message: String
)

data class IssueResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("issued_banknotes")
    val issuedBanknotes: List<BanknoteRaw>,

    @SerializedName("message")
    val message: String
)

data class BanknoteRaw(
    val amount: Int,
    val bnid: String,
    val code: Int,
    val signature: String,
    val time: Int
)

data class ReceiveResponse(
    @SerializedName("magic")
    val magic: String,

    @SerializedName("time")
    val time: Int,

    @SerializedName("transaction_hash")
    val transactionHash: String,

    @SerializedName("transaction_hash_signed")
    val transactionHashSigned: String,

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: Int
)

data class WalletRequest(
    val sok: String
)

data class IssueRequest(
    val amount: Int,
    val wid: String
)

data class ReceiveRequest(
    @SerializedName("bnid")
    val bnid: String,

    @SerializedName("otok")
    val otok: String,

    @SerializedName("otok_signature")
    val otokSignature: String,

    @SerializedName("time")
    val time: Int,

    @SerializedName("transaction_signature")
    val transactionSign: String,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("wid")
    val wid: String

)
