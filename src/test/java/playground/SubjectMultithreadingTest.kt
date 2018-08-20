package playground

import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


/**
 * https://artemzin.com/blog/rxjava-thread-safety-of-operators-and-subjects/
 */

class SubjectMultithreadingTest {

    @Test
    fun `break take(n)`() {
        val numberOfThreads = 10

        repeat(100000) {
            val publishSubject = PublishSubject.create<Int>() // Fix: .toSerialized().
            val actuallyReceived = AtomicInteger()

            publishSubject.take(3).subscribe { actuallyReceived.incrementAndGet() }

            val latch = CountDownLatch(numberOfThreads)
            var threads = listOf<Thread>()

            (0..numberOfThreads).forEach {
                threads += thread(start = false) {
                    publishSubject.onNext(it)
                    latch.countDown()
                }
            }

            threads.forEach { it.start() }

            latch.await()

            assertThat(actuallyReceived.get()).isEqualTo(3)
        }
    }
}