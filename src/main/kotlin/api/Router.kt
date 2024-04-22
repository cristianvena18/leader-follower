package api

import api.controllers.LeaderElectorController
import infra.marshallers.DefaultMarshaller
import infra.webservice.RouterApplication
import spark.Spark.*

class Router (
    private val defaultMarshaller: DefaultMarshaller = DefaultMarshaller(),
    private val leaderElectorController: LeaderElectorController = LeaderElectorController()
) : RouterApplication() {

    override fun routes() {

        path("/leaders") {
            get("", leaderElectorController.leaderIsAlive, defaultMarshaller)
        }

        path("/concourse") {
            post("", leaderElectorController.concourse, defaultMarshaller)
        }

        path("/followers") {
            post("/register", leaderElectorController.registerFollower, defaultMarshaller)
            post("/report", leaderElectorController.registerFollower, defaultMarshaller)
        }

        path("/workers") {
            post("", leaderElectorController.worker, defaultMarshaller)
        }
    }

    override fun mockRoutes() {
        path ("/mocks"){

        }
    }

}