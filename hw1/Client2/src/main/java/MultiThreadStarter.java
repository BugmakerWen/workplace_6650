import Model.*;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadStarter {
  private static String url;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingDeque<SwipeEvent> events;
  private static final int numOfThread = 160;
  private static final int totalReq = 500000;
  private static final int reqPerThread = 3125;

  public static void main(String[] args) throws InterruptedException {
    // url ="http://54.186.178.203:8080/Assignment1_war/";
    url ="http://localhost:8080/Assignment1_war_exploded/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingDeque<SwipeEvent>();
    List<Integer> latencies = new ArrayList<>();
    Information info = new Information(latencies);
    List<String[]> records = new ArrayList<>();
    records.add(new String[]{"Start Time", "Latency", "Type", "Code"});

    System.out.println("****************PROCESSING BEGIN******************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long startTime = System.currentTimeMillis();
    Generator generator = new Generator(events, totalReq);
    new Thread(generator).start();

    for (int i = 0; i < numOfThread; i++) {
      Processor processor = new Processor(url, successReq, failReq, reqPerThread, events, latch, records, info);
      new Thread(processor).start();
    }

    latch.await();

    try {
      FileWriter fileWriter = new FileWriter("/Users/mw/Desktop/WorkPlace/workplace_6650/hw1/Client2/src/main/java/CSV");
      CSVWriter writer = new CSVWriter(fileWriter);
      writer.writeAll(records);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("****************PROCESSING FINISHED******************");

    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;

    System.out.println("Num of successful req :" + successReq.get());
    System.out.println("Num of unsuccessful req :" + failReq.get());
    System.out.println("Time of Process :" + wallTime);
    System.out.println( "Actual Throughput: " + (int)((successReq.get() + failReq.get()) / (wallTime / 1000.0)) + " requests/second");

    System.out.println("Mean response time :" + info.meanResponseTime());
    System.out.println("Median response time :" + info.medianResponseTime());
    System.out.println("P99 response time :" + info.p99thTime());
    System.out.println("Minimum response time :" + info.minResponseTime());
    System.out.println("Maximum response time :" + info.maxResponseTime());
    System.out.println("current time :" + System.currentTimeMillis());
  }
}
