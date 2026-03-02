package com.fuck.learn.ui.activity.live

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fuck.learn.R
import com.fuck.learn.api.RetrofitClient
import com.fuck.learn.data.db.AppDatabase
import com.fuck.learn.data.db.StreamerForLive
import com.fuck.learn.utils.DouyinParamUtils
import com.fuck.learn.utils.DouyinUrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddLiveStreamerViewModel(application: Application) : AndroidViewModel(application) {

    private val streamerDao = AppDatabase.getDatabase(application).streamerForLiveDao()

    val streamers: StateFlow<List<StreamerForLive>> =
        streamerDao.getAllStreamers().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _addLiveStreamerUiState =
        MutableStateFlow<AddLiveStreamerUiState>(AddLiveStreamerUiState.Initial)
    val addLiveStreamerUiState: StateFlow<AddLiveStreamerUiState> =
        _addLiveStreamerUiState.asStateFlow()

    fun fetchUserProfile(input: String) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            _addLiveStreamerUiState.value = AddLiveStreamerUiState.Loading
            try {
                val secUid = DouyinUrlUtils.getSecUid(input)
                if (secUid.isNullOrEmpty()) {
                    _addLiveStreamerUiState.value =
                        AddLiveStreamerUiState.Error(context.getString(R.string.add_live_steamer_input_tip))
                    return@launch
                }
                refreshStreamer(secUid)
            } catch (e: Exception) {
                _addLiveStreamerUiState.value =
                    AddLiveStreamerUiState.Error(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
            }
        }
    }

    fun refreshStreamer(secUid: String) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            _addLiveStreamerUiState.value = AddLiveStreamerUiState.Loading
            try {
                val maxOrder = streamers.value.maxOfOrNull { it.displayOrder } ?: 0
                val body =
                    RetrofitClient.apiService.getDouyinUserProfile(DouyinParamUtils.getCookie(),secUid).body()?.string() ?: ""

                val nicknameRegex = """\\"nickname\\":\\"(.*?)\\"""".toRegex()
                val nickname = nicknameRegex.find(body)?.groups?.get(1)?.value

                if (!nickname.isNullOrBlank()) {

                    val avatarRegex = """\\"avatar300Url\\":\\"(.*?)\\"""".toRegex()
                    val avatarUrl = avatarRegex.find(body)?.groups?.get(1)?.value

                    val uidRegex = """\\"user\\":\{\\"uid\\":\\"(.*?)\\"""".toRegex()
                    val uid = uidRegex.find(body)?.groups?.get(1)?.value

                    val body =
                        RetrofitClient.apiService.getLiveWebRidsInfo(uid ?: "")

                    if (body.data?.anchorWebRids?.get(uid).isNullOrBlank()) {
                        Toast.makeText(context, context.getString(R.string.add_failed_never_lived), Toast.LENGTH_SHORT).show()
                        _addLiveStreamerUiState.value = AddLiveStreamerUiState.Error(context.getString(R.string.add_failed_never_lived))
                    } else {
                        val streamerForLive = StreamerForLive(
                            uid = uid ?: "",
                            secUid = secUid,
                            nickname = nickname,
                            avatarUrl = avatarUrl?.replace("\u002F", "/") ?: "",
                            webRids = body.data.anchorWebRids[uid] ?: "",
                            displayOrder = maxOrder + 1
                        )

                        val rowId = streamerDao.insert(streamerForLive)
                        if (rowId > -1L) {
                            _addLiveStreamerUiState.value = AddLiveStreamerUiState.Success
                        } else {
                            _addLiveStreamerUiState.value = AddLiveStreamerUiState.Error(context.getString(R.string.add_failed))
                        }
                    }
                } else {
                    _addLiveStreamerUiState.value = AddLiveStreamerUiState.Error(context.getString(R.string.user_not_found))
                }
            } catch (e: Exception) {
                _addLiveStreamerUiState.value =
                    AddLiveStreamerUiState.Error(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
            }
        }
    }

    fun deleteStreamer(streamerForLive: StreamerForLive) {
        viewModelScope.launch {
            streamerDao.delete(streamerForLive)
        }
    }

    fun updateStreamerOrder(reorderedStreamerForLive: List<StreamerForLive>) {
        viewModelScope.launch {
            val updatedStreamers = reorderedStreamerForLive.mapIndexed { index, streamer ->
                streamer.copy(displayOrder = index)
            }
            streamerDao.update(updatedStreamers)
        }
    }

    fun resetAddStreamerState() {
        _addLiveStreamerUiState.value = AddLiveStreamerUiState.Initial
    }
}

sealed class AddLiveStreamerUiState {
    object Initial : AddLiveStreamerUiState()
    object Loading : AddLiveStreamerUiState()
    object Success : AddLiveStreamerUiState()
    data class Error(val message: String) : AddLiveStreamerUiState()
}
