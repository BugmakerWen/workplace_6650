import Model.ChannelPoolFactory;
import Model.InfoOfMessage;
import Model.MessageToChannel;
import Model.UsersMatchDAO;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@WebServlet(name = "AssignmentServlet", value = "/AssignmentServlet")
public class AssignmentServlet extends HttpServlet {

  private Connection connection;
  private ChannelPoolFactory channelPoolFactory;
  private GenericObjectPool<Channel> pool;
  private UsersMatchDAO usersMatchDAO;

  @Override
  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("52.43.17.191");
    try {
      this.connection = factory.newConnection();
      this.channelPoolFactory = new ChannelPoolFactory(connection);
      this.pool = new GenericObjectPool<Channel>(this.channelPoolFactory);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

    AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
    System.setProperty("aws.accessKeyId", "ASIAQKTZF4LXNGJ2YRNI");
    System.setProperty("aws.secretAccessKey", "kPEj9DDHo4VDUi50xNoxM5M8ttexEbAk9n+0kWj5");
    System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEM7//////////wEaDHfmdITEF6I1GqHw9yLKAWZbOJUJXIOXQ6kLIF+jrFSrKc4n9YsjBetqb/jgWHh/i93DBb7t/WbS8/8OPsv2piiX3a7jvwStue+kAeFrwasY9lmYWk6HGAIMDKmR/Vv901IulpVhPKCSVLBKLlWK4igFanP+mEWYPkQogOL6RH4cLRjJ9MoQQe6rhz1OP7lJN0Oc2MfvWZj65SvG5M8dMDly2pvBQbRebja/AOef53YK0hcL1rYS3WFjbL7SoKnZbIDRJJCsSgsr4FxPHslOMOKKufdfrn8W3w8oo5KdoQYyLUNGK9UaNoVgVAVdLyvLaMTEpylRs3uDVMhbsbPIw66fHEqylX6NMr53XinOXg==");

    DynamoDbClient client = DynamoDbClient.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.US_WEST_2)
        .build();

    usersMatchDAO = new UsersMatchDAO(client);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    String[] urlParts = this.parseURL(urlPath, res);
    if (urlParts.length == 3 && urlParts[1].equals("matches")) {
      this.handleMatchesRequest(res, urlParts);
    } else if (urlParts.length == 3 && urlParts[1].equals("stats")) {
      this.handleStatsRequest(res, urlParts);
    } else {
      PrintWriter writer = res.getWriter();
      res.setStatus(400);
      writer.write("Invalid request");
      writer.close();
    }
  }

  private String[] parseURL(String urlPath, HttpServletResponse res) throws IOException {
    PrintWriter writer = res.getWriter();
    if (urlPath != null && !urlPath.isEmpty()) {
      return urlPath.split("/");
    } else {
      res.setStatus(400);
      writer.write("Invalid request");
      writer.close();
      return null;
    }
  }

  private void handleStatsRequest(HttpServletResponse res, String[] urlParts) throws IOException {
    PrintWriter writer = res.getWriter();
    Integer id = Integer.parseInt(urlParts[2]);
    if (id <= 0 || id > 5000) {
      res.setStatus(404);
      writer.write("User not found!");
    }

    res.setStatus(201);
    List<Integer> stats = this.usersMatchDAO.getStats(String.valueOf(id));
    MatchStats matchStats = new MatchStats();
    matchStats.setNumLlikes((Integer)stats.get(0));
    matchStats.setNumDislikes((Integer)stats.get(1));
    Gson gson = new Gson();
    String jsonStr = gson.toJson(matchStats);
    writer.write(jsonStr);
    System.out.println(jsonStr);
  }

  private void handleMatchesRequest(HttpServletResponse res, String[] urlParts) throws IOException {
    PrintWriter writer = res.getWriter();
    Integer id = Integer.parseInt(urlParts[2]);
    if (id <= 0 || id > 5000) {
      res.setStatus(404);
      writer.write("User not found!");
    }

    res.setStatus(201);
    List<String> topLikes = this.usersMatchDAO.getMatches(String.valueOf(id));
    Matches matches = new Matches();
    matches.setMatchList(topLikes);
    Gson gson = new Gson();
    String jsonStr = gson.toJson(matches);
    writer.write(jsonStr);
    System.out.println(jsonStr);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    PrintWriter writer = res.getWriter();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Missing parameter!");
      writer.close();
      return;
    }

    String[] urlParts = urlPath.split("/");

    if(validDoPostUrl(urlParts)) {
      if(urlParts[2].equals("left") || urlParts[2].equals("right")){
        InfoOfMessage input = processRequest(req, res);
        MessageToChannel messageToChannel = new MessageToChannel(urlParts[2], input);
        Channel channel = null;
        try {
          channel = this.pool.borrowObject();
          channel.queueDeclare("Queue1", false, false, false, null);
          channel.basicPublish("", "Queue1", null, messageToChannel.getMessageToChannel().getBytes());
          System.out.println(" [x] Sent '" + messageToChannel.getMessageToChannel() + "'");
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
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("Invalid request");
      }
    }else {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Invalid request");
      writer.close();
    }
  }

  private boolean validDoPostUrl(String[] urlParts) {
    if(urlParts.length == 3 && urlParts[0].equals("") && urlParts[1].equals("swipe"))
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
