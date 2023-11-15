package npo.kib.odc_demo.feature_app.data.p2p.connection_util

import com.upokecenter.cbor.CBORObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import npo.kib.odc_demo.common.core.getString
import npo.kib.odc_demo.common.core.loadPublicKey
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.PayloadContainer
import java.security.PublicKey
import java.util.UUID


object ObjectSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    //Kotlin Cbor serializer не преобразовывал вложенные классы в PayloadContainer,
    // поэтому используется сторонняя библиотека
    fun ByteArray.toPayloadContainer(): PayloadContainer {
        val cbor = CBORObject.DecodeFromBytes(this)
        return json.decodeFromString(cbor.ToJSONString())
    }

    fun PayloadContainer.toByteArray(): ByteArray {
        val jsonString = json.encodeToString(this)
        return CBORObject.FromJSONString(jsonString).EncodeToBytes()
    }

}

object UUIDSerializer : KSerializer<UUID?> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID? {
        val str = decoder.decodeString()
        return if (str == "null") null
        else UUID.fromString(str)
    }

    override fun serialize(encoder: Encoder, value: UUID?) {
        encoder.encodeString(value.toString())
    }
}

object UUIDSerializerNotNull : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object PublicKeySerializer : KSerializer<PublicKey?> {
    override val descriptor = PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PublicKey? {
        val str = decoder.decodeString()
        return if (str.isEmpty()) null
        else str.loadPublicKey()
    }

    override fun serialize(encoder: Encoder, value: PublicKey?) {
        if (value != null) {
            encoder.encodeString(value.getString())
        }
    }
}

object PublicKeySerializerNotNull : KSerializer<PublicKey> {
    override val descriptor = PrimitiveSerialDescriptor("PublicKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PublicKey = decoder.decodeString().loadPublicKey()

    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeString(value.getString())
    }
}