package thisissadeghi.kickoff.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Money

/**
 * Unified Denomination model for voucher products
 * Consolidates denomination models from productdetail, productlist, and orders features
 * Represents pricing/variant options for a voucher product
 */
@Serializable
data class Denomination(
    val id: Int,
    val sku: String,
    @SerialName("min_price")
    val minPrice: Money,
    @SerialName("max_price")
    val maxPrice: Money,
    @SerialName("selling_discount")
    val sellingDiscount: Double? = null,
    @SerialName("selling_activation_fee")
    val sellingActivationFee: Money? = null,
)
