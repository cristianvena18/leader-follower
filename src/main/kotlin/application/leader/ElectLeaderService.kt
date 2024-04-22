package application.leader

import application.follower.FollowerService
import infra.configs.APP_IP
import infra.configs.APP_PORT
import infra.configs.SERVICE_NAME
import infra.utils.UnirestUtils
import kong.unirest.json.JSONObject
import java.net.InetAddress


class ElectLeaderService(
    private val httpClient: UnirestUtils = UnirestUtils(),
    private val followerService: FollowerService = FollowerService(),
    private val leaderService: LeaderService = LeaderService()
) {
    companion object {
        private val NUMBER_CHOOSE = (0 ..10).random()
        val MY_IP = "http://${APP_IP}:${APP_PORT}"

        var LEADER: String? = null

        fun getNumberChoose() = NUMBER_CHOOSE
    }

    fun elect(force: Boolean = false) {
        val ips = getIps()

        var leader: String? = null

        for (ip in ips) {
            var body: Map<String, String> = mapOf();
            try {
                val response = httpClient.post("$ip/concourse", mapOf("number" to NUMBER_CHOOSE, "force" to force))
                body = JSONObject(response.body.toString()).toMap() as Map<String, String>
            } catch (_: Throwable) {

            }

            println("result is $body")

            if (body.get("leader") != null && !force) {
                leader = body.get("leader")
                break
            }

            if (body.get("result") == "lose") {
                leader = ip;
            } else if (body.get("result") == "win") {
                leader = MY_IP
            }
        }

        if (leader == null || leader == MY_IP) {
            startAsLeader()
        } else {
            println(leader)
            startAsFollower(leader)
        }
    }

    private fun startAsFollower(ip: String) {
        println("STARTING AS FOLLOWER")
        LEADER = ip
        followerService.reportToLeader {
            LEADER = null
            elect(force = true)
        }
    }

    private fun startAsLeader() {
        println("STARTING AS LEADER")
        LEADER = MY_IP
        leaderService.work {
            LEADER = null
            elect(force = true)
        }
    }

    private fun getIps(): List<String> {

        if (SERVICE_NAME == "localhost") {
            return listOf(
                "http://localhost:8081",
                "http://localhost:8080",
                "http://localhost:8082"
            ).filter { it != MY_IP }
        } else {

            val ips = InetAddress.getAllByName(SERVICE_NAME)

            return ips.map { "http://${it.hostAddress}:$APP_PORT" }.filter { it != MY_IP }
        }
    }
}