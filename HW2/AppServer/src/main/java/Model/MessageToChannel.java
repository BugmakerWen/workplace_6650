package Model;

public class MessageToChannel {
  private String leftOrRight;
  public InfoOfMessage info;

  public MessageToChannel(String leftOrRight, InfoOfMessage info) {
    this.leftOrRight = leftOrRight;
    this.info = info;
  }

  public String getMessageToChannel() {
    return leftOrRight + "/" +  info.getSwiper()+
        "/" + info.getSwipee() + "/" + info.getComment();
  }
}
