package com.example.workapp.ui.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Attractions
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Museum
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberMarkerState

fun CityCategory.getIcon(): ImageVector {
    return when (this) {
        CityCategory.METROPOLIS -> Icons.Rounded.Apartment
        CityCategory.COASTAL -> Icons.Rounded.BeachAccess
        CityCategory.CULTURAL -> Icons.Rounded.Museum
        CityCategory.TECH_HUB -> Icons.Rounded.Computer
        CityCategory.HISTORIC -> Icons.Rounded.AccountBalance
        CityCategory.NATURE -> Icons.Rounded.Park
        CityCategory.RESORT -> Icons.Rounded.Attractions
        CityCategory.BORDER_HUB -> Icons.AutoMirrored.Rounded.CompareArrows
    }
}

fun getCountryFlagEmoji(countryCode: String): String {
    val code = countryCode.uppercase()
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return "🌐"
    return code.map { letter ->
        String(Character.toChars(0x1F1E6 + (letter.code - 'A'.code)))
    }.joinToString(separator = "")
}

@Composable
fun CustomCityMarkerComposable(
    city: City,
    isSelected: Boolean,
    onMarkerClick: (City) -> Unit
) {
    val markerState = rememberMarkerState(
        key = city.id,
        position = LatLng(city.latitude, city.longitude)
    )

    MarkerComposable(
        state = markerState,
        title = city.name,
        snippet = "${city.stateOrRegion}, ${city.countryName}",
        zIndex = if (isSelected) 10f else 1f,
        onClick = {
            onMarkerClick(city)
            true // event consumed
        }
    ) {
        CityMarkerVisual(
            city = city,
            isSelected = isSelected,
            onClick = { onMarkerClick(city) }
        )
    }
}

@Composable
fun CityMarkerVisual(
    city: City,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.25f else 1.0f,
        label = "markerScale"
    )

    val countryBadgeColor = when (city.country.uppercase()) {
        "US" -> Color(0xFF1E88E5) // US Blue
        "MX" -> Color(0xFF2E7D32) // Mexico Green
        else -> MaterialTheme.colorScheme.primary
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        countryBadgeColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            shadowElevation = if (isSelected) 10.dp else 4.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = borderColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Country badge with flag
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(countryBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCountryFlagEmoji(city.country),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Category icon
                Icon(
                    imageVector = city.category.getIcon(),
                    contentDescription = city.category.displayName,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else countryBadgeColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // City Name
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Pointer tip triangle under pill
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 6.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else borderColor,
                    shape = GenericShape { size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                )
        )
    }
}
