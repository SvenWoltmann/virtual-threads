package eu.happycoders.virtualthreads.other;

import java.time.Duration;

public class CapturingCarrierSynchronized {

  // Run with
  // -Djdk.tracePinnedThreads=short -Djdk.virtualThreadScheduler.parallelism=2

  // -Djdk.tracePinnedThreads=short
  // -->
  // Thread[#32,ForkJoinPool-1-worker-1,5,CarrierThreads]
  //
  // eu.happycoders.virtualthreads.other.CapturingCarrierSynchronized.lambda$main$2(CapturingCarrierSynchronized.java:44) <== monitors:1

  // -Djdk.tracePinnedThreads=full
  // -->
  // Thread[#101,ForkJoinPool-1-worker-4,5,CarrierThreads]
  // java.base/java.lang.VirtualThread$VThreadContinuation.onPinned(VirtualThread.java:185)
  // java.base/jdk.internal.vm.Continuation.onPinned0(Continuation.java:393)
  // java.base/java.lang.VirtualThread.parkNanos(VirtualThread.java:631)
  // java.base/java.lang.VirtualThread.sleepNanos(VirtualThread.java:803)
  // java.base/java.lang.Thread.sleep(Thread.java:590)
  //
  // eu.happycoders.virtualthreads.other.CapturingCarrierSynchronized.lambda$main$2(CapturingCarrierSynchronized.java:48) <== monitors:1
  // java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
  // java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
  // java.base/java.lang.VirtualThread.run(VirtualThread.java:311)

  // The stack traces point to the sleep() instruction inside the synchronized block.
  // A stack trace for the same code location is printed only once, even if the code at that
  // location is executed multiple times.

  public static void main(String[] args) throws InterruptedException {
    // Start 2 virtual threads
    for (int i = 0; i < 2; i++) {
      Thread.startVirtualThread(
          () -> {
            while (true) {
              System.out.println("Virtual thread running on carrier: " + getCarrier());
              sleepIgnoringInterrupted(Duration.ofSeconds(1));
            }
          });
    }

    Thread.sleep(4500);

    // Start a virtual thread that blocks within a synchronized block
    Thread.startVirtualThread(
        () -> {
          synchronized (new Object()) {
            System.out.println(
                "\nSynchronized blocking operation 1 entered on carrier: " + getCarrier() + "\n");
            sleepIgnoringInterrupted(Duration.ofSeconds(8));
            System.out.println(
                "\nSynchronized blocking operation 1 exited on carrier: " + getCarrier() + "\n");
          }
        });

    Thread.sleep(4000);

    // Start a second virtual thread that blocks within a synchronized block
    Thread.startVirtualThread(
        () -> {
          synchronized (new Object()) {
            System.out.println(
                "\nSynchronized blocking operation 2 entered on carrier: " + getCarrier() + "\n");
            sleepIgnoringInterrupted(Duration.ofSeconds(8));
            System.out.println(
                "\nSynchronized blocking operation 2 exited on carrier: " + getCarrier() + "\n");
          }
        });

    Thread.sleep(12000);
  }

  private static void sleepIgnoringInterrupted(Duration duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // Let thread die
    }
  }

  private static String getCarrier() {
    String s = Thread.currentThread().toString();
    int atPos = s.indexOf('@');
    if (atPos == -1) {
      throw new IllegalArgumentException(s);
    }
    return s.substring(atPos + 1);
  }
}
