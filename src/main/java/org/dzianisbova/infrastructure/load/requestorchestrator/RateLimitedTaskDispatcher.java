package org.dzianisbova.infrastructure.load.requestorchestrator;

import org.dzianisbova.domain.load.ScenarioTask;
import org.dzianisbova.domain.load.ratelimiter.RateLimiter;
import org.dzianisbova.domain.load.requestorchestrator.RequestOrchestrator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateLimitedTaskDispatcher implements RequestOrchestrator {
    private final BlockingQueue<Runnable> taskQueue;
    private final ExecutorService executorService;
    private final RateLimiter rateLimiter;
    private final ExecutorService dispatcherThread;

    public RateLimitedTaskDispatcher(BlockingQueue<Runnable> taskQueue, ExecutorService executorService, RateLimiter rateLimiter) {
        this.taskQueue = taskQueue;
        this.executorService = executorService;
        this.rateLimiter = rateLimiter;
        this.dispatcherThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TaskDispatcher");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        dispatcherThread.submit(this::dispatchLoop);
    }

    @Override
    public void stop() {
        dispatcherThread.shutdownNow();
    }

    private void dispatchLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {

                ScenarioTask scenarioTask = (ScenarioTask) taskQueue.poll();
                if (scenarioTask != null) {

                    for (int i = 0; i < scenarioTask.getRequestCount(); i++) {
                        rateLimiter.acquire();
                    }
                    executorService.submit(scenarioTask);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
