package com.example.sr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sr.ui.theme.SrTheme
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sr.Repository.WorkRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelChildren


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SrTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CreateScreen(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize())
                }
            }
        }
    }
}


@Composable
fun CreateScreen(modifier: Modifier = Modifier) {
    val repository = remember { WorkRepository() }
    val scope = rememberCoroutineScope()

    var taskCount by rememberSaveable { mutableIntStateOf(0) }
    var completed by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp)
    ) {
        Text(stringResource(R.string.count_tasks, taskCount))

        CreateSlider(
            selected = taskCount,
            onSelectedChange = { taskCount = it },
            enabled = !isRunning
        )

        if (isRunning) {
            Text(stringResource(R.string.count_completed_tasks, completed, taskCount))
        }

        CreateButtons(
            firstButtonEnabled = taskCount > 0 && !isRunning,

            onFirstClick = {
                val total = taskCount
                completed = 0
                isRunning = true

                scope.launch {
                    try {
                        repeat(total) { index ->
                            val taskNumber = index + 1

                            Log.d("WorkTasks", "Последовательно: задача $taskNumber запущена")
                            repository.doWork()
                            Log.d("WorkTasks", "Последовательно: задача $taskNumber завершена")

                            completed++
                        }
                    } finally {
                        isRunning = false
                    }
                }
            },

            onSecondClick = {
                repeat(taskCount) { index ->
                    val taskNumber = index + 1

                    scope.launch {
                        Log.d("WorkTasks", "Задача $taskNumber запущена")
                        repository.doWork()
                        Log.d("WorkTasks", "Задача $taskNumber завершена")
                    }
                }
            },
            onThirdClick = {
                scope.coroutineContext.cancelChildren()
            }
        )
    }
}

@Composable
fun CreateSlider(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Slider(
        value = selected.toFloat(),
        onValueChange = { onSelectedChange(it.roundToInt()) },
        valueRange = 0f..10f,
        steps = 9,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
fun CreateButtons(
    firstButtonEnabled: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
    onThirdClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        Button(onClick = onFirstClick, enabled = firstButtonEnabled) {
            Text(stringResource(R.string.сonsistently))
        }

        Button(onClick = onSecondClick) {
            Text(stringResource(R.string.in_parallel))
        }

        Button(onClick = onThirdClick) {
            Text(stringResource(R.string.сancellation_of_coroutines))
        }
    }
}

@Preview (showBackground = true)
@Composable
fun ShowScreen() {
    SrTheme() {
        CreateScreen(modifier = Modifier.fillMaxSize())
    }
}