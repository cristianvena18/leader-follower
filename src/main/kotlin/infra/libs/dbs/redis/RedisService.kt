package infra.libs.dbs.redis

import infra.configs.*
import org.slf4j.LoggerFactory
import redis.clients.jedis.*
import java.text.SimpleDateFormat

class RedisService (jedisPoolParam :JedisPool?){
    constructor() : this(null)

    private val jedisPool :JedisPool = jedisPoolParam ?: initJedisPool()

    private fun initJedisPool() : JedisPool {
        val gpo = JedisPoolConfig()
        gpo.maxIdle = maxThreads

        return JedisPool(gpo, host, port, timeout,
            password, dbNumber, tls
        )
    }

    private val sdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val logger = LoggerFactory.getLogger(RedisService::class.java)  //mu.KotlinLogging.logger {}

    private val expirationKeyTime = expirationTime;

    fun setValue(key: String, value: String) {
        jedisPool.resource.let { jedis ->
            logger.debug("Setting value in redis ---> $key = $value")
            jedis.setex(key, expirationKeyTime ,value)
        }
    }

    fun getStatus() : String {
        return try {
            jedisPool.run{
                "{ jedis_connections : { active:$numActive , idle:$numIdle , waiters:$numWaiters }}"
            }
        } catch (e:Exception){
            "Error accessing Redis: $host"
        }
    }

    fun storeDummy(dummy : Any) {
        jedisPool.resource.use { jedis ->
            jedis.set("dummy", dummy.toString())
        }
    }

    fun getDummy() : String {
        jedisPool.resource.use {jedis->
            jedisPool.run{ logger.info (getStatus()) }
            return jedis.get("dummy")
        }
    }

    fun getValue(key : String) : String? {
        jedisPool.resource.use { jedis ->
            return jedis.get(key)
        }
    }


}