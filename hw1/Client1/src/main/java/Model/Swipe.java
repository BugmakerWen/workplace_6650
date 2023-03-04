package Model;

import io.swagger.client.model.SwipeDetails;

public class Swipe {
  private String leftOrRight;
  private SwipeDetails body;

  public Swipe(String leftOrRight, SwipeDetails body) {
    this.leftOrRight = leftOrRight;
    this.body = body;
  }

  public Swipe() {

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
