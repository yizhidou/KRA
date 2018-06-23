package com.client.KRA;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import com.tools.KRA.Message;
import com.tools.KRA.Utils;

public class Client {

	public static final String IP_ADDR = "localhost";//服务器地址   
    public static final int PORT = 12345;//服务器端口号
    
	public static void main(String[] args) {
		/*String ip = args[0];
		String port = args[1];
		String MasterKey = args[2];
		String filename = args[3];*/
		String MasterKey = "RAIN";
		String filename = "data.txt";
		
		System.out.println("客户端启动...");    
        System.out.println("当接收到服务器端字符为 \"OK\" 的时候, 客户端将终止\n");   
        
        InetAddress ia;
        byte[] mac = null;
		try {
			ia = InetAddress.getLocalHost();
			mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//    	System.out.println(Arrays.toString(mac));

        String Msg_type = "Authentication";
        int Nonce = 0;
        int r = -1;
        int ANonce;
        int CNonce;
        byte[] TK;
        Message msg = new Message();
        msg.type = "Authentication_request";
        while (true) {    
            Socket socket = null;  
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            
            try {  
                //创建一个流套接字并将其连接到指定主机上的指定端口号  
                socket = new Socket(IP_ADDR, PORT);    
                
                Message Msg = new Message();
				
                
                if(msg.type.compareTo("Authentication_request") == 0) {
					Msg.type = "Authentication_request";
					Msg.r = -1;
					Msg.code = "";
                }
                else if(msg.type.compareTo("Msg1") == 0) {
                	Random random = new Random(2017);
                	CNonce = random.nextInt();
                	ANonce = Integer.parseInt(msg.code);
                	TK = Utils.Hash(ANonce, CNonce, MasterKey);
                	System.out.println(ANonce +"\t"+ CNonce +"\t" + MasterKey + "\t" + Arrays.toString(TK));
                	Msg.type = "Msg2";
                	Msg.r = r;
                	Msg.code = String.valueOf(CNonce);
                }
                else if(msg.type.compareTo("Msg3") == 0) {
                	Msg.type = "Msg4";
                	Msg.r = r + 1;
                	Msg.code = "ACK";
                	Nonce = 0;
                	
                }
                else if(msg.type.compareTo("data") == 0) {
                	String cipher;
                	String plain = "";
                	
                	plain = new String(Files.readAllBytes(Paths.get(filename)));
                	System.out.println(plain);
             
                	byte[] p_byte = plain.getBytes();
                	int len = (int)Math.ceil((double)p_byte.length/16);
                	if(p_byte.length % 16 != 0) {
                		byte[] padding = new byte[16 - p_byte.length % 16];
                		System.arraycopy(padding, 0, p_byte, (len - 1)*16 + p_byte.length % 16 - 1, 16 - p_byte.length % 16);
                	}

                	for(int i=0; i<len; i++) {
                		byte[] msg_byte = new byte[16];
                		System.arraycopy(p_byte, i*16, msg_byte, 0, 16);
                		byte[] key = Utils.Hash(mac, Nonce);
                		Nonce++;
                		cipher = new String(Utils.XOR(msg_byte, key));
                		Msg.type = "data";
                		Msg.r = r + 1;
                		Msg.code = cipher;
                		
                		System.out.println(Msg.code);
                		
                		oos = new ObjectOutputStream(socket.getOutputStream());
        				oos.writeObject(Msg);
        				oos.flush();
        				Thread.sleep(1000);
        				
        				ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        				Object obj = ois.readObject();
        				if(obj != null) {
        					Message receive_msg = (Message)obj;
        					msg.type = receive_msg.type;
        					msg.r = receive_msg.r;
        					msg.code = receive_msg.code;
        					System.out.println("client receive:" + msg.type +"\t"+ msg.r + "\t" + msg.code);
        				}
        				
        				oos.close();
        				ois.close();
        				
                	}
                }
                else if (msg.type.compareTo("OK") == 0) {
                	System.out.println("客户端将关闭连接");    
                    Thread.sleep(500);    
                    break;
                }
                else {
                	System.out.println("Error");    
                    Thread.sleep(500);    
                    break;
                }
                
                System.out.println("client send:" + Msg.type);
                oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(Msg);
				oos.flush();
				
				ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				Object obj = ois.readObject();
				if(obj != null) {
					Message receive_msg = (Message)obj;
					msg.type = receive_msg.type;
					msg.r = receive_msg.r;
					msg.code = receive_msg.code;
					System.out.println("client receive:" + msg.type +"\t"+ msg.r + "\t" + msg.code);
				}
				oos.close();
				ois.close();
                /*//读取服务器端数据    
                DataInputStream input = new DataInputStream(socket.getInputStream());    
                //向服务器端发送数据    
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());    
                System.out.print("请输入: \t");    
                String str = new BufferedReader(new InputStreamReader(System.in)).readLine();    
                out.writeUTF(str);    
                    
                String ret = input.readUTF();     
                System.out.println("服务器端返回过来的是: " + ret);    
                // 如接收到 "OK" 则断开连接    
                if ("OK".equals(ret)) {    
                    System.out.println("客户端将关闭连接");    
                    Thread.sleep(500);    
                    break;    
                }    
                  
                out.close();  
                input.close();*/  
            } catch (Exception e) {  
                System.out.println("客户端异常:" + e.getMessage());   
            } finally {  
                if (socket != null) {  
                    try {  
                        socket.close();  
                    } catch (IOException e) {  
                        socket = null;   
                        System.out.println("客户端 finally 异常:" + e.getMessage());   
                    }  
                }  
            }  
        }    
    }
}
