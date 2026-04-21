package com.example.music_base.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdminIngestScreen(
    isAdminLoading: Boolean,
    onUploadTrack: (
        title: String,
        description: String?,
        artistId: String,
        albumId: String?,
        thumbnailUrl: String?,
        youtubeVideoId: String?,
        sourceType: String,
        youtubeUrl: String?,
        file: MultipartBody.Part?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var artistId by remember { mutableStateOf("") }
    var albumId by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var youtubeVideoId by remember { mutableStateOf("") }
    
    var sourceType by remember { mutableStateOf("youtube") }
    var youtubeUrl by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val isFormValid = title.isNotBlank() && artistId.isNotBlank() && ( (sourceType == "youtube" && youtubeUrl.isNotBlank()) || (sourceType == "file" && selectedFileUri != null))

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment ?: "audio_file"
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Ingestion Pipeline", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.paddingLarge)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Inject new content into the global music ecosystem",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = Dimens.paddingLarge)
            )

            // Source Mode Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.radiusMedium))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp)
            ) {
                SourceTab(
                    selected = sourceType == "youtube",
                    label = "YouTube Link",
                    icon = Icons.Default.Link,
                    modifier = Modifier.weight(1f)
                ) { sourceType = "youtube" }
                SourceTab(
                    selected = sourceType == "file",
                    label = "Audio File",
                    icon = Icons.Default.Attachment,
                    modifier = Modifier.weight(1f)
                ) { sourceType = "file" }
            }

            Spacer(Modifier.height(Dimens.paddingLarge))

            // Main Form
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
                AdminTextField(label = "Track Title", value = title, onValueChange = { title = it }, placeholder = "e.g. Starboy")
                
                // Artist ID Manual Input
                AdminTextField(
                    label = "Artist ID",
                    value = artistId,
                    onValueChange = { artistId = it },
                    placeholder = "e.g. artist-123"
                )

                AdminTextField(label = "Album UUID (Optional)", value = albumId, onValueChange = { albumId = it }, placeholder = "Copy from DB (Optional)")

                if (sourceType == "youtube") {
                    AdminTextField(
                        label = "YouTube URL",
                        value = youtubeUrl,
                        onValueChange = { 
                            youtubeUrl = it
                            // Auto-extract Video ID if possible
                            val regex = "^(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-0_-]{11})".toRegex()
                            val match = regex.find(it)
                            if (match != null) {
                                youtubeVideoId = match.groupValues[1]
                            }
                        },
                        placeholder = "https://youtube.com/watch?v=..."
                    )
                    
                    if (youtubeVideoId.isNotBlank()) {
                        Text(
                            "Extracted ID: $youtubeVideoId",
                            color = Primary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { filePickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(Dimens.radiusMedium),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Default.UploadFile, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedFileUri == null) "Select Audio File" else selectedFileName)
                    }
                }

                AdminTextField(label = "Thumbnail URL (Optional)", value = thumbnailUrl, onValueChange = { thumbnailUrl = it }, placeholder = "https://cloudinary.com/...")
                AdminTextField(label = "Description (Optional)", value = description, onValueChange = { description = it }, placeholder = "Track liner notes...")
            }

            if (!isFormValid) {
                Text(
                    "Please fill Title, Artist ID and Source to continue",
                    color = Color.Red.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = Dimens.paddingNormal)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val multipartPart = if (sourceType == "file" && selectedFileUri != null) {
                        val file = uriToTempFile(context, selectedFileUri!!)
                        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("file", file.name, requestFile)
                    } else null

                    onUploadTrack(
                        title,
                        description.ifBlank { null },
                        artistId,
                        albumId.ifBlank { null },
                        thumbnailUrl.ifBlank { null },
                        youtubeVideoId.ifBlank { null },
                        sourceType,
                        if (sourceType == "youtube") youtubeUrl else null,
                        multipartPart
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(Dimens.radiusPill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Primary else Color.White.copy(alpha = 0.05f),
                    contentColor = if (isFormValid) Color.Black else Color.White.copy(alpha = 0.3f)
                ),
                enabled = isFormValid && !isAdminLoading
            ) {
                if (isAdminLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text("Initialize Deployment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

private fun uriToTempFile(context: android.content.Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "ingest_audio_${System.currentTimeMillis()}.tmp")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
