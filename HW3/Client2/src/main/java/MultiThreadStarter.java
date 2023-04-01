import Model.*;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadStarter {
  private static String url;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingDeque<SwipeEvent> events;
  private static AtomicBoolean threadsFinished;
  private static List<Record> getRecords;
  private static final int numOfThread = 100;
  private static final int totalReq = 50000;
  private static final int reqPerThread = 500;

  public static void main(String[] args) throws InterruptedException {
    // url ="http://52.39.118.193:8080/Assignment1_war/";
    url ="http://localhost:8080/Assignment1_war_exploded/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingDeque<SwipeEvent>();
    List<String[]> records = new ArrayList<>();
    threadsFinished = new AtomicBoolean(false);
    List<Integer> doPostLatencies = new ArrayList<>();
    Information doPostInfo = new Information(doPostLatencies);
    getRecords = Collections.synchronizedList(new ArrayList<>());
    records.add(new String[]{"Start Time", "Latency", "Type", "Code"});

    System.out.println("****************PROCESSING BEGIN******************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long startTime = System.currentTimeMillis();
    Generator generator = new Generator(events, totalReq);
    new Thread(generator).start();

    for (int i = 0; i < numOfThread; i++) {
      Processor processor = new Processor(url, successReq, failReq, reqPerThread, events, latch, records, doPostInfo);
      new Thread(processor).start();
    }

    List<Integer> doGetLatencies = new ArrayList<>();
    Information doGetInfo = new Information(doGetLatencies);
    long getStartTime = System.currentTimeMillis();
    DoGetProcessor getProcessor = new DoGetProcessor(url, threadsFinished, getRecords, doGetInfo);
    Thread getThread = new Thread(getProcessor);
    getThread.start();

    latch.await();
    threadsFinished.set(true);


    System.out.println("****************PROCESSING FINISHED******************");

    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;
    long doGetTime = endTime - getStartTime;

    System.out.println("********************************************");
    System.out.println("Post report");
    System.out.println("Num of successful req :" + successReq.get());
    System.out.println("Num of unsuccessful req :" + failReq.get());
    System.out.println("Time of Process :" + wallTime);
    System.out.println( "Actual Throughput: " + (int)((successReq.get() + failReq.get()) / (wallTime / 1000.0)) + " requests/second");

    System.out.println("DoPost Mean response time :" + doPostInfo.meanResponseTime());
    System.out.println("DoPost Median response time :" + doPostInfo.medianResponseTime());
    System.out.println("DoPost Minimum response time :" + doPostInfo.minResponseTime());
    System.out.println("DoPost Maximum response time :" + doPostInfo.maxResponseTime());


    System.out.println("********************************************");
    System.out.println("Get report");
    System.out.println("DoGet Mean response time :" + doGetInfo.meanResponseTime());
    System.out.println("DoGet Median response time :" + doGetInfo.medianResponseTime());
    System.out.println("DoGet Minimum response time :" + doGetInfo.minResponseTime());
    System.out.println("DoGet Maximum response time :" + doGetInfo.maxResponseTime());
  }
}
