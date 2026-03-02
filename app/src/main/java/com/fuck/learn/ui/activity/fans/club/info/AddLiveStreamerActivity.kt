package com.fuck.learn.ui.activity.fans.club.info

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.fuck.learn.data.db.StreamerForFansClub
import com.fuck.learn.ui.theme.DouyinToolTheme
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class AddLiveStreamerActivity : ComponentActivity() {

    private val viewModel: AddLiveStreamerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DouyinToolTheme {
                var menuExpanded by remember { mutableStateOf(false) }
                var showAddGroupDialog by remember { mutableStateOf(false) }
                var showRenameGroupDialog by remember { mutableStateOf(false) }
                var showDeleteGroupDialog by remember { mutableStateOf(false) }
                var deleteTargetGroupId by remember { mutableLongStateOf(0L) }
                var renameTargetGroupId by remember { mutableLongStateOf(0L) }
                var renameTargetGroupName by remember { mutableStateOf("") }
                val groups by viewModel.groups.collectAsState()
                val selectedGroupId by viewModel.selectedGroupId.collectAsState()
                val context = LocalContext.current
                val groupNameExistsError = stringResource(R.string.group_name_exists_error)
                val appName = stringResource(R.string.app_name)

                LaunchedEffect(Unit) {
                    if (viewModel.shouldShowInitialGroupDialog()) {
                        showAddGroupDialog = true
                    }
                }

                LaunchedEffect(groups) {
                    if (groups.isNotEmpty() && selectedGroupId == 1L) {
                        viewModel.selectGroup(groups.first().id)
                    }
                }

                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
                    onResult = { uri ->
                        uri?.let { viewModel.exportDatabase(it) }
                    })

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(), onResult = { uri ->
                        uri?.let { viewModel.importDatabase(it) }
                    })

                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
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
                                },
                                actions = {
                                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = stringResource(R.string.import_label)
                                        )
                                    }
                                    IconButton(onClick = {
                                        val timestamp = SimpleDateFormat(
                                            "yyyy_MM_dd_HH_mm_ss",
                                            Locale.getDefault()
                                        ).format(Date())
                                        exportLauncher.launch("${appName}_backup_$timestamp.db")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = stringResource(R.string.export_label)
                                        )
                                    }
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.more_options_label)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }) {
                                        groups.forEach { group ->
                                            val isSelected = group.id == selectedGroupId
                                            DropdownMenuItem(
                                                modifier = if (isSelected) Modifier.background(
                                                MaterialTheme.colorScheme.primaryContainer
                                            ) else Modifier,
                                                text = { Text(text = group.name) },
                                                trailingIcon = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = {
                                                                menuExpanded = false
                                                                renameTargetGroupId = group.id
                                                                renameTargetGroupName = group.name
                                                                showRenameGroupDialog = true
                                                            }, modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = stringResource(
                                                                    R.string.rename_label
                                                                ),
                                                                modifier = Modifier.size(18.dp),
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        IconButton(
                                                            onClick = {
                                                                menuExpanded = false
                                                                deleteTargetGroupId = group.id
                                                                showDeleteGroupDialog = true
                                                            }, modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = stringResource(
                                                                    R.string.delete_label
                                                                ),
                                                                modifier = Modifier.size(18.dp),
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.selectGroup(group.id)
                                                    menuExpanded = false
                                                })
                                        }
                                        if (groups.isNotEmpty()) {
                                            HorizontalDivider()
                                        }
                                        DropdownMenuItem(text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = stringResource(R.string.add_group_label),
                                                )
                                            }
                                        }, onClick = {
                                            menuExpanded = false
                                            showAddGroupDialog = true
                                        })
                                    }
                                })
                            HorizontalDivider()
                        }
                    }) { innerPadding ->
                    AddLiveStreamerScreen(
                        modifier = Modifier.padding(innerPadding), viewModel = viewModel
                    )
                }

                if (showAddGroupDialog) {
                    val scope = rememberCoroutineScope()
                    AddGroupDialog(
                        title = stringResource(R.string.new_group_title),
                        initialName = "",
                        cancelable = groups.isNotEmpty(),
                        onDismiss = { showAddGroupDialog = false },
                        onConfirm = { name ->
                            scope.launch {
                                if (viewModel.addGroup(name)) {
                                    showAddGroupDialog = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        groupNameExistsError,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }

                if (showRenameGroupDialog) {
                    val scope = rememberCoroutineScope()
                    AddGroupDialog(
                        title = stringResource(R.string.rename_group_title),
                        initialName = renameTargetGroupName,
                        onDismiss = { showRenameGroupDialog = false },
                        onConfirm = { name ->
                            scope.launch {
                                if (viewModel.renameGroup(renameTargetGroupId, name)) {
                                    showRenameGroupDialog = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        groupNameExistsError,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }

                if (showDeleteGroupDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteGroupDialog = false },
                        title = { Text(stringResource(R.string.delete_group_title)) },
                        text = { Text(stringResource(R.string.delete_group_confirmation)) },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteGroup(deleteTargetGroupId)
                                showDeleteGroupDialog = false
                                if (groups.size == 1) {
                                    showAddGroupDialog = true
                                }
                            }) {
                                Text(stringResource(R.string.delete_button))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteGroupDialog = false }) {
                                Text(stringResource(R.string.cancel_button))
                            }
                        })
                }
            }
        }
    }
}

@Composable
fun AddGroupDialog(
    title: String,
    initialName: String,
    cancelable: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var groupName by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = { if (cancelable) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text(stringResource(R.string.group_name_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(groupName) }, enabled = groupName.isNotBlank()
            ) {
                Text(stringResource(R.string.ok_button))
            }
        },
        dismissButton = if (cancelable) {
            {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        } else null)
}

@Composable
fun AddLiveStreamerScreen(
    modifier: Modifier = Modifier, viewModel: AddLiveStreamerViewModel
) {
    var streamerUrl by remember { mutableStateOf("") }
    val addLiveStreamerUiState by viewModel.addLiveStreamerUiState.collectAsState()
    val streamersFromDb by viewModel.streamers.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = addLiveStreamerUiState is AddLiveStreamerUiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val successMessage = stringResource(R.string.success_label)

    var displayedStreamerForFansClubs by remember {
        mutableStateOf<List<StreamerForFansClub>>(
            emptyList()
        )
    }
    LaunchedEffect(streamersFromDb) {
        displayedStreamerForFansClubs = streamersFromDb
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        displayedStreamerForFansClubs = displayedStreamerForFansClubs.toMutableList().apply {
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
            }, modifier = Modifier.padding(top = 8.dp), enabled = !isLoading

        ) {
            Text(if (isLoading) stringResource(R.string.adding_label) else stringResource(R.string.add_button))
        }

        Spacer(modifier = Modifier.size(8.dp))
        HorizontalDivider()

        LazyColumn(
            state = lazyListState
        ) {
            items(displayedStreamerForFansClubs, key = { it.secUid }) { streamer ->
                ReorderableItem(
                    state = reorderableState, key = streamer.secUid
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
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.more_options_label)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.deleteStreamer(streamer) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_label)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.more_options_label),
                                    modifier = Modifier.draggableHandle(onDragStopped = {
                                        viewModel.updateStreamerOrder(displayedStreamerForFansClubs)
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
            }

            is AddLiveStreamerUiState.Success -> {
                LaunchedEffect(state) {
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    streamerUrl = ""
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
                }
            }

            is AddLiveStreamerUiState.Initial -> {
            }
        }

    }
}
