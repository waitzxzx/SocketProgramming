package pro1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
    public static void main(String args[]) throws Exception {
        String hostName = "localhost";
        int portNumber = 12001;
        int commandID;
        String username;
        String password;
        int usernameLen;
        int passwordLen;
        Socket clientSocket = new Socket(hostName, portNumber);      //建立客户端套接字
        System.out.println("Initiate a TCP connection to " + hostName + ":" + portNumber);
        while(true) {
            while (true) {
                //选择命令类型
                System.out.println("Enter '1' for user registration.\nEnter '2' for user authentication");
                Scanner scan = new Scanner(System.in);      ////字符读取流，读取来自用户的输入
                if (scan.hasNextInt()) {
                    int serviceType = scan.nextInt();
                    if (1 == serviceType) {
                        commandID = 1;
                        break;
                    } else if (2 == serviceType) {
                        commandID = 3;
                        break;
                    } else {
                        System.out.println("Error input,please try again!");
                    }
                }
            }

            while (true) {
                //用户名输入模块
                System.out.print("Username:");
                Scanner scan = new Scanner(System.in);
                if (scan.hasNextLine()) {
                    username = scan.nextLine();
                    usernameLen = username.length();
                    if (usernameLen > 20) {      //判断用户名长度
                        System.out.println("Username input is too long,please try again!");
                    } else {
                        System.out.println("OK!");
                       break;
                    }
                }
            }

            while (true) {
                //密码输入模块
                System.out.print("Password:");
                Scanner scan = new Scanner(System.in);
                if (scan.hasNextLine()) {
                    password = scan.nextLine();
                    passwordLen = password.length();
                    if (passwordLen < 0 || passwordLen > 30) {     //判断密码长度
                        System.out.println("Password input is too long,please try again!");
                    } else {
                        System.out.println("OK!");
                        break;
                    }
                }
            }

            //消息处理模块
            byte[] requestMessage = new byte[58];
            byte[] totalLengthByte = ByteBuffer.allocate(4).putInt(58).array();     //totalLength处理
            System.arraycopy(totalLengthByte, 0, requestMessage, 0, 4);
            byte[] commandIDByte = ByteBuffer.allocate(4).putInt(commandID).array();        //commandID处理
            System.arraycopy(commandIDByte, 0, requestMessage, 4, 4);
            byte[] usernameByte = username.getBytes("UTF-8");       //username处理
            System.arraycopy(usernameByte, 0, requestMessage, 8, usernameLen);
            byte[] passwordByte = password.getBytes("UTF-8");       //password处理
            System.arraycopy(passwordByte, 0, requestMessage, 28, passwordLen);
            //System.out.println(requestMessage.length);
            //for (int i = 0; i < 58; i++) {
            //    System.out.print(requestMessage[i]);
            //    System.out.print(" ");
            //}
            DataOutputStream toServer = new DataOutputStream(clientSocket.getOutputStream());       //建立套接字输出流
            toServer.write(requestMessage);     //向输出流写入数据

            DataInputStream fromServer = new DataInputStream(clientSocket.getInputStream());        //建立套接字输入流
            byte[] responseMessage = new byte[73];
            fromServer.readFully(responseMessage);      //从输入流中以字节形式读取

            //分割并显示来自服务器的消息各字段
            byte[] responseTotalLengthByte = new byte[4];
            System.arraycopy(responseMessage, 0, responseTotalLengthByte, 0, 4);
            int responseTotalLength = bytesToInt(responseTotalLengthByte, 0);
            System.out.println("Response message length:" + responseTotalLength);

            byte[] responseCommandIDByte = new byte[4];
            System.arraycopy(responseMessage, 4, responseCommandIDByte, 0, 4);
            int responseCommandID = bytesToInt(responseCommandIDByte, 0);
            System.out.println("Response message command ID:" + responseCommandID);

            byte[] statusByte = new byte[1];
            System.arraycopy(responseMessage, 8, statusByte, 0, 1);
            String status = new String(statusByte);
            System.out.println("Status:" + status);

            byte[] descriptionByte = new byte[64];
            System.arraycopy(responseMessage, 9, descriptionByte, 0, 64);
            String description = new String(descriptionByte);
            System.out.println("Description:" + description);
            System.out.println();
        }
    }
    public static int bytesToInt(byte[] src, int offset) {
        //把4位byte转换成int
        int value;
        value = (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }
}
