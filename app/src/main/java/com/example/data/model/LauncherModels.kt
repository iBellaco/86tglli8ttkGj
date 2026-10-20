package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppItem(
    @PrimaryKey val packageName: String,
    val activityName: String = "",
    val label: String,
    val iconType: String = "SYSTEM",
    val iconColorHex: String = "#38BDF8",
    val categoryId: String = "streaming",
    val isFavorite: Boolean = false,
    val isPinnedToDock: Boolean = false,
    val isHidden: Boolean = false,
    val orderIndex: Int = 0,
    val launchCount: Int = 0,
    val lastLaunched: Long = 0L
)

@Entity(tableName = "categories")
data class CategoryTab(
    @PrimaryKey val id: String,
    val title: String,
    val iconName: String = "apps",
    val colorHex: String = "#38BDF8",
    val isCustom: Boolean = false,
    val orderIndex: Int = 0,
    val isProtected: Boolean = false
)

data class TabGroupWithApps(
    val tab: CategoryTab,
    val apps: List<AppItem>,
    val appCount: Int = apps.size
)

data class TabCreationRequest(
    val title: String,
    val iconName: String = "apps",
    val colorHex: String = "#38BDF8",
    val assignedPackageNames: List<String> = emptyList()
)

@Entity(tableName = "widgets")
data class WidgetItem(
    @PrimaryKey val id: String,
    val title: String,
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0
)

enum class EnergyProfile(val title: String, val description: String, val targetFps: Int) {
    ULTRA_SAVER("Ultra Ahorro", "Reduce carga de GPU, animaciones mínimas y bajo consumo energético", 30),
    BALANCED("Equilibrado", "Balance óptimo entre fluidez de mando a distancia y eficiencia", 60),
    PERFORMANCE("Alto Rendimiento", "Máxima tasa de refresco, efectos de desenfoque y respuesta instantánea", 120)
}

data class SystemStats(
    val usedRamMb: Long,
    val totalRamMb: Long,
    val freeStorageGb: Long,
    val totalStorageGb: Long,
    val batteryOrPowerStatus: String,
    val networkType: String,
    val wifiSignalLevel: Int,
    val cpuTemperatureEstimate: String
)

data class CloudBackupPayload(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.1.0",
    val categories: List<CategoryTab>,
    val apps: List<AppItem>,
    val energyProfile: String,
    val autoDarkMode: Boolean,
    val hiddenAppsPin: String
)

data class RecentMediaItem(
    val id: String,
    val appName: String,
    val packageName: String,
    val title: String,
    val subtitle: String = "",
    val durationRemaining: String = "Quedan 18 min",
    val progressPercent: Float = 0.65f,
    val brandColorHex: String = "#EF4444",
    val thumbnailUrl: String? = null,
    val mediaUri: String? = null,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

data class WeatherState(
    val currentTemp: Int = 24,
    val condition: String = "Lluvia dispersa",
    val todayMin: Int = 17,
    val todayMax: Int = 26,
    val tomorrowMin: Int = 16,
    val tomorrowMax: Int = 25,
    val rainProbabilityPercent: Int = 65,
    val tomorrowRainProbabilityPercent: Int = 45,
    val rainStartHour: String = "14:00",
    val rainEndHour: String = "18:30",
    val isHighHeat: Boolean = false
) {
    val isRainAlertActive: Boolean
        get() = rainProbabilityPercent > 49

    // Alerta activa estrictamente si la temperatura es superior a 35°C
    val isHeatAlertActive: Boolean
        get() = currentTemp > 35 || todayMax > 35
}
