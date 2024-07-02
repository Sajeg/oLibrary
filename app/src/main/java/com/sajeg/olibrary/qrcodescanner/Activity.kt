package com.sajeg.olibrary.qrcodescanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
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

class Activity : ComponentActivity() {
    private lateinit var db: AppDatabase
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
                    scanQRCodes(this, controller)
                }
            }
        }
    }

    fun scanQRCodes(context: Context, cameraController: CameraController) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            MlKitAnalyzer(
                listOf(scanner),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(context)
            ) { result: MlKitAnalyzer.Result? ->
                if (result == null) {
                    return@MlKitAnalyzer
                }
                if (result.getValue(scanner) == null) {
                    return@MlKitAnalyzer
                }
                if (result.getValue(scanner)!!.size == 0) {
                    return@MlKitAnalyzer
                }
                val qrCode = result.getValue(scanner)!![0]
                val ean: String = qrCode.rawValue.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    openBook(getBookId(ean).toInt())
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
                    return@withContext "Book does not exist in Bibliothek"
                }
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
                return@withContext "ERROR Occurred"
            }
        }
    }

    private fun openBook(id: Int) {
        val book = db.bookDao().getById(id)
        if (book != null) {
            startActivity(Intent(this, BookInfo::class.java).apply {
                putExtra("recordId", book.recordId)
                putExtra("title", book.title)
                putExtra("author", book.author)
                putExtra("year", book.year)
                putExtra("language", book.language)
                putExtra("genre", book.genre)
                putExtra("series", book.series)
                putExtra("imageLink", book.imgUrl)
                putExtra("url", book.url)
            })
        }
    }

    private fun switchToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
