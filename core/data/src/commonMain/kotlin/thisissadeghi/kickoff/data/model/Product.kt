package thisissadeghi.kickoff.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import thisissadeghi.kickoff.common.Currency

/**
 * Unified Product model for all product-related features
 * Consolidates productlist/Product and productdetail/ProductDetail
 * Works for both list and detail API endpoints
 * Created by Ali Sadeghi on 30,Dec,2024
 */

@Serializable
data class Product(
    val id: Int,
    val name: String? = null,
    val logo: String? = null,
    @SerialName("currency_detail")
    val currency: Currency? = null,
    val categories: List<Category>? = emptyList(),
    @SerialName("redeem_countries")
    val redeemCountries: List<Country>? = emptyList(),
    @SerialName("show_countries")
    val showCountries: List<Country>? = emptyList(),
    val description: String? = null,
    @SerialName("marketing_description")
    val marketingDescription: String? = null,
    @SerialName("redeem_description")
    val redeemDescription: String? = null,
    @SerialName("legal_disclaimer")
    val legalDisclaimer: String? = null,
    val terms: String? = null,
    val keyword: String? = null,
    @SerialName("is_favorite")
    val isFavorite: Boolean? = false,
    @SerialName("store_product_detail")
    val storeProductDetail: StoreProductDetail? = null,
    @SerialName("related_products")
    val relatedProducts: List<Int>? = emptyList(),
    @SerialName("provider_type")
    val providerType: Int? = null,
    @SerialName("provider_type_display")
    val providerTypeDisplay: String? = null,
)

/**
 * Store product detail information
 * Consolidated from multiple feature modules - most complete version
 */
@Serializable
data class StoreProductDetail(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    @SerialName("is_enable")
    val isEnable: Boolean? = null,
    val cashback: String? = null,
    val discount: String? = null,
    val metadata: String? = null,
    @SerialName("javascript_metadata")
    val javascriptMetadata: String? = null,
    @Contextual
    val description: List<DescriptionItem>? = null,
    @SerialName("marketing_description")
    val marketingDescription: String? = null,
    @SerialName("redeem_description")
    val redeemDescription: String? = null,
    @SerialName("legal_disclaimer")
    val legalDisclaimer: String? = null,
    val terms: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    @SerialName("meta_description")
    val metaDescription: String? = null,
    @SerialName("meta_keywords")
    val metaKeywords: String? = null,
    @SerialName("related_products")
    val relatedProducts: List<Int>? = emptyList(),
    @SerialName("store_varieties")
    val storeVarieties: List<StoreVariety>? = emptyList(),
    val popularity: Int? = null,
    @SerialName("loyalty_contents")
    val loyaltyContents: List<String>? = emptyList(),
    @SerialName("seo_title")
    val seoTitle: String? = null,
    val product: Int? = null,
)

/**
 * Store variety information
 * Consolidated from multiple feature modules - most complete version
 */
@Serializable
data class StoreVariety(
    val id: Int? = null,
    @SerialName("activation_fee")
    val activationFee: String? = null,
    @SerialName("variety_detail")
    val varietyDetail: VarietyDetail? = null,
    val suggestions: List<Double>? = emptyList(),
    @SerialName("price_step")
    val priceStep: Int? = null,
)

/**
 * Variety detail information
 * Consolidated from multiple feature modules - most complete version
 */
@Serializable
data class VarietyDetail(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("pricing_model")
    val pricingModel: Int? = null,
    @SerialName("pricing_model_display")
    val pricingModelDisplay: String? = null,
    @SerialName("min_price")
    val minPrice: String? = null,
    @SerialName("max_price")
    val maxPrice: String? = null,
    val sku: String? = null,
    val upc: String? = null,
    val suggestions: List<Double>? = emptyList(),
    @SerialName("price_step")
    val priceStep: Int? = null,
)
