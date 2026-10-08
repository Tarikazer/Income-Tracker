package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.HouseholdDialog
import com.example.ui.components.IncomeControlWalletIcon
import com.example.ui.theme.*
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.ThemeMode
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val clipboardManager = LocalClipboardManager.current
    val household by viewModel.household.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.language.collectAsStateWithLifecycle()
    val backupRestoreMessage by viewModel.backupRestoreMessage.collectAsStateWithLifecycle()

    var showHouseholdDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    // SAF launchers for Backup & Restore
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportDataToUri(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importDataFromUri(context, uri)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = EmeraldBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.settingsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.home,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Appearance & Theme (Bleu Ciel & Blanc)
            item {
                SettingsSectionCard(title = strings.appearanceSection, icon = Icons.Rounded.Palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = strings.themeModeLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )

                        // Theme Mode Selectors: Dark, Light, System
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionButton(
                                label = strings.themeDark,
                                icon = Icons.Rounded.DarkMode,
                                isSelected = themeMode == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK, context) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_dark_button"
                            )
                            ThemeOptionButton(
                                label = strings.themeLight,
                                icon = Icons.Rounded.LightMode,
                                isSelected = themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT, context) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_light_button"
                            )
                            ThemeOptionButton(
                                label = strings.themeSystem,
                                icon = Icons.Rounded.BrightnessAuto,
                                isSelected = themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM, context) },
                                modifier = Modifier.weight(1f),
                                testTag = "theme_system_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Ciel Blue & White Palette Showcase Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSurfaceElevated,
                            border = BorderStroke(1.dp, EmeraldCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.colorPaletteLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                    Text(
                                        text = strings.colorPaletteDescription,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldPrimary
                                        )
                                    )
                                }

                                // Color swatch bubbles
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(BrandPrimaryBlue)
                                            .border(1.dp, PureWhite.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(BrandLightBlue)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(PureWhite)
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Language (Français & English)
            item {
                SettingsSectionCard(title = strings.languageSection, icon = Icons.Rounded.Language) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LanguageOptionRow(
                            label = "Français",
                            sublabel = "French",
                            flag = "🇫🇷",
                            isSelected = currentLanguage == AppLanguage.FRENCH,
                            onClick = { viewModel.setLanguage(AppLanguage.FRENCH, context) },
                            testTag = "language_french_row"
                        )

                        LanguageOptionRow(
                            label = "English",
                            sublabel = "Anglais",
                            flag = "🇬🇧",
                            isSelected = currentLanguage == AppLanguage.ENGLISH,
                            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH, context) },
                            testTag = "language_english_row"
                        )
                    }
                }
            }

            // Section 3: Data & Local Backup (SAF)
            item {
                SettingsSectionCard(title = strings.dataSection, icon = Icons.Rounded.SettingsBackupRestore) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = strings.backupDescription,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val filename = "income_control_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                                    exportLauncher.launch(filename)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = if (themeMode == ThemeMode.LIGHT) Color.White else Color(0xFF0B1320)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_export_backup_button")
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.exportBackup, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                border = BorderStroke(1.dp, EmeraldPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = EmeraldPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_restore_backup_button")
                            ) {
                                Icon(Icons.Rounded.DownloadForOffline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.restoreBackup, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (backupRestoreMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldSurfaceElevated,
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = backupRestoreMessage!!,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearBackupRestoreMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Profile & Household
            item {
                SettingsSectionCard(title = strings.householdSection, icon = Icons.Rounded.Person) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${strings.householdName}: ${household.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${strings.currencyLabel}: ${household.currency}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary
                                )
                            )
                        }

                        TextButton(
                            onClick = { showHouseholdDialog = true },
                            modifier = Modifier.testTag("settings_edit_household_button")
                        ) {
                            Text(strings.edit, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 5: About App & Developer Contact
            item {
                SettingsSectionCard(title = strings.appInfoSection, icon = Icons.Rounded.Info) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IncomeControlWalletIcon(
                                modifier = Modifier.size(44.dp),
                                showBackgroundCanvas = false
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // "Income Control" and "Frost Dev" in one line with no wrap text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = strings.appName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f))
                                    ) {
                                        Text(
                                            text = "Frost Dev",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Only leave Version (no official logo & theme edition text)
                                Text(
                                    text = strings.versionLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Text(
                            text = strings.offlineSecure,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        )

                        HorizontalDivider(
                            color = EmeraldCardBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // Developer Contact Card (Frost Dev - +212693780909)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = EmeraldSurfaceElevated,
                            border = BorderStroke(1.dp, EmeraldCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("developer_contact_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Code,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.createdBy,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = strings.developerPhone,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = AccentOnSurface,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                // Quick Action Buttons: Call, WhatsApp, Copy
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Call Button
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${strings.developerPhone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, strings.developerPhone, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldPrimary,
                                            contentColor = if (themeMode == ThemeMode.LIGHT) Color.White else Color(0xFF0B1320)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("call_developer_button")
                                    ) {
                                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.call, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // 2. WhatsApp Button
                                    Button(
                                        onClick = {
                                            try {
                                                val cleanNumber = strings.developerPhone.replace("+", "").trim()
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNumber"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, strings.developerPhone, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF25D366),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1.15f)
                                            .testTag("whatsapp_developer_button")
                                    ) {
                                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.whatsapp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // 3. Copy Button
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(strings.developerPhone))
                                            Toast.makeText(context, strings.numberCopied, Toast.LENGTH_SHORT).show()
                                        },
                                        border = BorderStroke(1.dp, EmeraldCardBorder),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(0.95f)
                                            .testTag("copy_developer_phone_button")
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.copy, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHouseholdDialog) {
        HouseholdDialog(
            currentName = household.name,
            currentCurrency = household.currency,
            onDismiss = { showHouseholdDialog = false },
            onSave = { name, curr ->
                viewModel.updateHousehold(name, curr)
                showHouseholdDialog = false
            },
            onOpenBackupRestore = {
                showBackupRestoreDialog = true
            }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            onDismiss = { showBackupRestoreDialog = false },
            onExportBackup = {
                val filename = "income_control_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                exportLauncher.launch(filename)
            },
            onImportBackup = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            statusMessage = backupRestoreMessage,
            onClearStatus = { viewModel.clearBackupRestoreMessage() }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = EmeraldSurface,
        border = BorderStroke(1.dp, EmeraldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldPrimary.copy(alpha = 0.18f) else EmeraldSurfaceElevated,
        label = "theme_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldPrimary else EmeraldCardBorder,
        label = "theme_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldPrimary else TextSecondary,
        label = "theme_content"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(
    label: String,
    sublabel: String,
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.14f) else EmeraldSurfaceElevated,
        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else EmeraldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = flag, fontSize = 22.sp)
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = sublabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = EmeraldPrimary,
                    unselectedColor = TextMuted
                )
            )
        }
    }
}
