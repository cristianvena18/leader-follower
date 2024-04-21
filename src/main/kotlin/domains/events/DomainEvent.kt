package domains.events

interface DomainEvent {
    abstract val eventName: String
}