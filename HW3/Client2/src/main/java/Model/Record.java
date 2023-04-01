package Model;

public class Record {
  private long startTime;
  private long endTime;
  private String response;
  private int responseCode;

  public Record(long startTime, long endTime, String response, int responseCode) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.response = response;
    this.responseCode = responseCode;
  }
}
