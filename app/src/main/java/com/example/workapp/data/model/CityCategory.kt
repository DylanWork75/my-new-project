package com.example.workapp.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class CityCategory(
    val displayName: String,
    val iconName: String
) {
    METROPOLIS("Metropolis", "location_city"),
    COASTAL("Coastal & Beaches", "beach_access"),
    CULTURAL("Arts & Culture", "museum"),
    TECH_HUB("Tech & Innovation", "computer"),
    HISTORIC("Historic Landmark", "account_balance"),
    NATURE("Nature & Parks", "park"),
    RESORT("Resort & Leisure", "attractions"),
    BORDER_HUB("Border Hub", "swap_horiz")
}
