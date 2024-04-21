import application.leader.ElectLeaderService
import infra.utils.Dotenv

fun main(args : Array<String>) {

    println ("Starting MS");

    //Variables de entorno de config local
    Dotenv().load()

    // SNS initialization
    //PublisherEventBus.initialize()

    //SqsConsumerService.run()

    //DB initialization
    //MongoDbRepository.initDb()

    //Router e inicio del WS
    val router = Router();

    router.init();

    ElectLeaderService().elect()
}