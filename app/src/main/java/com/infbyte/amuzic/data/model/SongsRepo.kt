package com.infbyte.amuzic.data.model

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.infbyte.amuzic.utils.loadThumbnail
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongsRepo @Inject constructor() {
    @Inject
    @ApplicationContext
    lateinit var appContext: Context

    private val _songs = mutableListOf<Song>()
    val songs: List<Song> = _songs
    var artists = listOf<Artist>()
        private set
    var albums = listOf<Album>()
        private set
    var folders = listOf<Folder>()
        private set
    private val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DATA
    )
    private val selection = null
    private val selectionArgs = null
    private val sortOrder = null

    suspend fun loadSongs(isLoading: () -> Unit, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            isLoading()
            val contentResolver = appContext.contentResolver
            val query = contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            query?.let {
                val idColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn =
                    it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val pathColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val songUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val thumbnail =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentResolver.loadThumbnail(songUri)
                        } else {
                            contentResolver.loadThumbnail(albumId)
                        }
                    val path = it.getString(pathColumn)
                    _songs += Song(
                        id,
                        title,
                        artist,
                        album,
                        extractFolderName(path),
                        songUri,
                        thumbnail
                    )
                }
                query.close()
            }
            loadArtists()
            loadAlbums()
            loadFolders()
            onComplete()
        }
    }

    private fun loadArtists() {
        artists = songs.map {
            it.artist
        }.toSet()
            .map { artist ->
                val numberOfSongs = songs.count { song ->
                    song.artist == artist
                }
                Artist(artist, numberOfSongs)
            }
    }

    private fun loadAlbums() {
        albums = songs.map {
            it.album
        }.toSet()
            .map { album ->
                val numberOfSongs = songs.count { song -> song.album == album }
                Album(album, numberOfSongs)
            }
    }

    private fun loadFolders() {
        folders = songs.map {
            it.folder
        }.toSet()
            .map { folder ->
                val numberOfSongs = songs.count { song -> song.folder == folder }
                Folder(folder, numberOfSongs)
            }
    }

    private fun extractFolderName(path: String) =
        path.substringBeforeLast('/').substringAfterLast('/')

    companion object {
        val ALBUM_PROJECTION = arrayOf(
            MediaStore.Audio.AlbumColumns.ALBUM_ID,
            MediaStore.Audio.AlbumColumns.ALBUM_ART
        )
    }
}
