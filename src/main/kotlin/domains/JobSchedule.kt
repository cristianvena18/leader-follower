package domains

class JobSchedule(
    private val jobId: String,
    val time: Long,
    val jobDataId: String,
): Task(jobId) {}