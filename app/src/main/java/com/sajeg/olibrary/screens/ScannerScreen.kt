package com.sajeg.olibrary.screens

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.sajeg.olibrary.Book
import com.sajeg.olibrary.Details
import com.sajeg.olibrary.MainActivity
import com.sajeg.olibrary.R
import com.sajeg.olibrary.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

var scannedCode = false
var lastProcessedTime = 0L
const val cooldownPeriod = 10000

@Composable
fun ScannerScreen(context: Context, navController: NavController) {
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS
            )
        }
    }
    CameraPreview(controller = controller, Modifier.fillMaxSize())
    if (!scannedCode) {
        scanQRCodes(context, controller, navController)
    }
}


fun scanQRCodes(
    context: Context,
    cameraController: CameraController,
    navController: NavController
) {
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
            Toast.makeText(context, "Searching book...", Toast.LENGTH_LONG).show()
            val qrCode = result.getValue(scanner)!![0]
            val ean: String = qrCode.rawValue.toString()
            CoroutineScope(Dispatchers.IO).launch {
                val id = withContext(Dispatchers.IO) {
                    getBookId(ean).toInt()
                }

                if (id != -2 && id != -1) {
                    openBook(id, context = context, navController = navController)
                } else if (id == -2) {
                    withContext(Dispatchers.Main) {
//                        sendNotification("Book is not in the library", context)
                        Toast.makeText(context, "Book not in library", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
//                        sendNotification("An error occurred", context)
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT)
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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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

//private fun sendNotification(status: String, context: Context): Int {
//    val intent = createNotificationIntent(context, "QRCode")
//    val builder = NotificationCompat.Builder(context, "TEST_BACKGROUND")
//        .setSmallIcon(R.drawable.qrcode)
//        .setContentTitle("Scan Result")
//        .setContentText(status)
//        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        .setContentIntent(
//            intent
//        )
//    val id = Random.nextInt()
//    with(NotificationManagerCompat.from(context)) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return@with
//        }
//        notify(id, builder.build())
//    }
//    return id
//}

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

private suspend fun openBook(
    id: Int,
    notificationId: Int = 0,
    context: Context,
    navController: NavController
) {
    var book: Book? = null
    CoroutineScope(Dispatchers.IO).launch {
        book = db.bookDao().getById(id)
    }.join()
    val intent = createNotificationIntent(context, "HomeScreen")
    if (book != null) {
        val builder = NotificationCompat.Builder(context, "SCAN_RESULT")
            .setSmallIcon(R.drawable.qrcode)
            .setContentTitle("Scan Result")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Found the Book ${book!!.title} ${book!!.getAuthorFormated()}")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                intent
            )
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(id, builder.build())
        }
        withContext(Dispatchers.Main) {
            navController.navigate(Details(book!!.rowid!!, book!!.title))
        }
    }
}

fun createNotificationIntent(context: Context, route: String): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("navigation_route", route)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    return PendingIntent.getActivity(
        context,
        route.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}