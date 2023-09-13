package eu.happycoders.virtualthreads.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

public class StackTraces {

  public static void main(String[] args) throws InterruptedException {
    dumpStack();
    // java.lang.Exception: Stack trace from thread Thread[#1,main,5,main]
    // at eu.happycoders.virtualthreads.other.StackTraces.dumpStack(Example03_StackTraces.java:41)
    // at eu.happycoders.virtualthreads.other.StackTraces.main(Example03_StackTraces.java:10)

    Thread.startVirtualThread(StackTraces::dumpStack).join();
    // java.lang.Exception: Stack trace from thread
    // VirtualThread[#30]/runnable@ForkJoinPool-1-worker-1
    // at eu.happycoders.virtualthreads.other.StackTraces.dumpStack(Example03_StackTraces.java:41)
    // at java.base/java.lang.VirtualThread.run(VirtualThread.java:311)

    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      executorService.submit(StackTraces::dumpStack);
      // java.lang.Exception: Stack trace from thread
      // VirtualThread[#33]/runnable@ForkJoinPool-1-worker-1
      // at eu.happycoders.virtualthreads.other.StackTraces.dumpStack(Example03_StackTraces.java:41)
      // at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
      // at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
      // at java.base/java.lang.VirtualThread.run(VirtualThread.java:311)
    }

    try (StructuredTaskScope<Void> scope = new StructuredTaskScope<>()) {
      scope.fork(
          () -> {
            dumpStack();
            // java.lang.Exception: Stack trace from thread
            // VirtualThread[#34]/runnable@ForkJoinPool-1-worker-1
            // at eu.happycoders.virtualthreads.other.StackTraces.dumpStack(Example03_StackTraces.java:41)
            // at eu.happycoders.virtualthreads.other.StackTraces.lambda$main$1(Example03_StackTraces.java:23)
            // at java.base/java.util.concurrent.StructuredTaskScope$SubtaskImpl.run(StructuredTaskScope.java:889)
            // at java.base/java.lang.VirtualThread.run(VirtualThread.java:311)

            try (StructuredTaskScope<Void> nestedScope = new StructuredTaskScope<>()) {
              nestedScope.fork(
                  () -> {
                    dumpStack();
                    // java.lang.Exception: Stack trace from thread
                    // VirtualThread[#35]/runnable@ForkJoinPool-1-worker-2
                    // at eu.happycoders.virtualthreads.other.StackTraces.dumpStack(Example03_StackTraces.java:41)
                    // at eu.happycoders.virtualthreads.other.StackTraces.lambda$main$0(Example03_StackTraces.java:27)
                    // at java.base/java.util.concurrent.StructuredTaskScope$SubtaskImpl.run(StructuredTaskScope.java:889)
                    // at java.base/java.lang.VirtualThread.run(VirtualThread.java:311)
                    return null;
                  });
              nestedScope.join();
            }

            return null;
          });
      scope.join();
    }
  }

  private static void dumpStack() {
    new Exception("Stack trace from thread " + Thread.currentThread()).printStackTrace();
  }
}
