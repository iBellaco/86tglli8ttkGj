package com.example

import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.TabGroupWithApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTabGroupWithApps_computesCorrectCount() {
    val tab = CategoryTab(
      id = "anime",
      title = "Anime & Cartoons",
      iconName = "movie",
      colorHex = "#A855F7",
      isCustom = true,
      orderIndex = 5
    )

    val apps = listOf(
      AppItem(
        packageName = "com.crunchyroll.crunchyroid",
        label = "Crunchyroll",
        categoryId = "anime"
      ),
      AppItem(
        packageName = "com.funimation.funimationnow",
        label = "Funimation",
        categoryId = "anime"
      )
    )

    val group = TabGroupWithApps(tab = tab, apps = apps)

    assertEquals(2, group.appCount)
    assertEquals("anime", group.tab.id)
    assertTrue(group.tab.isCustom)
    assertEquals("Crunchyroll", group.apps[0].label)
  }

  @Test
  fun testCategoryReordering() {
    val initialTabs = listOf(
      CategoryTab(id = "streaming", title = "Streaming", orderIndex = 0),
      CategoryTab(id = "games", title = "Juegos", orderIndex = 1),
      CategoryTab(id = "anime", title = "Anime", orderIndex = 2)
    )

    // Swap index 1 and 2
    val reordered = initialTabs.toMutableList()
    val moved = reordered.removeAt(2)
    reordered.add(1, moved)

    assertEquals("anime", reordered[1].id)
    assertEquals("games", reordered[2].id)
  }
}
