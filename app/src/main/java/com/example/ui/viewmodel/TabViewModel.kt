package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.TabGroupWithApps
import com.example.data.repository.LauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TabManagementUiState(
    val selectedTabId: String = "streaming",
    val isTabManagerOpen: Boolean = false,
    val isCreateTabOpen: Boolean = false,
    val tabBeingEdited: CategoryTab? = null,
    val tabBeingManagedApps: CategoryTab? = null,
    val toastMessage: String? = null,
    val validationError: String? = null
)

class TabViewModel(
    private val repository: LauncherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TabManagementUiState())
    val uiState: StateFlow<TabManagementUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<CategoryTab>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisibleApps: StateFlow<List<AppItem>> = repository.visibleApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Groups categories with their respective apps and count
    val tabGroups: StateFlow<List<TabGroupWithApps>> = combine(
        repository.categories,
        repository.visibleApps
    ) { cats, apps ->
        cats.map { cat ->
            val matchingApps = when (cat.id) {
                "favorites" -> apps.filter { it.isFavorite }
                else -> apps.filter { it.categoryId == cat.id }
            }
            TabGroupWithApps(
                tab = cat,
                apps = matchingApps,
                appCount = matchingApps.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tabId: String) {
        _uiState.value = _uiState.value.copy(selectedTabId = tabId)
    }

    fun openTabManager() {
        _uiState.value = _uiState.value.copy(isTabManagerOpen = true)
    }

    fun closeTabManager() {
        _uiState.value = _uiState.value.copy(isTabManagerOpen = false)
    }

    fun openCreateTabDialog() {
        _uiState.value = _uiState.value.copy(isCreateTabOpen = true, validationError = null)
    }

    fun closeCreateTabDialog() {
        _uiState.value = _uiState.value.copy(isCreateTabOpen = false, validationError = null)
    }

    fun openEditTabDialog(tab: CategoryTab) {
        _uiState.value = _uiState.value.copy(tabBeingEdited = tab, validationError = null)
    }

    fun closeEditTabDialog() {
        _uiState.value = _uiState.value.copy(tabBeingEdited = null, validationError = null)
    }

    fun openManageAppsDialog(tab: CategoryTab) {
        _uiState.value = _uiState.value.copy(tabBeingManagedApps = tab)
    }

    fun closeManageAppsDialog() {
        _uiState.value = _uiState.value.copy(tabBeingManagedApps = null)
    }

    fun createTab(
        title: String,
        iconName: String = "apps",
        colorHex: String = "#38BDF8",
        assignedPackageNames: List<String> = emptyList()
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(validationError = "El título de la pestaña no puede estar vacío")
            return false
        }

        viewModelScope.launch {
            val newTab = repository.addCustomCategory(
                title = cleanTitle,
                iconName = iconName,
                colorHex = colorHex,
                initialPackages = assignedPackageNames
            )
            _uiState.value = _uiState.value.copy(
                isCreateTabOpen = false,
                selectedTabId = newTab.id,
                validationError = null,
                toastMessage = "Pestaña '$cleanTitle' creada correctamente con ${assignedPackageNames.size} apps"
            )
        }
        return true
    }

    fun updateTab(tabId: String, newTitle: String, newIconName: String, newColorHex: String): Boolean {
        val cleanTitle = newTitle.trim()
        if (cleanTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(validationError = "El título de la pestaña no puede estar vacío")
            return false
        }

        viewModelScope.launch {
            repository.updateCustomCategory(
                id = tabId,
                title = cleanTitle,
                iconName = newIconName,
                colorHex = newColorHex
            )
            _uiState.value = _uiState.value.copy(
                tabBeingEdited = null,
                validationError = null,
                toastMessage = "Pestaña '$cleanTitle' actualizada"
            )
        }
        return true
    }

    fun deleteTab(tabId: String) {
        viewModelScope.launch {
            repository.deleteCategory(tabId, fallbackCategoryId = "streaming")
            _uiState.value = _uiState.value.copy(
                selectedTabId = "streaming",
                toastMessage = "Pestaña eliminada. Las apps se reasignaron a 'Streaming y TV'"
            )
        }
    }

    fun moveTabUp(tabId: String) {
        viewModelScope.launch {
            val currentList = categories.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == tabId }
            if (index > 0) {
                val item = currentList.removeAt(index)
                currentList.add(index - 1, item)
                repository.reorderCategories(currentList.map { it.id })
            }
        }
    }

    fun moveTabDown(tabId: String) {
        viewModelScope.launch {
            val currentList = categories.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == tabId }
            if (index != -1 && index < currentList.size - 1) {
                val item = currentList.removeAt(index)
                currentList.add(index + 1, item)
                repository.reorderCategories(currentList.map { it.id })
            }
        }
    }

    fun assignAppToTab(packageName: String, tabId: String) {
        viewModelScope.launch {
            repository.setAppCategory(packageName, tabId)
            _uiState.value = _uiState.value.copy(toastMessage = "Aplicación movida a categoría")
        }
    }

    fun toggleAppInTab(packageName: String, targetTabId: String, currentCategoryId: String) {
        viewModelScope.launch {
            val destination = if (currentCategoryId == targetTabId) "streaming" else targetTabId
            repository.setAppCategory(packageName, destination)
        }
    }

    fun batchAssignApps(packageNames: List<String>, tabId: String) {
        viewModelScope.launch {
            repository.batchAssignAppsToCategory(packageNames, tabId)
            _uiState.value = _uiState.value.copy(
                tabBeingManagedApps = null,
                toastMessage = "${packageNames.size} apps agrupadas en la pestaña"
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
