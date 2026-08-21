package com.example.ui.components.mocktests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTestType
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.BrandWarmCream
import com.example.ui.theme.BrandTerracotta

@Composable
fun MockTestFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    platforms: List<String>,
    platformFilter: String?,
    onPlatformFilterChange: (String?) -> Unit,
    typeFilter: MockTestType?,
    onTypeFilterChange: (MockTestType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search mocks by name, platform, weak chapters...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mock_search_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Platform Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(platforms) { p ->
                val isSelected = (p == "All" && platformFilter == null) || (platformFilter.equals(p, ignoreCase = true))
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (p == "All") onPlatformFilterChange(null)
                        else onPlatformFilterChange(p)
                    },
                    label = { Text(p, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandForestGreen,
                        selectedLabelColor = BrandWarmCream
                    )
                )
            }
        }

        // Mock Type Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { onTypeFilterChange(null) },
                    label = { Text("All Types", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTerracotta,
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(MockTestType.values()) { type ->
                val isSelected = typeFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeFilterChange(type) },
                    label = { Text(type.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTerracotta,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
