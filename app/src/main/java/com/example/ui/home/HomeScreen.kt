package com.example.ui.home

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.ui.theme.*

@Composable
fun HomeScreen() {
    var isSidebarOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DarkPurple)) {
        
        val blurRadius by animateDpAsState(
            targetValue = if (isSidebarOpen) 16.dp else 0.dp,
            animationSpec = tween(300),
            label = "blur"
        )
        
        val blurModifier = if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier)
        ) {
            HomeContent(onOpenSidebar = { isSidebarOpen = true })
        }

        // Scrim
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isSidebarOpen = false }
                    )
            )
        }

        // Sidebar
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { -it }
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetX = { -it }
            ),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Sidebar(onClose = { isSidebarOpen = false })
        }
    }
}

@Composable
fun HomeContent(onOpenSidebar: () -> Unit) {
    val scrollState = rememberLazyListState()
    
    val firstVisibleItemScrollOffset = remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
    val firstVisibleItemIndex = remember { derivedStateOf { scrollState.firstVisibleItemIndex } }
    
    val parallaxOffset = if (firstVisibleItemIndex.value == 0) {
        firstVisibleItemScrollOffset.value * 0.5f
    } else {
        1000f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPurple)
    ) {
        // 1. Background Image
        Image(
            painter = painterResource(id = R.drawable.background_home),
            contentDescription = "Home Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Dark Gradient Overlay to match HTML exactly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SoftPurple.copy(alpha = 0.5f),
                            DarkPurple.copy(alpha = 0.7f),
                            WarmBrown.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // 2. Fixed Banner with Parallax
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -parallaxOffset
                }
        ) {
            VideoBanner()
        }

        // 3. Scrollable Content
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // AspectRatio 16:9 spacer for the banner
                Spacer(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                ProfileCard(modifier = Modifier.padding(horizontal = 20.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                MainMenuGrid(modifier = Modifier.padding(horizontal = 20.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                PrayerScheduleCard(modifier = Modifier.padding(horizontal = 20.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FinanceCard(
                    title = "Total Tabungan",
                    amount = "Rp 0",
                    subtitle = "Belum ada data tabungan",
                    icon = "🏦",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FinanceCard(
                    title = "Total Hutang",
                    amount = "Rp 0",
                    subtitle = "Tidak ada hutang",
                    icon = "💳",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // 4. Header (Overlay at top)
        HomeHeader(
            onOpenSidebar = onOpenSidebar,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun Sidebar(onClose: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.65f)
            .background(DarkPurple)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 32.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SidebarCategory(
                title = "Menu Harian",
                items = listOf("Belanja", "Menabung", "Hutang", "Piutang", "Riwayat"),
                accentColor = SakuraPink
            )
            SidebarCategory(
                title = "Downloader",
                items = listOf("YouTube", "Instagram", "TikTok", "Pinterest", "Riwayat"),
                accentColor = PeachOrange
            )
            SidebarCategory(
                title = "Music Player",
                items = listOf("Daftar Musik", "Playlist"),
                accentColor = SoftPurpleAccent
            )
            SidebarCategory(
                title = "Daftar Tools",
                items = listOf("Gempa BMKG", "Cuaca", "Translate", "NSLookup", "SQLite", "Bot WhatsApp", "Buat Sticker", "Maps", "Browser", "QR"),
                accentColor = CyanBlue
            )
            SidebarCategory(
                title = "Menu AI",
                items = listOf("Chatbot", "Analisa Pengeluaran", "Edukasi Uang"),
                accentColor = AccentViolet
            )
            SidebarCategory(
                title = "Sistem",
                items = listOf("Kelola Server Backend", "Kelola API AI", "Tambah Tools", "Ganti Profil", "Ganti Tema", "Info Aplikasi", "Info Versi", "Changelog Update"),
                accentColor = AccentGold
            )
        }
        
        // Fade shadows for overscroll visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Brush.verticalGradient(listOf(DarkPurple, Color.Transparent)))
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, DarkPurple)))
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SidebarCategory(title: String, items: List<String>, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(GlassDark)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        items.forEach { item ->
            SidebarItem(text = item, accentColor = accentColor)
        }
    }
}

@Composable
fun SidebarItem(text: String, accentColor: Color) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = accentColor)
            ) {}
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .shadow(4.dp, CircleShape, spotColor = accentColor)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HomeHeader(onOpenSidebar: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Hamburger
        IconButton(
            onClick = onOpenSidebar,
            modifier = Modifier.size(44.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(WarmPeach)
                    )
                }
            }
        }
        
        // Custom Bell
        IconButton(
            onClick = { /* Notifications */ },
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = WarmPeach,
                    modifier = Modifier.size(28.dp)
                )
                // Yellow dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(8.dp)
                        .shadow(8.dp, CircleShape, spotColor = GoldenLight)
                        .clip(CircleShape)
                        .background(GoldenLight)
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoBanner() {
    val context = LocalContext.current
    val bannerResId = remember {
        context.resources.getIdentifier("banner", "raw", context.packageName)
    }

    val exoPlayer = remember(bannerResId) {
        if (bannerResId != 0) {
            try {
                ExoPlayer.Builder(context)
                    .build().apply {
                    val uri = Uri.parse("android.resource://${context.packageName}/$bannerResId")
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    trackSelectionParameters = trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                        .build()
                    prepare()
                    playWhenReady = true
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                exoPlayer?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f/9f)
            .background(DarkPurple)
    ) {
        if (exoPlayer != null) {
            AndroidView(
                factory = {
                    try {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                            useArtwork = false
                            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    } catch (e: Exception) {
                        FrameLayout(context)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Top and Bottom Borders
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlassWhite)
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlassWhite)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ProfileCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(GlassDark)
            .border(1.dp, GlassWhite, RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image Container (Gradient Border)
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = SakuraPink.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SakuraPink, GoldenLight)
                    )
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkPurple),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder SVG icon replacement
                Text("👤", fontSize = 24.sp, modifier = Modifier.graphicsLayer { alpha = 0.4f })
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Tidak ada pengguna",
                color = GoldenLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "OWNER • PRIVATE APP",
                color = WarmPeach.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun MainMenuGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(title = "Belanja", icon = "🛍️", modifier = Modifier.weight(1f))
            MenuCard(title = "Menabung", icon = "💰", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(title = "Hutang", icon = "💸", modifier = Modifier.weight(1f))
            MenuCard(title = "Piutang", icon = "📈", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MenuCard(title: String, icon: String, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(GlassDark)
            .border(1.dp, GlassWhite, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = Color.White)
            ) { /* Navigate */ }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarmPeach.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = WarmPeach.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun PrayerScheduleCard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(GlassDark)
            .border(1.dp, GlassWhite, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🕌",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jadwal Sholat",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BANJARNEGARA WIB",
                    color = GoldenLight.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80).copy(alpha = dotAlpha))
                )
            }
        }
        
        // Schedule
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrayerTimeItem("Imsak", "04:12")
            DividerVertical()
            PrayerTimeItem("Subuh", "04:22")
            DividerVertical()
            PrayerTimeItem("Dzuhur", "11:38", isHighlighted = true)
            DividerVertical()
            PrayerTimeItem("Ashar", "14:55")
            DividerVertical()
            PrayerTimeItem("Maghrib", "17:51")
            DividerVertical()
            PrayerTimeItem("Isya", "19:02")
        }
    }
}

@Composable
fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.White.copy(alpha = 0.05f))
    )
}

@Composable
fun PrayerTimeItem(name: String, time: String, isHighlighted: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (isHighlighted) {
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SakuraPink.copy(alpha = 0.1f))
                .border(1.dp, SakuraPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(6.dp)
        } else {
            Modifier.padding(6.dp)
        }
    ) {
        Text(
            text = name.uppercase(),
            color = if (isHighlighted) SakuraPink else WarmPeach.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = time,
            color = if (isHighlighted) Color.White else GoldenLight,
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun FinanceCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(GlassDark)
            .border(1.dp, GlassWhite, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarmPeach.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = WarmPeach.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = amount,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = subtitle,
            color = WarmPeach.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
