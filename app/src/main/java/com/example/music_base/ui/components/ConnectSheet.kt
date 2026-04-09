package com.example.music_base.ui.components

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSheet(
    onDismiss: () -> Unit,
    onDeviceSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    // State for real devices
    var devices by remember { mutableStateOf(listOf<DeviceData>()) }

    LaunchedEffect(Unit) {
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        devices = outputDevices.mapNotNull { info ->
            mapDeviceInfoToData(info)
        }.distinctBy { it.name } // Avoid duplicates from different ports
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Connect to a device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(devices) { device ->
                    DeviceItem(
                        name = device.name,
                        icon = device.icon,
                        description = device.type,
                        isActive = device.isCurrent,
                        onClick = { 
                            onDeviceSelected(device.name)
                        }
                    )
                }
                
                // Option to pair new devices
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pair new device", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Helpful Tip
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.BluetoothSearching, null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Only connected devices are shown. If you don't see your device, make sure it's paired and connected in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

        }
    }
}

private fun mapDeviceInfoToData(info: AudioDeviceInfo): DeviceData? {
    val name = info.productName.toString()
    
    return when (info.type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> 
            DeviceData("Phone Speaker", Icons.Rounded.Smartphone, "Internal", isCurrent = true)
        
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, 
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> 
            DeviceData(if (name.isBlank() || name == "null") "Bluetooth Device" else name, Icons.Rounded.Headphones, "Bluetooth")
            
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, 
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> 
            DeviceData("Wired Headphones", Icons.Rounded.Headphones, "Jack Output")
            
        AudioDeviceInfo.TYPE_USB_DEVICE, 
        AudioDeviceInfo.TYPE_USB_HEADSET -> 
            DeviceData(if (name.isBlank()) "USB Audio" else name, Icons.Rounded.Usb, "USB Output")
            
        AudioDeviceInfo.TYPE_HDMI -> 
            DeviceData("Display/HDMI", Icons.Rounded.Tv, "HDMI Output")
            
        else -> null // Filter out some techy/non-human device types
    }
}

@Composable
fun DeviceItem(
    name: String,
    icon: ImageVector,
    description: String? = null,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) Primary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Primary else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Primary else Color.White
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        
        if (isActive) {
            Icon(Icons.Rounded.Check, null, tint = Primary)
        }
    }
}

data class DeviceData(
    val name: String, 
    val icon: ImageVector, 
    val type: String,
    val isCurrent: Boolean = false
)
