package Model;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Consumer implements Runnable {

  private static final String QUEUE_NAME = "Queue1";
  private UsersMatchDAO usersMatchDAO;

  public Consumer(UsersMatchDAO usersMatchDAO) {
    this.usersMatchDAO = usersMatchDAO;
  }

  @Override
  public void run() {
    // ***************pull out message from RMQ
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("52.43.17.191");
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

    try {
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    } catch (IOException e) {
      e.printStackTrace();
    }

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      usersMatchDAO.sendMessageToDB(message);
      System.out.println(" [x] Received '" + message + "'");
    };

    try {
      channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
