package domains

import java.time.LocalDateTime

data class Follower(val ip: String, var isAlive: Boolean, var lastChecked: LocalDateTime)
