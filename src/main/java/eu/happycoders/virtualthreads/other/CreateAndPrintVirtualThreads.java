package eu.happycoders.virtualthreads.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAndPrintVirtualThreads {

  // Notice:
  // - platform threads have a default name
  // - virtual threads have no name by default

  public static void main(String[] args) throws InterruptedException {
    logCurrentThread("Main thread");
    // --> Thread[#1,main,5,main]
    //            |   |   |  |
    //            +---+---+--+- thread ID
    //                +---+--+- thread name
    //                    +--+- thread priority
    //                       +- thread group

    Thread.ofPlatform()
        .start(() -> logCurrentThread("Platform thread started with 'ofPlatform()'"))
        .join();
    // Thread[#30,Thread-0,5,main]
    //            |   |    |  |
    //            +---+----+--+- thread ID
    //                +----+--+- thread name (a default name as we didn't assign one)
    //                     +--+- thread priority
    //                        +- thread group

    // With a name
    Thread.ofPlatform()
        .name("My platform thread")
        .start(() -> logCurrentThread("Named platform thread started with 'ofPlatform()'"))
        .join();
    // --> Thread[#31,My platform thread,5,main]

    Thread.startVirtualThread(
            () -> logCurrentThread("Virtual thread started with 'startVirtualThread()'"))
        .join();
    // --> VirtualThread[#32]/runnable@ForkJoinPool-1-worker-1
    //                    |      |               |
    //                    +------+---------------+- thread ID
    //                           +---------------+- the carrier thread's state
    //                                           +- the carrier thread (worker 1 from ForkJoinPool
    // 1)

    Thread.ofVirtual()
        .start(() -> logCurrentThread("Virtual thread started with 'ofVirtual()'"))
        .join();
    // --> VirtualThread[#36]/runnable@ForkJoinPool-1-worker-1

    Thread.ofVirtual()
        .name("My virtual thread")
        .start(() -> logCurrentThread("Named virtual thread started with 'ofVirtual()'"))
        .join();
    // --> VirtualThread[#37,My virtual thread]/runnable@ForkJoinPool-1-worker-1
    //                    |        |               |               |
    //                    +--------+---------------+---------------+- thread ID
    //                             +---------------+---------------+- thread name
    //                                             +---------------+- the carrier thread's state
    //                                                             +- the carrier thread (worker 1
    // from ForkJoinPool 1)

    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      executorService.submit(
          () -> logCurrentThread("Created via 'Executors.newVirtualThreadPerTaskExecutor()'"));
    }
    // --> VirtualThread[#38]/runnable@ForkJoinPool-1-worker-1
  }

  private static void logCurrentThread(String description) {
    System.out.printf("%-58s %s%n", description + ":", Thread.currentThread());
  }
}
