package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DiagnosisKeyRetrievalPeriodicWorker test.
 */
@RunWith(AndroidJUnit4::class)
class DiagnosisKeyRetrievalPeriodicWorkerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var request: WorkRequest
    private lateinit var request2: WorkRequest
    // small delay because WorkManager does not run work instantly when delay is off
    private val delay = 500L

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        mockkObject(BackgroundWorkScheduler)
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        every { BackgroundWorkScheduler["buildDiagnosisKeyRetrievalPeriodicWork"]() } answers {
            request = this.callOriginal() as WorkRequest
            request
        }
    }

    /**
     * Test worker for success, retries and fail.
     */
    @Test
    fun testDiagnosisKeyRetrievalPeriodicWorker() {
        // Begin Test worker for success run.
        every { BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork() } just Runs

        BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()

        assertThat(request, notNullValue())

        var workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo, notNullValue())
        assertThat(workInfo.state, `is`((WorkInfo.State.ENQUEUED)))

        runPeriodicJobInitialDelayMet()
        assertThat(request, notNullValue())
        workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.runAttemptCount, `is`(0))

        runPeriodicJobPeriodDelayMet()
        assertThat(request, notNullValue())
        workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.runAttemptCount, `is`(0))

        // End Test worker for success run.

        // Begin test worker for retries and fails.

        every { BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork() } throws Exception("test exception")

        BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()

        assertThat(request, notNullValue())
        workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo, notNullValue())
        assertThat(workInfo.state, `is`((WorkInfo.State.ENQUEUED)))

        for (i in 1..BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 2) {
            // run job i times
            when (i) {
                BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 2 -> {
                    every { BackgroundWorkScheduler["buildDiagnosisKeyRetrievalPeriodicWork"]() } answers {
                        request2 = this.callOriginal() as WorkRequest
                        request2
                    }
                    runPeriodicJobInitialDelayMet()
                }
                else -> {
                    runPeriodicJobInitialDelayMet()
                }
            }

            // get job run #i result
            when (i) {
                BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 2 -> {
                    assertThat(request, notNullValue())
                    assertThat(request2, notNullValue())
                    workInfo = workManager.getWorkInfoById(request.id).get()
                    val workInfo2 = workManager.getWorkInfoById(request2.id).get()
                    assertThat(workInfo, nullValue())
                    assertThat(workInfo2.state, `is`(WorkInfo.State.ENQUEUED))
                    assertThat(workInfo2.runAttemptCount, `is`(0))
                }
                else -> {
                    assertThat(request, notNullValue())
                    workInfo = workManager.getWorkInfoById(request!!.id).get()
                    assertThat(workInfo.runAttemptCount, `is`(i))
                }
            }
        }
        // End test worker for retries and fails.
    }

    @After
    fun cleanUp() {
        workManager.cancelAllWork().result.get()
        unmockkAll()
    }

    private fun runPeriodicJobInitialDelayMet() {
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        testDriver?.setAllConstraintsMet(request.id)
        testDriver?.setInitialDelayMet(request.id)
        Thread.sleep(delay)
    }

    private fun runPeriodicJobPeriodDelayMet() {
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        testDriver?.setAllConstraintsMet(request.id)
        testDriver?.setPeriodDelayMet(request.id)
        Thread.sleep(delay)
    }
}
