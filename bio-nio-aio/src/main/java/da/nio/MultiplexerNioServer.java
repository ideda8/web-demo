package da.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 非阻塞
 */
public class MultiplexerNioServer implements Runnable {

    private ServerSocketChannel servChannel;
    private Selector selector;
    private volatile boolean stop = false;

    /**
     * 初始化多路复用器 绑定监听端口
     */
    public MultiplexerNioServer(int port){
        try {
            servChannel = ServerSocketChannel.open();   //三次握手对象
            selector = Selector.open(); //获得一个多路复用器 时间轮询
            servChannel.configureBlocking(false); //设置为非阻塞
            servChannel.socket().bind(new InetSocketAddress(port),1024); //绑定一个端口
            servChannel.register(selector, SelectionKey.OP_ACCEPT); //把selector注册到channel
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                int client = selector.select(); //epoll函数 进入阻塞状态 也可以设置超时时间select(1000)
                System.out.println("1"+client);
                //有连接进来才会往下走
                if(client==0){
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys(); //活跃的事件 已经抽象成SelectionKey对象
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        handle(key);
                    }catch (Exception e){
                        if(key!=null){
                            key.cancel();
                            if(key.channel()!=null){
                                key.channel().close();
                            }
                        }
                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void handle(SelectionKey key) throws IOException {
        if(key.isValid()){  //有效事件
            if(key.isAcceptable()) {    //可连接事件 第一次都是连接事件
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();    //三次握手
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);    //读
            }
            if(key.isReadable()){   //可读事件
                SocketChannel sc = (SocketChannel) key.channel();
                //bio中使用byte[] ByteBuffer也是封装了byte[] 但有更多高级的实现
                ByteBuffer readbuffer = ByteBuffer.allocate(1024); //写 0 1024 1024
                int readBytes = sc.read(readbuffer);
                if(readBytes>0){
                    readbuffer.flip();
                    byte[] bytes = new byte[readbuffer.remaining()];
                    readbuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("input is:"+body);
                    res(sc, body);  //写操作
                }
            }
        }
    }

    private void res(SocketChannel channel, String response) throws IOException {
        if(response!=null && response.length()>0){
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            channel.write(writeBuffer); //写操作

            System.out.println("res end");
        }
    }
}
