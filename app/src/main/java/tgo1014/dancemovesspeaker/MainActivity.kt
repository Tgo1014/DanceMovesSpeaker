package tgo1014.dancemovesspeaker

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tgo1014.dancemovesspeaker.ui.theme.DanceMovesSpeakerTheme
import java.util.Locale


class MainActivity : ComponentActivity() {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) {
            tts.setLanguage(Locale("pt", "BR"))
        }
        enableEdgeToEdge()
        setContent {
            DanceMovesSpeakerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenContent(tts)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScreenContent(tts: TextToSpeech) {
    val context = LocalContext.current
    val viewModel = remember { MainViewModel(context) }
    val state by viewModel.state.collectAsState()
    val movesList = state.movesList
    val bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    BottomSheetScaffold(
        modifier = Modifier
            .padding(WindowInsets.ime.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues()),
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = "Gafieira") },
                actions = {
                    IconButton(onClick = viewModel::share) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "")
                    }
                }
            )
        },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(bottom = 8.dp)
            ) {
                var text by remember { mutableStateOf(state.movesList.joinToString()) }
                var qtd by remember { mutableStateOf(state.movesToPick.toString()) }
                var delay by remember { mutableStateOf(state.delayBetweenVoiceInSecs.toString()) }
                Button(
                    onClick = { viewModel.onTalkingPressed() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (state.isTalking) Icons.Default.Clear else Icons.Default.PlayArrow,
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = if (state.isTalking) "PARAR" else "FALAR")
                }
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Lista de Passos (separados por ,)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isTalking
                )
                Row(Modifier.padding(top = 4.dp)) {
                    val pattern = remember { Regex("^\\d+\$") }
                    TextField(
                        value = qtd,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(pattern)) {
                                qtd = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Qtd Movimentos") },
                        enabled = !state.isTalking,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    TextField(
                        value = delay,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(pattern)) {
                                delay = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Delay Entre Movimentos") },
                        enabled = !state.isTalking,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = { viewModel.onUpdateSettings(text, qtd, delay) },
                    enabled = !state.isTalking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "ATUALIZAR CONFIGURACOES")
                }
            }
        },
        content = {
            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val split = state.textToSpeak.split(", ")
                movesList.forEach { text ->
                    FilterChip(
                        selected = text in split,
                        onClick = { /*TODO*/ },
                        label = { Text(text = text) }
                    )
                }
            }
        }
    )

    LaunchedEffect(state.textToSpeak) {
        if (state.movesList.isEmpty()) {
            return@LaunchedEffect
        }
        tts.speak(
            /* text = */ state.textToSpeak,
            /* queueMode = */ TextToSpeech.QUEUE_ADD,
            /* params = */ null,
            /* utteranceId = */ null
        )
    }

    LaunchedEffect(state.isTalking) {
        if (!state.isTalking) {
            tts.stop()
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val c = LocalContext.current
    DanceMovesSpeakerTheme {
        // Greeting(TextToSpeech(c))
    }
}