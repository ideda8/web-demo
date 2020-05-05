package da.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 阻塞IO
 */
public class BioServerSocket {
    public static void main(String[] args) {
        int port = 8080;        //监听端口
        ServerSocket server = null; //服务器必备对象
        Socket socket = null;       //客户端对象
        InputStream inputStream = null; //输入面板
        OutputStream outputStream = null;   //输出面板

        try {
            server = new ServerSocket(port);
            while (true){
                socket = server.accept();   //recvfrom 阻塞 三次握手
                System.out.println("accept");
                inputStream = socket.getInputStream();

                byte[] buffer = new byte[1024];     //读缓存区
                int length = 0;
                while ((length = inputStream.read(buffer)) > 0){    //阻塞
                    System.out.println("input:" + new String(buffer, 0, length));
                    outputStream = socket.getOutputStream();
                    outputStream.write("end".getBytes());
                }
                //new Thread(new SocketHandler(socket)).start(); //多线程 或者线程池
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {  //资源释放
            if(server!=null){
                try {
                    server.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(outputStream!=null){
                try {
                    outputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
