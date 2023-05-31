package com.infbyte.amuzic.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.utils.calcScroll

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArtistsScreen(
    isVisible: Boolean,
    showPopup: MutableState<Boolean>,
    artists: List<Artist>,
    onScroll: (Int) -> Unit,
    onArtistClick: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(1000)),
        exit = fadeOut(tween(1000))
    ) {
        val state = rememberLazyListState()
        val modifier = if (showPopup.value) {
            Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    if (it.action == MotionEvent.ACTION_DOWN) {
                        showPopup.value = false
                    }
                    true
                }
        } else {
            Modifier
                .fillMaxSize()
        }
        LazyColumn(modifier, state) {
            itemsIndexed(artists) { index, artist ->
                Artist(artist) {
                    onArtistClick(index)
                }
            }
        }
        if (state.isScrollInProgress) {
            onScroll(calcScroll(state))
        }
    }
}

@Composable
fun Artist(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            }
    ) {
        Image(
            ImageVector.vectorResource(R.drawable.ic_audiotrack),
            "",
            Modifier
                .size(48.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(30))
        )
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp)
        ) {
            Text(
                artist.name
            )
            Text(
                artist.numberOfSongs.toString(),
                Modifier.padding(start = 5.dp)
            )
        }
    }
}
