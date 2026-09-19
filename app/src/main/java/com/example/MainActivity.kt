package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BluetoothAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BluetoothControllerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: BluetoothControllerViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissionsMap ->
                        val allGranted = permissionsMap.values.all { it }
                        viewModel.setPermissionsGranted(allGranted)
                    }

                    // Check initial permissions
                    LaunchedEffect(Unit) {
                        val hasAll = requiredPermissions.all { perm ->
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                perm
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                        viewModel.setPermissionsGranted(hasAll)
                    }

                    BluetoothAppScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onRequestPermissions = {
                            permissionLauncher.launch(requiredPermissions)
                        }
                    )
                }
            }
        }
    }
}
