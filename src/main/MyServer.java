package main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.application.Platform;

import java.net.InetSocketAddress;

public class MyServer {
  private MainController mainController;
  private ChannelFuture channelFuture;
  private SocketChannel socketChannel;

  EventLoopGroup nioEventLoopGroup;

  public MyServer(MainController mainController) {
    this.mainController = mainController;
  }

  public void start() throws Exception {
    int port = mainController.getPort();

    if (channelFuture != null && channelFuture.channel().isOpen()) {
      mainController.log("already listening on " + channelFuture.channel().localAddress().toString());
      return;
    }

    final MyServerHandler myServerHandler = new MyServerHandler(mainController);

    nioEventLoopGroup = new NioEventLoopGroup();

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
      .group(nioEventLoopGroup)
      .channel(NioServerSocketChannel.class)
      .localAddress(new InetSocketAddress(port))
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
          MyServer.this.socketChannel = socketChannel;
          socketChannel.pipeline().addLast(myServerHandler);
          mainController.log("connected->" + socketChannel.remoteAddress().getAddress() + ":" +
            socketChannel.remoteAddress().getPort());
        }
      });

    channelFuture = serverBootstrap.bind();
    channelFuture.addListener(channelFuture -> {
      Platform.runLater(() -> {
        MyServer.this.mainController.log("server is listening on port " + port);
        MyServer.this.mainController.txtPort.setEditable(false);
      });
    });
  }

  public void stop() throws InterruptedException {
    ByteBuf closeMsg = Unpooled.wrappedBuffer("closed".getBytes());
    socketChannel.writeAndFlush(closeMsg);

    nioEventLoopGroup
      .shutdownGracefully()
      .addListener(channelFuture -> {
        Platform.runLater(() -> {
            MyServer.this.mainController.log("server shutdown");
            MyServer.this.mainController.txtPort.setEditable(true);
          });
      });
  }

  public void send(String msg) {
    if (socketChannel == null || !socketChannel.isOpen()) {
      mainController.log("channel is not open!");
    } else {
      ByteBuf sendMsg = Unpooled.wrappedBuffer(msg.getBytes());
      socketChannel.writeAndFlush(sendMsg);
    }
  }
}
