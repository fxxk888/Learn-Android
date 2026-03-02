package com.fuck.learn.ui.activity.fans.club.info

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.commit451.coiltransformations.BlurTransformation
import com.fuck.learn.R
import com.fuck.learn.data.db.HistoryForFansClub
import com.fuck.learn.ui.theme.DouyinToolTheme
import com.fuck.learn.ui.theme.ThemeManager
import com.fuck.learn.utils.LogUtils
import com.fuck.learn.utils.NumberUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class QueryFansClubInfoActivity : ComponentActivity() {

    private val viewModel: QueryFansClubInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val fansClubUiState by viewModel.fansClubUiState.collectAsState()
            val groups by viewModel.groups.collectAsState()
            val selectedGroupIds by viewModel.selectedGroupIds.collectAsState()
            val isAllSelected by viewModel.isAllSelected.collectAsState()

            var currentTime by remember { mutableStateOf("") }
            var filterMenuExpanded by remember { mutableStateOf(false) }
            val lazyListState = rememberLazyListState()

            val doneLabel = stringResource(R.string.done_label)
            val errorLabel = stringResource(R.string.error)

            LaunchedEffect(true) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                while (true) {
                    currentTime = sdf.format(Date())
                    delay(1000)
                }
            }

            DouyinToolTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
                        Column {
                            TopAppBar(
                                title = { Text(stringResource(R.string.fans_club_label)) },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.content_description_back)
                                        )
                                    }
                                },

                                actions = {
                                    IconButton(onClick = {
                                        context.startActivity(
                                            Intent(
                                                context, AddLiveStreamerActivity::class.java
                                            )
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = stringResource(R.string.add_live_steamer_label)
                                        )
                                    }

                                    Box {
                                        IconButton(onClick = { filterMenuExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = stringResource(R.string.filter_groups_label)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = filterMenuExpanded,
                                            onDismissRequest = { filterMenuExpanded = false }) {
                                            DropdownMenuItem(text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        stringResource(R.string.all_groups_label),
                                                        Modifier.weight(1f)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Checkbox(
                                                        checked = isAllSelected,
                                                        onCheckedChange = null
                                                    )
                                                }
                                            }, onClick = { viewModel.selectAllGroups() })

                                            groups.forEach { group ->
                                                DropdownMenuItem(
                                                    text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(group.name, Modifier.weight(1f))
                                                        Spacer(Modifier.width(8.dp))
                                                        Checkbox(
                                                            checked = selectedGroupIds.contains(
                                                                group.id
                                                            ), onCheckedChange = null
                                                        )
                                                    }
                                                },
                                                    onClick = { viewModel.toggleGroupSelection(group.id) })
                                            }
                                        }
                                    }
                                })

                            HorizontalDivider()

                            Text(
                                text = currentTime,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            HorizontalDivider()
                        }
                    }) { innerPadding ->
                    FansClubInfoScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        lazyListState = lazyListState
                    )
                }
            }

            when (val state = fansClubUiState) {
                is FansClubUiState.Loading -> {
                    // Now handled by the button's state
                }

                is FansClubUiState.Success -> {
                    LaunchedEffect(state) {
                        Toast.makeText(
                            context,
                            doneLabel,
                            Toast.LENGTH_LONG
                        ).show()
                        lazyListState.animateScrollToItem(3)
                    }
                }

                is FansClubUiState.Error -> {
                    LaunchedEffect(state) {
                        Toast.makeText(
                            context, state.message, Toast.LENGTH_LONG
                        ).show()
                        LogUtils.e("$errorLabel ${state.message}")
                    }
                }

                is FansClubUiState.Initial -> { /* Do nothing */
                }
            }
        }

        intent.getStringExtra("USER_PROFILE_URL")?.let {
            viewModel.onSecUidChange(it)
            if (intent.getBooleanExtra("AUTO_QUERY", false)) {
                viewModel.executeQuery()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopQuery()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FansClubInfoScreen(
    viewModel: QueryFansClubInfoViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val secUid by viewModel.secUid.collectAsStateWithLifecycle()
    val uiFansClubItems by viewModel.uiFansClubItems.collectAsStateWithLifecycle()
    val uiUserInfoItems by viewModel.uiUserInfoItems.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val isQuerying by viewModel.isQuerying.collectAsStateWithLifecycle()

    FansClubInfoContent(
        modifier = modifier,
        secUid = secUid,
        uiFansClubItems = uiFansClubItems,
        uiUserInfoItems = uiUserInfoItems,
        history = history,
        isQuerying = isQuerying,
        onSecUidChange = viewModel::onSecUidChange,
        onExecuteQuery = viewModel::executeQuery,
        onStopQuery = viewModel::stopQuery,
        onDeleteHistory = viewModel::onDeleteHistory,
        lazyListState = lazyListState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FansClubInfoContent(
    secUid: String,
    uiFansClubItems: List<UiFansClubItem>,
    uiUserInfoItems: UiUserInfoItem,
    history: List<HistoryForFansClub>,
    isQuerying: Boolean,
    onSecUidChange: (String) -> Unit,
    onExecuteQuery: () -> Unit,
    onStopQuery: () -> Unit,
    onDeleteHistory: (HistoryForFansClub) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(secUid)) }

    var sortState by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }

    val sortedFansClubItems = remember(uiFansClubItems, sortState) {
        uiFansClubItems.sortedByDescending {
            it.level?.toIntOrNull() ?: -1
        }
    }

    LaunchedEffect(secUid) {
        if (textFieldValue.text != secUid) {
            textFieldValue = TextFieldValue(secUid, TextRange(secUid.length))
        }
    }
    LazyColumn(
        modifier = modifier.padding(16.dp, 8.dp, 16.dp, 0.dp), state = lazyListState
    ) {
        item {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        onSecUidChange(it.text)
                    },
                    label = { Text(stringResource(R.string.add_live_steamer_input_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onExecuteQuery()
                        keyboardController?.hide()
                        expanded = false
                    })
                )
                ExposedDropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false }) {
                    history.forEach { item ->
                        DropdownMenuItem(text = { Text(item.nickname) }, onClick = {
                            onSecUidChange(item.url)
                            expanded = false
                        }, trailingIcon = {
                            IconButton(onClick = { onDeleteHistory(item) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_history),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        })
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (isQuerying) {
                        onStopQuery()
                    } else {
                        onExecuteQuery()
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
//                    .wrapContentWidth(Alignment.Start)
//                    .wrapContentWidth(Alignment.End)
            ) {
                Text(stringResource(if (isQuerying) R.string.user_info_stop_query else R.string.user_info_query))
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!uiUserInfoItems.nickname.isNullOrBlank()) {
            stickyHeader {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    UserInfoItem(uiUserInfoItems)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(sortedFansClubItems) { item ->
                FansClubItemRow(item = item)
            }

        }
    }
}

@Composable
fun UserInfoItem(item: UiUserInfoItem) {
    var showBlur by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (item.nickname != null) {
        Card(
            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box {
                val imageLoader = ImageLoader.Builder(context).components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }.build()

                AsyncImage(
                    model = item.skinBgUrl ?: R.drawable.default_bg,
                    imageLoader = imageLoader,
                    contentDescription = stringResource(R.string.background_image_desc),
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        val imageRequest = remember(item.avatarUrl, showBlur) {
                            ImageRequest.Builder(context).data(item.avatarUrl).apply {
                                if (showBlur) {
                                    transformations(
                                        BlurTransformation(
                                            context = context, radius = 25f, sampling = 1.1f
                                        )
                                    )
                                }
                            }.build()
                        }

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = stringResource(R.string.avatar_image_desc),
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { showBlur = !showBlur },
                            contentScale = ContentScale.Crop
                        )

                        item.skinUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = stringResource(R.string.skin_image_desc),
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.basicMarquee(1)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.id_prefix,
                                    item.account ?: "N/FansClubInfoBean"
                                ), fontSize = 14.sp, color = Color.White
                            )
                            item.ip?.let {

                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.level_prefix, item.level ?: "-"),
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "\uD83D\uDC8E: ${NumberUtils.withCommas(item.consumeMin)} - ${
                                NumberUtils.withCommas(
                                    item.consumeMax
                                )
                            }", fontSize = 14.sp, color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun FansClubItemRow(item: UiFansClubItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp), colors = CardDefaults.cardColors(
            containerColor = if (ThemeManager.getThemeMode(true)) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
                .height(28.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.nickname ?: "N/FansClubInfoBean",
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(1),
                maxLines = 1,
            )

            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {

                AsyncImage(
                    model = item.levelUrl,
                    contentDescription = stringResource(R.string.level_image_desc),
                    contentScale = ContentScale.Fit
                )

                if (0 != item.state) {
                    item.level?.let {
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = when (item.state) {
                                1 -> Color.White
                                2 -> Color.Black.copy(0.7f)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            AsyncImage(
                modifier = Modifier.width(28.dp),
                model = item.vipUrl,
                contentDescription = stringResource(R.string.vip_image_desc),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
