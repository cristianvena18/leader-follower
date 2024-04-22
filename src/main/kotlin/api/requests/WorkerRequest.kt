package api.requests

import domains.JobSchedule

data class WorkerRequest(val tasks: ArrayList<JobSchedule>)
