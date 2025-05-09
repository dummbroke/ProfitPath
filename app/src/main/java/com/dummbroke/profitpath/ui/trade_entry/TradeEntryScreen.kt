package com.dummbroke.profitpath.ui.trade_entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R // Assuming you'll add placeholder icons here
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

@Composable
fun TradeEntryScreen() {
    var tradedPair by remember { mutableStateOf("") }
    var selectedAssetClass by remember { mutableStateOf("Forex") } // Forex, Stocks, Crypto
    var specificAsset by remember { mutableStateOf("") }
    var strategy by remember { mutableStateOf("Scalping") } // Scalping, Swing Trading, Breakout
    var isWin by remember { mutableStateOf(true) }
    var isLong by remember { mutableStateOf(true) } // true for Long, false for Short
    var tradeDescription by remember { mutableStateOf(TextFieldValue("")) }
    var tradeDate by remember { mutableStateOf("Select Date") }
    var updateBalance by remember { mutableStateOf(false) }
    var balanceAmount by remember { mutableStateOf(TextFieldValue("")) }
    // Placeholder for image URI or Bitmap
    var selectedImageUri by remember { mutableStateOf<Any?>(null) }

    // New states for enhanced fields
    var entryPrice by remember { mutableStateOf(TextFieldValue("")) }
    var stopLossPrice by remember { mutableStateOf(TextFieldValue("")) }
    var takeProfitPrice by remember { mutableStateOf(TextFieldValue("")) }
    var preTradeRationale by remember { mutableStateOf(TextFieldValue("")) }
    var executionNotes by remember { mutableStateOf(TextFieldValue("")) }
    var postTradeReview by remember { mutableStateOf(TextFieldValue("")) }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMarketCondition by remember { mutableStateOf("Ranging") }

    val assetClasses = listOf("Forex", "Stocks", "Crypto")
    // Placeholder, will be dynamic based on selectedAssetClass and API
    val specificAssetsForForex = listOf("EUR/USD", "GBP/USD", "USD/JPY")
    val specificAssetsForStocks = listOf("AAPL", "MSFT", "GOOGL")
    val specificAssetsForCrypto = listOf("BTC/USD", "ETH/USD", "ADA/USD")

    val strategies = listOf("Scalping", "Swing Trading", "Breakout", "Position Trading", "Other")
    val marketConditionOptions = listOf("Bullish Trend", "Bearish Trend", "Ranging", "High Volatility", "Low Volatility", "News Event")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screenshot Upload Section
        item {
            ScreenshotUploadSection(selectedImageUri) {
                // Handle image selection logic
            }
        }

        // Input Fields Group
        item { SectionTitle("Trade Setup") }
        item { AssetClassSelector(selectedAssetClass, assetClasses) { selectedAssetClass = it } }
        item {
            SpecificAssetSelector(
                assetClass = selectedAssetClass,
                currentValue = specificAsset,
                forexAssets = specificAssetsForForex,
                stockAssets = specificAssetsForStocks,
                cryptoAssets = specificAssetsForCrypto,
                onValueChange = { specificAsset = it }
            )
        }
        item { StrategySelector(strategy, strategies) { strategy = it } }
        item { MarketConditionSelector(selectedMarketCondition, marketConditionOptions) { selectedMarketCondition = it } }
        item { PositionTypeToggle(isLong) { isLong = it } }

        item { SectionTitle("Price Levels") }
        item { PriceInputTextField(label = "Entry Price", value = entryPrice, onValueChange = { entryPrice = it }) }
        item { PriceInputTextField(label = "Stop-Loss Price", value = stopLossPrice, onValueChange = { stopLossPrice = it }) }
        item { PriceInputTextField(label = "Take-Profit Price (Optional)", value = takeProfitPrice, onValueChange = { takeProfitPrice = it }, imeAction = ImeAction.Next) }

        item { SectionTitle("Outcome & Notes") }
        item { WinLossToggle(isWin) { isWin = it } }
        item { DatePickerField(tradeDate) { /* Open Date Picker Dialog */ tradeDate = "2024-07-30" } } // Placeholder
        
        item { MultiLineTextField(label = "Pre-Trade Rationale / Setup", value = preTradeRationale, onValueChange = { preTradeRationale = it }) }
        item { MultiLineTextField(label = "Execution Notes", value = executionNotes, onValueChange = { executionNotes = it }) }
        item { MultiLineTextField(label = "Post-Trade Review / Lessons Learned", value = postTradeReview, onValueChange = { postTradeReview = it }) }
        
        item { OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma-separated)") },
            placeholder = { Text("e.g., FOMO, NewsPlay, GoodDiscipline") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )}

        item { SectionTitle("Optional Balance Update") }
        item { OptionalBalanceUpdate(updateBalance, balanceAmount, { updateBalance = it }, { balanceAmount = it }) }

        // Action Bar
        item {
            Spacer(modifier = Modifier.height(8.dp)) // Add some space before the final button
            Button(
                onClick = { /* Handle Save Trade */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Trade", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp) // Reduced bottom padding
    )
}

@Composable
fun ScreenshotUploadSection(
    selectedImageUri: Any?, // Can be Uri or placeholder
    onUploadClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onUploadClick() }
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                // TODO: Replace with actual image loading (e.g., Coil)
                // Image(painter = rememberImagePainter(selectedImageUri), contentDescription = "Trade Screenshot", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Text("Image Preview Area", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera, // Changed to PhotoCamera as CloudUpload might not be standard
                        contentDescription = "Upload Screenshot",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upload Screenshot", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetClassSelector(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Asset Class") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificAssetSelector(
    assetClass: String,
    currentValue: String,
    forexAssets: List<String>,
    stockAssets: List<String>,
    cryptoAssets: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val assetsToShow = when (assetClass) {
        "Forex" -> forexAssets
        "Stocks" -> stockAssets
        "Crypto" -> cryptoAssets
        else -> emptyList()
    }

    if (assetsToShow.isNotEmpty()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange, // Allow typing for search or use a read-only dropdown
                readOnly = assetsToShow.isNotEmpty(), // Make read-only if using dropdown selection
                label = { Text("Specific Asset (e.g., EUR/USD, AAPL, BTC/USD)") },
                trailingIcon = { if(assetsToShow.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text("Type or select asset") }
            )
            if (assetsToShow.isNotEmpty()){
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    assetsToShow.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    } else {
         OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            label = { Text("Specific Asset") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            placeholder = { Text("Enter asset symbol") }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategySelector(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Strategy") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketConditionSelector(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Market Condition") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PositionTypeToggle(isLong: Boolean, onToggle: (Boolean) -> Unit) {
    Column {
        Text("Position Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        SegmentedControl(
            items = listOf("Long", "Short"),
            selectedIndex = if (isLong) 0 else 1,
            onItemSelected = { index -> onToggle(index == 0) }
        )
    }
}


@Composable
fun WinLossToggle(isWin: Boolean, onToggle: (Boolean) -> Unit) {
    Column {
        Text("Outcome", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        SegmentedControl(
            items = listOf("Win", "Loss"),
            selectedIndex = if (isWin) 0 else 1,
            onItemSelected = { index -> onToggle(index == 0) },
            selectedColor = if (isWin) Color(0xFF26A69A) else Color(0xFFEF5350), // TradingView Green/Red
            unselectedColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PriceInputTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun MultiLineTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    lines: Int = 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = (lines * 36).dp), // Approximate height based on lines
        maxLines = lines + 1, // Allow slight overflow before scroll
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun DatePickerField(selectedDate: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text("Trade Date") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        trailingIcon = {
            Icon(
                Icons.Filled.CalendarToday,
                contentDescription = "Select Date",
                modifier = Modifier.clickable { onClick() }
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledTextColor = MaterialTheme.colorScheme.onSurface // Ensure text color is correct when readOnly
        )
    )
}

@Composable
fun OptionalBalanceUpdate(
    updateEnabled: Boolean,
    amount: TextFieldValue,
    onToggle: (Boolean) -> Unit,
    onAmountChange: (TextFieldValue) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Account Balance?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = updateEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        if (updateEnabled) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("New Account Balance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}


@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selectedIndex == index) selectedColor else unselectedColor)
                    .clickable { onItemSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = if (selectedIndex == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Trade Entry Screen Light")
@Composable
fun TradeEntryScreenPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        TradeEntryScreen()
    }
}

@Preview(showBackground = true, name = "Trade Entry Screen Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeEntryScreenPreviewDark() {
    ProfitPathTheme(darkTheme = true) {
        TradeEntryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenshotUploadSectionPreview() {
    ProfitPathTheme { Surface { ScreenshotUploadSection(null) {} } }
}

@Preview(showBackground = true)
@Composable
fun OptionalBalanceUpdatePreview() {
    ProfitPathTheme {
        Surface {
            var update by remember { mutableStateOf(true) }
            var text by remember { mutableStateOf(TextFieldValue("1000.00")) }
            OptionalBalanceUpdate(update,text, { update = it }, { text = it})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SegmentedControlPreview() {
    ProfitPathTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            var selectedIndex by remember { mutableStateOf(0) }
            SegmentedControl(
                items = listOf("Option 1", "Option 2"),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    }
} 