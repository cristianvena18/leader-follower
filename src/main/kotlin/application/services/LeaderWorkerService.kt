package application.services

import application.base.BaseWorkService
import application.leader.LeaderService
import domains.Task
import infra.utils.UnirestUtils
import kotlinx.coroutines.delay
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class LeaderWorkerService(
    private val httpClient: UnirestUtils = UnirestUtils()
) : BaseWorkService<List<Task>>() {
    override suspend fun receiveMessages(): List<Task> {
        delay(TimeUnit.SECONDS.toMillis(10L))

        val list = TaskMother.generateMany()

        return list
    }

    override suspend fun processMessage(task: List<Task>) {
        var lastError: Exception? = null;
        val followers = LeaderService.getFollowers();

        val arrayList = ArrayList<Task>()

        for (item in task) {
            arrayList.add(item)
        }

        val list = arrayList.chunked(Math.floorDiv(task.size, if (followers.isNotEmpty()) followers.size else 1))

        for (follower in followers) {
            try {
                httpClient.post("${follower.ip}/workers", mapOf("tasks" to list[followers.indexOf(follower)]))
            } catch (e: Exception) {
                lastError = e
            }
        }

        if (lastError != null) {
            throw lastError
        }
    }

    override suspend fun handleError(e: Throwable, task: List<Task>) {
        println("ERROR WHILE DELEGATING TO FOLLOWER A TASK $e, $task")
    }
}