package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Information {
  // start time, latency, type, code
  private List<Integer> latencies;

  public Information(List<Integer> latencies) {
    this.latencies = latencies;
    Collections.sort(this.latencies);
  }

  public void setLatencies(List<Integer> latencies) {
    this.latencies = latencies;
  }

  public List<Integer> getLatencies() {
    return latencies;
  }

  public int meanResponseTime() {
    int totalLatency = 0;

    for (int latency : this.latencies)
      totalLatency += latency;

    return totalLatency / this.latencies.size();
  }

  public int medianResponseTime() {
    int sizeOfLatencies = this.latencies.size();
    if (sizeOfLatencies % 2 == 0) {
      return (this.latencies.get((sizeOfLatencies + 1) / 2) + this.latencies.get(sizeOfLatencies / 2)) / 2;
    } else {
      return this.latencies.get(sizeOfLatencies / 2);
    }
  }

  public int p99thTime() {
    int totalNumber = latencies.size();
    return this.latencies.get((int)(totalNumber * 0.01));
  }

  public int minResponseTime() {
    return this.latencies.get(this.latencies.size() - 1);
  }

  public int maxResponseTime() {
    return this.latencies.get(0);
  }
}
