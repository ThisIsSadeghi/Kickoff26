package thisissadeghi.kickoff.data.remote.network.adapters

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import thisissadeghi.kickoff.data.model.DescriptionItem

/**
 * This adapter is used to convert color hex field into [Long] field by marking them with [SerialName]
 */
object DescriptionsJsonAdapter : KSerializer<List<DescriptionItem>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DescriptionItem", PrimitiveKind.STRING)
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    override fun deserialize(decoder: Decoder): List<DescriptionItem> {
        val rawData = decoder.decodeString()
        if (rawData.isEmpty()) return persistentListOf()

        val array = json.parseToJsonElement(rawData).jsonObject["content"]?.jsonArray
        return array?.map {
            DescriptionItem(
                title = it.jsonObject["title"]?.jsonPrimitive?.content,
                description = it.jsonObject["description"]?.jsonPrimitive?.content,
            )
        } ?: persistentListOf()
    }

    override fun serialize(
        encoder: Encoder,
        value: List<DescriptionItem>,
    ) {
        encoder.encodeString(json.encodeToString(value))
    }
}
