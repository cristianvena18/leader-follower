package application.leader

import application.base.BaseWorkService
import application.services.LeaderWorkerService
import domains.Follower
import domains.JobSchedule
import domains.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


class LeaderService(
    private val leaderWorkService: BaseWorkService<List<Task>> = LeaderWorkerService()
) {
    companion object {
        private var followers: MutableList<Follower> = ArrayList()

        fun getFollowers() = followers.toList();
    }

    private var startElectionAgain: (() -> Unit)? = null
    private var followerJob: Job? = null;

    fun registerFollower(ip: String) {
        var follower = followers.find { it.ip == ip }

        if (follower == null) {
            follower = Follower(ip, true, LocalDateTime.now())

            followers.add(follower);
        } else {
            if (!follower.isAlive) {
                follower.isAlive = true
            }

            follower.lastChecked = LocalDateTime.now()
        }
    }

    fun work(callback: () -> Unit) {
        leaderWorkService.start()
        followerJob = GlobalScope.launch {
            validateIfFollowersAreAlive()
        }

        startElectionAgain = callback
    }

    private suspend fun validateIfFollowersAreAlive() {
        do {
            try {
                val countBefore = followers.count()
                followers = followers.filter { !it.lastChecked.isBefore(LocalDateTime.now().minus(Duration.ofMinutes(2L))) }.toMutableList()
                // TODO: i might http request to check if followers is working for this replica?
                if (followers.count() < countBefore) {
                    println("i removed some followers not responding")
                }
            } catch (e: Exception) {
                println("ERROR WHILE validate if followers are alive: $e")

                followerJob?.cancel()
                leaderWorkService.stop()

                startElectionAgain?.let { it() }
            } finally {
                delay(TimeUnit.SECONDS.toMillis(30L))
            }
        } while (true)
    }
}