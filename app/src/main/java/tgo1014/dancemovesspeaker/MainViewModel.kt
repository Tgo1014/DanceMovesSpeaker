package tgo1014.dancemovesspeaker

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@SuppressLint("StaticFieldLeak")
class MainViewModel(
    private val context: Context
) : AndroidViewModel(context.applicationContext as Application) {

    private val defaultDelay = 25
    private val defaultMovesAmount = 3
    private var isTalking = false
    private val defaultStartList =
        "Gancho, Gancho Invertido, Gancho Redondo, Romário, Romário Invertido, Picadilho, S, Balança Corre Corre, Caidinha, Caidinha Por Fora, Peão, Contrá, Contrá Condutor Na Frente, Trança, Assalto, Sacada, Sacada Invertida, Vassourinha"
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        onUpdateSettings(defaultStartList)
    }

    fun onTalkingPressed() {
        isTalking = !isTalking
        _state.update { it.copy(isTalking = this.isTalking) }
        if (isTalking) {
            viewModelScope.launch {
                while (isTalking) {
                    val movesToSpeak = _state.value.movesList
                        .shuffled()
                        .take(_state.value.movesToPick)
                        .joinToString(", ")
                    _state.update { it.copy(textToSpeak = movesToSpeak) }
                    delay(_state.value.delayBetweenVoiceInSecs.seconds)
                }
            }
        }
    }

    fun onUpdateSettings(
        text: String,
        movesAmount: String = defaultMovesAmount.toString(),
        secs: String = defaultDelay.toString()
    ) {
        _state.update {
            it.copy(
                movesList = text.split(",").map { it.trim() },
                delayBetweenVoiceInSecs = secs.toIntOrNull() ?: defaultDelay,
                movesToPick = movesAmount.toIntOrNull() ?: defaultMovesAmount,
            )
        }
    }

    fun share() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("samba", _state.value.movesList.joinToString())
        clipboard.setPrimaryClip(clip)
    }

    data class State(
        val isTalking: Boolean = false,
        val movesList: List<String> = emptyList(),
        val textToSpeak: String = "",
        val movesToPick: Int = 3,
        val delayBetweenVoiceInSecs: Int = 30,
    )


}