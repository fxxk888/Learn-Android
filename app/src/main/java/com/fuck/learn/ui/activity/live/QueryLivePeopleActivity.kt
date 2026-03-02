package com.fuck.learn.ui.activity.live

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.fuck.learn.R
import com.fuck.learn.ui.theme.DouyinToolTheme
import com.fuck.learn.utils.LogUtils
import com.fuck.learn.utils.NumberUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
class QueryLivePeopleActivity : ComponentActivity() {
    private val viewModel: LivePeopleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val autoRefreshEnabled by viewModel.autoRefreshEnabled.collectAsState()
            val autoRefreshInterval by viewModel.autoRefreshInterval.collectAsState()
            var currentTime by remember { mutableStateOf("") }
            var menuExpanded by remember { mutableStateOf(false) }
            val livePeopleUiState by viewModel.livePeopleUiState.collectAsState()
            val liveRankItems by viewModel.liveRankItems.collectAsState()
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            LaunchedEffect(autoRefreshEnabled, autoRefreshInterval) {
                if (autoRefreshEnabled) {
                    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        while (true) {
                            if (livePeopleUiState == LivePeopleUiState.Success) {
                                viewModel.loadLivePeopleData()
                            }
                            delay(autoRefreshInterval * 1000L)
                        }
                    }
                }
            }

            LaunchedEffect(true) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                while (true) {
                    currentTime = sdf.format(Date())
                    delay(1000)
                }
            }

            DouyinToolTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    Column {
                        TopAppBar(
                            title = { Text(stringResource(R.string.live_people_label)) },
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
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.more_options_label)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }) {
                                        Row(
                                            modifier = Modifier.padding(
                                                start = 12.dp
                                            ), verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.auto_refresh_label),
                                                modifier = Modifier.weight(1f)
                                            )

                                            IconButton(onClick = { viewModel.incrementRefreshInterval() }) {
                                                Icon(Icons.Default.Add, stringResource(R.string.increase_interval_desc))
                                            }

                                            Text(
                                                text = "$autoRefreshInterval",
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.width(24.dp)
                                            )

                                            IconButton(onClick = { viewModel.decrementRefreshInterval() }) {
                                                Icon(Icons.Default.Remove, stringResource(R.string.decrease_interval_desc))
                                            }

                                            Checkbox(
                                                checked = autoRefreshEnabled, onCheckedChange = {
                                                    viewModel.setAutoRefreshEnabled(
                                                        it
                                                    )
                                                })
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
                }, floatingActionButton = {
                    FloatingActionButton(onClick = { viewModel.loadLivePeopleData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.more_options_label))
                    }
                }) { innerPadding ->
                    LivePeopleScreen(
                        modifier = Modifier.padding(innerPadding),
                        liveRankItems = liveRankItems.filter { it.message.isNullOrBlank() }
                    )
                }
            }

            when (val state = livePeopleUiState) {
                is LivePeopleUiState.Loading -> {
                    // Now handled by the button's state
                }

                is LivePeopleUiState.Success -> {

                }

                is LivePeopleUiState.Error -> {
                    LaunchedEffect(state) {
//                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            viewModel.loadLivePeopleData()
                        }
                        LogUtils.e("${context.getString(R.string.error)} ${state.message}")
                    }
                }

                is LivePeopleUiState.Initial -> { /* Do nothing */
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LivePeopleScreen(
    modifier: Modifier, liveRankItems: List<LiveRankItem>
) {
    LazyColumn {
        items(
            items = liveRankItems,
            contentType = { "liveRankItems" }
        ) { item ->
            val isInitial = remember { mutableStateOf(true) }
            val color = remember { Animatable(Color.Transparent) }
            val flashColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

            LaunchedEffect(item) {
                if (isInitial.value) {
                    isInitial.value = false
                    return@LaunchedEffect
                }
                color.animateTo(
                    flashColor,
                    animationSpec = tween(durationMillis = 300)
                )
                color.animateTo(Color.Transparent, animationSpec = tween(durationMillis = 500))
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color.value)
                    .padding(vertical = 8.dp,horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.streamer.avatarUrl,
                    contentDescription = stringResource(R.string.content_description_user_image),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.size(8.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = item.streamer.nickname,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(1)
                    )

                    if (item.prompts.isNullOrBlank()) {
                        item.formattedCreateTime?.let {
                            Spacer(modifier = Modifier.size(6.dp))

                            Text(
                                text = stringResource(R.string.start_at_prefix, it),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(1)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = item.prompts,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(1)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                if (item.prompts.isNullOrBlank()) {

                    Column(horizontalAlignment = Alignment.End) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.user_count_desc),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.size(4.dp))

                            item.userCount?.let {
                                Text(
                                    text = NumberUtils.withCommas(it),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (item.userCountDiff != 0) {
                                    val (icon, color) = if (item.userCountDiff > 0) {
                                        Pair(Icons.Default.KeyboardArrowUp, Color.Red)
                                    } else {
                                        Pair(Icons.Default.KeyboardArrowDown, Color.Green)
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.user_count_diff_desc),
                                        modifier = Modifier.size(16.dp),
                                        tint = color
                                    )

                                    Text(
                                        text = NumberUtils.withCommas(abs(item.userCountDiff)),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = color
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.size(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            item.visits?.let {
                                Text(
                                    text = stringResource(R.string.visits_prefix, NumberUtils.withCommas(it)),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (item.visitsDiff != 0) {
                                    val (icon, color) = if (item.visitsDiff > 0) {
                                        Pair(Icons.Default.KeyboardArrowUp, Color.Red)
                                    } else {
                                        Pair(Icons.Default.KeyboardArrowDown, Color.Green)
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.user_count_diff_desc),
                                        modifier = Modifier.size(16.dp),
                                        tint = color
                                    )

                                    Text(
                                        text = NumberUtils.withCommas(abs(item.visitsDiff)),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
        }
    }
}
