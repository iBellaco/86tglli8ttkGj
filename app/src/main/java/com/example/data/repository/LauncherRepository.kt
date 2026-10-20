package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.os.StatFs
import com.example.data.local.LauncherDao
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.CloudBackupPayload
import com.example.data.model.EnergyProfile
import com.example.data.model.SystemStats
import com.example.data.model.WidgetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(
    private val context: Context,
    private val dao: LauncherDao
) {
    val allApps: Flow<List<AppItem>> = dao.getAllApps()
    val visibleApps: Flow<List<AppItem>> = dao.getVisibleApps()
    val hiddenApps: Flow<List<AppItem>> = dao.getHiddenApps()
    val dockApps: Flow<List<AppItem>> = dao.getDockApps()
    val favoriteApps: Flow<List<AppItem>> = dao.getFavoriteApps()
    val categories: Flow<List<CategoryTab>> = dao.getAllCategories()
    val widgets: Flow<List<WidgetItem>> = dao.getAllWidgets()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingCategories = dao.getAllCategories().first()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                CategoryTab(id = "favorites", title = "Favoritos", iconName = "star", isCustom = false, orderIndex = 0),
                CategoryTab(id = "streaming", title = "Streaming y TV", iconName = "movie", isCustom = false, orderIndex = 1),
                CategoryTab(id = "games", title = "Juegos", iconName = "sports_esports", isCustom = false, orderIndex = 2),
                CategoryTab(id = "tools", title = "Herramientas", iconName = "build", isCustom = false, orderIndex = 3),
                CategoryTab(id = "music", title = "Música y Podcast", iconName = "music_note", isCustom = false, orderIndex = 4)
            )
            dao.insertCategories(defaultCategories)
        }

        val existingWidgets = dao.getAllWidgets().first()
        if (existingWidgets.isEmpty()) {
            val defaultWidgets = listOf(
                WidgetItem(id = "clock_weather", title = "Reloj y Clima Dinámico", isEnabled = true, orderIndex = 0),
                WidgetItem(id = "system_stats", title = "Monitor de Hardware y Eficiencia", isEnabled = true, orderIndex = 1),
                WidgetItem(id = "quick_actions", title = "Accesos Rápidos y Eficiencia", isEnabled = true, orderIndex = 2)
            )
            dao.insertWidgets(defaultWidgets)
        }

        val existingApps = dao.getAllApps().first()
        if (existingApps.isEmpty()) {
            val installedApps = scanInstalledApps()
            val appsToInsert = if (installedApps.isNotEmpty()) {
                installedApps
            } else {
                getPreconfiguredTvApps()
            }
            dao.insertApps(appsToInsert)
        }
    }

    private fun scanInstalledApps(): List<AppItem> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolvedSet = mutableSetOf<String>()
        val result = mutableListOf<AppItem>()

        val leanbackResolveInfos = packageManager.queryIntentActivities(leanbackIntent, 0)
        for (info in leanbackResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName && !resolvedSet.contains(pkg)) {
                resolvedSet.add(pkg)
                val label = info.loadLabel(packageManager).toString()
                result.add(
                    AppItem(
                        packageName = pkg,
                        activityName = info.activityInfo.name,
                        label = label,
                        iconType = guessAppIconType(pkg, label),
                        iconColorHex = guessAppColor(pkg),
                        categoryId = guessCategory(pkg, label),
                        isFavorite = pkg.contains("youtube") || pkg.contains("netflix"),
                        isPinnedToDock = pkg.contains("youtube") || pkg.contains("netflix") || result.size < 4,
                        orderIndex = result.size
                    )
                )
            }
        }

        val standardResolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        for (info in standardResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName && !resolvedSet.contains(pkg)) {
                resolvedSet.add(pkg)
                val label = info.loadLabel(packageManager).toString()
                result.add(
                    AppItem(
                        packageName = pkg,
                        activityName = info.activityInfo.name,
                        label = label,
                        iconType = guessAppIconType(pkg, label),
                        iconColorHex = guessAppColor(pkg),
                        categoryId = guessCategory(pkg, label),
                        isFavorite = false,
                        isPinnedToDock = result.size < 4,
                        orderIndex = result.size
                    )
                )
            }
        }

        // If very few system apps are present on emulator, supplement with popular TV suite
        if (result.size < 8) {
            val preconfigured = getPreconfiguredTvApps()
            for (p in preconfigured) {
                if (!resolvedSet.contains(p.packageName)) {
                    result.add(p)
                }
            }
        }

        return result
    }

    private fun getPreconfiguredTvApps(): List<AppItem> {
        return listOf(
            AppItem(
                packageName = "com.google.android.youtube.tv",
                label = "YouTube TV",
                iconType = "YOUTUBE",
                iconColorHex = "#EF4444",
                categoryId = "streaming",
                isFavorite = true,
                isPinnedToDock = true,
                orderIndex = 0
            ),
            AppItem(
                packageName = "com.netflix.ninja",
                label = "Netflix",
                iconType = "NETFLIX",
                iconColorHex = "#E50914",
                categoryId = "streaming",
                isFavorite = true,
                isPinnedToDock = true,
                orderIndex = 1
            ),
            AppItem(
                packageName = "com.amazon.amazonvideo.livingroom",
                label = "Prime Video",
                iconType = "PRIME",
                iconColorHex = "#00A8E1",
                categoryId = "streaming",
                isFavorite = true,
                isPinnedToDock = true,
                orderIndex = 2
            ),
            AppItem(
                packageName = "com.disney.disneyplus",
                label = "Disney+",
                iconType = "DISNEY",
                iconColorHex = "#113CCF",
                categoryId = "streaming",
                isFavorite = true,
                isPinnedToDock = true,
                orderIndex = 3
            ),
            AppItem(
                packageName = "com.spotify.tv.android",
                label = "Spotify TV",
                iconType = "SPOTIFY",
                iconColorHex = "#1DB954",
                categoryId = "music",
                isFavorite = true,
                isPinnedToDock = true,
                orderIndex = 4
            ),
            AppItem(
                packageName = "com.plexapp.android",
                label = "Plex Media",
                iconType = "PLEX",
                iconColorHex = "#E5A00D",
                categoryId = "streaming",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 5
            ),
            AppItem(
                packageName = "tv.twitch.android.app",
                label = "Twitch",
                iconType = "TWITCH",
                iconColorHex = "#9146FF",
                categoryId = "streaming",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 6
            ),
            AppItem(
                packageName = "org.xbmc.kodi",
                label = "Kodi Home",
                iconType = "KODI",
                iconColorHex = "#13B5EA",
                categoryId = "tools",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 7
            ),
            AppItem(
                packageName = "org.videolan.vlc",
                label = "VLC Media Player",
                iconType = "VLC",
                iconColorHex = "#FF7700",
                categoryId = "tools",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 8
            ),
            AppItem(
                packageName = "com.retroarch",
                label = "RetroArch TV",
                iconType = "RETROARCH",
                iconColorHex = "#334155",
                categoryId = "games",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 9
            ),
            AppItem(
                packageName = "com.android.tv.settings",
                label = "Ajustes de TV",
                iconType = "SETTINGS",
                iconColorHex = "#64748B",
                categoryId = "tools",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 10
            ),
            AppItem(
                packageName = "com.google.android.play.games",
                label = "Google Play Games",
                iconType = "GAMES",
                iconColorHex = "#10B981",
                categoryId = "games",
                isFavorite = false,
                isPinnedToDock = false,
                orderIndex = 11
            )
        )
    }

    private fun guessAppIconType(pkg: String, label: String): String {
        val p = pkg.lowercase()
        val l = label.lowercase()
        return when {
            p.contains("youtube") || l.contains("youtube") -> "YOUTUBE"
            p.contains("netflix") || l.contains("netflix") -> "NETFLIX"
            p.contains("amazonvideo") || p.contains("prime") || l.contains("prime") -> "PRIME"
            p.contains("disney") || l.contains("disney") -> "DISNEY"
            p.contains("spotify") || l.contains("spotify") -> "SPOTIFY"
            p.contains("plex") || l.contains("plex") -> "PLEX"
            p.contains("twitch") || l.contains("twitch") -> "TWITCH"
            p.contains("kodi") || l.contains("kodi") -> "KODI"
            p.contains("vlc") || l.contains("vlc") -> "VLC"
            p.contains("retroarch") || l.contains("retro") -> "RETROARCH"
            p.contains("setting") || l.contains("ajuste") || l.contains("config") -> "SETTINGS"
            p.contains("file") || l.contains("archivo") -> "FILES"
            p.contains("browser") || p.contains("chrome") || l.contains("navegador") -> "BROWSER"
            p.contains("game") || l.contains("juego") -> "GAMES"
            else -> "SYSTEM"
        }
    }

    private fun guessAppColor(pkg: String): String {
        val p = pkg.lowercase()
        return when {
            p.contains("youtube") -> "#EF4444"
            p.contains("netflix") -> "#E50914"
            p.contains("prime") || p.contains("amazon") -> "#00A8E1"
            p.contains("disney") -> "#113CCF"
            p.contains("spotify") -> "#1DB954"
            p.contains("twitch") -> "#9146FF"
            p.contains("vlc") -> "#FF7700"
            p.contains("kodi") -> "#13B5EA"
            p.contains("game") -> "#10B981"
            else -> "#38BDF8"
        }
    }

    private fun guessCategory(pkg: String, label: String): String {
        val p = pkg.lowercase()
        val l = label.lowercase()
        return when {
            p.contains("youtube") || p.contains("netflix") || p.contains("prime") ||
            p.contains("disney") || p.contains("plex") || p.contains("twitch") ||
            p.contains("video") || l.contains("tv") || l.contains("película") -> "streaming"
            p.contains("game") || p.contains("retroarch") || l.contains("juego") -> "games"
            p.contains("spotify") || p.contains("music") || p.contains("audio") || l.contains("música") -> "music"
            else -> "tools"
        }
    }

    suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        dao.setAppHidden(packageName, hidden)
    }

    suspend fun setAppPinnedToDock(packageName: String, pinned: Boolean) {
        dao.setAppPinnedToDock(packageName, pinned)
    }

    suspend fun setAppFavorite(packageName: String, favorite: Boolean) {
        dao.setAppFavorite(packageName, favorite)
    }

    suspend fun setAppCategory(packageName: String, categoryId: String) {
        dao.setAppCategory(packageName, categoryId)
    }

    suspend fun addCustomCategory(title: String, iconName: String, colorHex: String = "#38BDF8", initialPackages: List<String> = emptyList()): CategoryTab {
        val id = "cat_${System.currentTimeMillis()}"
        val existing = dao.getAllCategories().first()
        val newCategory = CategoryTab(
            id = id,
            title = title,
            iconName = iconName,
            colorHex = colorHex,
            isCustom = true,
            orderIndex = existing.size
        )
        dao.insertCategory(newCategory)
        for (pkg in initialPackages) {
            dao.setAppCategory(pkg, id)
        }
        return newCategory
    }

    suspend fun updateCustomCategory(id: String, title: String, iconName: String, colorHex: String) {
        dao.updateCategoryDetails(id, title, iconName, colorHex)
    }

    suspend fun deleteCategory(id: String, fallbackCategoryId: String = "streaming") {
        // Reassign all apps in this category to fallback category so user apps are never orphaned
        dao.reassignAppsCategory(oldCategoryId = id, newCategoryId = fallbackCategoryId)
        dao.deleteCategory(id)
    }

    suspend fun reorderCategories(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            dao.updateCategoryOrder(id, index)
        }
    }

    suspend fun batchAssignAppsToCategory(packageNames: List<String>, categoryId: String) {
        for (pkg in packageNames) {
            dao.setAppCategory(pkg, categoryId)
        }
    }

    suspend fun markAppLaunched(packageName: String) {
        dao.incrementLaunch(packageName, System.currentTimeMillis())
    }

    suspend fun toggleWidget(id: String, enabled: Boolean) {
        dao.setWidgetEnabled(id, enabled)
    }

    fun getSystemStats(): SystemStats {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorageGb = (statFs.blockCountLong * statFs.blockSizeLong) / (1024 * 1024 * 1024)
        val freeStorageGb = (statFs.availableBlocksLong * statFs.blockSizeLong) / (1024 * 1024 * 1024)

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)
        val netType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet (LAN)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi (5GHz)"
            else -> "En línea"
        }

        return SystemStats(
            usedRamMb = totalRamMb.takeIf { it > 0 }?.let { usedRamMb } ?: 1840,
            totalRamMb = totalRamMb.takeIf { it > 0 } ?: 4096,
            freeStorageGb = freeStorageGb.takeIf { it > 0 } ?: 22,
            totalStorageGb = totalStorageGb.takeIf { it > 0 } ?: 32,
            batteryOrPowerStatus = "Alimentación AC (TV Cable)",
            networkType = netType,
            wifiSignalLevel = 4,
            cpuTemperatureEstimate = "42°C (Eficiente)"
        )
    }

    suspend fun exportConfigurationJson(): String = withContext(Dispatchers.IO) {
        val apps = dao.getAllApps().first()
        val cats = dao.getAllCategories().first()
        val root = JSONObject()
        root.put("version", "1.0.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("deviceName", "Android TV Box / Smart TV")

        val catsArray = JSONArray()
        for (c in cats) {
            val co = JSONObject()
            co.put("id", c.id)
            co.put("title", c.title)
            co.put("iconName", c.iconName)
            co.put("isCustom", c.isCustom)
            co.put("orderIndex", c.orderIndex)
            catsArray.put(co)
        }
        root.put("categories", catsArray)

        val appsArray = JSONArray()
        for (a in apps) {
            val ao = JSONObject()
            ao.put("packageName", a.packageName)
            ao.put("label", a.label)
            ao.put("categoryId", a.categoryId)
            ao.put("isFavorite", a.isFavorite)
            ao.put("isPinnedToDock", a.isPinnedToDock)
            ao.put("isHidden", a.isHidden)
            ao.put("orderIndex", a.orderIndex)
            appsArray.put(ao)
        }
        root.put("apps", appsArray)

        root.toString(2)
    }

    suspend fun importConfigurationJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("categories")) {
                val catsArray = root.getJSONArray("categories")
                val importedCats = mutableListOf<CategoryTab>()
                for (i in 0 until catsArray.length()) {
                    val obj = catsArray.getJSONObject(i)
                    importedCats.add(
                        CategoryTab(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            iconName = obj.optString("iconName", "apps"),
                            isCustom = obj.optBoolean("isCustom", false),
                            orderIndex = obj.optInt("orderIndex", i)
                        )
                    )
                }
                if (importedCats.isNotEmpty()) {
                    dao.insertCategories(importedCats)
                }
            }

            if (root.has("apps")) {
                val appsArray = root.getJSONArray("apps")
                for (i in 0 until appsArray.length()) {
                    val obj = appsArray.getJSONObject(i)
                    val pkg = obj.getString("packageName")
                    if (obj.has("categoryId")) {
                        dao.setAppCategory(pkg, obj.getString("categoryId"))
                    }
                    if (obj.has("isFavorite")) {
                        dao.setAppFavorite(pkg, obj.getBoolean("isFavorite"))
                    }
                    if (obj.has("isPinnedToDock")) {
                        dao.setAppPinnedToDock(pkg, obj.getBoolean("isPinnedToDock"))
                    }
                    if (obj.has("isHidden")) {
                        dao.setAppHidden(pkg, obj.getBoolean("isHidden"))
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
