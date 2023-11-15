/*
   Декларирование одного блока
 */

package npo.kib.odc_demo.common.core.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import npo.kib.odc_demo.common.core.Crypto
import npo.kib.odc_demo.common.core.checkHashes
import npo.kib.odc_demo.common.core.getStringPem
import npo.kib.odc_demo.feature_app.data.db.BlockchainConverter
import npo.kib.odc_demo.feature_app.data.p2p.connection_util.PublicKeySerializerNotNull
import npo.kib.odc_demo.feature_app.data.p2p.connection_util.UUIDSerializer
import npo.kib.odc_demo.feature_app.data.p2p.connection_util.UUIDSerializerNotNull
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.CustomType
import java.security.PublicKey
import java.util.UUID

@Serializable
@Entity(
    tableName = "block",
    foreignKeys = [ForeignKey(
        entity = BanknoteWithProtectedBlock::class,
        parentColumns = ["bnid"],
        childColumns = ["block_bnid"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["block_bnid"])]
)
@TypeConverters(BlockchainConverter::class)
data class Block(
    /*
    Блок публичного блокчейна к каждой банкноте
    */
    @PrimaryKey
    @Serializable(with = UUIDSerializerNotNull::class)
    val uuid: UUID,

    @Serializable(with = UUIDSerializer::class)
    val parentUuid: UUID?,

    // BankNote id
    @ColumnInfo(name = "block_bnid")
    val bnid: String,

    // One Time Open key
    @Serializable(with = PublicKeySerializerNotNull::class)
    val otok: PublicKey,

    val time: Int,
    val magic: String?,
    val transactionHash: String?,
    val transactionHashSignature: String?,
) : CustomType {
    fun makeBlockHashValue(): ByteArray {
        return if (parentUuid == null) {
            Crypto.hash(
                uuid.toString(), otok.getStringPem(), bnid, time.toString()
            )
        }
        else {
            Crypto.hash(
                uuid.toString(), parentUuid.toString(), otok.getStringPem(), bnid, time.toString()
            )
        }
    }

    fun verification(publicKey: PublicKey): Boolean {
        // publicKey -- otok or bok
        if (magic == null) {
            throw Exception("Блок не до конца определён. Не задан magic")
        }
        if (transactionHash == null) {
            throw Exception("Блок не до конца определён. Не задан hashValue")
        }
        if (transactionHashSignature == null) {
            throw Exception("Блок не до конца определён. Не задан signature")
        }

        val hashValueCheck = makeBlockHashValue()
        if (!checkHashes(hashValueCheck, transactionHash.encodeToByteArray())) {
            throw Exception("Некорректно подсчитан hashValue")
        }
        return Crypto.verifySignature(
            transactionHash.encodeToByteArray(),
            transactionHashSignature,
            publicKey
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Block

        if (uuid != other.uuid) return false
        if (parentUuid != other.parentUuid) return false
        if (bnid != other.bnid) return false
        if (otok != other.otok) return false
        if (time != other.time) return false
        if (magic != other.magic) return false
        if (transactionHash != null) {
            if (other.transactionHash == null) return false
            if (!transactionHash.contentEquals(other.transactionHash)) return false
        }
        else if (other.transactionHash != null) return false
        if (transactionHashSignature != other.transactionHashSignature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + (parentUuid?.hashCode() ?: 0)
        result = 31 * result + bnid.hashCode()
        result = 31 * result + otok.hashCode()
        result = 31 * result + time
        result = 31 * result + (magic?.hashCode() ?: 0)
        result = 31 * result + (transactionHash?.encodeToByteArray()?.contentHashCode() ?: 0)
        result = 31 * result + (transactionHashSignature?.hashCode() ?: 0)
        return result
    }
}
