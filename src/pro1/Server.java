package pro1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Server {
    public static void main(String args[]) throws Exception {
        int portNumber = 12001;
        int responseCommandID;  //???
        ServerSocket serverSocket = new ServerSocket(portNumber);       //建立服务器端套接字
        System.out.println("Listening port:" + portNumber);
        Socket socket = serverSocket.accept();       //建立TCP连接
        System.out.println("Connect successfully! User from" + socket.getInetAddress());

        while (true) {
            DataInputStream fromClient = new DataInputStream(socket.getInputStream());    //建立套接字输入流
            byte[] receivedMessage = new byte[58];
            fromClient.readFully(receivedMessage);      //从输入流中以字节形式读取
            //分割并显示来自客户端的消息各字段
            byte[] totalLengthByte = new byte[4];
            System.arraycopy(receivedMessage, 0, totalLengthByte, 0, 4);
            int totalLength = bytesToInt(totalLengthByte, 0);
            System.out.println("Received message length:" + totalLength);
            byte[] commandIDByte = new byte[4];
            System.arraycopy(receivedMessage, 4, commandIDByte, 0, 4);
            int commandID = bytesToInt(commandIDByte, 0);
            System.out.println("Received message command ID:" + commandID);
            byte[] usernameByte = new byte[20];
            System.arraycopy(receivedMessage, 8, usernameByte, 0, 20);
            String username = new String(usernameByte);
            System.out.println("Username:" + username);
            byte[] passwordByte = new byte[30];
            System.arraycopy(receivedMessage, 28, passwordByte, 0, 30);
            String password = new String(passwordByte);
            System.out.println("Password:" + password);
            System.out.println();

            String status = "";
            String description = "";
            String passwordMD5 = Md5(password);      //密码MD5值

            File file = new File("user.txt");       //如果文件不存在自动创建
            file.createNewFile();

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);     //建立读取缓冲区

            if (1 == commandID) {       //如果收到请求注册信息
                responseCommandID = 2;
                boolean flag = false;
                //检查是否重名
                String record = br.readLine();
                while (record != null) {

                    String[] nameSplit = record.split("\\*");       //用*分离用户名
                    //System.out.println(nameSplit[0].trim() + " " + username.trim());
                    if (nameSplit[0].trim().equals(username.trim())) {
                        description = "Duplication of username!";
                        status = "0";
                        flag = true;
                        break;
                    } else {
                        record = br.readLine();
                    }
                }
                br.close();
                fr.close();
                if (!flag) {
                    //如果不存在重名
                    FileWriter fw = new FileWriter(file, true);       //追加写入
                    fw.write(username);
                    fw.write("*");      //用于区分用户名和密码
                    fw.write(passwordMD5);
                    fw.write("\r\n");       //写入换行，不可以"\n"
                    fw.close();
                    description = "Registration Success!";
                    status = "1";
                }
            } else if (3 == commandID) {        //如果收到认证请求信息
                responseCommandID = 4;
                String record = br.readLine();
                while (record != null) {
                    String[] split = record.split("\\*");       //用*分离用户名和密码
                    //System.out.println(split[0] + " " + username);
                    if (!split[0].trim().equals(username.trim())) {      //判断用户名字段
                        //System.out.println(split[0]+" "+username);
                        description = "Username does not exist!";
                        status = "0";
                    } else {     //判断密码字段
                        //System.out.println(split[1]+" "+passwordMD5);
                        if (split[1].equals(passwordMD5)) {
                            description = "Authentication success!";
                            status = "1";
                            break;
                        } else {
                            description = "Wrong password!";
                            status = "0";
                            break;
                        }
                    }
                    record = br.readLine();
                }
            } else break;
            //System.out.println(description + status);

            //消息处理模块
            byte[] responseMessage = new byte[73];
            byte[] responseTotalLengthByte = ByteBuffer.allocate(4).putInt(73).array();     //totalLength处理
            System.arraycopy(responseTotalLengthByte, 0, responseMessage, 0, 4);
            byte[] responseCommandIDByte = ByteBuffer.allocate(4).putInt(responseCommandID).array();        //commandID处理
            System.arraycopy(responseCommandIDByte, 0, responseMessage, 4, 4);
            byte[] statusByte = status.getBytes("UTF-8");
            System.arraycopy(statusByte, 0, responseMessage, 8, statusByte.length);     //status处理
            byte[] descriptionByte = description.getBytes("UTF-8");
            System.arraycopy(descriptionByte, 0, responseMessage, 9, descriptionByte.length);
            //for (int i = 0; i < 73; i++) {
            //    System.out.print(responseMessage[i]);
            //    System.out.print(" ");
            //}

            DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());     //建立套接字输出流
            toClient.write(responseMessage);        //向输出流写入数据
        }
        socket.close();
        serverSocket.close();
    }

    public static int bytesToInt(byte[] src, int offset) {      //把4位byte转换成int
        int value;
        value = (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }
    public static String Md5(String input) throws NoSuchAlgorithmException {
        //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //输入的字符串转换成字节数组
        byte[] inputByteArray = input.getBytes();
        //inputByteArray是输入字符串转换得到的字节数组
        messageDigest.update(inputByteArray);
        //转换并返回结果，也是字节数组，包含16个元素
        byte[] resultByteArray = messageDigest.digest();
        //字符数组转换成字符串返回
        return byteArrayToHex(resultByteArray);
    }

    public static String byteArrayToHex(byte[] byteArray) {
        //首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        //new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符）
        char[] resultCharArray = new char[byteArray.length * 2];
        //遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        //字符数组组合成字符串返回
        return new String(resultCharArray);
    }
}