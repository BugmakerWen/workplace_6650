package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public class TableUpdateProcessor {
  private final String columnNameThreeListOfMatches = "PotentialMatches";
  private final String columnNameOneNumOfLikes = "numOfLikes";
  private final String columnNameTwoNumOfDislikes = "numOfDislikes";
  private final int MAXLIKES = 100;
  private final String tableName = "UsersMatch";
  private final String tableKey = "SwiperID";
  private DynamoDbClient dbClient;

  public TableUpdateProcessor(DynamoDbClient dbClient) {
    this.dbClient = dbClient;
  }

  public void updateRecordInTable(String[] message, Map<String, AttributeValue> item) {
    int currNumOfLike = setLikeOrDislikeNumber("like", item);
    int currNumOfDislike = setLikeOrDislikeNumber("dislike", item);
    List<AttributeValue> listOfMatch = new ArrayList<>(item.get(columnNameThreeListOfMatches).l());

    if (UserLikes(message[0])) {
      currNumOfLike = updateLike(message[2], currNumOfLike, listOfMatch);
    } else {
      currNumOfDislike = currNumOfDislike + 1;
    }

    HashMap<String,AttributeValue> itemKey = setKeyValue(message[1]);

    HashMap<String, AttributeValueUpdate> updatedAttribute = updateAttribute(currNumOfLike, currNumOfDislike, listOfMatch);

    UpdateItemRequest requestForStats = setUpdateRequest(itemKey, updatedAttribute);

    updateTable(requestForStats);
  }

  private int setLikeOrDislikeNumber(String likeOrDislike, Map<String,AttributeValue> item) {
    if (likeOrDislike.equals("like")) {
      return Integer.valueOf(item.get(columnNameOneNumOfLikes).n());
    } else {
      return Integer.valueOf(item.get(columnNameTwoNumOfDislikes).n());
    }
  }
  private boolean UserLikes(String likeOrDislike) {
    return likeOrDislike.equals("right");
  }

  private int updateLike(String nameOfSwipe, Integer numOfLike, List<AttributeValue> listOfMatch) {
    if (numOfLike == MAXLIKES) {
      listOfMatch.remove(0);
      numOfLike--;
    }

    AttributeValue attributeValue = AttributeValue.builder().s(nameOfSwipe).build();
    listOfMatch.add(attributeValue);

    return numOfLike + 1;
  }

  private HashMap setKeyValue(String ID) {
    HashMap<String,AttributeValue> key = new HashMap<>();
    key.put(tableKey, AttributeValue.builder().s(ID).build());
    return key;
  }

  private HashMap updateAttribute(int numOfLike, int nunOfDislike, List<AttributeValue> listOfMatch) {
    HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
    updatedValues.put(columnNameOneNumOfLikes, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().n(String.valueOf(numOfLike)).build())
        .action(AttributeAction.PUT)
        .build());
    updatedValues.put(columnNameTwoNumOfDislikes, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().n(String.valueOf(nunOfDislike)).build())
        .action(AttributeAction.PUT)
        .build());
    updatedValues.put(columnNameThreeListOfMatches, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().l(listOfMatch).build())
        .action(AttributeAction.PUT)
        .build());

    return updatedValues;
  }

  private UpdateItemRequest setUpdateRequest(HashMap<String,AttributeValue> itemKey, HashMap<String, AttributeValueUpdate> updatedAttribute) {
    UpdateItemRequest requestForStats = UpdateItemRequest.builder()
        .tableName(tableName)
        .key(itemKey)
        .attributeUpdates(updatedAttribute)
        .build();

    return requestForStats;
  }
  private void updateTable(UpdateItemRequest requestForStats) {
    try {
      dbClient.updateItem(requestForStats);
    } catch (ResourceNotFoundException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.out.println("The Amazon DynamoDB table was updated!");
  }
}
