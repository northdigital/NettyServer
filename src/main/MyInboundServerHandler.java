package main;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@Sharable
public class MyInboundServerHandler extends ChannelInboundHandlerAdapter {
  private MainController mainController;

  public MyInboundServerHandler(MainController mainController) {
    this.mainController = mainController;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    mainController.log("MyInboundServerHandler -> channelActive -> " + ctx.channel().remoteAddress().toString());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    mainController.log("MyInboundServerHandler -> channelInactive -> " + ctx.channel().remoteAddress().toString());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf in = (ByteBuf) msg;
    mainController.log("MyInboundServerHandler -> Received -> " + in.toString(CharsetUtil.UTF_8));
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
