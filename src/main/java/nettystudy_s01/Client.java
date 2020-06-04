package nettystudy_s01;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {

	public static void main(String[] args) {
		//创建线程池，用来处理整个channel中的所有事件
		EventLoopGroup group = new NioEventLoopGroup(1);
		//辅助启动类
		Bootstrap b = new Bootstrap();
		//TODO：netty中所有的方法都是异步的。
		try {
			ChannelFuture f = b.group(group)
				.channel(NioSocketChannel.class)//指定连到服务器上的channel的类型
					.handler(new ClientChannelInitializer())
						.connect("localhost", 8888);//设置连接端口
			
			f.addListener(new ChannelFutureListener() {				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(!future.isSuccess()){
						System.out.println("not connected");
					} else {
						System.out.println("connect");
					}
				}
			});
			f.sync();
			
			System.out.println("...");
			
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
		
	}
	
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ClientHandler());
	}
	
}

class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//channel第一次连上可用，写出一串字符串 Direct Memory
		ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
		ctx.writeAndFlush(buf);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
		} finally {
			if(buf != null)
				ReferenceCountUtil.release(buf);
//			System.out.println(buf.refCnt());
		}
	}
	
}
