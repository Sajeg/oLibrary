package com.sajeg.olibrary

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.os.Handler
import android.os.Looper

class UpdateDbBackground : JobService() {
    private val mHandler = Handler(Looper.getMainLooper())
    override fun onStartJob(params: JobParameters?): Boolean {
        DatabaseBookManager.startDBDownload(applicationContext, true)

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
            .setPeriodic(2 * 24 * 60 * 60 * 1000) // 2 days in milliseconds
            .build()

        jobScheduler.schedule(jobInfo)
    }
}