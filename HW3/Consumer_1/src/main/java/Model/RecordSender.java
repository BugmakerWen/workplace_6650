package Model;

import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

public class RecordSender {
  private DynamoDbClient dbClient;
  private final String tableName;
  private GetItemResponse getItemResponse;

  public RecordSender(DynamoDbClient dbClient, String tableName) {
    this.dbClient = dbClient;
    this.tableName = tableName;
  }

  public void sendRecordToUsersMatchTable (String record) {
    UserMatchRecord processedRecord = new UserMatchRecord(record);
    sendTheRecord(processedRecord);
  }

  private void sendTheRecord(UserMatchRecord processedRecord) {
    if (checkIfRecordExists((processedRecord))) {
      new TableUpdateProcessor(this.dbClient).updateRecordInTable(processedRecord.getMessage(), getItemResponse.item());
    } else {
      new RecordInserter(dbClient).putDataToUserTable(processedRecord.getMessage());
    }
  }
  private boolean checkIfRecordExists(UserMatchRecord processedRecord) {
    GetItemRequest getItemRequest = GetItemRequest.builder()
        .tableName(tableName)
        .key(
            Map.of(
                "SwiperID", processedRecord.getAttributeValue()
            )
        )
        .build();

    this.getItemResponse = dbClient.getItem(getItemRequest);

    return getItemResponse.hasItem();
  }
}
