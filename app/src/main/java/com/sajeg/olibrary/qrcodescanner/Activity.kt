package com.sajeg.olibrary.qrcodescanner

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.sajeg.olibrary.Book
import com.sajeg.olibrary.MainActivity
import com.sajeg.olibrary.R
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.details.BookInfo
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class Activity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private var scannedCode = false
    private var lastProcessedTime = 0L
    private val cooldownPeriod = 10000
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                //Nothing
            } else {
                switchToMain()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "library"
        ).build()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            Log.d("Permission", "Already granted")
        }
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = false,
                            onClick = { switchToMain() },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.home),
                                    contentDescription = ""
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = true,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.qrcode),
                                    contentDescription = ""
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.person),
                                    contentDescription = ""
                                )
                            }
                        )
                    }
                }) { innerPadding ->
                    var modifier = Modifier.padding(innerPadding)
                    val controller = remember {
                        LifecycleCameraController(this).apply {
                            setEnabledUseCases(
                                CameraController.IMAGE_ANALYSIS
                            )
                        }
                    }
                    CameraPreview(controller = controller, Modifier.fillMaxSize())
                    if (!scannedCode) {
                        scanQRCodes(this, controller)
                    }
                }
            }
        }
    }

    private fun scanQRCodes(context: Context, cameraController: CameraController) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
            .build()
        val scanner = BarcodeScanning.getClient(options)
        if (scannedCode) {
            return
        }
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            MlKitAnalyzer(
                listOf(scanner),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(context)
            ) { result: MlKitAnalyzer.Result? ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessedTime < cooldownPeriod) {
                    return@MlKitAnalyzer
                }
                if (result == null) {
                    return@MlKitAnalyzer
                }
                if (result.getValue(scanner) == null) {
                    return@MlKitAnalyzer
                }
                if (result.getValue(scanner)!!.size == 0) {
                    return@MlKitAnalyzer
                }
                scannedCode = true
                lastProcessedTime = currentTime
                Toast.makeText(this@Activity, "Searching book...", Toast.LENGTH_LONG).show()
                val qrCode = result.getValue(scanner)!![0]
                val ean: String = qrCode.rawValue.toString()
                lifecycleScope.launch {
                    val id = withContext(Dispatchers.IO) {
                        getBookId(ean).toInt()
                    }

                    if (id != -2 && id != -1) {
                        openBook(id)
                    } else if (id == -2) {
                        withContext(Dispatchers.Main) {
                            sendNotification("Book is not in the library")
                            Toast.makeText(this@Activity, "Book not in library", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            sendNotification("An error occurred")
                            Toast.makeText(this@Activity, "An error occurred", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun CameraPreview(
        controller: LifecycleCameraController,
        modifier: Modifier
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = modifier
        )
    }

    private fun sendNotification(status: String): Int {
        val intent = Intent(this, Activity::class.java)
        val builder = NotificationCompat.Builder(this, "TEST_BACKGROUND")
            .setSmallIcon(R.drawable.qrcode)
            .setContentTitle("Scan Result")
            .setContentText(status)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE))
        val id = Random.nextInt()
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@Activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(id, builder.build())
        }
        return id
    }

    private suspend fun getBookId(ean: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val websiteUrl = URL(
                    "https://www.stadtbibliothek.oldenburg.de/olsuchergebnisse?p_r_p_arena_urn%3Aarena_search_query=${
                        ean.replace(
                            " ",
                            "+"
                        )
                    }&p_r_p_arena_urn%3Aarena_search_type=solr\n"
                )
                val connection = websiteUrl.openConnection() as HttpURLConnection
                val inputStream = connection.inputStream

                val doc = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
                val id = doc.select("span.arena-record-id").firstOrNull()
                if (id != null) {
                    return@withContext id.text()
                } else {
                    return@withContext "-2"
                }
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
                return@withContext "-1"
            }
        }
    }

    private suspend fun openBook(id: Int, notificationId: Int = 0) {
        var book: Book? = null
        CoroutineScope(Dispatchers.IO).launch {
            book = db.bookDao().getById(id)
        }.join()
        if (book != null) {
            val intent = Intent(this, BookInfo::class.java).apply {
                putExtra("recordId", book!!.recordId)
                putExtra("title", book!!.title)
                putExtra("author", book!!.author)
                putExtra("year", book!!.year)
                putExtra("language", book!!.language)
                putExtra("genre", book!!.genre)
                putExtra("series", book!!.series)
                putExtra("imageLink", book!!.imgUrl)
                putExtra("url", book!!.url)
                putExtra("notificationId", notificationId)
            }
            val builder = NotificationCompat.Builder(this, "TEST_BACKGROUND")
                .setSmallIcon(R.drawable.qrcode)
                .setContentTitle("Scan Result")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Found the Book ${book!!.title} ${book!!.getAuthorFormated()}"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE))
            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(
                        this@Activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@with
                }
                notify(id, builder.build())
            }
            startActivity(intent)
        }
    }

    private fun switchToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
