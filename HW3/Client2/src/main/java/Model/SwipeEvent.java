package Model;

import io.swagger.client.model.SwipeDetails;

public class SwipeEvent {
  private String leftOrRight;
  private SwipeDetails body;

  public SwipeEvent(String leftOrRight, SwipeDetails body) {
    this.leftOrRight = leftOrRight;
    this.body = body;
  }

  public SwipeEvent() {

  }

  public String getLeftOrRight() {
    return leftOrRight;
  }

  public SwipeDetails getBody() {
    return body;
  }

  public void setLeftOrRight(String leftOrRight) {
    this.leftOrRight = leftOrRight;
  }

  public void setBody(SwipeDetails body) {
    this.body = body;
  }
}
