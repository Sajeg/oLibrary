package com.sajeg.olibrary.database

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sajeg.olibrary.R
import kotlin.random.Random

class UpdateDbBackground : JobService() {
    private val mHandler = Handler(Looper.getMainLooper())
    override fun onStartJob(params: JobParameters?): Boolean {
        DatabaseBookManager.startDBDownload(applicationContext, true)
        scheduleJob()
        val builder = NotificationCompat.Builder(this, "TEST_BACKGROUND")
            .setSmallIcon(R.drawable.qrcode)
            .setContentTitle("Job ran")
            .setContentText("This is just to let you know, tha the job just ran")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@UpdateDbBackground,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@with
            }
            notify(Random.nextInt(), builder.build())
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        mHandler.removeCallbacksAndMessages(null)
        return true
    }

    private fun scheduleJob() {
        // Example of scheduling the job to run every 2 days
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(1, ComponentName(this, UpdateDbBackground::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Example network constraint
            .setPeriodic(30 * 60 * 1000) // 2 days in milliseconds, Every 30 minutes for debug purpose
            .build()

        jobScheduler.schedule(jobInfo)
    }
}