package kim.yeonghoon.kospialarm.util

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import kim.yeonghoon.kospialarm.worker.KospiCheckWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * WorkManagerHelper 단위 테스트.
 *
 * WorkManagerTestInitHelper를 사용한 WorkManager 스케줄링을 테스트합니다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WorkManagerHelperTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()

        // 테스트용 WorkManager 초기화
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun `given context, when startPeriodicKospiCheck is called, then should enqueue periodic work`() {
        // 실행
        WorkManagerHelper.startPeriodicKospiCheck(context)

        // 검증
        val workInfos = workManager.getWorkInfosForUniqueWork(KospiCheckWorker.WORK_NAME).get()
        assertEquals(1, workInfos.size)

        val workInfo = workInfos[0]
        assertTrue(
            workInfo.state == WorkInfo.State.ENQUEUED ||
            workInfo.state == WorkInfo.State.RUNNING
        )
    }

    @Test
    fun `given work is running, when stopPeriodicKospiCheck is called, then should cancel work`() {
        // 준비
        WorkManagerHelper.startPeriodicKospiCheck(context)

        // 실행
        WorkManagerHelper.stopPeriodicKospiCheck(context)

        // 검증
        val workInfos = workManager.getWorkInfosForUniqueWork(KospiCheckWorker.WORK_NAME).get()
        assertTrue(workInfos.isEmpty() || workInfos.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun `given work already exists, when startPeriodicKospiCheck is called again, then should keep existing work`() {
        // 준비
        WorkManagerHelper.startPeriodicKospiCheck(context)
        val firstWorkInfos = workManager.getWorkInfosForUniqueWork(KospiCheckWorker.WORK_NAME).get()
        val firstWorkId = firstWorkInfos[0].id

        // 실행
        WorkManagerHelper.startPeriodicKospiCheck(context)

        // 검증
        val secondWorkInfos = workManager.getWorkInfosForUniqueWork(KospiCheckWorker.WORK_NAME).get()
        assertEquals(1, secondWorkInfos.size)
        assertEquals(firstWorkId, secondWorkInfos[0].id)
    }

    @Test
    fun `given no work exists, when stopPeriodicKospiCheck is called, then should not throw exception`() {
        // 실행/검증 - 예외가 발생하지 않아야 함
        WorkManagerHelper.stopPeriodicKospiCheck(context)
    }
}
