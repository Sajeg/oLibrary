package com.sajeg.olibrary

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.ui.theme.OLibraryTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
lateinit var db: AppDatabase
var modifierPadding: Modifier = Modifier

class MainActivity : ComponentActivity() {
    private lateinit var downloadReceiver: DownloadReceiver
    private lateinit var navController: NavHostController
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        downloadReceiver = DownloadReceiver()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "library"
        ).build()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            Log.d("Permission", "Already granted")
        }
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    modifierPadding = Modifier.padding(innerPadding)
                    SetupNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        intent.getStringExtra("navigation_route")?.let { route ->
            Log.d("Intent", route)
            if (route == "qrcode") {
                navController.navigate(QRCode)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    @Composable
    fun BottomNavBar(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        NavigationBar {
            NavigationBarItem(
                selected = if (currentDestination != null) currentDestination.route == "com.sajeg.olibrary.HomeScreen" else false,
                onClick = { navController.navigate(HomeScreen) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = ""
                    )
                }
            )
            NavigationBarItem(
                selected = if (currentDestination != null) currentDestination.route == "com.sajeg.olibrary.QRCode" else false,
                onClick = { navController.navigate(QRCode) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.qrcode),
                        contentDescription = ""
                    )
                }
            )
            NavigationBarItem(
                selected = if (currentDestination != null) currentDestination.route == "com.sajeg.olibrary.Account" else false,
                onClick = { navController.navigate(Account) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = ""
                    )
                }
            )
        }
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                "TEST_BACKGROUND",
                "Background test",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                "SCAN_RESULT",
                "Book Scan",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}
