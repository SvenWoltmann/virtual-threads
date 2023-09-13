package eu.happycoders.virtualthreads.other;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class VirtualThreadsStartingPlatformThreads {

  // Start four virtual threads, each starting an executor service
  // running four platform threads doing heaving CPU calculations.
  // --> All good!

  // Run with
  // -Djdk.virtualThreadScheduler.parallelism=1

  public static void main(String[] args) throws InterruptedException {
    try (ExecutorService executor1 = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < 4; i++) {
        executor1.submit(
            () -> {
              System.out.println(Thread.currentThread() + " starting executor service...");
              try (ExecutorService executor2 = Executors.newCachedThreadPool()) {
                for (int j = 0; j < 4; j++) {
                  executor2.submit(VirtualThreadsStartingPlatformThreads::makeCpuBusy);
                }
              }
              System.out.println(Thread.currentThread() + " finished executor service...");
            });
      }
    }
  }

  private static void makeCpuBusy() {
    System.out.println(Thread.currentThread() + " starting calculation...");
    ThreadLocalRandom current = ThreadLocalRandom.current();
    BigInteger sum = BigInteger.ZERO;
    for (int i = 0; i < 100_000_000; i++) {
      BigInteger a = BigInteger.valueOf(current.nextLong());
      BigInteger b = BigInteger.valueOf(current.nextLong());
      sum = sum.add(a.multiply(b));
    }

    System.out.println(Thread.currentThread() + " finished calculation; sum = " + sum);
  }
}
