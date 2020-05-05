package da.nio;

public class NioServer {
    public static void main(String[] args) {
        new Thread(new MultiplexerNioServer(8080)).start();
    }
}
