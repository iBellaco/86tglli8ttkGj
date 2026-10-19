package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.LauncherDatabase
import com.example.data.repository.LauncherRepository
import com.example.ui.components.AppActionDialog
import com.example.ui.components.HiddenAppsDialog
import com.example.ui.components.NewTabDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TvAppCard
import com.example.ui.components.TvCategoryTabs
import com.example.ui.components.TvQuickAccessDock
import com.example.ui.components.TvTopBar
import com.example.ui.components.TvWidgetsRow
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvCyanAccent
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = LauncherDatabase.getDatabase(applicationContext)
        val repo = LauncherRepository(applicationContext, db.launcherDao())

        setContent {
            val viewModel: LauncherViewModel = viewModel {
                LauncherViewModel(repo)
            }
            val uiState by viewModel.uiState.collectAsState()
            val categories by viewModel.categories.collectAsState()
            val dockApps by viewModel.dockApps.collectAsState()
            val displayedApps by viewModel.displayedApps.collectAsState()
            val hiddenApps by viewModel.hiddenApps.collectAsState()

            MyApplicationTheme(darkTheme = uiState.isNightModeActive) {
                // TV Back navigation handling
                BackHandler(
                    enabled = uiState.isVoiceSearchActive ||
                            uiState.selectedAppForActions != null ||
                            uiState.isSettingsOpen ||
                            uiState.isCloudSyncOpen ||
                            uiState.isNewTabDialogOpen ||
                            uiState.selectedTabId == "hidden" ||
                            uiState.searchQuery.isNotEmpty()
                ) {
                    when {
                        uiState.isVoiceSearchActive -> viewModel.closeVoiceSearch()
                        uiState.selectedAppForActions != null -> viewModel.selectAppForActions(null)
                        uiState.isSettingsOpen -> viewModel.closeSettings()
                        uiState.isCloudSyncOpen -> viewModel.closeCloudSync()
                        uiState.isNewTabDialogOpen -> viewModel.closeNewTabDialog()
                        uiState.searchQuery.isNotEmpty() -> viewModel.updateSearchQuery("")
                        uiState.selectedTabId == "hidden" -> viewModel.lockHiddenApps()
                    }
                }

                NovaTvLauncherScreen(
                    viewModel = viewModel,
                    categories = categories,
                    dockApps = dockApps,
                    displayedApps = displayedApps,
                    hiddenApps = hiddenApps
                )
            }
        }
    }
}

@Composable
fun NovaTvLauncherScreen(
    viewModel: LauncherViewModel,
    categories: List<com.example.data.model.CategoryTab>,
    dockApps: List<com.example.data.model.AppItem>,
    displayedApps: List<com.example.data.model.AppItem>,
    hiddenApps: List<com.example.data.model.AppItem>
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val backgroundBrush = remember(uiState.isNightModeActive) {
        if (uiState.isNightModeActive) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF090D16),
                    Color(0xFF0C1322),
                    Color(0xFF0A0F1D)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF1F5F9),
                    Color(0xFFE2E8F0),
                    Color(0xFFCBD5E1)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Bar (Clock, Weather, Energy Profile, Voice Search, Quick Action Buttons)
            TvTopBar(
                energyProfile = uiState.energyProfile,
                isNightMode = uiState.isNightModeActive,
                isHiddenUnlocked = uiState.isHiddenAppsUnlocked,
                onVoiceSearchClick = { viewModel.openVoiceSearch() },
                onToggleNightMode = { viewModel.toggleNightModeManual() },
                onToggleHiddenClick = { viewModel.selectTab("hidden") },
                onCloudSyncClick = { viewModel.openSettings() },
                onSettingsClick = { viewModel.openSettings() },
                onEnergyProfileClick = { viewModel.openSettings() }
            )

            // 2. Dynamic Widgets Row (Hardware Performance & Efficiency, TV Inputs)
            TvWidgetsRow(
                systemStats = uiState.systemStats,
                onOptimizeRamClick = { viewModel.optimizeRam(context) }
            )

            // 3. Quick Access Dock (1-click pinned favorite apps)
            TvQuickAccessDock(
                dockApps = dockApps,
                onLaunchApp = { app -> viewModel.launchApp(context, app) },
                onOpenMenu = { app -> viewModel.selectAppForActions(app) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Customizable Tabs Row
            TvCategoryTabs(
                categories = categories,
                selectedTabId = uiState.selectedTabId,
                onTabSelected = { tabId -> viewModel.selectTab(tabId) },
                onAddNewTabClick = { viewModel.openNewTabDialog() }
            )

            // Optional Search Bar display when active
            if (uiState.searchQuery.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TvCyanAccent) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        },
                        placeholder = { Text("Filtrando aplicaciones...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // 5. Main TV Apps Grid
            if (uiState.selectedTabId == "hidden" && !uiState.isHiddenAppsUnlocked) {
                // If user selected hidden tab while locked, prompt unlock dialog
                HiddenAppsDialog(
                    hiddenApps = hiddenApps,
                    isUnlocked = false,
                    pinError = uiState.pinError,
                    onUnlockWithPin = { pin -> viewModel.unlockHiddenAppsWithPin(pin) },
                    onLockAgain = { viewModel.lockHiddenApps() },
                    onUnhideApp = { app -> viewModel.toggleHideApp(app) },
                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                    onDismiss = { viewModel.selectTab("streaming") }
                )
            } else {
                if (displayedApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (uiState.selectedTabId == "hidden") "No hay aplicaciones ocultas" else "No se encontraron aplicaciones en esta categoría",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Usa el botón Menú del mando sobre cualquier app para moverla a esta pestaña",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 135.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayedApps, key = { it.packageName }) { app ->
                            TvAppCard(
                                app = app,
                                onLaunch = { selected -> viewModel.launchApp(context, selected) },
                                onOpenMenu = { selected -> viewModel.selectAppForActions(selected) }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs Overlay
        // 1. Voice Search Dialog
        if (uiState.isVoiceSearchActive) {
            VoiceSearchDialog(
                transcript = uiState.voiceTranscript,
                onVoiceResult = { result -> viewModel.processVoiceResult(context, result) },
                onDismiss = { viewModel.closeVoiceSearch() }
            )
        }

        // 2. App Action Dialog (DPad Menu / Long press)
        uiState.selectedAppForActions?.let { app ->
            AppActionDialog(
                app = app,
                categories = categories,
                onDismiss = { viewModel.selectAppForActions(null) },
                onLaunch = {
                    viewModel.selectAppForActions(null)
                    viewModel.launchApp(context, app)
                },
                onToggleHide = { viewModel.toggleHideApp(app) },
                onTogglePinDock = { viewModel.togglePinDock(app) },
                onToggleFavorite = { viewModel.toggleFavorite(app) },
                onChangeCategory = { catId -> viewModel.changeAppCategory(app, catId) }
            )
        }

        // 3. Settings & Cloud Sync Dialog
        if (uiState.isSettingsOpen) {
            SettingsDialog(
                energyProfile = uiState.energyProfile,
                isAutoNightMode = uiState.isAutoNightMode,
                isNightModeActive = uiState.isNightModeActive,
                cloudSyncMessage = uiState.cloudSyncMessage,
                onSetEnergyProfile = { viewModel.setEnergyProfile(it) },
                onToggleAutoNightMode = { viewModel.toggleAutoNightMode() },
                onToggleNightModeManual = { viewModel.toggleNightModeManual() },
                onOptimizeRam = { viewModel.optimizeRam(context) },
                onExportBackup = { callback -> viewModel.exportBackup(callback) },
                onImportBackup = { json -> viewModel.importBackup(json, context) },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // 4. Create New Category Tab Dialog
        if (uiState.isNewTabDialogOpen) {
            NewTabDialog(
                onAddTab = { title, iconName -> viewModel.addCustomCategory(title, iconName) },
                onDismiss = { viewModel.closeNewTabDialog() }
            )
        }
    }
}
