package com.mobitechs.classapp.data.model.response

data class OfferBannerResponse(
    val message: String,
    val offerBanners: List<OfferBanner>,
    val status: Boolean,
    val status_code: Int
)


data class OfferBanner(
    val id: Int,
    val banner_type: String,
    val banner_url: String,
    val offer_code: String,
    val start_date: String,
    val end_date: String,

    val is_active: String,
    val created_at: String,
    val deleted_at: Any,
    val updated_at: String,
    val added_by: Any
)