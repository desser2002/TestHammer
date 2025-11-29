package org.dzianisbova.infrastructure.load.taskproducer;

import org.dzianisbova.domain.load.ScenarioTask;
import org.dzianisbova.domain.load.taskproducer.TaskProducer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueFeeder implements TaskProducer {
    private final BlockingQueue<Runnable> taskQueue;
    private final ScenarioTask scenarioTask;
    private final Duration testDuration;
    private final ExecutorService producerExecutor;


    public QueueFeeder(BlockingQueue<Runnable> taskQueue, ScenarioTask scenarioTask, Duration testDuration) {
        this.taskQueue = taskQueue;
        this.scenarioTask = scenarioTask;
        this.testDuration = testDuration;

        this.producerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TaskProducer");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        Instant testEndTime = Instant.now().plus(testDuration);
        producerExecutor.submit(() -> {
            try {
                while (Instant.now().isBefore(testEndTime)) {
                    taskQueue.put(scenarioTask);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

    }

    @Override
    public void stop() {
        producerExecutor.shutdownNow();
    }
}
