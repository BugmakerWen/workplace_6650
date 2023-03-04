public class Processor {
  private static int numOfThread = 100;

  public static void main(String[] args) {
    for (int i = 0; i < numOfThread; i++) {
      Consumer consumer = new Consumer();
      Thread thread = new Thread(consumer);
      thread.start();
    }
  }
}
