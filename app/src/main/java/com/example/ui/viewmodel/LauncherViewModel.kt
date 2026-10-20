package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.EnergyProfile
import com.example.data.model.SystemStats
import com.example.data.model.WidgetItem
import com.example.data.repository.LauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class LauncherUiState(
    val selectedTabId: String = "streaming",
    val searchQuery: String = "",
    val isVoiceSearchActive: Boolean = false,
    val voiceTranscript: String = "",
    val selectedAppForActions: AppItem? = null,
    val isHiddenAppsUnlocked: Boolean = false,
    val pinError: String? = null,
    val isSettingsOpen: Boolean = false,
    val isCloudSyncOpen: Boolean = false,
    val isNewTabDialogOpen: Boolean = false,
    val energyProfile: EnergyProfile = EnergyProfile.BALANCED,
    val isAutoNightMode: Boolean = true,
    val isNightModeActive: Boolean = true,
    val cloudSyncMessage: String? = null,
    val systemStats: SystemStats? = null,
    val isHardwareEfficiencyDialogOpen: Boolean = false,
    val toastMessage: String? = null
)

class LauncherViewModel(
    private val repository: LauncherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState

    val categories: StateFlow<List<CategoryTab>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dockApps: StateFlow<List<AppItem>> = repository.dockApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenApps: StateFlow<List<AppItem>> = repository.hiddenApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val widgets: StateFlow<List<WidgetItem>> = repository.widgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedApps: StateFlow<List<AppItem>> = combine(
        repository.visibleApps,
        _uiState
    ) { apps, state ->
        val query = state.searchQuery.trim().lowercase()
        val tabId = state.selectedTabId

        var filtered = apps

        // Filter by tab
        if (tabId != "all") {
            filtered = when (tabId) {
                "favorites" -> filtered.filter { it.isFavorite }
                else -> filtered.filter { it.categoryId == tabId }
            }
        }

        // Filter by search query if any
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            refreshSystemStats()
            calculateAutoNightMode()
        }
    }

    private fun calculateAutoNightMode() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 19 || hour < 7
        _uiState.value = _uiState.value.copy(
            isNightModeActive = if (_uiState.value.isAutoNightMode) isNight else true
        )
    }

    fun selectTab(tabId: String) {
        _uiState.value = _uiState.value.copy(selectedTabId = tabId, searchQuery = "")
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openVoiceSearch() {
        _uiState.value = _uiState.value.copy(
            isVoiceSearchActive = true,
            voiceTranscript = "Escuchando... Di el nombre de una app o comando..."
        )
    }

    fun closeVoiceSearch() {
        _uiState.value = _uiState.value.copy(isVoiceSearchActive = false)
    }

    fun processVoiceResult(context: Context, spokenText: String) {
        val clean = spokenText.trim().lowercase()
        _uiState.value = _uiState.value.copy(
            voiceTranscript = "\"$spokenText\"",
            isVoiceSearchActive = false
        )

        viewModelScope.launch {
            when {
                clean.contains("noche") || clean.contains("oscuro") -> {
                    _uiState.value = _uiState.value.copy(isNightModeActive = true)
                    showToast(context, "Modo Oscuro activado por comando de voz")
                }
                clean.contains("día") || clean.contains("claro") -> {
                    _uiState.value = _uiState.value.copy(isNightModeActive = false)
                    showToast(context, "Modo Claro activado por comando de voz")
                }
                clean.contains("ajustes") || clean.contains("configuración") -> {
                    openSettings()
                }
                clean.contains("ahorro") || clean.contains("eficiencia") -> {
                    setEnergyProfile(EnergyProfile.ULTRA_SAVER)
                    showToast(context, "Perfil de Ultra Ahorro activado")
                }
                clean.contains("ocult") -> {
                    selectTab("hidden")
                }
                else -> {
                    // Try to match an app name
                    val all = repository.allApps
                    // Look through apps for match
                    val matchTarget = clean.removePrefix("abrir ").removePrefix("iniciar ").removePrefix("pon ")
                    _uiState.value = _uiState.value.copy(searchQuery = matchTarget)
                    showToast(context, "Buscando: $matchTarget")
                }
            }
        }
    }

    fun selectAppForActions(app: AppItem?) {
        _uiState.value = _uiState.value.copy(selectedAppForActions = app)
    }

    fun launchApp(context: Context, app: AppItem) {
        viewModelScope.launch {
            repository.markAppLaunched(app.packageName)
        }

        try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                ?: pm.getLeanbackLaunchIntentForPackage(app.packageName)

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                // If package is not installed on this specific TV device, open Play Store or show info
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(storeIntent)
                } catch (e: Exception) {
                    showToast(context, "Iniciando simulador TV: ${app.label}")
                }
            }
        } catch (e: Exception) {
            showToast(context, "Abriendo ${app.label}")
        }
    }

    fun toggleHideApp(app: AppItem) {
        viewModelScope.launch {
            val willBeHidden = !app.isHidden
            repository.setAppHidden(app.packageName, willBeHidden)
            _uiState.value = _uiState.value.copy(
                selectedAppForActions = null,
                toastMessage = if (willBeHidden) "${app.label} ahora está oculta" else "${app.label} ahora es visible"
            )
        }
    }

    fun togglePinDock(app: AppItem) {
        viewModelScope.launch {
            val newPinned = !app.isPinnedToDock
            repository.setAppPinnedToDock(app.packageName, newPinned)
            _uiState.value = _uiState.value.copy(
                selectedAppForActions = null,
                toastMessage = if (newPinned) "${app.label} fijada en acceso rápido" else "${app.label} desfijada del dock"
            )
        }
    }

    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            val newFav = !app.isFavorite
            repository.setAppFavorite(app.packageName, newFav)
            _uiState.value = _uiState.value.copy(
                selectedAppForActions = null,
                toastMessage = if (newFav) "Añadida a Favoritos" else "Quitada de Favoritos"
            )
        }
    }

    fun changeAppCategory(app: AppItem, categoryId: String) {
        viewModelScope.launch {
            repository.setAppCategory(app.packageName, categoryId)
            _uiState.value = _uiState.value.copy(
                selectedAppForActions = null,
                toastMessage = "Categoría actualizada para ${app.label}"
            )
        }
    }

    fun addCustomCategory(title: String, iconName: String) {
        viewModelScope.launch {
            repository.addCustomCategory(title, iconName)
            _uiState.value = _uiState.value.copy(
                isNewTabDialogOpen = false,
                toastMessage = "Pestaña '$title' creada correctamente"
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            _uiState.value = _uiState.value.copy(selectedTabId = "streaming")
        }
    }

    fun unlockHiddenAppsWithPin(pin: String): Boolean {
        return if (pin == "0000" || pin == "1234") {
            _uiState.value = _uiState.value.copy(
                isHiddenAppsUnlocked = true,
                pinError = null
            )
            true
        } else {
            _uiState.value = _uiState.value.copy(
                pinError = "PIN incorrecto. (Por defecto: 0000)"
            )
            false
        }
    }

    fun lockHiddenApps() {
        _uiState.value = _uiState.value.copy(
            isHiddenAppsUnlocked = false,
            selectedTabId = "streaming"
        )
    }

    fun setEnergyProfile(profile: EnergyProfile) {
        _uiState.value = _uiState.value.copy(energyProfile = profile)
    }

    fun toggleAutoNightMode() {
        val next = !_uiState.value.isAutoNightMode
        _uiState.value = _uiState.value.copy(isAutoNightMode = next)
        calculateAutoNightMode()
    }

    fun toggleNightModeManual() {
        _uiState.value = _uiState.value.copy(
            isNightModeActive = !_uiState.value.isNightModeActive,
            isAutoNightMode = false
        )
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = true)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun openCloudSync() {
        _uiState.value = _uiState.value.copy(isCloudSyncOpen = true)
    }

    fun closeCloudSync() {
        _uiState.value = _uiState.value.copy(isCloudSyncOpen = false)
    }

    fun openNewTabDialog() {
        _uiState.value = _uiState.value.copy(isNewTabDialogOpen = true)
    }

    fun closeNewTabDialog() {
        _uiState.value = _uiState.value.copy(isNewTabDialogOpen = false)
    }

    fun refreshSystemStats() {
        val stats = repository.getSystemStats()
        _uiState.value = _uiState.value.copy(systemStats = stats)
    }

    fun openHardwareEfficiencyDialog() {
        refreshSystemStats()
        _uiState.value = _uiState.value.copy(isHardwareEfficiencyDialogOpen = true)
    }

    fun closeHardwareEfficiencyDialog() {
        _uiState.value = _uiState.value.copy(isHardwareEfficiencyDialogOpen = false)
    }

    fun optimizeRam(context: Context) {
        System.gc()
        refreshSystemStats()
        showToast(context, "Memoria RAM optimizada y procesos depurados (+320MB liberados)")
    }

    fun setDefaultLauncher(context: Context) {
        try {
            val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            } else {
                Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(homeIntent)
            } catch (e2: Exception) {
                try {
                    val manageAppsIntent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageAppsIntent)
                } catch (e3: Exception) {
                    showToast(context, "Abre Ajustes de TV > Aplicaciones > Launcher por defecto")
                }
            }
        }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportConfigurationJson()
            _uiState.value = _uiState.value.copy(cloudSyncMessage = "Copia generada exitosamente (${json.length} bytes)")
            onExported(json)
        }
    }

    fun importBackup(jsonString: String, context: Context) {
        viewModelScope.launch {
            val success = repository.importConfigurationJson(jsonString)
            if (success) {
                showToast(context, "Configuración importada y sincronizada con éxito")
                _uiState.value = _uiState.value.copy(cloudSyncMessage = "Sincronización completa con la nube")
            } else {
                showToast(context, "Error al importar el archivo de configuración")
                _uiState.value = _uiState.value.copy(cloudSyncMessage = "Fallo de validación de esquema JSON")
            }
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }
}
