package Model;
import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;

public class Generator implements Runnable{
  private static final int swiperUserIdStart = 1;
  private static final int swiperUserIdEnd = 5000;
  private static final int swipeeIdStart = 1;
  private static final int swipeeIdEnd = 1000000;
  private static final int commentLength = 256;
  private static final int Magic_ZERO = 0;
  private static final int Magic_ONE = 1;
  private static final String stopID = "STOP";

  private BlockingDeque<Swipe> queue;
  private int numOfEvents;

  public Generator(BlockingDeque<Swipe> queue, int numOfEvents) {
    this.queue = queue;
    this.numOfEvents = numOfEvents;
  }

  @Override
  public void run() {
    for (int i = 0; i < this.numOfEvents; i++) {
      Integer swiperID = ThreadLocalRandom.current().nextInt(swiperUserIdStart, swiperUserIdEnd);
      Integer swipeeID = ThreadLocalRandom.current().nextInt(swipeeIdStart, swipeeIdEnd);
      Integer leftOrRight = ThreadLocalRandom.current().nextInt(Magic_ZERO, Magic_ONE + Magic_ONE);
      String comment = RandomStringUtils.random(commentLength, true, false);

      SwipeDetails detail = new SwipeDetails();
      detail.setSwiper(swiperID.toString());
      detail.setSwipee(swipeeID.toString());
      detail.setComment(comment);
      Swipe swipe = new Swipe(leftOrRight == 0 ? "left" : "right", detail);
      this.queue.offer(swipe);
    }
  }
}
