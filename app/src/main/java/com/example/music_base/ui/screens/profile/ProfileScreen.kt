package com.example.music_base.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.viewmodel.*
import com.example.music_base.data.model.*
import com.example.music_base.ui.screens.auth.LoginScreen
import com.example.music_base.ui.screens.auth.RegisterScreen
import com.example.music_base.ui.screens.settings.SettingsContent
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    initialSubScreen: ProfileSubScreen = ProfileSubScreen.Home,
    onArtistClick: (Artist) -> Unit = {},
    onHistoryTrackClick: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {}
) {

    val authState by authViewModel.authState.collectAsState()
    val followedArtists by musicViewModel.followedArtists.collectAsState()
    var currentSubScreen by remember { mutableStateOf(initialSubScreen) }

    // Sync with initialSubScreen when it changes from outside (e.g. MainActivity navigation)
    LaunchedEffect(initialSubScreen) {
        currentSubScreen = initialSubScreen
    }

    // Reset sub-screen when auth state changes if needed
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            currentSubScreen = ProfileSubScreen.Home
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val state = authState // Capture for smart-casting
        when (state) {
            is AuthState.Authenticated -> {
                val user = state.user
                
                val userPlaylists by musicViewModel.userPlaylists.collectAsState()
                val insights by musicViewModel.listeningInsights.collectAsState()
                val topArtistsFromHistory by musicViewModel.topArtistsFromHistory.collectAsState()
                val isHistoryLoading by musicViewModel.isHistoryLoading.collectAsState()

                // Auto-refresh history+insights whenever this profile screen is shown
                LaunchedEffect(Unit) {
                    musicViewModel.loadPlaybackHistory()
                }


                if (currentSubScreen == ProfileSubScreen.Following) {
                    BackHandler { currentSubScreen = ProfileSubScreen.Home }
                    FollowingArtistsScreen(
                        artists = followedArtists,
                        onBackClick = { currentSubScreen = ProfileSubScreen.Home },
                        onArtistClick = onArtistClick
                    )
                } else if (currentSubScreen == ProfileSubScreen.History) {
                    BackHandler { currentSubScreen = ProfileSubScreen.Home }
                    PlaybackHistoryScreen(
                        viewModel = musicViewModel,
                        onBackClick = { currentSubScreen = ProfileSubScreen.Home },
                        onTrackClick = onHistoryTrackClick,
                        onAddToPlaylist = onAddToPlaylist,
                        onShare = onShare
                    )

                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        item {
                            ProfileHeaderSection(
                                username = user.displayName,
                                followingCount = followedArtists.size,
                                onFollowingClick = { currentSubScreen = ProfileSubScreen.Following }
                            )
                        }
                        item {
                            ListeningInsightsSection(
                                insights = insights,
                                isHistoryLoading = isHistoryLoading,
                                topArtists = topArtistsFromHistory,
                                onArtistClick = onArtistClick
                            )
                        }
                        item {
                            TopArtistsSectionReal(
                                artists = followedArtists.take(5),
                                onArtistClick = onArtistClick
                            )
                        }




                        item {
                            PublicPlaylistsSectionReal(
                                playlists = userPlaylists.filter { it.isPublic == true }
                            )
                        }
                        item {
                            HistoryShortcutButton(
                                onClick = { currentSubScreen = ProfileSubScreen.History }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
            AuthState.Unauthenticated, is AuthState.Error, AuthState.Loading, AuthState.Idle, AuthState.RegistrationSuccess, AuthState.InitialLoading -> {
                when (currentSubScreen) {
                    ProfileSubScreen.Home -> {
                        SettingsContent(
                            viewModel = authViewModel,
                            onNavigateToLogin = { currentSubScreen = ProfileSubScreen.Login },
                            onNavigateToRegister = { currentSubScreen = ProfileSubScreen.Register }
                        )
                    }
                    ProfileSubScreen.Login -> {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToRegister = { currentSubScreen = ProfileSubScreen.Register }
                        )
                        // Add a way to go back to guest settings if needed, or just let them logout/cancel
                    }
                    ProfileSubScreen.Register -> {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = { currentSubScreen = ProfileSubScreen.Login }
                        )
                    }
                    ProfileSubScreen.Following -> {
                        currentSubScreen = ProfileSubScreen.Home
                    }
                    ProfileSubScreen.History -> {
                        currentSubScreen = ProfileSubScreen.Home
                    }
                }
                
                // Error feedback is now handled by SonicToast in the sub-screens.

                if ((state is AuthState.Loading || state is AuthState.InitialLoading) && currentSubScreen == ProfileSubScreen.Home) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

enum class ProfileSubScreen {
    Home, Login, Register, Following, History
}

@Composable
fun ProfileHeaderSection(
    username: String,
    followingCount: Int,
    onFollowingClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.padding(bottom = 24.dp).size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp).background(Color(0xFF72FE8F).copy(alpha = 0.2f), CircleShape).blur(40.dp)
            )
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDpnImc8ni-aAddAenXQt3FvuJOl3tgnNmyxG43Hk6gpO823q_vWltPkSpcjGd2dp3dVhE7lJ96UXc4XTByK22kij7X0XiPbgD0M7E5uqK-2ZSU4cAGFNi4WFZn52nuvkKKYbdtp36_sd5FQ9ax3OcdDk1PwNeSUSxyON8jKiCD7gUQPiWYKQ5vsbLYY2IZC2VK8KkxfuxSYMSVrgAP4uM-dlozAFrYUA4LgfOOJN446ZPGLjoOi5HIOvunrXIcnrCH5-vfsbGzgp0",
                contentDescription = "Profile",
                modifier = Modifier.size(170.dp).clip(CircleShape).border(4.dp, Color(0xFF262626), CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Text("VERIFIED COLLECTOR", color = Color(0xFF72FE8F), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(username, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onFollowingClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(followingCount.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("FOLLOWING", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 2.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TopArtistsSectionReal(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("FOLLOWING", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Artists You Follow", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (artists.isEmpty()) {
            Text("Follow some artists to see them here!", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                items(artists) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(90.dp).clickable { onArtistClick(artist) }
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF262626)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(artist.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("ARTIST", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PublicPlaylistsSectionReal(
    playlists: List<Playlist>
) {
    if (playlists.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("VAULT", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Public Playlists", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            playlists.take(4).forEach { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131313), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF262626))
                    ) {
                        if (!playlist.coverUrl.isNullOrBlank()) {
                            AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(playlist.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(playlist.description ?: "Public playlist", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryShortcutButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1A1A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF72FE8F).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("HISTORY", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Listening History", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("→", color = Color(0xFF72FE8F), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FollowingArtistsScreen(
    artists: List<Artist>,
    onBackClick: () -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 8.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                "Following",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (artists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "You aren't following anyone yet",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Explore artists and follow your favorites!",
                        color = Color(0xFF72FE8F),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onBackClick() }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(artists) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onArtistClick(artist) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262626)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                artist.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Artist",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningInsightsSection(
    insights: InsightData?,
    isHistoryLoading: Boolean,
    topArtists: List<TopArtistInfo>,
    onArtistClick: (Artist) -> Unit = {}
) {


    Column(modifier = Modifier.fillMaxWidth()) {
        Text("STATISTICS", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Text("Listening Insights", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(

            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("🔥 STREAK", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isHistoryLoading && insights == null) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White.copy(alpha = 0.5f))
                    } else {
                        Text("${insights?.streak ?: 0} Days", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            
            // Active Hour Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("⏰ ACTIVE AT", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isHistoryLoading && insights == null) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White.copy(alpha = 0.5f))
                    } else {
                        Text(insights?.preferredTimeOfDay ?: "--", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

        }
        
        if (topArtists.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("VIBES", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("Your Top Artists", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                items(topArtists) { info ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(85.dp).clickable { onArtistClick(info.artist) }
                    ) {
                        AsyncImage(
                            model = info.artist.imageUrl,
                            contentDescription = info.artist.name,
                            modifier = Modifier.size(85.dp).clip(CircleShape).background(Color(0xFF262626)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(info.artist.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${info.playCount} plays", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}


