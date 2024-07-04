package com.sajeg.olibrary

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
lateinit var db: AppDatabase
var modifierPadding: Modifier = Modifier

class MainActivity : ComponentActivity() {
    private lateinit var downloadReceiver: DownloadReceiver
    lateinit var navController: NavHostController
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
                        NavigationBar {
                            var selectedItem by remember { mutableStateOf("Home") }
                            NavigationBarItem(
                                selected = (selectedItem == "Home"),
                                onClick = { navController.navigate(HomeScreen); selectedItem = "Home" },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.home),
                                        contentDescription = ""
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = (selectedItem == "QR"),
                                onClick = { navController.navigate(QRCode); selectedItem = "QR" },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.qrcode),
                                        contentDescription = ""
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = (selectedItem == "Account"),
                                onClick = { navController.navigate(Account); selectedItem = "Account" },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.person),
                                        contentDescription = ""
                                    )
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    modifierPadding = Modifier.padding(innerPadding)
                    SetupNavGraph(navController = navController)
//                    MainCompose(Modifier.padding(innerPadding), BookSearchViewModel(db.bookDao()))
//                    val connectivityManager = getSystemService(ConnectivityManager::class.java)
//                    if (!connectivityManager.isActiveNetworkMetered) {
//                        CheckForUpdates()
//                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
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

    @Composable
    fun CheckForUpdates() {
        var needsUpdate by remember { mutableStateOf(false) }
        var installedVersion by remember { mutableStateOf("") }
        var newestVersion by remember { mutableStateOf("") }

        LaunchedEffect(key1 = Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                installedVersion = DatabaseBookManager.installedVersion(this@MainActivity)
            }.join()

            CoroutineScope(Dispatchers.IO).launch {
                newestVersion = DatabaseBookManager.newestVersion(this@MainActivity)
            }.join()

            if (newestVersion != installedVersion) {
                //Open Download Dialog
                needsUpdate = true
            }
        }
        if (needsUpdate) {
            DownloadDialog(context = this@MainActivity, installedVersion, onInput = {
                needsUpdate = false
            })
        }
    }

    @Composable
    fun DownloadDialog(context: Context, installedVersion: String, onInput: () -> Unit) {
        var firstDownload = installedVersion == ""

        AlertDialog(
            onDismissRequest = {
                if (firstDownload) {
                    val activity = (context as? Activity)
                    activity?.finish()
                }
            },
            confirmButton = {
                if (!firstDownload) {
                    TextButton(
                        onClick = {
                            onInput()
                        },
                        content = {
                            Text(text = "Later")
                        }
                    )
                }
                TextButton(
                    onClick = {
                        DatabaseBookManager.startDBDownload(context)
                        firstDownload = false
                        onInput()
                    },
                    content = {
                        Text(text = "Start Download")
                    }
                )
            },
            title = {
                if (firstDownload) {
                    Text(text = "Download Books")
                } else {
                    Text(text = "Update available")
                }
            },
            text = {
                if (firstDownload) {
                    Text(
                        text = "In order to use this App it requires an Download of about 120mb. " +
                                "You can use the App while it downloads the catalog."
                    )
                } else {
                    Text(
                        text = "Update the book catalog now to have the newest titles. " +
                                "You can use the App while it updates the catalog."
                    )
                }
            }
        )
    }
}
