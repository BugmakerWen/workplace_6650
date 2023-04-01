package Model;

import java.util.ArrayList;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class TableFactory {
  private DynamoDbClient dbClient;
  private final String tableName;

  public TableFactory(DynamoDbClient dbClient, String tableName) {
    this.dbClient = dbClient;
    this.tableName = tableName;
  }

  public void CreatUsersMatchTable() {
    initDynamoDBClient();
    ArrayList keySchema = DefineDBKey();
    ArrayList attributeDefinitions = DefineAttributes();
    CreateTableRequest createTableRequest = CreateTableReq(keySchema, attributeDefinitions);

    try {
      CreateTableResponse createTableResponse = dbClient.createTable(createTableRequest);
      System.out.println("Table created successfully: " + createTableResponse.tableDescription().tableName());
    } catch (DynamoDbException e) {
      System.err.println("Error occurred while creating table: " + e.getMessage());
    }
  }

  private void initDynamoDBClient() {
    dbClient = DynamoDbClient.builder()
        .region(Region.US_WEST_2)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  private ArrayList DefineDBKey() {
    ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
    keySchema.add(KeySchemaElement.builder()
        .attributeName("SwiperID")
        .keyType(KeyType.HASH)
        .build());

    return keySchema;
  }

  private ArrayList DefineAttributes() {
    ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(AttributeDefinition.builder()
        .attributeName("SwiperID")
        .attributeType(ScalarAttributeType.S)
        .build());

    return attributeDefinitions;
  }

  private CreateTableRequest CreateTableReq(ArrayList keySchema, ArrayList attributeDefinitions) {
    CreateTableRequest createTableRequest = CreateTableRequest.builder()
        .tableName(tableName)
        .keySchema(keySchema)
        .attributeDefinitions(attributeDefinitions)
        .billingMode(BillingMode.PAY_PER_REQUEST)
        .build();

    return createTableRequest;
  }
}
