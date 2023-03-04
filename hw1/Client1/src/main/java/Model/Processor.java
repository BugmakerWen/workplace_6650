package Model;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.swagger.client.*;

public class Processor implements Runnable{
  private String urlBase;
  private AtomicInteger successReq;
  private AtomicInteger failReq;
  private int totalReq;
  private BlockingQueue<Swipe> events;
  private CountDownLatch countDownLatch;

  public Processor(String urlBase,
      AtomicInteger successReq, AtomicInteger failReq, int totalReq,
      BlockingDeque<Swipe> events, CountDownLatch countDownLatch) {
    this.urlBase = urlBase;
    this.successReq = successReq;
    this.failReq = failReq;
    this.totalReq = totalReq;
    this.events = events;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    SwipeApi swipeApi = new SwipeApi(apiClient);
    swipeApi.getApiClient().setBasePath(this.urlBase);
    int countOfSuccess = 0;
    int countOfFail = 0;

    for (int i = 0; i < this.totalReq; i++) {
      Swipe currEvent = this.events.poll();
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

  private boolean doSwipe(SwipeApi swipeApi, Swipe event) {
    int tryTime = 0;
    while (tryTime < 5) {
      try {
        long startTime = System.currentTimeMillis();
        ApiResponse<Void> resp = swipeApi.swipeWithHttpInfo(event.getBody(), event.getLeftOrRight());
        if (resp.getStatusCode() == 201) {
          long endTime = System.currentTimeMillis();
          System.out.println(endTime - startTime);
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
