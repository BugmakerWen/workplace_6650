import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Consumer_2 implements Runnable {

  private static final String QUEUE_NAME = "Queue2";
  public Map<String, listOfPotentialMatches> potentialMatches = new HashMap<>();

  @Override
  public void run() {
    // ***************pull out message from RMQ
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("52.43.19.197");
    Connection connection = null;
    Channel channel = null;

    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
      System.out.println("Waiting for messages. To exit press CTRL+C");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + message + "'");
      String[] stringArray = message.split("/");
      String swiper = stringArray[1];
      String swipee = stringArray[2];

      if (!potentialMatches.containsKey(swiper)) {
        potentialMatches.put(swiper, new listOfPotentialMatches());
      }

      if (stringArray[0] == "right") {
        potentialMatches.get(swiper).addNewMatch(swipee);
      }
    };

    try {
      channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
