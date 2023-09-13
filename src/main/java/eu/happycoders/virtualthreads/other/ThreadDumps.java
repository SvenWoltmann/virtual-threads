package eu.happycoders.virtualthreads.other;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

public class ThreadDumps {

  // Start threads, let them sleep, then look at the thread dumps:
  // - jcmd <pid> Thread.print
  // - jcmd <pid> Thread.dump_to_file -format=plain <file>
  // - jcmd <pid> Thread.dump_to_file -format=json <file>
  // - cjd (true is default)

  // --> Thread.print doesn't print virtual threads
  // --> Only threads created in a nested structured task scope have a parent other than "root"

  // -Djdk.trackAllThreads=false:
  // Virtual threads created directly and that are not blocked in network I/O are not tracked.
  // Tracked are:
  // - virtual threads created via ExecutorService
  // - virtual threads created via StructuredTaskScope
  // - virtual threads created directly and currently blocked in network I/O

  public static void main(String[] args) throws InterruptedException {
    // - a platform thread
    // - a platform thread created manually from within that platform thread
    Thread.ofPlatform()
        .name("Platform thread")
        .start(
            () -> {
              System.out.printf(
                  "Thread %s creating nested sleeping thread...%n", Thread.currentThread());

              try {
                Thread.ofPlatform().name("Nested platform thread").start(ThreadDumps::sleep).join();
              } catch (InterruptedException e) {
                // Let thread die
              }
              System.out.println("I died.");
            });

    // - a virtual thread created manually
    // - a virtual thread created manually from within that virtual thread
    Thread.ofVirtual()
        .name("Virtual thread")
        .start(
            () -> {
              System.out.printf(
                  "Thread %s creating nested sleeping thread...%n", Thread.currentThread());

              try {
                Thread.ofVirtual().name("Nested virtual thread").start(ThreadDumps::sleep).join();
              } catch (InterruptedException e) {
                // Let thread die
              }
              System.out.println("I died.");
            });

    // - a virtual thread created in an ExecutorService
    // - a virtual thread created in an ExecutorService nested in another ExecutorService
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    executorService.submit(
        () -> {
          System.out.printf(
              "Thread %s creating nested sleeping thread...%n", Thread.currentThread());

          try (ExecutorService nestedExecutorService =
              Executors.newVirtualThreadPerTaskExecutor()) {
            nestedExecutorService.submit(ThreadDumps::sleep);
          }

          System.out.println("I died.");
        });

    // - a virtual thread created in a StructuredTaskScope
    // - a virtual thread created in a StructuredTaskScope nested in another StructuredTaskScope

    try (StructuredTaskScope<Void> structuredTaskScope = new StructuredTaskScope<>()) {
      structuredTaskScope.fork(
          () -> {
            System.out.printf(
                "Thread %s creating nested sleeping thread...%n", Thread.currentThread());

            try (StructuredTaskScope<Void> nestedStructuredTaskScope =
                new StructuredTaskScope<>()) {
              nestedStructuredTaskScope.fork(
                  () -> {
                    // This nested thread is the only one shown with a parent!
                    // "parent": "java.util.concurrent.StructuredTaskScope@6193b845"
                    // All the other threads, including the nested ones have this parent:
                    // "parent": "<root>"
                    sleep();
                    return null;
                  });
              nestedStructuredTaskScope.join();
            }

            System.out.println("I died.");
            return null;
          });
      structuredTaskScope.join();
    }
  }

  private static void sleep() {
    System.out.printf("Thread %s sleeping...%n", Thread.currentThread());
    try {
      Thread.sleep(Duration.ofDays(1));
    } catch (InterruptedException e) {
      // Let thread die
    }
    System.out.println("I died.");
  }
}
