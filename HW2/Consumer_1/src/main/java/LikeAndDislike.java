public class LikeAndDislike {
  int like;
  int dislike;

  public LikeAndDislike() {
    this.like = 0;
    this.dislike = 0;
  }

  public int getLike() {
    return like;
  }

  public void incrementLike() {
    this.like++;
  }

  public int getDislike() {
    return dislike;
  }

  public void incrementDislike() {
    this.dislike++;
  }
}
