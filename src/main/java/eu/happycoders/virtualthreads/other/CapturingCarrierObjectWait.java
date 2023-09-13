package eu.happycoders.virtualthreads.other;

import java.time.Duration;

public class CapturingCarrierObjectWait {

  // Run with
  // -Djdk.virtualThreadScheduler.parallelism=2

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

    // Start a virtual thread that blocks on Object.wait()
    Thread.startVirtualThread(
        () -> {
          Object o = new Object();
          synchronized (o) {
            System.out.println("\nObject.wait() entered on carrier: " + getCarrier() + "\n");
            try {
              o.wait();
            } catch (InterruptedException e) {
              // Let thread die
            }
          }
        });

    Thread.sleep(4000);

    // Start a second virtual thread that blocks on Object.wait()
    Thread.startVirtualThread(
        () -> {
          System.out.println("\nObject.wait() entered on carrier: " + getCarrier() + "\n");
          Object o = new Object();
          synchronized (o) {
            try {
              o.wait();
            } catch (InterruptedException e) {
              // Let thread die
            }
          }
        });

    Thread.sleep(4000);
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
