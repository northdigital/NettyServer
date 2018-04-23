package main;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@Sharable
public class MyInboundServerHandler extends ChannelInboundHandlerAdapter {
  private static final char CTRL_START = 0x02;
  private static final char CTRL_END = 0x03;
  private static final String EMPTY_BUFFER = "";

  private MainController mainController;
  private Boolean startRead = false, endRead = false;
  private String messageBuffer = EMPTY_BUFFER;

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
    String msgStr = in.toString(CharsetUtil.UTF_8);

    for (char ch : msgStr.toCharArray()) {
      boolean ctrlStartRead = ch == CTRL_START;
      boolean ctrlEndRead = ch == CTRL_END;

      if (ctrlStartRead) {
        startRead = true;
        endRead = false;
        messageBuffer = EMPTY_BUFFER;
      }

      if (ctrlEndRead) {
        endRead = true;
      }

      if (!ctrlStartRead && !ctrlEndRead && startRead) {
        messageBuffer += ch;
      }

      if (startRead && endRead) {
        mainController.log("MyInboundServerHandler -> Received -> " + messageBuffer);
        startRead = false;
        endRead = false;
        messageBuffer = EMPTY_BUFFER;
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
