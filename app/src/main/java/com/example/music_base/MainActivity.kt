package com.example.music_base

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.music_base.data.model.Track
import com.example.music_base.data.model.User
import com.example.music_base.ui.components.*
import com.example.music_base.ui.screens.home.HomeScreen
import com.example.music_base.ui.screens.player.PlayerScreen
import com.example.music_base.ui.screens.profile.ProfileScreen
import com.example.music_base.ui.screens.profile.ProfileSubScreen
import com.example.music_base.ui.screens.settings.SettingsScreen
import com.example.music_base.ui.screens.auth.LoginScreen
import com.example.music_base.data.player.MusicPlayerManager
import com.example.music_base.ui.screens.album.AlbumDetailScreen
import com.example.music_base.ui.screens.artist.ArtistDetailScreen
import com.example.music_base.ui.screens.playlist.PlaylistDetailScreen
import com.example.music_base.data.model.Album
import com.example.music_base.data.model.Artist
import com.example.music_base.data.model.Playlist
import com.example.music_base.data.model.PlaylistDetail
import com.example.music_base.ui.screens.auth.RegisterScreen
import androidx.compose.ui.platform.LocalContext
import com.example.music_base.ui.screens.home.ShowAllType
import com.example.music_base.ui.screens.home.ShowAllScreen
import com.example.music_base.ui.viewmodel.AuthState
import com.example.music_base.ui.viewmodel.AuthViewModel
import com.example.music_base.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import com.example.music_base.ui.viewmodel.MusicState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.music_base.ui.theme.MusicBaseTheme
import com.example.music_base.ui.screens.library.LibraryScreen
import com.example.music_base.ui.screens.library.LikedTracksScreen
import com.example.music_base.ui.components.SonicToast
import com.example.music_base.ui.components.ToastType
import com.example.music_base.ui.screens.admin.AdminDashboardScreen
import com.example.music_base.ui.screens.admin.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            MusicBaseTheme {
                val app = application as MusicApp
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(app.authRepository)
                )
                val musicViewModel: MusicViewModel = viewModel(
                    factory = MusicViewModel.Factory(app.musicRepository)
                )
                
                MusicMainApp(authViewModel, musicViewModel)
            }
        }
    }
}

@Composable
fun MusicMainApp(authViewModel: AuthViewModel, musicViewModel: MusicViewModel) {
    val authState by authViewModel.authState.collectAsState()

    // Stop music playback when the user logs out
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            MusicPlayerManager.stop()
        }
    }

    AnimatedContent(
        targetState = when (authState) {
            is AuthState.Authenticated -> "authenticated"
            AuthState.InitialLoading -> "loading"
            else -> "auth_gateway"
        },
        transitionSpec = {
            fadeIn(tween(500)) togetherWith fadeOut(tween(500))
        },
        label = "authGateway"
    ) { targetState ->
        when (targetState) {
            "authenticated" -> {
                MainAppScaffold(authViewModel, musicViewModel)
            }
            "loading" -> {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            }
            "auth_gateway" -> {
                AuthGatewayFlow(authViewModel)
            }
        }
    }
}

@Composable
fun AuthGatewayFlow(viewModel: AuthViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = isRegisterMode,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "authSwitch"
        ) { isRegister ->
            if (isRegister) {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { isRegisterMode = false }
                )
            } else {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { isRegisterMode = true }
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MainAppScaffold(authViewModel: AuthViewModel, musicViewModel: MusicViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var selectedNavItem by remember { mutableStateOf(BottomNavItem.Home) }
    var selectedAdminItem by remember { mutableStateOf(AdminBottomNavItem.Command) }
    var isAdminModeActive by remember { mutableStateOf<Boolean>(currentUser?.isAdmin == true) }
    
    // Auto-activate admin mode for admins on login, but allow manual toggle
    LaunchedEffect(currentUser) {
        if (currentUser?.isAdmin == true) {
            isAdminModeActive = true
        }
    }
    val context = LocalContext.current
    val playbackIsPlaying by MusicPlayerManager.isPlaying.collectAsState()
    val playbackPosition by MusicPlayerManager.currentPosition.collectAsState()
    val playbackDuration by MusicPlayerManager.duration.collectAsState()
    val playbackTrack by MusicPlayerManager.currentTrack.collectAsState()
    val isShuffleEnabled by MusicPlayerManager.isShuffle.collectAsState()
    val repeatMode by MusicPlayerManager.repeatMode.collectAsState()
    val scope = rememberCoroutineScope()

    val userPlaylists by musicViewModel.userPlaylists.collectAsState()
    
    // Single source of truth for the displayed track info: the MusicPlayerManager
    val currentTrack by MusicPlayerManager.currentTrack.collectAsState()
    
    var openedAlbum by remember { mutableStateOf<Album?>(null) }
    var openedArtist by remember { mutableStateOf<Artist?>(null) }
    var openedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playingFrom by remember { mutableStateOf("Sonic Gallery") }
    
    val likedTrackIds by musicViewModel.likedTrackIds.collectAsState()
    
    val accessToken by authViewModel.accessToken.collectAsState(initial = null)
    val authState by authViewModel.authState.collectAsState()
    // Explicitly removed duplicate currentUser shadowing to prevent unresolved references

    LaunchedEffect(currentTrack?.id) {
        if (currentTrack != null) {
            while (true) {
                kotlinx.coroutines.delay(10_000)
                val track = currentTrack
                if (playbackIsPlaying && track != null && playbackPosition > 1000L) {
                    musicViewModel.recordPlayback(track.id, playbackPosition, playbackDuration)
                }
            }
        }
    }

    LaunchedEffect(accessToken) {
        if (accessToken != null) {
            musicViewModel.loadMyPlaylists()
            musicViewModel.loadFollowedArtists()
            musicViewModel.loadSuggestedTracks()
            musicViewModel.loadPlaybackHistory()
            musicViewModel.loadLikedTracks()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            musicViewModel.loadMyPlaylists()
            musicViewModel.loadPlaybackHistory()
            musicViewModel.loadLikedTracks()
        }
    }

    var showPlayer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAlbumDetail by remember { mutableStateOf(false) }
    var showArtistDetail by remember { mutableStateOf(false) }
    var showPlaylistDetail by remember { mutableStateOf(false) }
    var showLikedTracks by remember { mutableStateOf(false) }
    var profileInitialSubScreen by remember { mutableStateOf(ProfileSubScreen.Home) }
    var showAllScreenType by remember { mutableStateOf<ShowAllType?>(null) }

    var toastMessageState by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    var toastType by remember { mutableStateOf(ToastType.Info) }

    // Global Add to Playlist state
    var trackToAddToPlaylist by remember { mutableStateOf<Track?>(null) }
    var showGlobalPlaylistSheet by remember { mutableStateOf(false) }

    val onAddToPlaylistGlobal: (Track) -> Unit = { track ->
        trackToAddToPlaylist = track
        showGlobalPlaylistSheet = true
    }

    // Global Share state
    var trackToShare by remember { mutableStateOf<Track?>(null) }
    var showGlobalShareSheet by remember { mutableStateOf(false) }

    // Global Connect state
    var showGlobalConnectSheet by remember { mutableStateOf(false) }

    val onShareGlobal: (Track) -> Unit = { track ->

        trackToShare = track
        showGlobalShareSheet = true
    }


    LaunchedEffect(Unit) {
        musicViewModel.toastMessage.collect { message ->
            toastMessageState = message
            toastType = when {
                message.contains("success", ignoreCase = true) -> ToastType.Success
                message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true) -> ToastType.Error
                else -> ToastType.Info
            }
            showToast = true
        }
    }

    BackHandler(enabled = showPlayer || showSettings || showAlbumDetail || showArtistDetail || showPlaylistDetail || showLikedTracks || showAllScreenType != null) {
        if (showPlayer) showPlayer = false
        else if (showSettings) showSettings = false
        else if (showAlbumDetail) showAlbumDetail = false
        else if (showArtistDetail) showArtistDetail = false
        else if (showPlaylistDetail) showPlaylistDetail = false
        else if (showLikedTracks) showLikedTracks = false
        else if (showAllScreenType != null) showAllScreenType = null
    }

    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isOverlayVisible = showPlayer || showSettings || showAlbumDetail ||
                               showArtistDetail || showPlaylistDetail ||
                               showLikedTracks || (showAllScreenType != null)

        // ── BASE LAYER: Scaffold with nav only ──
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!isOverlayVisible) {
                    MusicTopAppBar(
                        title = when (selectedNavItem) {
                            BottomNavItem.Home -> "Music-Base"
                            BottomNavItem.Search -> "Search"
                            BottomNavItem.Library -> "Your Library"
                            BottomNavItem.Profile -> "Profile"
                        },
                        username = currentUser?.displayName,
                        userAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDpnImc8ni-aAddAenXQt3FvuJOl3tgnNmyxG43Hk6gpO823q_vWltPkSpcjGd2dp3dVhE7lJ96UXc4XTByK22kij7X0XiPbgD0M7E5uqK-2ZSU4cAGFNi4WFZn52nuvkKKYbdtp36_sd5FQ9ax3OcdDk1PwNeSUSxyON8jKiCD7gUQPiWYKQ5vsbLYY2IZC2VK8KkxfuxSYMSVrgAP4uM-dlozAFrYUA4LgfOOJN446ZPGLjoOi5HIOvunrXIcnrCH5-vfsbGzgp0",
                        onSettingsClick = { showSettings = true },
                        adminToggle = if (currentUser?.isAdmin == true) {
                            {
                                isAdminModeActive = !isAdminModeActive
                                // Reset nav items when switching modes
                                if (isAdminModeActive) selectedAdminItem = AdminBottomNavItem.Command
                                else selectedNavItem = BottomNavItem.Home
                            }
                        } else null,
                        isAdminMode = isAdminModeActive
                    )
                }
            },
            bottomBar = {
                if (!isOverlayVisible) {
                    if (isAdminModeActive && currentUser?.isAdmin == true) {
                        AdminBottomNavigation(
                            selectedItem = selectedAdminItem,
                            onItemSelected = { selectedAdminItem = it }
                        )
                    } else {
                        MusicBottomNavigation(
                            selectedItem = selectedNavItem,
                            onItemSelected = { selectedNavItem = it }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                if (isAdminModeActive && currentUser?.isAdmin == true) {
                    // --- ADMIN MODE CONTENT ---
                    AnimatedContent(
                        targetState = selectedAdminItem,
                        label = "adminNavTransition"
                    ) { adminNavItem ->
                        when (adminNavItem) {
                            AdminBottomNavItem.Command -> {
                                val uiState by musicViewModel.uiState.collectAsState()
                                val recentTracks = when (val s = uiState) {
                                    is MusicState.Success -> s.tracks
                                    else -> emptyList()
                                }
                                val totalTrackCount by musicViewModel.totalTrackCount.collectAsState()
                                AdminDashboardScreen(
                                    recentTracks = recentTracks,
                                    totalTrackCount = totalTrackCount,
                                    onEditTrack = { track ->
                                        musicViewModel.setToastMessage("Edit logic for ${track.title}")
                                    },
                                    onDeleteTrack = { track ->
                                        musicViewModel.deleteTrack(track.id)
                                    }
                                )
                            }
                            AdminBottomNavItem.Ingest -> {
                                AdminIngestScreen(
                                    onUploadAudio = { title, artist ->
                                        musicViewModel.uploadAudio(title, artist)
                                    },
                                    onSyncUrl = { url ->
                                        musicViewModel.syncTrackFromUrl(url)
                                    }
                                )
                            }
                            AdminBottomNavItem.Database -> {
                                val allTracks by musicViewModel.tracks.collectAsState()
                                val isAdminLoading by musicViewModel.isAdminLoading.collectAsState()
                                
                                LaunchedEffect(Unit) {
                                    if (allTracks.isEmpty()) {
                                        musicViewModel.loadAdminTracks(isRefresh = true)
                                    }
                                }

                                AdminDatabaseScreen(
                                    tracks = allTracks,
                                    isLoading = isAdminLoading,
                                    onLoadMore = { musicViewModel.loadAdminTracks() },
                                    onSearch = { query -> musicViewModel.loadAdminTracks(isRefresh = true, query = query) },
                                    onEditTrack = { track -> musicViewModel.updateTrackMock(track) },
                                    onDeleteTrack = { track -> musicViewModel.deleteTrack(track.id) },
                                    onRefresh = { musicViewModel.loadAdminTracks(isRefresh = true) }
                                )
                            }
                        }
                    }
                } else {
                    // --- LISTENER MODE CONTENT ---
                    AnimatedContent(
                        targetState = selectedNavItem,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(500)) { fullWidth -> direction * fullWidth } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(tween(400)) { fullWidth -> -direction * fullWidth } + fadeOut(tween(300)))
                    },
                    label = "navTransition"
                ) { targetNavItem ->
                    when (targetNavItem) {
                        BottomNavItem.Home -> {
                            HomeScreen(
                                viewModel = musicViewModel,
                                accessToken = accessToken,
                                onTrackClick = { track, tracks ->
                                    scope.launch {
                                        val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                                        val index = tracks.indexOf(track).coerceAtLeast(0)
                                        if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                            val updatedTracks = tracks.toMutableList()
                                            if (index < updatedTracks.size) updatedTracks[index] = fullTrack
                                            MusicPlayerManager.setQueue(updatedTracks, index)
                                            playingFrom = "Home"
                                            showPlayer = true
                                        } else {
                                            Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onArtistClick = { artist ->
                                    openedArtist = artist
                                    musicViewModel.getArtistTracks(artist.id)
                                    showArtistDetail = true
                                },
                                onAlbumClick = { album ->
                                    openedAlbum = album
                                    musicViewModel.getAlbumDetail(album.id)
                                    showAlbumDetail = true
                                },
                                onPlaylistClick = { playlist ->
                                    openedPlaylist = playlist
                                    musicViewModel.getPlaylistDetail(playlist.id)
                                    showPlaylistDetail = true
                                },
                                onShowAllClick = { type ->
                                    showAllScreenType = null // Reset first to avoid transition issues
                                    showAllScreenType = type
                                },
                                onContinueListeningClick = { index, tracks, listenedSeconds ->
                                    scope.launch {
                                        val track = tracks[index]
                                        val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                                        if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                            val updatedTracks = tracks.toMutableList()
                                            updatedTracks[index] = fullTrack
                                            MusicPlayerManager.setQueue(
                                                updatedTracks, 
                                                index, 
                                                if (listenedSeconds > 2) listenedSeconds * 1000L else 0L
                                            )
                                            playingFrom = "Continue Listening"
                                            showPlayer = true
                                        } else {
                                            Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onAddToPlaylist = onAddToPlaylistGlobal,
                                onShare = onShareGlobal
                            )
                        }

                        BottomNavItem.Search -> com.example.music_base.ui.screens.search.SearchScreen(
                            viewModel = musicViewModel,
                            onTrackClick = { track ->
                                scope.launch {
                                    val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                                    if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                        val searchResults = musicViewModel.searchResults.value
                                        if (searchResults.contains(track)) {
                                            val index = searchResults.indexOf(track)
                                            val updatedResults = searchResults.toMutableList()
                                            updatedResults[index] = fullTrack
                                            MusicPlayerManager.setQueue(updatedResults, index)
                                        } else {
                                            MusicPlayerManager.setQueue(listOf(fullTrack), 0)
                                        }
                                        playingFrom = "Search Results"
                                        showPlayer = true
                                    } else {
                                        Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onAddToPlaylist = onAddToPlaylistGlobal,
                            onShare = onShareGlobal
                        )

                        BottomNavItem.Library -> LibraryScreen(
                            viewModel = musicViewModel,
                            onPlaylistClick = { playlist ->
                                openedPlaylist = playlist
                                musicViewModel.getPlaylistDetail(playlist.id)
                                showPlaylistDetail = true
                            },
                            onArtistClick = { artist ->
                                openedArtist = artist
                                musicViewModel.getArtistTracks(artist.id)
                                showArtistDetail = true
                            },
                            onLikedSongsClick = {
                                showLikedTracks = true
                            }
                        )
                        BottomNavItem.Profile -> ProfileScreen(
                            authViewModel = authViewModel,
                            musicViewModel = musicViewModel,
                            initialSubScreen = profileInitialSubScreen,
                            onArtistClick = { artist ->
                                openedArtist = artist
                                musicViewModel.getArtistTracks(artist.id)
                                showArtistDetail = true
                            },
                            onHistoryTrackClick = { trackId, trackName, trackThumb, listenedSeconds ->
                                scope.launch {
                                    val fullTrack = musicViewModel.fetchTrackDetail(trackId)
                                    
                                    if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                        MusicPlayerManager.setQueue(listOf(fullTrack), 0, if (listenedSeconds > 2) listenedSeconds * 1000L else 0L)
                                        playingFrom = "History"
                                        showPlayer = true
                                    }

                                }
                            },
                            onAddToPlaylist = onAddToPlaylistGlobal,
                            onShare = onShareGlobal
                        )

                    }
                }
            }
        }

        // MiniPlayer — sits above bottom nav but respects Scaffold padding
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column {
                    AnimatedVisibility(
                        visible = currentTrack != null && !showPlayer,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        currentTrack?.let { track ->
                            MiniPlayer(
                                currentTrack = track.copy(currentPosition = playbackPosition, duration = playbackDuration.toDouble() / 1000.0),
                                isPlaying = playbackIsPlaying,
                                onPlayPauseClick = { MusicPlayerManager.togglePlayPause() },
                                onPlayerClick = { showPlayer = true },
                                onDevicesClick = { },
                                onDismiss = {
                                    MusicPlayerManager.stop()
                                },
                                onNextTrack = {
                                    MusicPlayerManager.next()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }

    // ── OVERLAY LAYERS: outside Scaffold → covers full screen ──

        // Show All Overlay (Lowest in Z-order among overlays)
        AnimatedVisibility(
            visible = showAllScreenType != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            showAllScreenType?.let { type ->
                val suggestedTracks by musicViewModel.suggestedTracks.collectAsState()
                val rankingTracks by musicViewModel.rankingTracks.collectAsState()
                val isLoadingMore by musicViewModel.isLoadingMore.collectAsState()
                val successState = musicViewModel.uiState.collectAsState().value as? MusicState.Success
                val albums = successState?.albums ?: emptyList()
                val artists = successState?.artists ?: emptyList()
                
                val tracksToList = when (type) {
                    ShowAllType.TOP_RANKING -> rankingTracks
                    ShowAllType.SUGGESTED -> suggestedTracks
                    else -> emptyList()
                }
                val albumsToList = if (type == ShowAllType.TOP_ALBUMS) albums else emptyList()
                val artistsToList = if (type == ShowAllType.TOP_ARTISTS) artists else emptyList()
                
                var trackForMenu by remember { mutableStateOf<Track?>(null) }
                val likedTrackIds by musicViewModel.likedTrackIds.collectAsState()

                ShowAllScreen(
                    type = type,
                    tracks = tracksToList,
                    albums = albumsToList,
                    artists = artistsToList,
                    currentPlayingTrack = currentTrack,
                    onBackClick = { 
                        showAllScreenType = null
                        musicViewModel.resetPagination()
                    },
                    onTrackClick = { index, tracks ->
                        scope.launch {
                            val track = tracks[index]
                            val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                            if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                val updatedTracks = tracks.toMutableList()
                                updatedTracks[index] = fullTrack
                                MusicPlayerManager.setQueue(updatedTracks, index)
                                playingFrom = type.title
                                showPlayer = true
                            } else {
                                Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onTrackMoreClick = { trackForMenu = it },
                    onAlbumClick = { album ->
                        openedAlbum = album
                        musicViewModel.getAlbumDetail(album.id)
                        showAlbumDetail = true
                    },
                    onAlbumMoreClick = { /* Optional handling */ },
                    onArtistClick = { artist ->
                        openedArtist = artist
                        musicViewModel.getArtistTracks(artist.id)
                        showArtistDetail = true
                    },
                    isLoadingMore = isLoadingMore,
                    onLoadMore = {
                        when (type) {
                            ShowAllType.SUGGESTED -> musicViewModel.loadMoreSuggestedTracks()
                            ShowAllType.TOP_RANKING -> musicViewModel.loadMoreRankingTracks()
                            ShowAllType.TOP_ALBUMS -> musicViewModel.loadMoreAlbums()
                            ShowAllType.TOP_ARTISTS -> musicViewModel.loadMoreArtists()
                        }
                    }
                )

                if (trackForMenu != null) {
                    TrackActionSheet(
                        track = trackForMenu!!,
                        onDismiss = { trackForMenu = null },
                        onToggleLike = { musicViewModel.toggleTrackLike(trackForMenu!!.id) },
                        isLiked = likedTrackIds.contains(trackForMenu!!.id),
                        onAddToPlaylist = { 
                            onAddToPlaylistGlobal(trackForMenu!!)
                        },
                        onShare = {
                            onShareGlobal(trackForMenu!!)
                        }

                    )
                }
            }
        }

        // Album Detail Overlay
        AnimatedVisibility(
            visible = showAlbumDetail,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            openedAlbum?.let { album ->
                val albumDetail by musicViewModel.albumDetail.collectAsState()
                val isDetailLoading by musicViewModel.isDetailLoading.collectAsState()
                AlbumDetailScreen(
                    album = album,
                    albumDetail = albumDetail,
                    currentPlayingTrack = currentTrack,
                    isLoading = isDetailLoading,
                    onBackClick = { showAlbumDetail = false },
                    onTrackClick = { track, tracks ->
                        scope.launch {
                            val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                            val index = tracks.indexOf(track)
                            if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                val updatedTracks = tracks.toMutableList()
                                if (index != -1) updatedTracks[index] = fullTrack
                                MusicPlayerManager.setQueue(updatedTracks, if (index != -1) index else 0)
                                playingFrom = album.title
                                showPlayer = true
                            } else {
                                Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onPlayAllClick = { tracks ->
                        MusicPlayerManager.setQueue(tracks, 0)
                        showPlayer = true
                    },
                    onToggleShuffle = { 
                        if (!isShuffleEnabled) MusicPlayerManager.toggleShuffle(musicViewModel.likedTrackIds.value)
                        val albumTracks = albumDetail?.tracks ?: emptyList()
                        if (albumTracks.isNotEmpty()) {
                            MusicPlayerManager.setQueue(albumTracks, 0)
                            playingFrom = album.title
                            showPlayer = true
                        }
                    },
                    isShuffleEnabled = isShuffleEnabled,
                    onShuffleClick = { tracks ->
                        if (!isShuffleEnabled) MusicPlayerManager.toggleShuffle(musicViewModel.likedTrackIds.value)
                        MusicPlayerManager.setQueue(tracks, 0)
                        playingFrom = album.title
                        showPlayer = true
                    },
                    onFavoriteClick = { track ->
                        // Feature removed due to missing API
                    },
                    onRefresh = { musicViewModel.getAlbumDetail(album.id) },
                    onShare = onShareGlobal
                )
            }

        }

        // Artist Detail Overlay
        AnimatedVisibility(
            visible = showArtistDetail,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            openedArtist?.let { artist ->
                val tracks by musicViewModel.artistTracks.collectAsState()
                val isLoading by musicViewModel.isArtistDetailLoading.collectAsState()
                val followedIds by musicViewModel.followedArtistIds.collectAsState()
                ArtistDetailScreen(
                    artist = artist,
                    tracks = tracks,
                    currentPlayingTrack = currentTrack,
                    isFollowing = followedIds.contains(artist.id),
                    isLoading = isLoading,
                    onBackClick = { showArtistDetail = false },
                    onFollowClick = {
                        if (followedIds.contains(artist.id)) {
                            musicViewModel.unfollowArtist(artist.id)
                        } else {
                            musicViewModel.followArtist(artist.id)
                        }
                    },
                    onTrackClick = { track, allTracks ->
                        scope.launch {
                            val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                            val index = allTracks.indexOf(track)
                            if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                val updatedTracks = allTracks.toMutableList()
                                if (index != -1) updatedTracks[index] = fullTrack
                                MusicPlayerManager.setQueue(updatedTracks, if (index != -1) index else 0)
                                playingFrom = artist.name
                                showPlayer = true
                            } else {
                                Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onPlayAllClick = { allTracks ->
                        MusicPlayerManager.setQueue(allTracks, 0)
                        showPlayer = true
                    },
                    isShuffleEnabled = isShuffleEnabled,
                    onShuffleClick = { 
                        if (!isShuffleEnabled) MusicPlayerManager.toggleShuffle(musicViewModel.likedTrackIds.value)
                        if (tracks.isNotEmpty()) {
                            MusicPlayerManager.setQueue(tracks, 0)
                            playingFrom = artist.name
                            showPlayer = true
                        }
                    },
                    onRefresh = { musicViewModel.getArtistTracks(artist.id) },
                    onShare = onShareGlobal
                )
            }

        }

        val currentPlaylistDetail by musicViewModel.playlistDetail.collectAsState()
        val isPlaylistDetailLoading by musicViewModel.isPlaylistDetailLoading.collectAsState()

        // Playlist Detail Overlay
        AnimatedVisibility(
            visible = showPlaylistDetail,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            openedPlaylist?.let { playlist ->
                PlaylistDetailScreen(
                    viewModel = musicViewModel,
                    playlist = playlist,
                    playlistDetail = currentPlaylistDetail,
                    currentPlayingTrack = currentTrack,
                    onBackClick = { showPlaylistDetail = false },
                    onTrackClick = { track, tracks ->
                        scope.launch {
                            val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                            val index = tracks.indexOf(track)
                            if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                                val updatedTracks = tracks.toMutableList()
                                if (index != -1) updatedTracks[index] = fullTrack
                                MusicPlayerManager.setQueue(updatedTracks, if (index != -1) index else 0)
                                playingFrom = playlist.name
                                showPlayer = true
                            } else {
                                Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onPlayAllClick = { tracks ->
                        MusicPlayerManager.setQueue(tracks, 0)
                        playingFrom = playlist.name
                        showPlayer = true
                    },
                    isShuffleEnabled = isShuffleEnabled,
                    onToggleShuffle = { 
                        if (!isShuffleEnabled) MusicPlayerManager.toggleShuffle(musicViewModel.likedTrackIds.value)
                        val playlistTracks: List<Track> = currentPlaylistDetail?.tracks?.data?.map { it.track } ?: emptyList()
                        if (playlistTracks.isNotEmpty()) {
                            MusicPlayerManager.setQueue(playlistTracks, 0)
                            showPlayer = true
                            playingFrom = playlist.name
                        }
                    },
                    onShuffleClick = { tracks ->
                        MusicPlayerManager.setQueue(tracks, 0)
                        playingFrom = playlist.name
                        showPlayer = true
                    },
                    onDeletePlaylist = {
                        musicViewModel.deletePlaylist(playlist.id)
                        showPlaylistDetail = false
                    },
                    onRefresh = {
                        musicViewModel.getPlaylistDetail(playlist.id)
                    },
                    onEditPlaylist = { name, desc, isPub ->
                        musicViewModel.updatePlaylist(playlist.id, name, desc, isPub)
                    },
                    onAddToPlaylist = onAddToPlaylistGlobal,
                    onShare = onShareGlobal
                )
            }
        }


        // Liked Tracks Overlay
        AnimatedVisibility(
            visible = showLikedTracks,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            LikedTracksScreen(
                viewModel = musicViewModel,
                currentPlayingTrack = currentTrack,
                onBackClick = { showLikedTracks = false },
                onTrackClick = { track, tracks ->
                    scope.launch {
                        val fullTrack = musicViewModel.fetchTrackDetail(track.id)
                        val index = tracks.indexOf(track)
                        if (fullTrack != null && !fullTrack.audioUrl.isNullOrBlank()) {
                            val updatedTracks = tracks.toMutableList()
                            if (index != -1) updatedTracks[index] = fullTrack
                            MusicPlayerManager.setQueue(updatedTracks, if (index != -1) index else 0)
                            playingFrom = "Liked Songs"
                            showPlayer = true
                        } else {
                            Toast.makeText(context, "Cannot play: Missing audio URL", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onPlayAllClick = { tracks ->
                    MusicPlayerManager.setQueue(tracks, 0)
                    playingFrom = "Liked Songs"
                    showPlayer = true
                },
                isShuffleEnabled = isShuffleEnabled,
                onToggleShuffle = { 
                    if (!isShuffleEnabled) MusicPlayerManager.toggleShuffle(musicViewModel.likedTrackIds.value)
                    val tracks = musicViewModel.likedTracks.value
                    if (tracks.isNotEmpty()) {
                        MusicPlayerManager.setQueue(tracks, 0)
                        playingFrom = "Liked Songs"
                        showPlayer = true
                    }
                },
                onShuffleClick = { tracks ->
                    MusicPlayerManager.setQueue(tracks, 0)
                    playingFrom = "Liked Songs"
                    showPlayer = true
                },
                onAddToPlaylist = onAddToPlaylistGlobal,
                onShare = onShareGlobal
            )
        }


        // Settings Overlay
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            SettingsScreen(
                onBackClick = { showSettings = false },
                viewModel = authViewModel,
                onNavigateToLogin = {
                    showSettings = false
                    profileInitialSubScreen = ProfileSubScreen.Login
                    selectedNavItem = BottomNavItem.Profile
                },
                onNavigateToRegister = {
                    showSettings = false
                    profileInitialSubScreen = ProfileSubScreen.Register
                    selectedNavItem = BottomNavItem.Profile
                }
            )
        }

        // Z-order fixed: Show All sits below other detail screens

        // Full Screen Player (topmost)
        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            currentTrack?.let { track ->
                PlayerScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight)
                        .pointerInput(Unit) { 
                            // This block consumes all touch events, 
                            // preventing them from leaking to the background
                        },
                    track = track.copy(currentPosition = playbackPosition, duration = playbackDuration.toDouble() / 1000.0),
                    isPlaying = playbackIsPlaying,
                    userPlaylists = userPlaylists,
                    playingFrom = playingFrom,
                    onPlayPauseClick = { MusicPlayerManager.togglePlayPause() },
                    onPreviousClick = { MusicPlayerManager.previous() },
                    onNextClick = { MusicPlayerManager.next() },
                    onBackClick = { showPlayer = false },
                    onAddToPlaylistClick = { playlist ->
                        musicViewModel.addTrackToPlaylist(playlist.id, track.id)
                    },
                    onCreatePlaylist = { name, description, isPub ->
                        musicViewModel.createPlaylist(name, description, isPub)
                    },
                    onSeek = { MusicPlayerManager.seekTo(it) },
                    isLiked = likedTrackIds.contains(track.id),
                    onLikeClick = { musicViewModel.toggleTrackLike(track.id) },
                    onArtistClick = { artist ->
                        openedArtist = artist
                        musicViewModel.getArtistTracks(artist.id)
                        showArtistDetail = true
                        showPlayer = false
                    },
                    isShuffleEnabled = isShuffleEnabled,
                    onShuffleClick = { musicViewModel.likedTrackIds.value.let { MusicPlayerManager.toggleShuffle(it) } },
                    repeatMode = repeatMode,
                    onRepeatClick = { MusicPlayerManager.toggleRepeat() },
                    onConnectClick = { showGlobalConnectSheet = true }
                )

            }
        }

        // SonicToast Layer (highest priority)
        SonicToast(
            message = toastMessageState,
            type = toastType,
            isVisible = showToast,
            onDismiss = { showToast = false }
        )

        // Global Add to Playlist Sheet
        if (showGlobalPlaylistSheet && trackToAddToPlaylist != null) {
            AddToPlaylistSheet(
                playlists = userPlaylists,
                onDismiss = { showGlobalPlaylistSheet = false },
                onPlaylistClick = { playlist ->
                    musicViewModel.addTrackToPlaylist(playlist.id, trackToAddToPlaylist!!.id)
                    showGlobalPlaylistSheet = false
                    trackToAddToPlaylist = null
                },
                onCreateNewClick = {
                   // This can be expanded to show CreatePlaylistDialog
                }
            )
        }

        // Global Share Sheet
        if (showGlobalShareSheet && trackToShare != null) {
            ShareSheet(
                track = trackToShare!!,
                onDismiss = {
                    showGlobalShareSheet = false
                    trackToShare = null
                }
            )
        }

        // Global Connect Sheet
        if (showGlobalConnectSheet) {
            com.example.music_base.ui.components.ConnectSheet(
                onDismiss = { showGlobalConnectSheet = false },
                onDeviceSelected = { deviceName ->
                    // For now, just toast the selection
                    musicViewModel.setToastMessage("Connecting to $deviceName...")
                    showGlobalConnectSheet = false
                }
            )
        }
    }
}



