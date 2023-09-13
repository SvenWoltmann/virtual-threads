package eu.happycoders.virtualthreads.other;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubmittingToExecutorServiceFromVirtualThreads {

  // This demo shows how submitting tasks from a virtual thread after the executor service is
  // already being shut down (auto-closeable!) throws a RejectedExecutionException.

  public static void main(String[] args) throws InterruptedException {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < 4; i++) {
        executor.submit(
            () -> {
              sleepIgnoringInterrupted(Duration.ofMillis(100));
              try {
                executor.submit(
                    () -> {
                      System.out.println(Thread.currentThread());
                    });
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
      }
    }
  }

  private static void sleepIgnoringInterrupted(Duration duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // Let thread die
    }
  }
}
