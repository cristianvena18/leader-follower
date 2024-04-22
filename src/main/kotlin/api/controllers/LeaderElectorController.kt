package api.controllers

import api.requests.Concourse
import api.requests.RegisterFollower
import api.requests.WorkerRequest
import application.leader.ElectLeaderService
import application.leader.LeaderService
import application.services.FollowerWorkerService
import domains.Task
import infra.libs.gson.GsonUtils
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Route

private val logger = LoggerFactory.getLogger(LeaderElectorController::class.java)

class LeaderElectorController(
    private val leaderService: LeaderService = LeaderService(),
    private val workerService: FollowerWorkerService = FollowerWorkerService()
) {

    val leaderIsAlive = Route { _: Request, _: Response ->
        mapOf("message" to "OK")
    }

    val concourse = Route { req: Request, _: Response ->

        val request = GsonUtils.getJsonBody(req.body(), Concourse::class.java);

        val myNumber = ElectLeaderService.getNumberChoose()

        if (ElectLeaderService.LEADER != null && !request.force) {
            println("leader is elected")
            mapOf("leader" to ElectLeaderService.LEADER)
        } else {
            if (request.number > myNumber) {
                println("i lose")
                mapOf("result" to "win");
            } else {
                println("i win")
                mapOf("result" to "lose")
            }
        }
    }

    val registerFollower = Route { req: Request, _: Response ->

        val request = GsonUtils.getJsonBody(req.body(), RegisterFollower::class.java);

        leaderService.registerFollower(request.ip);

        mapOf("result" to "OK")
    }


    val worker = Route { req: Request, _: Response ->

        println(req.body())
        val request = GsonUtils.getJsonBody(req.body(), WorkerRequest::class.java)

        workerService.start(request.tasks)

        mapOf("result" to "OK")
    }
}