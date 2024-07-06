package com.sajeg.olibrary.database

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sajeg.olibrary.R
import kotlin.random.Random

class UpdateDbBackground : JobService() {
    private val TAG = "UpdateDbBackground"
    private val mHandler = Handler(Looper.getMainLooper())

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")
        mHandler.post {
            DatabaseBookManager.startDBDownload(applicationContext, true)
            showNotification()
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopped")
        mHandler.removeCallbacksAndMessages(null)
        return true
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, "TEST_BACKGROUND")
            .setSmallIcon(R.drawable.qrcode)
            .setContentTitle("Job ran")
            .setContentText("This is just to let you know that the job just ran")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ContextCompat.checkSelfPermission(
                    this@UpdateDbBackground,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notify(Random.nextInt(), builder.build())
            } else {
                Log.e(TAG, "Notification permission not granted")
            }
        }
    }

    companion object {
        fun scheduleJob(context: Context) {
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName = ComponentName(context, UpdateDbBackground::class.java)

            val jobInfo = JobInfo.Builder(1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(2 * 24 * 60 * 1000)
                .setPersisted(true)
                .build()

            val resultCode = jobScheduler.schedule(jobInfo)
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("UpdateDbBackground", "Job scheduled successfully")
            } else {
                Log.e("UpdateDbBackground", "Job scheduling failed")
            }
        }
    }
}