package io.narayana.lra.coordinator.domain.model;

public enum LRAStatusReason {

    Unknown("Unknown"),
    Timeout("LRA timeout"),
    AfterLRARequestPhase("After LRA calls phase"),
    AfterLRAFailed("After LRA call(s) failed"),
    NestedClose("Nested LRA was closed so it must remain cancellable"),
    ParticipantResponse("One or more participants responded with Compensating or Completing response"),
    ParticipantFailedStatusCall("One or more participants failed the Status request"),
    ForgetLRARequestPhase("Forget LRA call(s) phase")

    ;

    private String message;

    LRAStatusReason(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
