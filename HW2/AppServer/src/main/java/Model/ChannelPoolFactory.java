package Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

public class ChannelPoolFactory extends BasePooledObjectFactory<Channel>{

  private final Connection connection;

  public ChannelPoolFactory(Connection connection) {
    this.connection = connection;
  }

  @Override
  synchronized public Channel create() throws IOException {
    Channel channel = connection.createChannel();
    return channel;
  }

  @Override
  public PooledObject<Channel> wrap(Channel chan) {
    return new DefaultPooledObject(chan);
  }
}
