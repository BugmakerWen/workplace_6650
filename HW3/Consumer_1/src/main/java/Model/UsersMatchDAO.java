package Model;

import java.util.List;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
public class UsersMatchDAO {
  private DynamoDbClient dbClient;
  private final String tableName = "UsersMatch";
  public UsersMatchDAO(DynamoDbClient dbClient) {
    this.dbClient = dbClient;
  }
  public void creatTable() {
    TableFactory tableFactory = new TableFactory(dbClient, tableName);
    tableFactory.CreatUsersMatchTable();
  }
  public void sendMessageToDB(String record) {
    RecordSender recordSender = new RecordSender(dbClient, tableName);
    recordSender.sendRecordToUsersMatchTable(record);
  }

  public List<Integer> getStats(String userID) {
    DBResponse dbResponse = new DBResponse(dbClient);
    return dbResponse.getStats(userID);
  }

  public List<String> getMatches(String userID) {
    DBResponse dbResponse = new DBResponse(dbClient);
    return dbResponse.getListOfMatches(userID);
  }
}
