package application.services

import application.base.BaseWorkService
import domains.JobSchedule
import domains.Task
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class FollowerWorkerService : BaseWorkService<Task>() {

    fun start(tasks: ArrayList<JobSchedule>) = launch {
        val channel = Channel<Task>()

        repeat(tasks.size) {
            launchProcessor(channel);
        }

        tasks.forEach { channel.send(it) }
    }

    override suspend fun processMessage(task: Task) {
        val job = task as JobSchedule;
        println("TASK: ${task.id}: ${job.jobDataId} at: ${job.time}")
    }

    override suspend fun handleError(e: Throwable, task: Task) {
        println("ERROR: ${e.message} on task")
    }

    override suspend fun receiveMessages(): Task {
        TODO("Not yet implemented")
    }
}