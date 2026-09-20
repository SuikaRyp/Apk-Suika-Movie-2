package com.suikamovie.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suikamovie.app.ui.navigation.Screen
import com.suikamovie.app.ui.theme.BgCard
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.TextLight

private data class BottomNavItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Screen.Home, "Beranda", Icons.Filled.Home),
    BottomNavItem(Screen.Search, "Pencarian", Icons.Filled.Search),
    BottomNavItem(Screen.History, "Riwayat", Icons.Filled.History),
    BottomNavItem(Screen.Account, "Akun", Icons.Filled.Person),
)

@Composable
fun SuikaBottomNavBar(currentRoute: String?, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = BgCard,
        modifier = Modifier.height(64.dp),
    ) {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight,
                    indicatorColor = BgCard,
                ),
            )
        }
    }
}
