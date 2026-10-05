package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.Text
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CoupleNavigationBar(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val navBarBorderBrush = Brush.linearGradient(
        colors = listOf(Color(0x2AFFFFFF), Color(0x05FFFFFF))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(color = Color(0x1FFFFFFF), shape = RoundedCornerShape(20.dp))
            .border(width = 1.dp, brush = navBarBorderBrush, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple("Home", Icons.Filled.Home, 0),
                Triple("Learn", Icons.Filled.MenuBook, 1),
                Triple("Ledger", Icons.Filled.AttachMoney, 2),
                Triple("Locate", Icons.Filled.LocationOn, 3),
                Triple("Setup", Icons.Filled.Settings, 4)
            )

            navItems.forEach { (title, icon, index) ->
                val isSelected = selectedTab == index
                val itemThemeColor = if (index % 2 == 0) ElectricLavender else CosmicCyan

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.selectTab(index) }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) itemThemeColor else SecondaryTextLavender,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else SecondaryTextLavender
                    )
                }
            }
        }
    }
}
