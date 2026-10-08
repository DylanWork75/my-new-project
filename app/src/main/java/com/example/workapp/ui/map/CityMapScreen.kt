package com.example.workapp.ui.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.workapp.BuildConfig
import com.example.workapp.automation.AppAutomationAccessibilityService
import com.example.workapp.automation.KoogAutomationViewModel
import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

val DEFAULT_NORTH_AMERICA_CENTER = LatLng(32.0, -100.0)
const val DEFAULT_NORTH_AMERICA_ZOOM = 4.0f

@Composable
fun CityMapScreen(
    viewModel: MapViewModel,
    automationViewModel: KoogAutomationViewModel,
    modifier: Modifier = Modifier,
    onCitySelected: ((City) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    CityMapContent(
        uiState = uiState,
        automationViewModel = automationViewModel,
        onSelectCity = { city ->
            viewModel.selectCity(city)
            if (city != null) {
                onCitySelected?.invoke(city)
            }
        },
        onCountryFilterChange = { viewModel.setCountryFilter(it) },
        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onResetCamera = { viewModel.resetCameraToNorthAmerica() },
        onClearSelection = { viewModel.clearSelection() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityMapContent(
    uiState: MapUiState,
    automationViewModel: KoogAutomationViewModel,
    onSelectCity: (City?) -> Unit,
    onCountryFilterChange: (String?) -> Unit,
    onCategoryFilterChange: (CityCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onResetCamera: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val isTwoPane = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    val automationState by automationViewModel.uiState.collectAsState()
    var showAutomationDialog by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_NORTH_AMERICA_CENTER, DEFAULT_NORTH_AMERICA_ZOOM)
    }

    LaunchedEffect(uiState.cameraMoveEvent) {
        uiState.cameraMoveEvent?.let { target ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(target.latitude, target.longitude),
                    target.zoom
                ),
                durationMs = 1000
            )
        }
    }

    var showListBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "US & Mexico Cities Map",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAutomationDialog = true },
                        modifier = Modifier.testTag("OpenAutomationDialogButton")
                    ) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "Open screen automation")
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("CityCountBadge")
                    ) {
                        Text(
                            text = "${uiState.filteredCities.size} Cities",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("CityMapScreen")
    ) { innerPadding ->
        if (isTwoPane) {
            // Foldable / Tablet Two-Pane Side-by-Side Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Left / Center Pane: Interactive Map
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    InteractiveMapBox(
                        cities = uiState.filteredCities,
                        selectedCity = uiState.selectedCity,
                        cameraPositionState = cameraPositionState,
                        onMarkerClick = { city -> onSelectCity(city) },
                        onMapClick = { onClearSelection() }
                    )

                    FloatingActionButton(
                        onClick = onResetCamera,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .testTag("ResetCameraButton")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Reset Camera View"
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag("LoadingIndicator")
                        )
                    }
                }

                // Right Pane: City List or Selected City Detail Pane
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 3.dp
                ) {
                    if (uiState.selectedCity != null) {
                        // Display Detail View in Right Pane
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                IconButton(
                                    onClick = onClearSelection,
                                    modifier = Modifier.testTag("BackToListButton")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back to list"
                                    )
                                }
                                Text(
                                    text = "City Details",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("SelectedCityDetailCard"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                CityDetailContent(
                                    city = uiState.selectedCity!!,
                                    onDismiss = onClearSelection,
                                    onCenterCamera = { city -> onSelectCity(city) },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        // Display City List & Filter in Right Pane
                        CityListPane(
                            cities = uiState.filteredCities,
                            selectedCity = uiState.selectedCity,
                            searchQuery = uiState.searchQuery,
                            selectedCountry = uiState.selectedCountryFilter,
                            selectedCategory = uiState.selectedCategoryFilter,
                            onSearchQueryChange = onSearchQueryChange,
                            onCountryFilterChange = onCountryFilterChange,
                            onCategoryFilterChange = onCategoryFilterChange,
                            onSelectCity = { city -> onSelectCity(city) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Phone Single-Pane Layout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                InteractiveMapBox(
                    cities = uiState.filteredCities,
                    selectedCity = uiState.selectedCity,
                    cameraPositionState = cameraPositionState,
                    onMarkerClick = { city -> onSelectCity(city) },
                    onMapClick = { onClearSelection() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    SearchAndFilterBar(
                        searchQuery = uiState.searchQuery,
                        selectedCountry = uiState.selectedCountryFilter,
                        selectedCategory = uiState.selectedCategoryFilter,
                        onSearchQueryChange = onSearchQueryChange,
                        onCountryFilterChange = onCountryFilterChange,
                        onCategoryFilterChange = onCategoryFilterChange
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 130.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FloatingActionButton(
                        onClick = onResetCamera,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("ResetCameraButton")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Reset Camera View"
                        )
                    }

                    ExtendedFloatingActionButton(
                        onClick = { showListBottomSheet = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("OpenCityListButton")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatListNumbered,
                            contentDescription = "View List"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("List", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("LoadingIndicator")
                    )
                }

                SelectedCityDetailOverlay(
                    city = uiState.selectedCity,
                    onDismiss = onClearSelection,
                    onCenterCamera = { city -> onSelectCity(city) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (showListBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showListBottomSheet = false },
                    sheetState = sheetState
                ) {
                    CityListPane(
                        cities = uiState.filteredCities,
                        selectedCity = uiState.selectedCity,
                        searchQuery = uiState.searchQuery,
                        selectedCountry = uiState.selectedCountryFilter,
                        selectedCategory = uiState.selectedCategoryFilter,
                        onSearchQueryChange = onSearchQueryChange,
                        onCountryFilterChange = onCountryFilterChange,
                        onCategoryFilterChange = onCategoryFilterChange,
                        onSelectCity = { city ->
                            onSelectCity(city)
                            showListBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    )
                }
            }
        }

        if (showAutomationDialog) {
            KoogAutomationDialog(
                isRunning = automationState.isRunning,
                response = automationState.response,
                error = automationState.error,
                onRun = {
                    automationViewModel.runInstruction(it)
                    if (automationViewModel.canStartInstruction()) showAutomationDialog = false
                },
                onOpenAccessibilitySettings = {
                    AppAutomationAccessibilityService.openAccessibilitySettings(it)
                },
                onDismiss = { showAutomationDialog = false }
            )
        }
    }
}

@Composable
private fun KoogAutomationDialog(
    isRunning: Boolean,
    response: String?,
    error: String?,
    onRun: (String) -> Unit,
    onOpenAccessibilitySettings: (android.content.Context) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Screen automation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Koog uses OpenAI GPT-4o to choose among tap, swipe, and pinch tools. Coordinates are absolute screen pixels. Automation only runs while WorkApp is in front.")
                Text("Enable WorkApp screen automation in Android Accessibility settings before running a command.")
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("What should the app do?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("AutomationPromptField"),
                    enabled = !isRunning,
                    minLines = 2,
                    maxLines = 4
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                response?.let { Text(it) }
                if (isRunning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Running automation…", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onRun(prompt) }, enabled = !isRunning && prompt.isNotBlank()) {
                Text("Run")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onOpenAccessibilitySettings(context) }) {
                    Text("Accessibility settings")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

@Composable
fun InteractiveMapBox(
    cities: List<City>,
    selectedCity: City?,
    cameraPositionState: CameraPositionState,
    onMarkerClick: (City) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false
        )
    }
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("GoogleMapView")
                .semantics { contentDescription = "Interactive city map" },
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = { onMapClick() }
        ) {
            cities.forEach { city ->
                CustomCityMarkerComposable(
                    city = city,
                    isSelected = city.id == selectedCity?.id,
                    onMarkerClick = onMarkerClick
                )
            }
        }

        if (BuildConfig.MAPS_API_KEY.isBlank() || BuildConfig.MAPS_API_KEY == "DEFAULT_MAPS_API_KEY") {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .testTag("MissingMapsApiKeyMessage"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Map setup required", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Add a Google Maps API key to local.properties as MAPS_API_KEY=your_key, then rebuild the app.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    selectedCountry: String?,
    selectedCategory: CityCategory?,
    onSearchQueryChange: (String) -> Unit,
    onCountryFilterChange: (String?) -> Unit,
    onCategoryFilterChange: (CityCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search US & MX cities or states...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear Search",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("SearchTextField")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCountry == null,
                    onClick = { onCountryFilterChange(null) },
                    label = { Text("All Countries", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("FilterChipAllCountries")
                )

                FilterChip(
                    selected = selectedCountry == "US",
                    onClick = {
                        onCountryFilterChange(if (selectedCountry == "US") null else "US")
                    },
                    label = { Text("🇺🇸 US", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE3F2FD),
                        selectedLabelColor = Color(0xFF1565C0)
                    ),
                    modifier = Modifier.testTag("FilterChipUS")
                )

                FilterChip(
                    selected = selectedCountry == "MX",
                    onClick = {
                        onCountryFilterChange(if (selectedCountry == "MX") null else "MX")
                    },
                    label = { Text("🇲🇽 Mexico", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xE8E8F5E9),
                        selectedLabelColor = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.testTag("FilterChipMX")
                )

                Spacer(modifier = Modifier.width(4.dp))

                CityCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onCategoryFilterChange(if (isSelected) null else category)
                        },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier.testTag("FilterChipCategory_${category.name}")
                    )
                }
            }
        }
    }
}
