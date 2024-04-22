package application.services

import domains.JobSchedule
import domains.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class TaskMother {

    companion object {
        fun generateMany(): List<Task> {
            return (0..10).map {
                generateOne()
            }
        }

        fun generateOne(): Task {
            return JobSchedule(
                UUID.randomUUID().toString(),
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                UUID.randomUUID().toString()
            )
        }
    }
}