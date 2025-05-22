package com.dummbroke.profitpath.ui.trade_entry

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.dummbroke.profitpath.ui.trade_asset.TradeAssetViewModel
import com.dummbroke.profitpath.ui.trade_asset.Asset
import com.google.firebase.auth.FirebaseAuth
import com.dummbroke.profitpath.ui.performance.PerformanceViewModel
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.Checkbox
import java.util.concurrent.TimeUnit
import com.dummbroke.profitpath.ui.navigation.AdMobBanner
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import kotlinx.coroutines.tasks.await

private const val PREFS_NAME = "TradeEntryGuidePrefs"
private const val KEY_LAST_SHOWN_DATE = "last_shown_date"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeEntryScreen(
    navController: NavController,
    tradeId: String? = null,
    tradeEntryViewModel: TradeEntryViewModel = viewModel()
) {
    var tradedPair by remember { mutableStateOf("") }
    var selectedAssetClass by remember { mutableStateOf("Forex") }
    var specificAsset by remember { mutableStateOf("") }
    var strategy by remember { mutableStateOf("Scalping") }
    var isWin by remember { mutableStateOf(true) }
    var isLong by remember { mutableStateOf(true) }
    var tradeDate by remember { mutableStateOf("Select Date") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var entryPrice by remember { mutableStateOf(TextFieldValue("")) }
    var stopLossPrice by remember { mutableStateOf(TextFieldValue("")) }
    var takeProfitPrice by remember { mutableStateOf(TextFieldValue("")) }
    var preTradeRationale by remember { mutableStateOf(TextFieldValue("")) }
    var executionNotes by remember { mutableStateOf(TextFieldValue("")) }
    var postTradeReview by remember { mutableStateOf(TextFieldValue("")) }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMarketCondition by remember { mutableStateOf("Ranging") }
    var pnlAmountStr by remember { mutableStateOf(TextFieldValue("")) }

    // New state variables
    var entryAmountUSDStr by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by tradeEntryViewModel.uiState.collectAsState()
    val editTrade by tradeEntryViewModel.editTradeState.collectAsState()

    // Observe clear form event from ViewModel
    LaunchedEffect(Unit) {
        tradeEntryViewModel.clearFormEvent.collect {
            // Reset all form fields to initial state
            tradedPair = ""
            selectedAssetClass = "Forex"
            specificAsset = ""
            strategy = "Scalping"
            isWin = true
            isLong = true
            tradeDate = "Select Date"
            selectedImageUri = null
            entryPrice = TextFieldValue("")
            stopLossPrice = TextFieldValue("")
            takeProfitPrice = TextFieldValue("")
            preTradeRationale = TextFieldValue("")
            executionNotes = TextFieldValue("")
            postTradeReview = TextFieldValue("")
            tags = TextFieldValue("")
            selectedMarketCondition = "Ranging"
            pnlAmountStr = TextFieldValue("")
            entryAmountUSDStr = TextFieldValue("")
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { pickedUri: Uri? ->
            if (pickedUri != null) {
                Log.d("TradeEntryScreen", "Image picked: $pickedUri")
                selectedImageUri = pickedUri
                Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("TradeEntryScreen", "Image picking cancelled or URI is null.")
                Toast.makeText(context, "Image picking cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Permission denied to read media.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun handleImageUploadClick() {
        when (ContextCompat.checkSelfPermission(context, permissionToRequest)) {
            PermissionChecker.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                permissionLauncher.launch(permissionToRequest)
            }
        }
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
    )

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        tradeDate = sdf.format(Date(it))
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val assetViewModel: TradeAssetViewModel = viewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userAssets by assetViewModel.assets.collectAsState()

    // --- Guide Dialog State and Logic ---
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var showGuideDialog by remember { mutableStateOf(false) }
    var dontShowAgainChecked by remember { mutableStateOf(false) }

    val lastShownTimestamp = remember { prefs.getLong(KEY_LAST_SHOWN_DATE, 0L) }
    val oneWeekInMillis = TimeUnit.DAYS.toMillis(7)

    LaunchedEffect(userId, userAssets, lastShownTimestamp) {
        // Show guide if no assets are added and it hasn't been shown this week
        val currentTime = System.currentTimeMillis()
        val shouldShow = userId.isNotBlank() && userAssets.isEmpty() &&
                         (currentTime - lastShownTimestamp > oneWeekInMillis)
        
        if (shouldShow) {
            showGuideDialog = true
        }
    }

    fun dismissGuideDialog() {
        if (dontShowAgainChecked) {
            prefs.edit().putLong(KEY_LAST_SHOWN_DATE, System.currentTimeMillis()).apply()
        }
        showGuideDialog = false
    }
    // --- End Guide Dialog State and Logic ---

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) assetViewModel.fetchAssets(userId)
    }

    val performanceViewModel: PerformanceViewModel = viewModel()
    val strategies by performanceViewModel.strategies.collectAsState()
    val strategyOptions = listOf(
        "Bullish Trading",
        "Bearish Trading",
        "Trend Trading",
        "Momentum Trading",
        "Mean Reversion",
        "Breakout Trading",
        "Reversal Trading",
        "Range Trading",
        "Gap Trading",
        "Price Action Trading",
        "News Trading",
        "Earnings Trading",
        "Merger & Acquisition Trading",
        "Central Bank Policy Trading",
        "Algorithmic Trading",
        "Arbitrage Trading",
        "High-Frequency Trading (HFT)",
        "Pairs Trading"
    )

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TradeEntryUiState.Success -> {
                snackbarHostState.showSnackbar("Trade saved successfully!")
                selectedImageUri = null
                tradeEntryViewModel.resetUiState()
            }
            is TradeEntryUiState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                tradeEntryViewModel.resetUiState()
            }
            is TradeEntryUiState.Loading -> {
                snackbarHostState.showSnackbar("Saving trade...")
            }
            TradeEntryUiState.Idle -> Unit
        }
    }

    LaunchedEffect(tradeId) {
        if (tradeId != null) {
            tradeEntryViewModel.loadTradeForEdit(tradeId)
        }
    }

    // Prefill form fields if editing
    LaunchedEffect(editTrade) {
        editTrade?.let { trade ->
            selectedAssetClass = trade.assetClass ?: "Forex"
            specificAsset = trade.specificAsset ?: ""
            strategy = trade.strategyUsed ?: "Scalping"
            isWin = (trade.outcome == "Win")
            isLong = (trade.positionType == "Long")
            tradeDate = trade.tradeDate?.toDate()?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "Select Date"
            selectedImageUri = null // You may want to handle screenshotPath differently
            entryPrice = TextFieldValue(trade.entryPrice?.toString() ?: "")
            stopLossPrice = TextFieldValue(trade.stopLossPrice?.toString() ?: "")
            takeProfitPrice = TextFieldValue(trade.takeProfitPrice?.toString() ?: "")
            preTradeRationale = TextFieldValue(trade.preTradeRationale ?: "")
            executionNotes = TextFieldValue(trade.executionNotes ?: "")
            postTradeReview = TextFieldValue(trade.postTradeReview ?: "")
            tags = TextFieldValue(trade.tags?.joinToString(", ") ?: "")
            selectedMarketCondition = trade.marketCondition ?: "Ranging"
            pnlAmountStr = TextFieldValue(trade.pnlAmount?.toString() ?: "")
            entryAmountUSDStr = TextFieldValue(trade.entryAmountUSD?.toString() ?: "")
        }
    }

    // Show image: if editing and no new image picked, use screenshotPath
    val imageToShow = selectedImageUri ?: editTrade?.screenshotPath?.let { Uri.parse(it) }

    // Navigate back to history after update in edit mode
    LaunchedEffect(uiState, tradeId) {
        if (tradeId != null && uiState is TradeEntryUiState.Success) {
            navController.popBackStack()
        }
    }

    var showLimitDialog by remember { mutableStateOf(false) }
    var tradeLimitReached by remember { mutableStateOf(false) }

    // --- Trade Limit Check ---
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            val firestore = FirebaseFirestore.getInstance()
            val tradesRef = firestore.collection("users").document(userId).collection("trades")
            val snapshot = tradesRef.get().await()
            if (snapshot.size() >= 5) {
                showLimitDialog = true
                tradeLimitReached = true
            }
        }
    }
    // --- End Trade Limit Check ---

    if (showGuideDialog) {
        AssetGuideDialog(
            guideSteps = remember { listOf(
                Pair(R.drawable.guide_1, "When adding a new trade entry, you need to specify the asset you traded. If the asset you need is not listed in the 'Select Asset' dropdown, you'll need to add it first."),
                Pair(R.drawable.guide_2, "To add a new asset, open the navigation drawer (burger menu) from the top bar and select 'Manage Assets'."),
                Pair(R.drawable.guide_3, "On the 'Manage Assets' screen, tap the '+' button in the top right corner to add a new asset."),
                Pair(R.drawable.guide_4, "Fill in the details for the new asset: enter the Asset Name (e.g., XAU/USD, AAPL) and select the Asset Type (Forex, Crypto, Stocks) from the dropdown."),
                Pair(R.drawable.guide_5, "Tap 'Save' to add the asset. Once saved, it will appear in your list of assets and will be available in the 'Select Asset' dropdown on the Trade Entry screen.")
            ) },
            dontShowAgainChecked = dontShowAgainChecked,
            onCheckedChange = { dontShowAgainChecked = it },
            onDismiss = { dismissGuideDialog() }
        )
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { /* Do nothing, force user to close with button */ },
            title = { Text("🎉 Congratulations! 🎉") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Thank you for participating in the Profit Path testing! You've reached the maximum of 5 trade entries for this version. But wait... Want to extend your entry limit? Click this BUTTON!", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                        context.startActivity(intent)
                    }) {
                        Text("🚀 Want to extend your entry limit? Click this BUTTON! 🚀", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showLimitDialog = false
                    navController.popBackStack()
                }) {
                    Text("Close")
                }
            }
        )
    } else {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize()) {
                AdMobBanner(modifier = Modifier.fillMaxWidth().height(50.dp))
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ScreenshotUploadSection(imageToShow, onUploadClick = ::handleImageUploadClick)
                    }

                    item { SectionTitle("Trade Setup") }
                    item { AssetClassSelector(selectedAssetClass, listOf("Forex", "Stocks", "Crypto")) { selectedAssetClass = it } }
                    item {
                        SpecificAssetSelector(
                            assetClass = selectedAssetClass,
                            currentValue = specificAsset,
                            userAssets = userAssets,
                            onValueChange = { specificAsset = it }
                        )
                    }
                    item { StrategySelector(strategy, strategyOptions) { strategy = it } }
                    item { MarketConditionSelector(selectedMarketCondition, listOf("Bullish Trend", "Bearish Trend", "Ranging", "High Volatility", "Low Volatility", "News Event")) { selectedMarketCondition = it } }
                    item { PositionTypeToggle(isLong) { isLong = it } }

                    item { SectionTitle("Price Levels") }
                    item { PriceInputTextField(label = "Entry Price", value = entryPrice, onValueChange = { entryPrice = it }) }
                    item { PriceInputTextField(label = "Stop-Loss Price", value = stopLossPrice, onValueChange = { stopLossPrice = it }) }
                    item { PriceInputTextField(label = "Take-Profit Price (Optional)", value = takeProfitPrice, onValueChange = { takeProfitPrice = it }, imeAction = ImeAction.Next) }

                    item { SectionTitle("Outcome & Notes") }
                    item { WinLossToggle(isWin) { isWin = it } }
                    item { PriceInputTextField(
                        label = "P&L Amount (e.g., 50.75 or -20.10)",
                        value = pnlAmountStr,
                        onValueChange = {pnlAmountStr = it},
                        imeAction = ImeAction.Next,
                        placeholder = "(Optional, for shadow trades)"
                    ) }
                    item { DatePickerField(tradeDate) { showDatePickerDialog = true } }

                    item { PriceInputTextField(
                        label = "Entry Amount (USD)",
                        value = entryAmountUSDStr,
                        onValueChange = { entryAmountUSDStr = it },
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                        placeholder = "(Optional, for shadow trades)"
                    ) }
                    
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

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (tradeId != null) {
                            Button(
                                onClick = {
                                    tradeEntryViewModel.updateTradeEntry(
                                        tradeId = tradeId,
                                        assetClass = selectedAssetClass,
                                        specificAsset = specificAsset,
                                        strategyUsed = strategy,
                                        marketCondition = selectedMarketCondition,
                                        positionType = if (isLong) "Long" else "Short",
                                        entryPriceStr = entryPrice.text,
                                        stopLossPriceStr = stopLossPrice.text,
                                        takeProfitPriceStr = takeProfitPrice.text,
                                        outcome = if (isWin) "Win" else "Loss",
                                        tradeDateStr = tradeDate,
                                        pnlAmountStr = pnlAmountStr.text,
                                        preTradeRationale = preTradeRationale.text,
                                        executionNotes = executionNotes.text,
                                        postTradeReview = postTradeReview.text,
                                        tagsStr = tags.text,
                                        selectedImageUri = selectedImageUri,
                                        entryAmountUSDStr = entryAmountUSDStr.text
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = uiState !is TradeEntryUiState.Loading
                            ) {
                                if (uiState is TradeEntryUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("Update", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    tradeEntryViewModel.saveTradeEntry(
                                        assetClass = selectedAssetClass,
                                        specificAsset = specificAsset,
                                        strategyUsed = strategy,
                                        marketCondition = selectedMarketCondition,
                                        positionType = if (isLong) "Long" else "Short",
                                        entryPriceStr = entryPrice.text,
                                        stopLossPriceStr = stopLossPrice.text,
                                        takeProfitPriceStr = takeProfitPrice.text,
                                        outcome = if (isWin) "Win" else "Loss",
                                        tradeDateStr = tradeDate,
                                        pnlAmountStr = pnlAmountStr.text,
                                        preTradeRationale = preTradeRationale.text,
                                        executionNotes = executionNotes.text,
                                        postTradeReview = postTradeReview.text,
                                        tagsStr = tags.text,
                                        selectedImageUri = selectedImageUri,
                                        entryAmountUSDStr = entryAmountUSDStr.text
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = uiState !is TradeEntryUiState.Loading
                            ) {
                                if (uiState is TradeEntryUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("Save Trade", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                AdMobBanner(modifier = Modifier.fillMaxWidth().height(50.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetGuideDialog(
    guideSteps: List<Pair<Int, String>>,
    dontShowAgainChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Add Assets") },
        text = {
            Column {
                // Display Image
                Image(
                    painter = painterResource(id = guideSteps[currentStep].first),
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit // Use Fit to prevent distortion
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Display Text Instruction
                Text(
                    text = "Step ${currentStep + 1}/${guideSteps.size}: ${guideSteps[currentStep].second}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { currentStep-- },
                        enabled = currentStep > 0
                    ) {
                        Text("Previous")
                    }
                    Button(
                        onClick = { currentStep++ },
                        enabled = currentStep < guideSteps.size - 1
                    ) {
                        Text("Next")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // "Don't show again" Checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dontShowAgainChecked,
                        onCheckedChange = onCheckedChange
                    )
                    Text("Don't show this guide again this week", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ScreenshotUploadSection(
    selectedImageUri: Uri?,
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
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                    contentDescription = "Trade Screenshot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
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

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp)
    )
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
    userAssets: List<Asset> = emptyList(),
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredUserAssets = userAssets.filter { it.type.equals(assetClass, ignoreCase = true) }
    val assetsToShow = filteredUserAssets.map { it.name }.distinct()
    if (assetsToShow.isNotEmpty()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                readOnly = assetsToShow.isNotEmpty(),
                label = { Text("Select Asset") },
                trailingIcon = { if(assetsToShow.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text("") }
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
            label = { Text("Select Asset") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            placeholder = { Text("") }
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
            selectedColor = if (isWin) Color(0xFF26A69A) else Color(0xFFEF5350),
            unselectedColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PriceInputTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Number,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        placeholder = { Text(placeholder) }
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
            .defaultMinSize(minHeight = (lines * 36).dp),
        maxLines = lines + 1,
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
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
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
    val context = LocalContext.current
    ProfitPathTheme(darkTheme = false) {
        TradeEntryScreen(navController = NavController(context))
    }
}

@Preview(showBackground = true, name = "Trade Entry Screen Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradeEntryScreenPreviewDark() {
    val context = LocalContext.current
    ProfitPathTheme(darkTheme = true) {
        TradeEntryScreen(navController = NavController(context))
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenshotUploadSectionPreview() {
    ProfitPathTheme { Surface { ScreenshotUploadSection(null) {} } }
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