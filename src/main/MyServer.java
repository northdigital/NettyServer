package main;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

class MyServer {
  private MainController mainController;
  private Channel listenerChannel;

  private EventLoopGroup nioEventLoopGroup;
  private List<SocketChannel> socketChannels;

  public MyServer(MainController mainController) {
    this.mainController = mainController;
    socketChannels = new ArrayList<>();
  }

  public void start() throws Exception {
    int port = mainController.getPort();

    if (listenerChannel != null && listenerChannel.isOpen()) {
      mainController.log("already listening on " + listenerChannel.localAddress().toString());
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
          MyServer.this.socketChannels.add(socketChannel);
          socketChannel.pipeline().addLast(myServerHandler);

          socketChannel.closeFuture().addListener(channelFeature ->
            Platform.runLater(() -> {
              socketChannels.remove(socketChannel);
              MyServer.this.mainController.log("channel closed ");
            }));

          mainController.log("connected->" + socketChannel.remoteAddress().getAddress() + ":" +
            socketChannel.remoteAddress().getPort());
        }
      });

    ChannelFuture channelFuture = serverBootstrap.bind();
    listenerChannel = channelFuture.channel();

    channelFuture.addListener(genericFeature -> Platform.runLater(() -> {
      MyServer.this.mainController.log("server is listening on port " + port);
      MyServer.this.mainController.txtPort.setEditable(false);
    }));
  }

  public void stop() {
    for (SocketChannel socketChannel : socketChannels) {
      if(socketChannel.isOpen()) {
        ByteBuf closeMsg = Unpooled.wrappedBuffer("closed".getBytes());
        socketChannel.writeAndFlush(closeMsg);
      }
    }

    nioEventLoopGroup
      .shutdownGracefully()
      .addListener(channelFuture -> Platform.runLater(() -> {
        MyServer.this.mainController.log("server shutdown");
        MyServer.this.mainController.txtPort.setEditable(true);
      }));
  }

  public void send(String msg) {
    for (SocketChannel socketChannel : socketChannels) {
      if (socketChannel.isOpen()) {
        ByteBuf sendMsg = Unpooled.wrappedBuffer(msg.getBytes());
        socketChannel.writeAndFlush(sendMsg);
      }
    }
  }
}
