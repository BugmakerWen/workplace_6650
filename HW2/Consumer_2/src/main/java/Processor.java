public class Processor {
  private static int numOfThread = 100;

  public static void main(String[] args) {
    for (int i = 0; i < numOfThread; i++) {
      Consumer_2 consumer = new Consumer_2();
      Thread thread = new Thread(consumer);
      thread.start();
    }
  }
}
