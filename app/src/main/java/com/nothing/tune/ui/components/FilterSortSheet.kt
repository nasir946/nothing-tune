package com.nothing.tune.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.data.SortOption

data class FilterSortState(
    val sortOption: SortOption = SortOption.ARTIST,
    val ascending: Boolean = true,
    val hiResOnly: Boolean = false,
    val losslessOnly: Boolean = false,
    val audiobooksOnly: Boolean = false,
    val selectedGenre: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(
    currentState: FilterSortState,
    onDismiss: () -> Unit,
    onApply: (FilterSortState) -> Unit
) {
    var sortOption by remember { mutableStateOf(currentState.sortOption) }
    var ascending by remember { mutableStateOf(currentState.ascending) }
    var hiResOnly by remember { mutableStateOf(currentState.hiResOnly) }
    var losslessOnly by remember { mutableStateOf(currentState.losslessOnly) }
    var audiobooksOnly by remember { mutableStateOf(currentState.audiobooksOnly) }

    val sortOptions = listOf(
        SortOption.ARTIST,
        SortOption.AUTHOR,
        SortOption.ALBUM,
        SortOption.YEAR,
        SortOption.GENRE,
        SortOption.DATE_ADDED,
        SortOption.SIZE,
        SortOption.DURATION,
        SortOption.CUSTOM
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color(0xFF141416)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                text = "FILTER & SORT",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Sort Options Section
            Text(
                text = "SORT BY",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortOptions.forEach { opt ->
                    val isSelected = sortOption == opt
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { sortOption = opt }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = opt.label,
                            color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Order Direction Section
            Text(
                text = "ORDER",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(Pair("Ascending ↑", true), Pair("Descending ↓", false)).forEach { (label, isAsc) ->
                    val isSelected = ascending == isAsc
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { ascending = isAsc }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Filter Criteria Section
            Text(
                text = "FILTER QUALITY & CATEGORY",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hi-Res
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (hiResOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (hiResOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { hiResOnly = !hiResOnly }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Hi-Res Only",
                        color = if (hiResOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (hiResOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Lossless
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (losslessOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (losslessOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { losslessOnly = !losslessOnly }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lossless Only",
                        color = if (losslessOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (losslessOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Audiobooks
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (audiobooksOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (audiobooksOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { audiobooksOnly = !audiobooksOnly }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Audiobooks Only",
                        color = if (audiobooksOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (audiobooksOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 4. Action Buttons (Apply & Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reset button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E22))
                        .border(1.dp, Color(0xFF2E2E32), RoundedCornerShape(24.dp))
                        .clickable {
                            sortOption = SortOption.ARTIST
                            ascending = true
                            hiResOnly = false
                            losslessOnly = false
                            audiobooksOnly = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Apply button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable {
                            onApply(
                                FilterSortState(
                                    sortOption = sortOption,
                                    ascending = ascending,
                                    hiResOnly = hiResOnly,
                                    losslessOnly = losslessOnly,
                                    audiobooksOnly = audiobooksOnly
                                )
                            )
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Apply",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
