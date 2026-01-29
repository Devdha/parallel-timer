package com.example.paralleltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paralleltimer.di.TimerViewModelFactory
import com.example.paralleltimer.ui.screen.HomeScreen
import com.example.paralleltimer.ui.theme.ParallelTimerTheme
import com.example.paralleltimer.ui.viewmodel.TimerListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ParallelTimerApp
        val factory = TimerViewModelFactory(app.container.timerRepository)

        setContent {
            ParallelTimerTheme {
                val viewModel: TimerListViewModel = viewModel(factory = factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onAddPreset = viewModel::addPreset,
                    onDeletePreset = viewModel::deletePreset
                )
            }
        }
    }
}
