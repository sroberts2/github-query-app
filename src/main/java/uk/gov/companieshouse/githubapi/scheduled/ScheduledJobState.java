package uk.gov.companieshouse.githubapi.scheduled;

import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduledJobState {
    private final AtomicBoolean state;

    public ScheduledJobState() {
        state = new AtomicBoolean(false);
    }

    public void updateRunningState(final boolean newState) {
        state.set(newState);
    }

    public boolean isRunning() {
        return state.get();
    }
}
