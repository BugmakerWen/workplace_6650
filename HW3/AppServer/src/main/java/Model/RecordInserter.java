package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

public class RecordInserter {
  private DynamoDbClient dbClient;
  private final String columnNameOneNumOfLikes = "numOfLikes";
  private final String columnNameTwoNumOfDislikes = "numOfDislikes";
  private final String columnNameThreeListOfMatches = "PotentialMatches";
  private final String tableName = "UsersMatch";
  private final String tableKey = "SwiperID";
  private final String StringONE = "1";
  private final String StringZERO = "0";
  private HashMap<String, AttributeValue> item;
  private List<AttributeValue> attributeValues;

  public RecordInserter(DynamoDbClient dbClient) {
    this.dbClient = dbClient;
  }

  public void putDataToUserTable(String[] record) {
    this.attributeValues = new ArrayList();
    this.item = new HashMap();
    item.put(tableKey, AttributeValue.builder().s(record[1]).build());

    if (UserLikes(record[0])) {
      insertLike(record);
    } else {
      insertDislike();
    }

    PutItemRequest request = insertMatches();

    updateResponse(request);
  }

  private boolean UserLikes(String likeOrDislike) {
    return likeOrDislike.equals("right");
  }

  private void insertLike(String[] record) {
    item.put(columnNameOneNumOfLikes, AttributeValue.builder().n(StringONE).build());
    item.put(columnNameTwoNumOfDislikes, AttributeValue.builder().n(StringZERO).build());
    AttributeValue attributeValue = AttributeValue.builder().s(record[2]).build();
    attributeValues.add(attributeValue);
  }

  private void insertDislike() {
    item.put("numOfLikes", AttributeValue.builder().n("0").build());
    item.put("numOfDislikes", AttributeValue.builder().n("1").build());
  }

  private PutItemRequest insertMatches() {
    item.put(columnNameThreeListOfMatches, AttributeValue.builder().l(attributeValues).build());
    return PutItemRequest.builder().tableName(tableName).item(item).build();
  }
  private void updateResponse(PutItemRequest request) {
    try {
      PutItemResponse response = this.dbClient.putItem(request);
      System.out.println("Users was successfully updated. The request id is " + response.responseMetadata().requestId());
    } catch (ResourceNotFoundException var6) {
      System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", "Users");
      System.err.println("Be sure that it exists and that you've typed its name correctly!");
      System.exit(1);
    } catch (DynamoDbException var7) {
      System.err.println(var7.getMessage());
      System.exit(1);
    }
  }
}
