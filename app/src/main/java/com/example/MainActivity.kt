package com.example

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.components.TabAppAssignmentDialog
import com.example.ui.components.TabEditDialog
import com.example.ui.components.TvAppCard
import com.example.ui.components.TvCategoryTabs
import com.example.ui.components.TvQuickAccessDock
import com.example.ui.components.TvTabManagerDialog
import com.example.ui.components.TvTopBar
import com.example.ui.components.TvWidgetsRow
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvCyanAccent
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.TabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = LauncherDatabase.getDatabase(applicationContext)
        val repo = LauncherRepository(applicationContext, db.launcherDao())

        setContent {
            val launcherViewModel: LauncherViewModel = viewModel {
                LauncherViewModel(repo)
            }
            val tabViewModel: TabViewModel = viewModel {
                TabViewModel(repo)
            }

            val uiState by launcherViewModel.uiState.collectAsState()
            val tabUiState by tabViewModel.uiState.collectAsState()
            val categories by tabViewModel.categories.collectAsState()
            val tabGroups by tabViewModel.tabGroups.collectAsState()
            val dockApps by launcherViewModel.dockApps.collectAsState()
            val displayedApps by launcherViewModel.displayedApps.collectAsState()
            val hiddenApps by launcherViewModel.hiddenApps.collectAsState()
            val allVisibleApps by tabViewModel.allVisibleApps.collectAsState()

            val context = LocalContext.current

            // Toast feedback
            LaunchedEffect(tabUiState.toastMessage) {
                tabUiState.toastMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    tabViewModel.clearToast()
                }
            }

            MyApplicationTheme(darkTheme = uiState.isNightModeActive) {
                // TV Back navigation handling
                BackHandler(
                    enabled = uiState.isVoiceSearchActive ||
                            uiState.selectedAppForActions != null ||
                            uiState.isSettingsOpen ||
                            uiState.isCloudSyncOpen ||
                            uiState.selectedTabId == "hidden" ||
                            uiState.searchQuery.isNotEmpty() ||
                            tabUiState.isTabManagerOpen ||
                            tabUiState.isCreateTabOpen ||
                            tabUiState.tabBeingEdited != null ||
                            tabUiState.tabBeingManagedApps != null
                ) {
                    when {
                        tabUiState.tabBeingManagedApps != null -> tabViewModel.closeManageAppsDialog()
                        tabUiState.tabBeingEdited != null -> tabViewModel.closeEditTabDialog()
                        tabUiState.isCreateTabOpen -> tabViewModel.closeCreateTabDialog()
                        tabUiState.isTabManagerOpen -> tabViewModel.closeTabManager()
                        uiState.isVoiceSearchActive -> launcherViewModel.closeVoiceSearch()
                        uiState.selectedAppForActions != null -> launcherViewModel.selectAppForActions(null)
                        uiState.isSettingsOpen -> launcherViewModel.closeSettings()
                        uiState.isCloudSyncOpen -> launcherViewModel.closeCloudSync()
                        uiState.searchQuery.isNotEmpty() -> launcherViewModel.updateSearchQuery("")
                        uiState.selectedTabId == "hidden" -> launcherViewModel.lockHiddenApps()
                    }
                }

                NovaTvLauncherScreen(
                    launcherViewModel = launcherViewModel,
                    tabViewModel = tabViewModel,
                    categories = categories,
                    tabGroups = tabGroups,
                    dockApps = dockApps,
                    displayedApps = displayedApps,
                    hiddenApps = hiddenApps,
                    allVisibleApps = allVisibleApps
                )
            }
        }
    }
}

@Composable
fun NovaTvLauncherScreen(
    launcherViewModel: LauncherViewModel,
    tabViewModel: TabViewModel,
    categories: List<com.example.data.model.CategoryTab>,
    tabGroups: List<com.example.data.model.TabGroupWithApps>,
    dockApps: List<com.example.data.model.AppItem>,
    displayedApps: List<com.example.data.model.AppItem>,
    hiddenApps: List<com.example.data.model.AppItem>,
    allVisibleApps: List<com.example.data.model.AppItem>
) {
    val context = LocalContext.current
    val uiState by launcherViewModel.uiState.collectAsState()
    val tabUiState by tabViewModel.uiState.collectAsState()

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
                onVoiceSearchClick = { launcherViewModel.openVoiceSearch() },
                onToggleNightMode = { launcherViewModel.toggleNightModeManual() },
                onToggleHiddenClick = { launcherViewModel.selectTab("hidden") },
                onCloudSyncClick = { launcherViewModel.openSettings() },
                onSettingsClick = { launcherViewModel.openSettings() },
                onEnergyProfileClick = { launcherViewModel.openSettings() }
            )

            // 2. Dynamic Widgets Row (Hardware Performance & Efficiency, TV Inputs)
            TvWidgetsRow(
                systemStats = uiState.systemStats,
                onOptimizeRamClick = { launcherViewModel.optimizeRam(context) }
            )

            // 3. Quick Access Dock (1-click pinned favorite apps)
            TvQuickAccessDock(
                dockApps = dockApps,
                onLaunchApp = { app -> launcherViewModel.launchApp(context, app) },
                onOpenMenu = { app -> launcherViewModel.selectAppForActions(app) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Customizable Tabs Row with Tab Management trigger
            TvCategoryTabs(
                categories = categories,
                selectedTabId = uiState.selectedTabId,
                onTabSelected = { tabId -> launcherViewModel.selectTab(tabId) },
                onAddNewTabClick = { tabViewModel.openCreateTabDialog() },
                onOpenTabManager = { tabViewModel.openTabManager() }
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
                        onValueChange = { launcherViewModel.updateSearchQuery(it) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TvCyanAccent) },
                        trailingIcon = {
                            IconButton(onClick = { launcherViewModel.updateSearchQuery("") }) {
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
                    onUnlockWithPin = { pin -> launcherViewModel.unlockHiddenAppsWithPin(pin) },
                    onLockAgain = { launcherViewModel.lockHiddenApps() },
                    onUnhideApp = { app -> launcherViewModel.toggleHideApp(app) },
                    onLaunchApp = { app -> launcherViewModel.launchApp(context, app) },
                    onDismiss = { launcherViewModel.selectTab("streaming") }
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
                                text = "Pulsa 'Gestionar Pestañas' o mantén pulsado Menú sobre una app para asignarla aquí",
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
                                onLaunch = { selected -> launcherViewModel.launchApp(context, selected) },
                                onOpenMenu = { selected -> launcherViewModel.selectAppForActions(selected) }
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
                onVoiceResult = { result -> launcherViewModel.processVoiceResult(context, result) },
                onDismiss = { launcherViewModel.closeVoiceSearch() }
            )
        }

        // 2. App Action Dialog (DPad Menu / Long press)
        uiState.selectedAppForActions?.let { app ->
            AppActionDialog(
                app = app,
                categories = categories,
                onDismiss = { launcherViewModel.selectAppForActions(null) },
                onLaunch = {
                    launcherViewModel.selectAppForActions(null)
                    launcherViewModel.launchApp(context, app)
                },
                onToggleHide = { launcherViewModel.toggleHideApp(app) },
                onTogglePinDock = { launcherViewModel.togglePinDock(app) },
                onToggleFavorite = { launcherViewModel.toggleFavorite(app) },
                onChangeCategory = { catId -> launcherViewModel.changeAppCategory(app, catId) }
            )
        }

        // 3. Settings & Cloud Sync Dialog
        if (uiState.isSettingsOpen) {
            SettingsDialog(
                energyProfile = uiState.energyProfile,
                isAutoNightMode = uiState.isAutoNightMode,
                isNightModeActive = uiState.isNightModeActive,
                cloudSyncMessage = uiState.cloudSyncMessage,
                onSetEnergyProfile = { launcherViewModel.setEnergyProfile(it) },
                onToggleAutoNightMode = { launcherViewModel.toggleAutoNightMode() },
                onToggleNightModeManual = { launcherViewModel.toggleNightModeManual() },
                onOptimizeRam = { launcherViewModel.optimizeRam(context) },
                onExportBackup = { callback -> launcherViewModel.exportBackup(callback) },
                onImportBackup = { json -> launcherViewModel.importBackup(json, context) },
                onDismiss = { launcherViewModel.closeSettings() }
            )
        }

        // 4. Tab Manager Dialog
        if (tabUiState.isTabManagerOpen) {
            TvTabManagerDialog(
                tabGroups = tabGroups,
                allApps = allVisibleApps,
                onAddTab = { tabViewModel.openCreateTabDialog() },
                onEditTab = { tab -> tabViewModel.openEditTabDialog(tab) },
                onDeleteTab = { tabId -> tabViewModel.deleteTab(tabId) },
                onMoveTabUp = { tabId -> tabViewModel.moveTabUp(tabId) },
                onMoveTabDown = { tabId -> tabViewModel.moveTabDown(tabId) },
                onManageAppsForTab = { tab -> tabViewModel.openManageAppsDialog(tab) },
                onDismiss = { tabViewModel.closeTabManager() }
            )
        }

        // 5. Create New Custom Tab Dialog (with instant app grouping)
        if (tabUiState.isCreateTabOpen) {
            NewTabDialog(
                allApps = allVisibleApps,
                onAddTab = { title, iconName, colorHex, initialPackages ->
                    val success = tabViewModel.createTab(title, iconName, colorHex, initialPackages)
                    if (success) {
                        launcherViewModel.selectTab(tabUiState.selectedTabId)
                    }
                },
                onDismiss = { tabViewModel.closeCreateTabDialog() }
            )
        }

        // 6. Edit Tab Dialog
        tabUiState.tabBeingEdited?.let { tab ->
            TabEditDialog(
                tab = tab,
                onSave = { newTitle, newIcon, newColor ->
                    tabViewModel.updateTab(tab.id, newTitle, newIcon, newColor)
                },
                onDismiss = { tabViewModel.closeEditTabDialog() }
            )
        }

        // 7. Group / Assign Apps to Tab Dialog
        tabUiState.tabBeingManagedApps?.let { tab ->
            TabAppAssignmentDialog(
                tab = tab,
                allApps = allVisibleApps,
                onSaveAssignments = { selectedPackages ->
                    tabViewModel.batchAssignApps(selectedPackages, tab.id)
                },
                onDismiss = { tabViewModel.closeManageAppsDialog() }
            )
        }
    }
}
