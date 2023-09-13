package eu.happycoders.virtualthreads.other;

import java.lang.Thread.Builder;

public class ThreadDumpsWithDeadlocks {

  // Start threads, let them sleep, then look at the thread dumps:
  // - jcmd <pid> Thread.print
  // - jcmd <pid> Thread.dump_to_file -format=plain <file>
  // - jcmd <pid> Thread.dump_to_file -format=json <file>

  // --> The old thread dump contains deadlock information only for the deadlock between two
  // platform threads.
  // --> The new thread dump doesn't contain any deadlock or even lock information.

  public static void main(String[] args) throws InterruptedException {
    // 1. Two platform threads that deadlock with each other
    createDeadlock(
        Thread.ofPlatform(), "Platform thread 1.1",
        Thread.ofPlatform(), "Platform thread 1.2");

    // Found one Java-level deadlock:
    // =============================
    // "Platform thread 1":
    // waiting to lock monitor 0x000001fb10835120 (object 0x00000004446291a0, a java.lang.Object),
    // which is held by "Platform thread 2"
    //
    // "Platform thread 2":
    // waiting to lock monitor 0x000001fb10834ec0 (object 0x0000000444629190, a java.lang.Object),
    // which is held by "Platform thread 1"

    // 2. Two virtual threads that deadlock with each other
    createDeadlock(
        Thread.ofVirtual(), "Virtual thread 2.1",
        Thread.ofVirtual(), "Virtual thread 2.2");

    // --> No deadlock found

    // 3. A platform thread that deadlock with a virtual thread
    createDeadlock(
        Thread.ofPlatform(), "Platform thread 3.1",
        Thread.ofVirtual(), "Virtual thread 3.2");

    // --> No deadlock found, but platform thread 3.1 is shown in BLOCKED state, waiting for a lock
  }

  private static void createDeadlock(
      Builder threadBuilder1, String name1, Builder threadBuilder2, String name2) {
    Object lockA = new Object();
    Object lockB = new Object();

    threadBuilder1
        .name(name1)
        .start(
            () -> {
              synchronized (lockA) {
                System.out.printf("%s, %s acquired lock 1%n", name1, Thread.currentThread());
                try {
                  Thread.sleep(500);
                  synchronized (lockB) {
                    System.out.printf("%s, %s acquired lock 2%n", name1, Thread.currentThread());
                  }
                } catch (InterruptedException e) {
                  // Let the thread die
                }
              }
              System.out.printf("%s, %s ended%n", name1, Thread.currentThread());
            });

    threadBuilder2
        .name(name2)
        .start(
            () -> {
              synchronized (lockB) {
                System.out.printf("%s, %s acquired lock 2%n", name2, Thread.currentThread());
                synchronized (lockA) {
                  System.out.printf("%s, %s acquired lock 1%n", name2, Thread.currentThread());
                }
              }
              System.out.printf("%s, %s ended%n", name2, Thread.currentThread());
            });
  }
}
