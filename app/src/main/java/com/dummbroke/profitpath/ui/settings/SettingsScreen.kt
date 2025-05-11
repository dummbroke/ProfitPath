package com.dummbroke.profitpath.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme

// --- Data Models (if needed, for complex settings) ---
data class TradingStyleOption(val id: String, val displayName: String)

// --- Main Settings Screen Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // Observe states from ViewModel
    val userEmail by settingsViewModel.userEmail.collectAsState()
    val traderNameInput by settingsViewModel.traderName.collectAsState()
    val currentBalanceInput by settingsViewModel.currentBalance.collectAsState()
    val tradingStyle by settingsViewModel.tradingStyle.collectAsState()

    // Local states for text fields to allow immediate user input reflection
    // These will be initialized by the ViewModel's state and then used to update the ViewModel.
    var localTraderName by remember(traderNameInput) { mutableStateOf(TextFieldValue(traderNameInput)) }
    var localCurrentBalance by remember(currentBalanceInput) {
        mutableStateOf(TextFieldValue(if (currentBalanceInput == "0.0" || currentBalanceInput.isEmpty()) "0.00" else currentBalanceInput))
    }


    val showLogoutConfirmDialog by settingsViewModel.showLogoutConfirmDialog.collectAsState()
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showTradingStyleDialog by remember { mutableStateOf(false) }

    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    // Dummy data for display (to be replaced or augmented by ViewModel)
    // val loggedInUserEmail = "john.trader@example.com" // Replaced by userEmail state
    val appVersion = "1.0.0-beta" // This can remain or be moved to ViewModel if dynamic
    val cloudSyncStatus by settingsViewModel.cloudSyncStatus.collectAsState() // Assuming ViewModel provides this
    val screenshotCacheSize by settingsViewModel.screenshotCacheSize.collectAsState() // Assuming ViewModel provides this

    val tradingStyles = listOf(
        TradingStyleOption("scalper", "Scalper"),
        TradingStyleOption("day_trader", "Day Trader"),
        TradingStyleOption("swing_trader", "Swing Trader"),
        TradingStyleOption("position_trader", "Position Trader"),
        TradingStyleOption("investor", "Investor"),
        TradingStyleOption("other", "Other")
    )


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp) // Padding at the very bottom of the list
    ) {
        // Account Management Section
        item {
            SettingsSectionTitle("Account Management")
            SettingItem(
                iconRes = R.drawable.ic_settings_person_placeholder,
                title = "Logged in as",
                subtitle = userEmail ?: "Loading..." // Display email from ViewModel
            )
            SettingItem(iconRes = R.drawable.ic_settings_lock_reset_placeholder, title = "Change Password", isClickable = true, onClick = { /* TODO: Navigate to Change Password flow */ }) {}
            SettingItem(
                iconRes = R.drawable.ic_settings_logout_placeholder,
                title = "Logout",
                isClickable = true,
                onClick = { settingsViewModel.onLogoutClicked() }
            ) {}
            SettingItem(iconRes = R.drawable.ic_settings_delete_forever_placeholder, title = "Delete Account", titleColor = MaterialTheme.colorScheme.error, isClickable = true, onClick = { showDeleteAccountDialog = true }) {}
        }

        // Profile Customization Section
        item {
            SettingsSectionTitle("Profile Customization")
            EditableSettingItem(
                title = "Trader Name",
                value = localTraderName,
                onValueChange = { localTraderName = it },
                onSave = { settingsViewModel.updateTraderName(localTraderName.text) }
            )
            SettingItem(
                iconRes = R.drawable.ic_settings_profile_style_placeholder,
                title = "Preferred Trading Style",
                subtitle = tradingStyles.find { it.id == tradingStyle }?.displayName ?: "Select Style",
                isClickable = true,
                onClick = { showTradingStyleDialog = true }
            )
        }

        // Appearance & Display Section
        item {
            SettingsSectionTitle("Appearance & Display")
            SwitchSettingItem(
                iconRes = R.drawable.ic_settings_theme_mode_placeholder,
                title = "Dark Mode",
                checked = isDarkMode,
                onCheckedChange = { settingsViewModel.toggleTheme(it) }
            )
        }

        // Data & Sync Management Section
        item {
            SettingsSectionTitle("Data & Sync Management")
            EditableSettingItem(
                title = "Current Account Balance (USD)",
                value = localCurrentBalance,
                onValueChange = { localCurrentBalance = it },
                keyboardType = KeyboardType.Number,
                onSave = { 
                    val balanceValue = localCurrentBalance.text.toDoubleOrNull() ?: 0.0
                    settingsViewModel.updateCurrentBalance(balanceValue)
                }
            )
            SettingItem(iconRes = R.drawable.ic_settings_export_json_placeholder, title = "Export Trades to JSON", isClickable = true, onClick = { /* TODO: Handle JSON Export */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_export_csv_placeholder, title = "Export Trades to CSV", isClickable = true, onClick = { /* TODO: Handle CSV Export */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_cloud_sync_placeholder, title = "Cloud Sync Status", subtitle = cloudSyncStatus)
            SettingItem(iconRes = R.drawable.ic_settings_clear_cache_placeholder, title = "Screenshot Cache (${screenshotCacheSize})", subtitle = "Clear locally stored images", isClickable = true, onClick = { showClearCacheDialog = true }) {}
        }

        // Application Information & Support Section
        item {
            SettingsSectionTitle("Application Information & Support")
            SettingItem(iconRes = R.drawable.ic_settings_app_info_placeholder, title = "App Version", subtitle = appVersion)
            SettingItem(iconRes = R.drawable.ic_settings_privacy_policy_placeholder, title = "Privacy Policy", isClickable = true, onClick = { /* TODO: Open URL */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_terms_service_placeholder, title = "Terms of Service", isClickable = true, onClick = { /* TODO: Open URL */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_send_feedback_placeholder, title = "Send Feedback", isClickable = true, onClick = { /* TODO: Open Email/Form */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_rate_app_placeholder, title = "Rate App", isClickable = true, onClick = { /* TODO: Open Play Store */ }) {}
        }
    }

    if (showLogoutConfirmDialog) {
        ConfirmationDialog(
            title = "Logout?",
            text = "Are you sure you want to log out?",
            confirmButtonText = "Logout",
            onConfirm = { 
                settingsViewModel.onLogoutConfirmed()
                // Dialog is dismissed by ViewModel state change or explicit call after event handling
            },
            onDismiss = { settingsViewModel.onDismissLogoutDialog() }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmationDialog(
            title = "Delete Account?",
            text = "Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                settingsViewModel.deleteAccount() // ViewModel handles deletion
                showDeleteAccountDialog = false // Dismiss dialog here after initiating action
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showClearCacheDialog) {
        ConfirmationDialog(
            title = "Clear Screenshot Cache?",
            text = "This will remove all locally downloaded trade screenshots from your device. Paths in Firestore will remain. Are you sure?",
            confirmButtonText = "Clear Cache",
            onConfirm = {
                settingsViewModel.clearScreenshotCache() // ViewModel handles cache clearing
                showClearCacheDialog = false // Dismiss dialog here
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    if (showTradingStyleDialog) {
        TradingStyleSelectionDialog(
            currentStyleId = tradingStyle ?: tradingStyles.first().id, // Ensure a default if null
            tradingStyles = tradingStyles,
            onDismiss = { showTradingStyleDialog = false },
            onConfirm = { selectedStyleId ->
                settingsViewModel.updateTradingStyle(selectedStyleId)
                showTradingStyleDialog = false // Dismiss dialog after confirming
            }
        )
    }
}

// --- Reusable Setting Item Composables ---
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingItem(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable && onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (isClickable) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings_arrow_right_placeholder),
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSettingItem(
    @DrawableRes iconRes: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingItem(iconRes = iconRes, title = title, isClickable = false, trailingContent = {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        )
    })
}

@Composable
fun EditableSettingItem(
    title: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = (keyboardType == KeyboardType.Number || title == "Trader Name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                IconButton(onClick = onSave) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save_check_placeholder),
                        contentDescription = "Save $title",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmButtonText: String = "Confirm",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    // onDismiss() // Usually dismiss after confirm, but can be conditional
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (title.contains("Delete")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingStyleSelectionDialog(
    currentStyleId: String,
    tradingStyles: List<TradingStyleOption>,
    onDismiss: () -> Unit,
    onConfirm: (selectedStyleId: String) -> Unit
) {
    var selectedStyleId by remember(currentStyleId) { mutableStateOf(currentStyleId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true) // Allow dismissing by clicking outside
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp), // Consistent with other dialogs/cards
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Select Trading Style",
                    style = MaterialTheme.typography.headlineSmall, // Or titleLarge
                    modifier = Modifier.padding(bottom = 20.dp) // Increased padding
                )

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) { // Allow dialog to shrink if content is small
                    items(tradingStyles) { styleOption ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedStyleId = styleOption.id }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (styleOption.id == selectedStyleId),
                                onClick = { selectedStyleId = styleOption.id },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(styleOption.displayName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedStyleId) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "Settings Screen Light")
@Composable
fun SettingsScreenPreviewLight() {
    ProfitPathTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen()
        }
    }
}

@Preview(showBackground = true, name = "Settings Screen Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreviewDark() {
    ProfitPathTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen()
        }
    }
}

@Preview
@Composable
fun ConfirmationDialogPreview() {
    ProfitPathTheme {
        Surface {
             ConfirmationDialog(
                title = "Test Dialog",
                text = "This is a test confirmation dialog to see how it looks.",
                onConfirm = {}, onDismiss = {}
            )   
        }
    }
} 

@Preview(showBackground = true, name = "Trading Style Dialog")
@Composable
fun TradingStyleDialogPreview() {
    ProfitPathTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val tradingStyles = listOf(
                TradingStyleOption("scalper", "Scalper"),
                TradingStyleOption("day_trader", "Day Trader"),
                TradingStyleOption("swing_trader", "Swing Trader")
            )
            TradingStyleSelectionDialog(
                currentStyleId = "day_trader",
                tradingStyles = tradingStyles,
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

// Placeholder for the save icon, create this drawable resource
// e.g., res/drawable/ic_save_check_placeholder.xml
// <vector xmlns:android="http://schemas.android.com/apk/res/android"
//     android:width="24dp"
//     android:height="24dp"
//     android:viewportWidth="24"
//     android:viewportHeight="24"
//     android:tint="?attr/colorControlNormal">
//   <path
//       android:fillColor="@android:color/white"
//       android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"/>
// </vector>
// For now, you might need to use a built-in icon or temporarily remove the icon if it causes build errors.
// Consider using Icons.Filled.Check or Icons.Filled.Done if available and appropriate.
// For the preview, I used painterResource(id = R.drawable.ic_save_check_placeholder)
// You'll need to add an actual drawable resource with this name.
// A simple checkmark icon would suffice.
// Example: R.drawable.ic_settings_save_placeholder
// Or ensure you have a generic save or check icon.
// For the purpose of this edit, I will assume a placeholder `R.drawable.ic_save_check_placeholder` exists.
// If not, please replace `R.drawable.ic_save_check_placeholder` in `EditableSettingItem` with a valid drawable.
 