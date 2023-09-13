package eu.happycoders.virtualthreads.other;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class CPUBoundTasksDontYield {

  // Run with
  // -Djdk.virtualThreadScheduler.parallelism=2

  public static void main(String[] args) throws InterruptedException {
    makeCpuBusy();

    // Start 10 virtual threads
    long start = System.currentTimeMillis();
    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < 10; i++) {
        executorService.submit(CPUBoundTasksDontYield::makeCpuBusy);
      }
    }
    long time = System.currentTimeMillis() - start;
    System.out.printf("Finished after %d ms%n", time);
  }

  private static void makeCpuBusy() {
    String startedThreadAsString = Thread.currentThread().toString();
    long start = System.currentTimeMillis();
    ThreadLocalRandom current = ThreadLocalRandom.current();
    BigInteger sum = BigInteger.ZERO;
    for (int i = 0; i < 20_000_000; i++) {
      BigInteger a = BigInteger.valueOf(current.nextLong());
      BigInteger b = BigInteger.valueOf(current.nextLong());
      sum = sum.add(a.multiply(b));
    }

    long time = System.currentTimeMillis() - start;
    String finishedThreadAsString = Thread.currentThread().toString();
    System.out.printf(
        "Started as %s, finished as %s after %d ms; sum = %d%n",
        startedThreadAsString, finishedThreadAsString, time, sum);
  }
}
