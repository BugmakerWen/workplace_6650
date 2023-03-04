import Model.ChannelPoolFactory;
import Model.InfoOfMessage;
import Model.MessageToChannel;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "AssignmentServlet", value = "/AssignmentServlet")
public class AssignmentServlet extends HttpServlet {

  private Connection connection;
  private ChannelPoolFactory channelPoolFactory;
  private GenericObjectPool<Channel> pool;

  @Override
  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("52.43.19.197");
    try {
      this.connection = factory.newConnection();
      this.channelPoolFactory = new ChannelPoolFactory(connection);
      this.pool = new GenericObjectPool<Channel>(this.channelPoolFactory);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    PrintWriter writer = res.getWriter();

    //check if the URL is empty
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Missing parameter!");
      writer.close();
      return;
    }

    // check if the URL is valid
    String[] urlParts = urlPath.split("/");

    //valid url
    if(validUrl(urlParts)) {
      if(urlParts[1].equals("left") || urlParts[1].equals("right")){
        InfoOfMessage input = processRequest(req, res);
        MessageToChannel messageToChannel = new MessageToChannel(urlParts[1], input);
        Channel channel = null;

        try {
          // set channel
          channel = this.pool.borrowObject();
          channel.queueDeclare("Queue1", false, false, false, null);
          channel.basicPublish("", "Queue1", null, messageToChannel.getMessageToChannel().getBytes());
          channel.queueDeclare("Queue2", false, false, false, null);
          channel.basicPublish("", "Queue2", null, messageToChannel.getMessageToChannel().getBytes());
          System.out.println(" [x] Sent '" + messageToChannel + "'");
          // set response status
          res.setStatus(HttpServletResponse.SC_CREATED);
          writer.write(input.getComment());
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            pool.returnObject(channel);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }else {
        // invalid request
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("Invalid request");
      }
    }else {
      // invalid url
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Invalid request");
      writer.close();
      return;
    }


  }

  private boolean validUrl(String[] urlParts) {
    if(urlParts.length == 2 && urlParts[0].equals(""))
      return true;
    return false;
  }

  protected InfoOfMessage processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Gson gson = new Gson();
    InfoOfMessage reqBody = null;

    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      reqBody = (InfoOfMessage) gson.fromJson(sb.toString(), InfoOfMessage.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return reqBody;
  }

}
