package thisissadeghi.kickoff.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Currency

/**
 * Unified VoucherProduct model for all voucher product features
 * Consolidates ProductDetailResponse from productdetail and VoucherProduct from productlist
 * Works for both list and detail API endpoints
 *
 * Detail-specific fields (regionsDetail, categoriesDetail, description) are nullable
 * to support both list view and detail view scenarios
 */
@Serializable
data class VoucherProduct(
    val id: Int,
    val name: String? = null,
    val image: String? = null,
    val marketing: String? = null,
    val redeem: String? = null,
    @SerialName("legal_disclaimer")
    val legalDisclaimer: String? = null,
    val terms: String? = null,
    @SerialName("currency_detail")
    val currency: Currency? = null,
    @SerialName("regions_detail")
    val regionsDetail: List<Region>? = null,
    @SerialName("categories_detail")
    val categoriesDetail: List<Category>? = null,
    @SerialName("collection_id")
    val collectionId: Int? = null,
    val denominations: List<Denomination> = emptyList(),
    @SerialName("created_time")
    val createdTime: String? = null,
    @SerialName("updated_time")
    val updatedTime: String? = null,
    val description: String? = null,
)
