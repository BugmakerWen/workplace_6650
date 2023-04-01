package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DoGetProcessor implements Runnable{
  private String urlBase;
  private AtomicBoolean threadsFinished;
  private List<Record> getRecords;
  private MatchesApi matchesApi;
  private StatsApi statsApi;
  private ScheduledExecutorService executor;
  private Information doGetInfo;
  private final int usersPerSecond = 5;
  private static final int userIDStart = 1;
  private static final int userIDEnd = 5000;

  public DoGetProcessor(String urlBase, AtomicBoolean threadsFinished, List<Record> getRecords,
      Information doGetInfo) {
    this.urlBase = urlBase;
    this.threadsFinished = threadsFinished;
    this.getRecords = getRecords;
    this.doGetInfo = doGetInfo;
  }

  @Override
  public void run() {
    setUpClient();
    setUpScheduledExecutor();
    Runnable task = () -> {
      doProcessor();
    };
    executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
  }

  private void setUpClient() {
    ApiClient apiClient = new ApiClient();
    this.matchesApi = new MatchesApi(apiClient);
    this.matchesApi.getApiClient().setBasePath(this.urlBase);
    this.statsApi = new StatsApi(apiClient);
    this.statsApi.getApiClient().setBasePath(this.urlBase);
  }

  private void setUpScheduledExecutor() {
    this.executor = Executors.newSingleThreadScheduledExecutor();
  }

  private void doProcessor() {
    if (threadsFinished.get()) {
      executor.shutdownNow();
      return;
    }
    List<String> randomUsers = generateRandomUsers(usersPerSecond);
    for (String userID : randomUsers) {
      Random random = new Random();
      int randomInt = random.nextInt(2);
      if (randomInt == 0) {
        doMatch(matchesApi, userID);
      } else {
        doStats(statsApi, userID);
      }
    }
  }
  private List<String> generateRandomUsers(int usersInUnitTime) {
    List<String> users = new ArrayList<>();
    for(int i = 0; i < usersInUnitTime; i++) {
      Integer user = ThreadLocalRandom.current().nextInt(userIDStart, userIDEnd + 1);
      users.add(String.valueOf(user));
    }
    return users;
  }

  private boolean doMatch(MatchesApi matchesApi, String userID) {
    long start = System.currentTimeMillis();
    try {
      ApiResponse<Matches> response = matchesApi.matchesWithHttpInfo(userID);
      long end = System.currentTimeMillis();
      doGetInfo.getLatencies().add((int)(end - start));
      getRecords.add(new Record(start, end, "GET", 201));
      Matches matches = response.getData();
      System.out.println(matches.toString());
      return true;
    }catch (ApiException e) {
      long end = System.currentTimeMillis();
      getRecords.add(new Record(start, end, "GET", 404));
      System.out.println("The user " + userID + " is not found!");
      return false;
    }
  }

  private boolean doStats(StatsApi statsApi, String userID) {
    long start = System.currentTimeMillis();
    try {
      ApiResponse<MatchStats> response = statsApi.matchStatsWithHttpInfo(userID);
      long end = System.currentTimeMillis();
      doGetInfo.getLatencies().add((int)(end - start));
      getRecords.add(new Record(start, end, "GET", 201));
      MatchStats matchStats = response.getData();
      System.out.println(matchStats.toString());
      return true;
    }catch (ApiException e) {
      long end = System.currentTimeMillis();
      getRecords.add(new Record(start, end, "GET", 404));
      System.out.println("The user " + userID + " is not found!");
      return false;
    }
  }

}
