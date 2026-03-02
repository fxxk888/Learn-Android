package com.fuck.learn.ui.activity.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fuck.learn.R
import com.fuck.learn.api.RetrofitClient
import com.fuck.learn.data.db.AppDatabase
import com.fuck.learn.data.db.StreamerForLive
import com.fuck.learn.utils.DouyinParamUtils
import com.fuck.learn.utils.LogUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LiveRankItem(
    val streamer: StreamerForLive,
    val userCount: Int?,
    val createTime: Int?,
    val visits: Int?,
    val prompts: String?,
    val message: String?,
    val userCountDiff: Int = 0,
    val visitsDiff: Int = 0
) {
    val formattedCreateTime: String?
        get() = createTime?.let {
            val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(it.toLong() * 1000))
        }
}

class LivePeopleViewModel(application: Application) : AndroidViewModel(application) {

    private val streamerDao = AppDatabase.getDatabase(application).streamerForLiveDao()

    val streamers: StateFlow<List<StreamerForLive>> =
        streamerDao.getAllStreamers().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _autoRefreshEnabled = MutableStateFlow(false)
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()

    private val _autoRefreshInterval = MutableStateFlow(1)
    val autoRefreshInterval: StateFlow<Int> = _autoRefreshInterval.asStateFlow()

    private val _liveRankItems = MutableStateFlow<List<LiveRankItem>>(emptyList())
    val liveRankItems: StateFlow<List<LiveRankItem>> = _liveRankItems.asStateFlow()

    private val _livePeopleUiState = MutableStateFlow<LivePeopleUiState>(LivePeopleUiState.Initial)
    val livePeopleUiState: StateFlow<LivePeopleUiState> = _livePeopleUiState.asStateFlow()

    private var dataLoadingJob: Job? = null
    private val refreshTrigger = MutableStateFlow(0L)

    init {
        startObservingStreamers()
    }

    fun setAutoRefreshEnabled(enabled: Boolean) {
        _autoRefreshEnabled.value = enabled
    }

    fun incrementRefreshInterval() {
        _autoRefreshInterval.value++
    }

    fun decrementRefreshInterval() {
        if (_autoRefreshInterval.value > 1) {
            _autoRefreshInterval.value--
        }
    }

    private fun startObservingStreamers() {
        dataLoadingJob = viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            combine(
                streamers, refreshTrigger
            ) { list, trigger -> list to trigger }.collectLatest { (currentStreamers, _) ->
                _livePeopleUiState.value = LivePeopleUiState.Loading
                try {
                    if (currentStreamers.isEmpty()) {
                        _liveRankItems.value = emptyList()
                        _livePeopleUiState.value = LivePeopleUiState.Success
                        return@collectLatest
                    }

                    val previousItemsMap = _liveRankItems.value.associateBy { it.streamer.webRids }

                    val resultList = currentStreamers.map {
                        previousItemsMap[it.webRids] ?: LiveRankItem(
                            it, null, null, null, context.getString(R.string.wait), null
                        )
                    }.toMutableList()
                    _liveRankItems.value = resultList.toList()

                    val cookie = DouyinParamUtils.getCookie()

                    currentStreamers.forEachIndexed { index, streamer ->
                        var createTime: Int?
                        var message: String?
                        var visits: Int?
                        var userCount: Int?

                        val liveRoomIdBean = RetrofitClient.apiService.getLiveRoomIdInfo(
                            cookie = cookie, web_rid = streamer.webRids
                        )

                        val previousItem = previousItemsMap[streamer.webRids]

                        if (liveRoomIdBean.data?.roomStatus != 4 && liveRoomIdBean.data?.data?.getOrNull(
                                0
                            )?.status != 4 && !liveRoomIdBean.data?.data?.getOrNull(0)?.idStr.isNullOrBlank() && liveRoomIdBean.data.prompts.isNullOrBlank()
                        ) {
                            val livePeopleBean = RetrofitClient.apiService.getLiveInfo(
                                room_id = liveRoomIdBean.data.data.getOrNull(0)?.idStr ?: "",
                            )
                            userCount = livePeopleBean.data?.room?.userCount
                            createTime = livePeopleBean.data?.room?.createTime
                            visits = livePeopleBean.data?.room?.roomViewStats?.displayValue
                            message = null
                        } else {
                            userCount = null
                            createTime = null
                            visits = null
                            message = context.getString(R.string.not_started_label)
                        }

                        visits = visits?.let { current ->
                            previousItem?.visits?.let { prev ->
                                if (current - prev > 0) current
                                else prev
                            } ?: current
                        }
                        val userCountDiff =
                            userCount?.let { current -> previousItem?.userCount?.let { prev -> current - prev } }
                                ?: 0
                        val visitsDiff = visits?.let { current ->
                            previousItem?.visits?.let { prev ->
                                if (current - prev > 0) current - prev
                                else 0
                            }
                        } ?: 0

                        val newItem = LiveRankItem(
                            streamer = streamer,
                            userCount = userCount,
                            createTime = createTime,
                            visits = visits,
                            prompts = liveRoomIdBean.data?.prompts,
                            message = message,
                            userCountDiff = userCountDiff,
                            visitsDiff = visitsDiff
                        )

                        resultList[index] = newItem
                        _liveRankItems.value = resultList.toList()
                    }
                    _livePeopleUiState.value = LivePeopleUiState.Success
                } catch (e: Exception) {
                    _livePeopleUiState.value = LivePeopleUiState.Error(e.message.toString())
                    LogUtils.e("${e.message}")
                }
            }
        }
    }

    fun loadLivePeopleData() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    override fun onCleared() {
        super.onCleared()
        dataLoadingJob?.cancel()
    }
}

sealed class LivePeopleUiState {
    object Initial : LivePeopleUiState()
    object Loading : LivePeopleUiState()
    object Success : LivePeopleUiState()
    data class Error(val message: String) : LivePeopleUiState()
}
