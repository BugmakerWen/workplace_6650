import java.util.LinkedList;
import java.util.Queue;

public class listOfPotentialMatches {
  private Queue<String> list = new LinkedList<>();
  private int capacity = 100;

  public listOfPotentialMatches() {
  }

  public void addNewMatch(String swpiee) {
    if (list.size() == capacity) {
      list.remove();
    }
    list.add(swpiee);
  }
}
