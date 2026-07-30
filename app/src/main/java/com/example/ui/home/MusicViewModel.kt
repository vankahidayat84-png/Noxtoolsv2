package com.example.ui.home

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MusicTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri
)

class MusicViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    private val _tracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    val tracks: StateFlow<List<MusicTrack>> = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicTrack?>(null)
    val currentTrack: StateFlow<MusicTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun initPlayer(context: Context) {
        if (exoPlayer == null) {
            try {
                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }
                        
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            // Update track if needed based on continuous playback.
                            // Currently handling one track at a time logic below.
                        }
                    })
                }
            } catch (e: Exception) {
                // Ignore initialization errors safely
            }
        }
    }

    fun loadMusic(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val musicList = withContext(Dispatchers.IO) {
                    val list = mutableListOf<MusicTrack>()
                    val projection = arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION
                    )
                    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                    
                    context.contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        "${MediaStore.Audio.Media.TITLE} ASC"
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn) ?: "Unknown"
                            val artist = cursor.getString(artistColumn) ?: "Unknown"
                            val duration = cursor.getLong(durationColumn)
                            val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                            list.add(MusicTrack(id, title, artist, duration, contentUri))
                        }
                    }
                    list
                }
                _tracks.value = musicList
            } catch (e: Exception) {
                // Safe fallback for error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playTrack(track: MusicTrack) {
        _currentTrack.value = track
        try {
            exoPlayer?.let { player ->
                player.setMediaItem(MediaItem.fromUri(track.uri))
                player.prepare()
                player.play()
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun togglePlayPause() {
        try {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    if (player.playbackState == Player.STATE_IDLE) {
                        player.prepare()
                    }
                    player.play()
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Exception) {}
    }
}
