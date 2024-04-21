import api.controllers.LeaderElectorController
import infra.marshallers.DefaultMarshaller
import infra.webservice.RouterApplication
import spark.Spark.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Router::class.java)

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
        }

        /*path ("/qr-requests") {
            post("", qrRequestsController.createQrRequest, defaultMarshaller)
            delete("/:qrReqId", qrRequestsController.deleteQrRequest, defaultMarshaller)
            get("/:qrReqId", qrRequestsController.getQrRequestById, defaultMarshaller)
        }*/
    }

    override fun mockRoutes() {
        path ("/mocks"){

        }
    }

}