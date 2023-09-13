package eu.happycoders.virtualthreads.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemOutPrintYields {

  // Notice:
  // - carriers can change on System.out.println()

  public static void main(String[] args) {
    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      int numberOfVirtualThreads = Runtime.getRuntime().availableProcessors() * 3 / 2;
      for (int i = 0; i < numberOfVirtualThreads; i++) {
        executorService.submit(
            () -> {
              String lastCarrier = null;
              for (int j = 0; j < 20; j++) {
                Thread thread = Thread.currentThread();
                String message = thread.toString();
                String carrier = getCarrier(thread);
                if (lastCarrier == null) {
                  lastCarrier = carrier;
                } else if (!carrier.equals(lastCarrier)) {
                  message += " --> Carrier of VT #" + thread.threadId() + " changed to " + carrier;
                  lastCarrier = carrier;
                }
                System.out.println(message);
              }
            });
      }
    }
  }

  private static String getCarrier(Thread thread) {
    String s = thread.toString();
    int atPos = s.indexOf('@');
    if (atPos == -1) {
      throw new IllegalArgumentException(s);
    }
    return s.substring(atPos + 1);
  }
}
