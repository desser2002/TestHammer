package org.dzianisbova.domain.metrics;

import java.util.ArrayList;
import java.util.List;

public class FinalReporter implements StatisticObserver {
    private final List<StatsSnapshot> finalReport = new ArrayList<>();
    private final StatisticPublisher publisher;

    public FinalReporter(StatisticPublisher publisher) {
        this.publisher = publisher;
    }

    public void start() {
        publisher.addObserver(this);
    }

    public void stop() {
        publisher.removeObserver(this);
    }

    @Override
    public void onStatisticsUpdated(StatsSnapshot snapshot) {
        if (snapshot != null) {
            finalReport.add(snapshot);
        }
    }

    public void reset() {
        finalReport.clear();
    }

    public List<StatsSnapshot> getFinalReport() {
        return finalReport;
    }
}
