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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.tune.data.SortOption
import com.nothing.tune.ui.theme.AccentRed

data class FilterSortState(
    val sortOption: SortOption = SortOption.ARTIST,
    val ascending: Boolean = true,
    val hiResOnly: Boolean = false,
    val losslessOnly: Boolean = false,
    val audiobooksOnly: Boolean = false,
    val specificYear: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val selectedGenre: String? = null,
    val minDurationSec: Int? = null,
    val maxDurationSec: Int? = null
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

    // Year states
    var specificYearInput by remember { mutableStateOf(currentState.specificYear?.toString() ?: "") }
    var minYearInput by remember { mutableStateOf(currentState.minYear?.toString() ?: "") }
    var maxYearInput by remember { mutableStateOf(currentState.maxYear?.toString() ?: "") }
    var yearFilterMode by remember {
        mutableStateOf(
            when {
                currentState.specificYear != null -> "Specific"
                currentState.minYear != null || currentState.maxYear != null -> "Range"
                else -> "All"
            }
        )
    }

    // Genre states
    var selectedGenre by remember { mutableStateOf(currentState.selectedGenre ?: "All") }
    val globalGenres = listOf(
        "All", "Rock", "Pop", "Jazz", "Electronic", "Classical", "Hip-Hop",
        "Lo-Fi", "R&B", "Metal", "Soundtrack", "Audiobook", "Blues", "Chill", "Folk", "Ambient"
    )

    // Duration states
    var durationPreset by remember {
        mutableStateOf(
            when {
                currentState.minDurationSec == null && currentState.maxDurationSec == 120 -> "< 2 min"
                currentState.minDurationSec == 120 && currentState.maxDurationSec == 300 -> "2 – 5 min"
                currentState.minDurationSec == 300 && currentState.maxDurationSec == 600 -> "5 – 10 min"
                currentState.minDurationSec == 600 -> "> 10 min"
                currentState.minDurationSec != null || currentState.maxDurationSec != null -> "Custom"
                else -> "All"
            }
        )
    }
    var minMinutesInput by remember { mutableStateOf(currentState.minDurationSec?.let { (it / 60).toString() } ?: "") }
    var maxMinutesInput by remember { mutableStateOf(currentState.maxDurationSec?.let { (it / 60).toString() } ?: "") }

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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
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
                fontSize = 11.sp,
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
                            .padding(horizontal = 14.dp, vertical = 7.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Order Direction Section
            Text(
                text = "ORDER",
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Pair("Ascending ↑", true), Pair("Descending ↓", false)).forEach { (label, isAsc) ->
                    val isSelected = ascending == isAsc
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { ascending = isAsc }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
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

            // 3. Genre Selector Section
            Text(
                text = "GENRE SELECTOR",
                color = Color(0xFF888888),
                fontSize = 11.sp,
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
                globalGenres.forEach { g ->
                    val isSelected = selectedGenre == g
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { selectedGenre = g }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = g,
                            color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Year Filter Section (Specific Year & Range of Years)
            Text(
                text = "YEAR FILTER",
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Specific", "Range").forEach { mode ->
                    val isSelected = yearFilterMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { yearFilterMode = mode }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (mode) {
                                "All" -> "All Years"
                                "Specific" -> "Specific Year"
                                else -> "Year Range"
                            },
                            color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (yearFilterMode == "Specific") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = specificYearInput,
                        onValueChange = { specificYearInput = it },
                        placeholder = { Text("e.g. 2024", color = Color.Gray, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF333336)
                        ),
                        modifier = Modifier.width(130.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("2024", "2023", "2022", "2020", "2015", "2010", "2000").forEach { y ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1E22))
                                    .clickable { specificYearInput = y }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = y, color = Color(0xFFAAAAAA), fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else if (yearFilterMode == "Range") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minYearInput,
                            onValueChange = { minYearInput = it },
                            placeholder = { Text("From Year (e.g. 2010)", color = Color.Gray, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0xFF333336)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxYearInput,
                            onValueChange = { maxYearInput = it },
                            placeholder = { Text("To Year (e.g. 2024)", color = Color.Gray, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0xFF333336)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("2020 – 2026", "2020", "2026"),
                            Triple("2010 – 2019", "2010", "2019"),
                            Triple("2000 – 2009", "2000", "2009"),
                            Triple("1990 – 1999", "1990", "1999")
                        ).forEach { (lbl, startY, endY) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1E22))
                                    .clickable {
                                        minYearInput = startY
                                        maxYearInput = endY
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = lbl, color = Color(0xFFAAAAAA), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Duration Range Section
            Text(
                text = "DURATION RANGE",
                color = Color(0xFF888888),
                fontSize = 11.sp,
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
                listOf("All", "< 2 min", "2 – 5 min", "5 – 10 min", "> 10 min", "Custom").forEach { dLabel ->
                    val isSelected = durationPreset == dLabel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                            .clickable { durationPreset = dLabel }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = dLabel,
                            color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (durationPreset == "Custom") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minMinutesInput,
                        onValueChange = { minMinutesInput = it },
                        placeholder = { Text("Min Minutes", color = Color.Gray, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF333336)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxMinutesInput,
                        onValueChange = { maxMinutesInput = it },
                        placeholder = { Text("Max Minutes", color = Color.Gray, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF333336)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Quality & Category Filters
            Text(
                text = "SPECIAL QUALITY & AUDIOBOOK",
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (hiResOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (hiResOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { hiResOnly = !hiResOnly }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Hi-Res Only",
                        color = if (hiResOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (hiResOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (losslessOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (losslessOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { losslessOnly = !losslessOnly }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Lossless Only",
                        color = if (losslessOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (losslessOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (audiobooksOnly) Color.White else Color(0xFF1E1E22))
                        .border(1.dp, if (audiobooksOnly) Color.White else Color(0xFF2E2E32), RoundedCornerShape(18.dp))
                        .clickable { audiobooksOnly = !audiobooksOnly }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Audiobooks Only",
                        color = if (audiobooksOnly) Color.Black else Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = if (audiobooksOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 7. Action Buttons (Apply & Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            yearFilterMode = "All"
                            specificYearInput = ""
                            minYearInput = ""
                            maxYearInput = ""
                            selectedGenre = "All"
                            durationPreset = "All"
                            minMinutesInput = ""
                            maxMinutesInput = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset All",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable {
                            val specY = if (yearFilterMode == "Specific") specificYearInput.toIntOrNull() else null
                            val minY = if (yearFilterMode == "Range") minYearInput.toIntOrNull() else null
                            val maxY = if (yearFilterMode == "Range") maxYearInput.toIntOrNull() else null

                            val (minD, maxD) = when (durationPreset) {
                                "< 2 min" -> Pair(null, 120)
                                "2 – 5 min" -> Pair(120, 300)
                                "5 – 10 min" -> Pair(300, 600)
                                "> 10 min" -> Pair(600, null)
                                "Custom" -> Pair(
                                    minMinutesInput.toIntOrNull()?.times(60),
                                    maxMinutesInput.toIntOrNull()?.times(60)
                                )
                                else -> Pair(null, null)
                            }

                            onApply(
                                FilterSortState(
                                    sortOption = sortOption,
                                    ascending = ascending,
                                    hiResOnly = hiResOnly,
                                    losslessOnly = losslessOnly,
                                    audiobooksOnly = audiobooksOnly,
                                    specificYear = specY,
                                    minYear = minY,
                                    maxYear = maxY,
                                    selectedGenre = if (selectedGenre == "All") null else selectedGenre,
                                    minDurationSec = minD,
                                    maxDurationSec = maxD
                                )
                            )
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Apply Filters",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}
