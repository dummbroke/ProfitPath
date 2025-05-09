package com.dummbroke.profitpath.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.theme.ProfitPathTheme
import androidx.compose.ui.graphics.Color

// --- Data Models (if needed, for complex settings) ---

// --- Main Settings Screen Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    // States for editable settings
    var traderName by remember { mutableStateOf(TextFieldValue("John Trader")) }
    var tradingStyle by remember { mutableStateOf("Day Trader") }
    var isDarkMode by remember { mutableStateOf(true) } // Assuming default is dark
    var currentBalance by remember { mutableStateOf(TextFieldValue("12550.75")) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Dummy data for display
    val loggedInUserEmail = "john.trader@example.com"
    val appVersion = "1.0.0-beta"
    val cloudSyncStatus = "Last synced: 10:35 AM"
    val screenshotCacheSize = "150.5 MB"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp) // Padding at the very bottom of the list
    ) {
        // Account Management Section
        item {
            SettingsSectionTitle("Account Management")
            SettingItem(iconRes = R.drawable.ic_settings_person_placeholder, title = "Logged in as", subtitle = loggedInUserEmail)
            SettingItem(iconRes = R.drawable.ic_settings_lock_reset_placeholder, title = "Change Password", isClickable = true, onClick = { /* TODO: Navigate to Change Password flow */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_logout_placeholder, title = "Logout", isClickable = true, onClick = { /* TODO: Handle Logout */ }) {}
            SettingItem(iconRes = R.drawable.ic_settings_delete_forever_placeholder, title = "Delete Account", titleColor = MaterialTheme.colorScheme.error, isClickable = true, onClick = { showDeleteAccountDialog = true }) {}
        }

        // Profile Customization Section
        item {
            SettingsSectionTitle("Profile Customization")
            EditableSettingItem(title = "Trader Name", value = traderName, onValueChange = { traderName = it })
            // TODO: Implement Dropdown for Trading Style
            SettingItem(iconRes = R.drawable.ic_settings_profile_style_placeholder, title = "Preferred Trading Style", subtitle = tradingStyle, isClickable = true, onClick = { /* TODO: Show dropdown */ }) {}
        }

        // Appearance & Display Section
        item {
            SettingsSectionTitle("Appearance & Display")
            SwitchSettingItem(iconRes = R.drawable.ic_settings_theme_mode_placeholder, title = "Dark Mode", checked = isDarkMode, onCheckedChange = { isDarkMode = it })
        }

        // Data & Sync Management Section
        item {
            SettingsSectionTitle("Data & Sync Management")
            EditableSettingItem(title = "Current Account Balance", value = currentBalance, onValueChange = { currentBalance = it }, keyboardType = KeyboardType.Number)
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

    if (showDeleteAccountDialog) {
        ConfirmationDialog(
            title = "Delete Account?",
            text = "Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = { /* TODO: Handle actual account deletion */ showDeleteAccountDialog = false },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showClearCacheDialog) {
        ConfirmationDialog(
            title = "Clear Screenshot Cache?",
            text = "This will remove all locally downloaded trade screenshots from your device. Paths in Firestore will remain. Are you sure?",
            confirmButtonText = "Clear Cache",
            onConfirm = { /* TODO: Handle actual cache clearing */ showClearCacheDialog = false },
            onDismiss = { showClearCacheDialog = false }
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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = (keyboardType == KeyboardType.Number), // Single line for numbers
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
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
                    onDismiss() // Usually dismiss after confirm
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