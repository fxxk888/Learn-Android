package com.fuck.learn.ui.activity.live

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fuck.learn.R
import com.fuck.learn.data.db.StreamerForLive
import com.fuck.learn.ui.theme.DouyinToolTheme
import com.fuck.learn.utils.LogUtils
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
class AddLiveStreamerActivity : ComponentActivity() {

    private val viewModel: AddLiveStreamerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DouyinToolTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column {
                            TopAppBar(
                                title = { Text(stringResource(R.string.add_live_steamer_label)) },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.content_description_back)
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }) { innerPadding ->
                    AddLiveStreamerScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AddLiveStreamerScreen(
    modifier: Modifier = Modifier,
    viewModel: AddLiveStreamerViewModel
) {
    var streamerUrl by remember { mutableStateOf("") }
    val addLiveStreamerUiState by viewModel.addLiveStreamerUiState.collectAsState()
    val streamersFromDb by viewModel.streamers.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = addLiveStreamerUiState is AddLiveStreamerUiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val successMessage = stringResource(R.string.success_label)
    val errorMessagePrefix = stringResource(R.string.error)

    var displayedStreamerForLive by remember { mutableStateOf<List<StreamerForLive>>(emptyList()) }
    LaunchedEffect(streamersFromDb) {
        displayedStreamerForLive = streamersFromDb
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        displayedStreamerForLive = displayedStreamerForLive.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Column(modifier = modifier.padding(16.dp, 8.dp, 16.dp, 0.dp)) {
        OutlinedTextField(
            value = streamerUrl,
            onValueChange = { streamerUrl = it },
            label = { Text(stringResource(R.string.add_live_steamer_input_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                viewModel.fetchUserProfile(streamerUrl)
                keyboardController?.hide()
            })
        )
        Button(
            onClick = {
                viewModel.fetchUserProfile(streamerUrl)
                keyboardController?.hide()
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isLoading

        ) {
            Text(if (isLoading) stringResource(R.string.adding_label) else stringResource(R.string.add_button))
        }

        Spacer(modifier = Modifier.size(8.dp))
        HorizontalDivider()

        LazyColumn(
            state = lazyListState
        ) {
            items(displayedStreamerForLive, key = { it.secUid }) { streamer ->
                ReorderableItem(
                    state = reorderableState,
                    key = streamer.secUid
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = streamer.avatarUrl,
                                contentDescription = "${streamer.nickname} ${stringResource(R.string.avatar_image_desc)}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Text(text = streamer.nickname, modifier = Modifier.weight(1f))

                            IconButton(
                                onClick = { viewModel.refreshStreamer(streamer.secUid) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.more_options_label))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.deleteStreamer(streamer) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_label))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.more_options_label),
                                    modifier = Modifier.draggableHandle(onDragStopped = {
                                        viewModel.updateStreamerOrder(displayedStreamerForLive)
                                    })
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        when (val state = addLiveStreamerUiState) {
            is AddLiveStreamerUiState.Loading -> {
                // Now handled by the button's state
            }

            is AddLiveStreamerUiState.Success -> {
                LaunchedEffect(state) {
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    streamerUrl = "" // Clear input field
                    if (streamersFromDb.isNotEmpty()) {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(index = streamersFromDb.lastIndex)
                        }
                    }
                    viewModel.resetAddStreamerState()
                }
            }

            is AddLiveStreamerUiState.Error -> {
                LaunchedEffect(state) {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetAddStreamerState()
                    LogUtils.e("$errorMessagePrefix ${state.message}")
                }
            }

            is AddLiveStreamerUiState.Initial -> { /* Do nothing */
            }
        }

    }
}
