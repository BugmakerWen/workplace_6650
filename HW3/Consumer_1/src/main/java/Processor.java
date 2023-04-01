import Model.*;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;

public class Processor {
  private static int numOfThread = 1;

  public static void main(String[] args) {
    AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
    System.setProperty("aws.accessKeyId", "ASIAQKTZF4LXNGJ2YRNI");
    System.setProperty("aws.secretAccessKey", "kPEj9DDHo4VDUi50xNoxM5M8ttexEbAk9n+0kWj5");
    System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEM7//////////wEaDHfmdITEF6I1GqHw9yLKAWZbOJUJXIOXQ6kLIF+jrFSrKc4n9YsjBetqb/jgWHh/i93DBb7t/WbS8/8OPsv2piiX3a7jvwStue+kAeFrwasY9lmYWk6HGAIMDKmR/Vv901IulpVhPKCSVLBKLlWK4igFanP+mEWYPkQogOL6RH4cLRjJ9MoQQe6rhz1OP7lJN0Oc2MfvWZj65SvG5M8dMDly2pvBQbRebja/AOef53YK0hcL1rYS3WFjbL7SoKnZbIDRJJCsSgsr4FxPHslOMOKKufdfrn8W3w8oo5KdoQYyLUNGK9UaNoVgVAVdLyvLaMTEpylRs3uDVMhbsbPIw66fHEqylX6NMr53XinOXg==");

    DynamoDbClient client = DynamoDbClient.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.US_WEST_2)
        .build();

    UsersMatchDAO usersMatchDAO = new UsersMatchDAO(client);
    usersMatchDAO.creatTable();

    for (int i = 0; i < numOfThread; i++) {
      Consumer consumer1 = new Consumer(usersMatchDAO);
      Thread thread = new Thread(consumer1);
      thread.start();
    }
  }
}
