package Model;
import io.swagger.client.api.SwipeApi;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.swagger.client.*;

public class Processor implements Runnable{
  private String urlBase;
  private AtomicInteger successReq;
  private AtomicInteger failReq;
  private int totalReq;
  private BlockingQueue<SwipeEvent> events;
  private CountDownLatch countDownLatch;
  public List<String[]> records;
  public Information info;

  public Processor(String urlBase, AtomicInteger successReq,
      AtomicInteger failReq, int totalReq,
      BlockingQueue<SwipeEvent> events, CountDownLatch countDownLatch,
      List<String[]> records, Information info) {
    this.urlBase = urlBase;
    this.successReq = successReq;
    this.failReq = failReq;
    this.totalReq = totalReq;
    this.events = events;
    this.countDownLatch = countDownLatch;
    this.records = records;
    this.info = info;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    SwipeApi swipeApi = new SwipeApi(apiClient);
    swipeApi.getApiClient().setBasePath(this.urlBase);
    int countOfSuccess = 0;
    int countOfFail = 0;

    for (int i = 0; i < this.totalReq; i++) {
      SwipeEvent currEvent = this.events.poll();
      if (doSwipe(swipeApi, currEvent)) {
        countOfSuccess += 1;
      } else {
        countOfFail += 1;
      }
    }

    successReq.getAndAdd(countOfSuccess);
    failReq.getAndAdd(countOfFail);
    countDownLatch.countDown();
  }

  private boolean doSwipe(SwipeApi swipeApi, SwipeEvent event) {
    int tryTime = 0;
    while (tryTime < 5) {
      try {
        long startTime = System.currentTimeMillis();
        ApiResponse<Void> resp = swipeApi.swipeWithHttpInfo(event.getBody(), event.getLeftOrRight());
        if (resp.getStatusCode() == 201) {
          long endTime = System.currentTimeMillis();
          System.out.println(endTime - startTime);
          this.info.getLatencies().add((int)(endTime - startTime));
          String[] record = new String[4];
          record[0] = String.valueOf(startTime);
          record[1] = String.valueOf(endTime - startTime);
          record[2] = "POST";
          record[3] = "201";
          records.add(record);
          return true;
        }
      } catch (ApiException e) {
        tryTime++;
        e.printStackTrace();
      }
    }
    return false;
  }
}
