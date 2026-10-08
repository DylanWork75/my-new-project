package com.example.workapp.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.workapp.R
import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import com.example.workapp.ui.theme.WorkAppTheme
import java.text.NumberFormat
import java.util.Locale

fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lngDir = if (longitude >= 0) "E" else "W"
    return String.format(Locale.US, "%.4f° %s, %.4f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lngDir)
}

fun getCityHeaderImageUrl(city: City): String {
    val url = city.imageUrl
    if (url != null && url.isNotBlank()) {
        return url
    }
    return when (city.id) {
        "us-nyc" -> "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=800"
        "us-lax" -> "https://images.unsplash.com/photo-1580655653885-65763b2597d0?q=80&w=800"
        "us-chi" -> "https://images.unsplash.com/photo-1494522855154-9297ac14b55f?q=80&w=800"
        "us-sfo" -> "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?q=80&w=800"
        "us-mia" -> "https://images.unsplash.com/photo-1514214246283-d427a95c5d2f?q=80&w=800"
        "us-aus" -> "https://images.unsplash.com/photo-1531218150217-54595bc2b934?q=80&w=800"
        "us-sea" -> "https://images.unsplash.com/photo-1502175371642-14a743538b00?q=80&w=800"
        "us-las" -> "https://images.unsplash.com/photo-1581351123004-757df051db8e?q=80&w=800"
        "us-was" -> "https://images.unsplash.com/photo-1501469537000-0629a28892f2?q=80&w=800"
        "mx-mex" -> "https://images.unsplash.com/photo-1518659267384-514f50d17574?q=80&w=800"
        "mx-cun" -> "https://images.unsplash.com/photo-1510097467424-192d713fd8b2?q=80&w=800"
        "mx-gdl" -> "https://images.unsplash.com/photo-1565688842819-3e33b1e8d9a2?q=80&w=800"
        "mx-mty" -> "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=800"
        "mx-oax" -> "https://images.unsplash.com/photo-1569383746724-6f1b882b8f46?q=80&w=800"
        "mx-sma" -> "https://images.unsplash.com/photo-1588383848791-ebce4aa697dd?q=80&w=800"
        else -> if (city.country == "US") {
            "https://images.unsplash.com/photo-1477959858617-67f30ac4ce78?q=80&w=800"
        } else {
            "https://images.unsplash.com/photo-1512813195386-6cf811ad3542?q=80&w=800"
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.isBlank()
}

@Composable
fun SelectedCityDetailOverlay(
    city: City?,
    onDismiss: () -> Unit,
    onCenterCamera: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = city != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (city != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("SelectedCityDetailCard"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                CityDetailContent(
                    city = city,
                    onDismiss = onDismiss,
                    onCenterCamera = onCenterCamera,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CityDetailContent(
    city: City,
    onDismiss: (() -> Unit)?,
    onCenterCamera: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Photo Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(getCityHeaderImageUrl(city))
                    .crossfade(true)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .build(),
                contentDescription = city.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Top Badges (Flag & Close Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCountryFlagEmoji(city.country),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = city.countryName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .testTag("CloseCityDetailButton")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close details",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Bottom title overlay on header image
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("CityDetailName")
                )
                Text(
                    text = "${city.stateOrRegion}, ${city.countryName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.testTag("CityDetailRegion")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Badges row: Category, Rating, Population, Timezone
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Chip
            AssistChip(
                onClick = { },
                label = { Text(city.category.displayName, fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = city.category.getIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF8E1))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${city.rating}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                )
            }

            // Population
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = "Population",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = NumberFormat.getIntegerInstance().format(city.population),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (city.timeZone.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = "Timezone",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = city.timeZone,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Coordinates & Location Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Coordinates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coordinates: ${formatCoordinates(city.latitude, city.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = city.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (city.popularAttractions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Key Attractions",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                city.popularAttractions.forEach { attraction ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(attraction, fontSize = 12.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Focus Map & Get Directions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onCenterCamera(city) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("CenterCameraOnCityButton")
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Focus Map", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val gmmIntentUri = Uri.parse("geo:${city.latitude},${city.longitude}?q=${Uri.encode(city.name)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        try {
                            context.startActivity(genericIntent)
                        } catch (_: Exception) { }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("GetDirectionsButton")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Get Directions", fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityDetailContentPreview() {
    WorkAppTheme {
        CityDetailContent(
            city = City(
                id = "us-nyc",
                name = "New York City",
                country = "US",
                countryName = "United States",
                stateOrRegion = "New York",
                latitude = 40.7128,
                longitude = -74.0060,
                description = "The global epicenter of culture, finance, media, and theater.",
                category = CityCategory.METROPOLIS,
                iconIndicator = "location_city",
                population = 8335897,
                rating = 4.8,
                popularAttractions = listOf("Central Park", "Statue of Liberty", "Empire State Building"),
                timeZone = "EST"
            ),
            onDismiss = {},
            onCenterCamera = {}
        )
    }
}

