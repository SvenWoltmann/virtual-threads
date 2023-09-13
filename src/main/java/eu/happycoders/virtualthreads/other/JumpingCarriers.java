package eu.happycoders.virtualthreads.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JumpingCarriers {

  // -Djdk.virtualThreadScheduler.parallelism=2

  public static void main(String[] args) {
    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < 4; i++) {
        final int threadNo = i;
        executorService.submit(
            () -> {
              for (int j = 0; j < 10; j++) {
                System.out.println(" ".repeat(threadNo * 10) + Thread.currentThread());
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  // Let thread die
                }
              }
            });
        try {
          Thread.sleep(250);
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }
}
