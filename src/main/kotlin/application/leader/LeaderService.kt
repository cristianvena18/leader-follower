package application.leader


class LeaderService() {
    companion object {
        private val followers: MutableList<String> = ArrayList()
    }

    fun registerFollower(ip: String) {
        followers.add(ip);
    }
}