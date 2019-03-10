package io.inapinch.pipeline.scheduling

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import com.google.common.base.Suppliers
import io.inapinch.pipeline.db.PipelineDao
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

enum class ScheduleType { PIPELINE }

val DEFAULT_TIME_ZONE : ZoneId = ZoneId.of("MST")

data class ScheduledItem(val actionId: UUID, val type: ScheduleType,
                         val cron: Cron, val timeZoneId: ZoneId = DEFAULT_TIME_ZONE) {

    private val executionTime : ExecutionTime = ExecutionTime.forCron(cron)

    fun active(currentTime: LocalDateTime) = executionTime.isMatch(currentTime.atZone(timeZoneId))

    fun run(pipelineDao: PipelineDao) {
        LOG.info("Starting {} - {}: {}", type, actionId, cron.asString())
        if(type == ScheduleType.PIPELINE)
            pipelineDao.request(actionId).ifPresent { pipelineDao.saveResult(UUID.randomUUID(), it.apply(), actionId) }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ScheduledItem::class.java)
    }
}

object Scheduler {
    private val LOG = LoggerFactory.getLogger(Scheduler::class.java)

    private var scheduler: Disposable? = null

    fun start(pipelineDao: PipelineDao) {
        val startTime = LocalDateTime.now()
        scheduler?.dispose()

        val scheduledItems = Suppliers.memoizeWithExpiration(pipelineDao::scheduledItems, 10, TimeUnit.MINUTES)

        scheduler = Flowable.interval(1, TimeUnit.SECONDS)
                .map {
                    /*
                       This is to guarantee seconds are sequential, even if time misbehaves
                       Ex:
                        start = LocalDateTime.now() - 12:01:01.999
                        next = LocalDateTime.now() - 12:01:03.001
                        next = 12:01:01.999 + 1 second = 12:01:02.999
                    */
                    startTime.plusSeconds(it)
                }
                .flatMap { currentTime ->
                    Flowable.fromIterable(scheduledItems.get())
                            .filter { it.active(currentTime) }
                }
                .subscribe({ it.run(pipelineDao) }, { LOG.error("Something went wrong", it) }) {
                            LOG.warn("Scheduler completed, restarting.")
                            Scheduler.start(pipelineDao)
                }
    }
}