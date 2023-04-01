package Model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class UserMatchRecord {
  private String UserID;
  private AttributeValue attributeValue;
  private String[] message;

  public UserMatchRecord(String record) {
    this.message = record.split("/");
    this.UserID = this.message[1];
    this.attributeValue = AttributeValue.builder().s(this.UserID).build();
  }

  public String getUserID() {
    return UserID;
  }

  public AttributeValue getAttributeValue() {
    return attributeValue;
  }

  public String[] getMessage() {
    return message;
  }
}
