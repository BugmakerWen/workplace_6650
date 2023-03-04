import Model.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadStarter {
  private static String url;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  public static BlockingDeque<Swipe> events;
  private static final int numOfThread = 50;
  private static final int totalReq = 500000;
  private static final int reqPerThread = 10000;

  public static void main(String[] args) throws InterruptedException {
    url ="http://localhost:8080/Assignment1_war_exploded/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingDeque<Swipe>();

    System.out.println("****************PROCESSING BEGIN******************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long startTime = System.currentTimeMillis();
    Generator generator = new Generator(events, totalReq);
    new Thread(generator).start();

    for (int i = 0; i < numOfThread; i++) {
      Processor processor = new Processor(url, successReq, failReq, reqPerThread, events, latch);
      new Thread(processor).start();
    }

    latch.await();

    System.out.println("****************PROCESSING FINISHED******************");

    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;

    System.out.println("Num of successful req :" + successReq.get());
    System.out.println("Num of unsuccessful req :" + failReq.get());
    System.out.println("Time of Process :" + wallTime);
    System.out.println( "Actual Throughput: " + (int)((successReq.get() + failReq.get()) / (wallTime / 1000.0)) + " requests/second");
  }
}
