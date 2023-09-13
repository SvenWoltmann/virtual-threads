package eu.happycoders.virtualthreads.sandbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadDemo {

  public static void main(String[] args) throws InterruptedException {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int j = 0; j < 1000; j++) {
        executor.submit(
            () -> {
              for (int i = 0; i < 10; i++) {
                System.out.println("Hello, I am " + Thread.currentThread());
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  return;
                }
              }
            });
      }
    }
  }
}
