package com.example.location

import android.content.Intent
import android.net.Uri
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.GlassCard
import com.example.Text
import com.example.data.model.PartnerLocationRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocationTrackerScreen(viewModel: MainViewModel) {
    val profiles by viewModel.profilesFlow.collectAsStateWithLifecycle()
    val locations by viewModel.partnerLocationsFlow.collectAsStateWithLifecycle()
    val sharingEnabled by viewModel.locationSharingEnabled.collectAsStateWithLifecycle()
    val statusMessage by viewModel.locationStatusMessage.collectAsStateWithLifecycle()
    val activeContext by viewModel.activeUserContext.collectAsStateWithLifecycle()

    val myProfile = profiles.find { it.id == activeContext }
    val partnerId = if (activeContext == "user") "girlfriend" else "user"
    val partnerProfile = profiles.find { it.id == partnerId }

    val myLocation = locations.find { it.ownerId == PartnerLocationPublisher.LOCAL_OWNER_ID }
    val partnerLocation = locations.find { it.ownerId == partnerId }

    val context = LocalContext.current

    fun hasAllSharingPermissions(): Boolean =
        LocationPermissionHelper.hasFineOrCoarseLocation(context) &&
            LocationPermissionHelper.hasPostNotifications(context) &&
            LocationPermissionHelper.hasBackgroundLocation(context)

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setLocationSharingEnabled(true)
            viewModel.onLocationPermissionResult(true)
        } else {
            viewModel.onLocationPermissionResult(false)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            advanceLocationPermissionFlow()
        } else {
            viewModel.onLocationPermissionResult(false)
        }
    }

    val finePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            advanceLocationPermissionFlow()
        } else {
            viewModel.onLocationPermissionResult(false)
        }
    }

    fun advanceLocationPermissionFlow() {
        when {
            !LocationPermissionHelper.hasFineOrCoarseLocation(context) -> {
                finePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !LocationPermissionHelper.hasPostNotifications(context) -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !LocationPermissionHelper.hasBackgroundLocation(context) -> {
                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            else -> {
                viewModel.setLocationSharingEnabled(true)
            }
        }
    }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Duo Location Tracker",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Share live GPS with your partner · synced via Supabase",
                        fontSize = 12.sp,
                        color = SecondaryTextLavender
                    )
                }
                Icon(
                    imageVector = Icons.Filled.NearMe,
                    contentDescription = null,
                    tint = CosmicCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(
                    colors = listOf(CosmicCyan.copy(alpha = 0.5f), ElectricLavender.copy(alpha = 0.3f))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Share my location", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "Uses a foreground notification to share GPS every ~30s, including while the app is in the background.",
                            fontSize = 11.sp,
                            color = SecondaryTextLavender
                        )
                    }
                    Switch(
                        checked = sharingEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (hasAllSharingPermissions()) {
                                    viewModel.setLocationSharingEnabled(true)
                                } else {
                                    advanceLocationPermissionFlow()
                                }
                            } else {
                                viewModel.setLocationSharingEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CosmicCyan,
                            checkedTrackColor = CosmicCyan.copy(alpha = 0.35f)
                        )
                    )
                }
                if (statusMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(statusMessage, fontSize = 11.sp, color = SecondaryTextLavender)
                }
                if (!LocationPermissionHelper.hasFineOrCoarseLocation(context)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Grant location access to enable sharing.",
                        fontSize = 11.sp,
                        color = SweetheartedPeach,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !LocationPermissionHelper.hasBackgroundLocation(context)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Allow “All the time” location for reliable updates when the app is closed.",
                        fontSize = 11.sp,
                        color = SweetheartedPeach,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            val distanceText = if (
                myLocation != null && partnerLocation != null &&
                myLocation.isSharingEnabled && partnerLocation.isSharingEnabled &&
                myLocation.latitude != 0.0 && partnerLocation.latitude != 0.0
            ) {
                LocationMath.formatDistance(
                    LocationMath.distanceMeters(
                        myLocation.latitude,
                        myLocation.longitude,
                        partnerLocation.latitude,
                        partnerLocation.longitude
                    )
                )
            } else {
                "Distance unavailable — both partners need sharing on"
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Couple distance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(distanceText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = CosmicCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.refreshPartnerLocationsNow() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF))
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh partner location", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        item {
            LocationPersonCard(
                title = "Me (${myProfile?.name ?: "You"})",
                emoji = myProfile?.avatarEmoji ?: if (activeContext == "user") "🦁" else "🌸",
                record = myLocation,
                themeColor = if (activeContext == "user") ElectricLavender else SweetheartedPeach,
                isSelf = true
            )
        }

        item {
            LocationPersonCard(
                title = "Partner (${partnerProfile?.name ?: "Partner"})",
                emoji = partnerProfile?.avatarEmoji ?: if (partnerId == "user") "🦁" else "🌸",
                record = partnerLocation,
                themeColor = if (partnerId == "user") ElectricLavender else SweetheartedPeach,
                isSelf = false
            )
        }

        item {
            Text(
                text = "Privacy: only your couple workspace receives coordinates. A persistent notification appears while background sharing is on. Turn sharing off anytime.",
                fontSize = 10.sp,
                color = SecondaryTextLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
@Composable
private fun LocationPersonCard(
    title: String,
    emoji: String,
    record: PartnerLocationRecord?,
    themeColor: Color,
    isSelf: Boolean
) {
    val context = LocalContext.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(colors = listOf(themeColor.copy(alpha = 0.45f), Color.Transparent))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(themeColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val status = when {
                    record == null -> "No location yet"
                    !record.isSharingEnabled -> "Sharing paused"
                    record.latitude == 0.0 && record.longitude == 0.0 -> "Waiting for GPS fix…"
                    else -> String.format(Locale.US, "%.5f, %.5f", record.latitude, record.longitude)
                }
                Text(status, fontSize = 12.sp, color = SecondaryTextLavender)
                record?.let {
                    if (it.updatedAt > 0) {
                        val time = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(it.updatedAt))
                        Text("Updated $time · ±${it.accuracyMeters.toInt()} m", fontSize = 10.sp, color = SecondaryTextLavender)
                    }
                }
            }
            if (record != null && record.isSharingEnabled && record.latitude != 0.0) {
                IconButton(
                    onClick = {
                        val uri = Uri.parse("geo:${record.latitude},${record.longitude}?q=${record.latitude},${record.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                ) {
                    Icon(
                        imageVector = if (isSelf) Icons.Filled.MyLocation else Icons.Filled.LocationOn,
                        contentDescription = "Open in maps",
                        tint = themeColor
                    )
                }
            }
        }
    }
}
