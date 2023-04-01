package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class DBResponse {
  private DynamoDbClient dbClient;
  private final String columnNameOneNumOfLikes = "numOfLikes";
  private final String columnNameTwoNumOfDislikes = "numOfDislikes";
  private final String columnNameThreeListOfMatches = "PotentialMatches";
  private final String tableKey = "SwiperID";
  private final String tableName = "UsersMatch";
  public DBResponse(DynamoDbClient dbClient) {
    this.dbClient = dbClient;
  }
  public List<Integer> getStats(String userID) {
    List<Integer> stats = new ArrayList();
    QueryResponse response = this.getResponse(userID);
    stats.add(this.getLikesOfUser(response));
    stats.add(this.getDislikesOfUser(response));
    return stats;
  }

  public int getLikesOfUser(QueryResponse response) {
    return Integer.parseInt(((AttributeValue)((Map)response.items().get(0)).get(columnNameOneNumOfLikes)).n());
  }

  public int getDislikesOfUser(QueryResponse response) {
    return Integer.parseInt(((AttributeValue)((Map)response.items().get(0)).get(columnNameTwoNumOfDislikes)).n());
  }

  public List<String> getListOfMatches(String userID) {
    QueryResponse response = this.getResponse(userID);
    ArrayList<AttributeValue> responseList = new ArrayList(((AttributeValue)((Map)response.items().get(0)).get(columnNameThreeListOfMatches)).l());
    List<String> result = responseList.stream().map(AttributeValue::s).collect(Collectors.toList());
    return result;
  }

  public QueryResponse getResponse(String userID) {
    HashMap<String, String> attrNameAlias = new HashMap();
    String partitionAlias = "#IronManBest";
    attrNameAlias.put(partitionAlias, tableKey);
    HashMap<String, AttributeValue> attrValues = new HashMap();
    attrValues.put(":SwiperID", AttributeValue.builder().s(userID).build());
    QueryRequest queryReq = QueryRequest.builder().tableName(tableName).keyConditionExpression(partitionAlias + " = :SwiperID").expressionAttributeNames(attrNameAlias).expressionAttributeValues(attrValues).build();

    try {
      QueryResponse response = this.dbClient.query(queryReq);
      return response;
    } catch (DynamoDbException var7) {
      System.err.println(var7.getMessage());
      System.exit(1);
      return null;
    }
  }
}
