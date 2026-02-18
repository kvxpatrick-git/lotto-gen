package com.hsmomo.lottogen.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hsmomo.lottogen.presentation.bookmark.BookmarkScreen
import com.hsmomo.lottogen.presentation.components.AdBanner
import com.hsmomo.lottogen.presentation.generate.GenerateScreen
import com.hsmomo.lottogen.presentation.history.HistoryScreen
import com.hsmomo.lottogen.presentation.settings.SettingsScreen
import com.hsmomo.lottogen.presentation.statistics.StatisticsScreen

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("번호발행", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("히스토리", Icons.Filled.Refresh, Icons.Outlined.Refresh),
    BottomNavItem("통계", Icons.Filled.Info, Icons.Outlined.Info),
    BottomNavItem("북마크", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem("설정", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun HomeScreen(onShowMessage: (String) -> Unit) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            Column {
                AdBanner()
                NavigationBar {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            label = { Text(text = item.title) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTabIndex == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> GenerateScreen(onShowMessage = onShowMessage)
                1 -> HistoryScreen(onShowMessage = onShowMessage)
                2 -> StatisticsScreen(onShowMessage = onShowMessage)
                3 -> BookmarkScreen(onShowMessage = onShowMessage)
                4 -> SettingsScreen()
            }
        }
    }
}