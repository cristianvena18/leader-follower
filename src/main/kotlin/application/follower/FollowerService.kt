package application.follower

import application.leader.ElectLeaderService
import application.services.FollowerWorkerService
import domains.Task
import infra.utils.UnirestUtils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.util.concurrent.TimeUnit

class FollowerService(
    private val httpClient: UnirestUtils = UnirestUtils(),
    private val workerService: FollowerWorkerService = FollowerWorkerService()
) {
    private var reportJob: Job? = null
    private var startElectNewLeader: (() -> Unit?)? = null

    fun reportToLeader(callback: () -> Unit) {
        // launch a global thread for report follower
        reportJob = GlobalScope.launch {
            schedule()
        }
        startElectNewLeader = callback;
    }

    private suspend fun schedule() {
        do {
            try {
                httpClient.post("${ElectLeaderService.LEADER}/followers/report", mapOf("ip" to ElectLeaderService.MY_IP))
            } catch (e: Exception) {
                //TODO: add validation if leader is down and start election new leader

                println("ERROR WHILE REPORTING FOLLOWER: $e")
                if (e.message.toString().contains("Connection refused")) {
                    // I will cancel report job because this replica can be used as leader
                    reportJob?.cancel()
                    // the same with worker service, it's going to start again after elect a new leader
                    workerService.stop()

                    startElectNewLeader?.let { it() }
                }
            } finally {
                delay(TimeUnit.MINUTES.toMillis("1".toLong()))
            }
        } while (true)
    }
}