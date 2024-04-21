package infra.repositories

import infra.libs.dbs.mongo.MongoService

class MongoDbRepository {
    companion object Collections {
        val repository = MongoService()

        fun initDb() {

        }
    }
}