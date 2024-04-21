package infra.libs.dbs.mongo

import com.mongodb.BasicDBObject
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Projections
import com.mongodb.connection.SslSettings
import api.constants.Messages
import api.exceptions.HttpException
import api.exceptions.withCause
import infra.configs.*
import org.bson.*

class MongoService(
    val client: com.mongodb.client.MongoClient? = null
) {
    private val connectionString = ConnectionString(buildConnectionString())
    private val database = connectionString.database ?: MONGO_DB_DBNAME
    private val mongoClient = client ?: buildMongoClient()



    private fun buildConnectionString(): String {
        var string: String = MONGO_DB_URI

        if (MONGO_DB_USER != "" && MONGO_DB_PASS != "") {
            string = MONGO_DB_URI.replace("://", "://$MONGO_DB_USER:$MONGO_DB_PASS@")
        }

        return string
    }

    private fun buildMongoClient(): com.mongodb.client.MongoClient {
        if (MONGO_DB_TRUST_STORE != "") {
            System.setProperty("javax.net.ssl.trustStore", MONGO_DB_TRUST_STORE)
            System.getProperty("javax.net.ssl.trustStoreType", MONGO_DB_TRUST_STORE_TYPE)
            System.setProperty("javax.net.ssl.trustStorePassword", MONGO_DB_TRUST_STORE_PASS)
        }
        if (MONGO_DB_TLS) {
            val settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSslSettings { builder: SslSettings.Builder ->
                    builder.enabled(MONGO_DB_TLS)
                }
                .retryWrites(MONGO_DB_RETRY_WRITES)
                .build()
            return MongoClients.create(settings)
        }

        return MongoClients.create(connectionString)
    }

    fun findOne (collection: String, query: Map<String, Any>) : Document? {
        val result = find(collection, query)
        if (result.size > 1){
            throw HttpException(status = 409,
                message = Messages.ERROR_DUPLICATED_CONSTRAINT,
                cause = withCause(message = "Constraint error: Requested resource has ${result.size} results.")
            )
        } else if (result.size == 0){
            return null
        }
        return result.first()
    }

    private fun createQuery (query: Map<String, Any>) : Document{
        val queryDoc = Document()
        query.forEach { k, v ->
            val value = when (v) {
                is Int -> BsonInt32(v)
                is Boolean -> BsonBoolean(v)
                is Map<*, *> -> createQuery(v as Map<String, Any>) // Cuando es un Map, llamamos recursivamente a createQuery
                is List<*> -> BsonArray(v.map { BsonString(it.toString()) }) // Cuando es una lista, lo convertimos a BsonArray
                else -> BsonString(v.toString())
            }
            queryDoc.append(k, value)
        }
        return queryDoc
    }

    fun find (collection: String, query: Map <String, Any>) : List<Document?> {
        val queryDoc = createQuery(query)
        return mongoClient.getDatabase(database)
            .getCollection(collection)
            .find(queryDoc).projection(Projections.exclude("_id", "raw_data")).toList()
    }

    fun findAll (collection: String, query: Map<String, Any>) : List<Document?>? {
        val result = find(collection, query)
        if (result.isEmpty()){
            return null
        }
        return result
    }

    fun save(collection: String, toSaveData: Map<String, Any>) {
        mongoClient.getDatabase(database)
            .getCollection(collection)
            .insertOne(Document(toSaveData))
    }

    fun update(collection: String, query: Map<String, Any>, toUpdateData: Map<String, Any?>) {
        val queryDoc = createQuery(query)
        val document = Document("\$set", Document(toUpdateData))
        mongoClient.getDatabase(database)
            .getCollection(collection)
            .updateOne(queryDoc, document)
    }


    fun updateMany(collection: String, query: Map<String, Any>, toUpdateData: Map<String, Any?>) {
        val queryDoc = createQuery(query)
        val document = Document("\$set", Document(toUpdateData))
        mongoClient.getDatabase(database)
            .getCollection(collection)
            .updateMany(queryDoc, document)
    }

    fun ensureSequence(collection: String, seqName: String) {
        val existing = findOne(collection, mapOf("_id" to seqName))
        if (existing.isNullOrEmpty()) {
            save(collection, mapOf("_id" to seqName, "seq" to 0))
        }
    }

    @Synchronized
    fun syncFindAndUpdate(collection: String, query: BasicDBObject, updValue: BasicDBObject) : Document? {
        return mongoClient.getDatabase(database)
            .getCollection(collection).findOneAndUpdate(query, updValue)
    }
    fun getNextSequenceValue(collection: String, sequenceName: String) : Long {
        val find = BasicDBObject()
        find["_id"] = sequenceName
        val update = BasicDBObject()
        update["\$inc"] = BasicDBObject("seq", 1)

        val result = syncFindAndUpdate(collection, find, update)

        println("Result: ${result?.toString()}")
        return result?.get("seq").toString().toLong()
    }

    fun addIndex(collection: String, newIndex: Map<String, Any>, isUnique: Boolean = false, indexName: String? = null) {
        val keys = createQuery(newIndex)
        val indexOptions = IndexOptions().unique(isUnique)
        if (indexName!= null) {
            indexOptions.name(indexName)
        }

        mongoClient.getDatabase(database)
            .getCollection(collection)
            .createIndex(keys, indexOptions)
    }

    fun healthCheck(): Any {
        val status = mongoClient.clusterDescription.serverDescriptions.component1().state.toString()
        return if (status === "CONNECTED") status
        else "NOT_CONNECTED"
    }
}