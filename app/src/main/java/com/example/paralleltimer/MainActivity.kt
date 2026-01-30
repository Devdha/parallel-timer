package com.example.paralleltimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paralleltimer.di.TimerViewModelFactory
import com.example.paralleltimer.notification.NotificationHelper
import com.example.paralleltimer.ui.screen.HomeScreen
import com.example.paralleltimer.ui.theme.ParallelTimerTheme
import com.example.paralleltimer.ui.viewmodel.TimerListViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result - no action needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        val app = application as ParallelTimerApp
        val factory = TimerViewModelFactory(
            app.container.timerRepository,
            app.container.timerAlarmScheduler
        )

        setContent {
            ParallelTimerTheme {
                val viewModel: TimerListViewModel = viewModel(factory = factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onAddPreset = viewModel::addPreset,
                    onDeletePreset = viewModel::deletePreset,
                    onClearSnackbar = viewModel::clearSnackbar
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
