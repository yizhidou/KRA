package com.ap.KRA;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import com.tools.KRA.Message;
import com.tools.KRA.Utils;

public class AP {

	public static final int PORT = 12345;// 监听的端口号

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String MasterKey = args[0];
		String port = args[1];*/
		String MasterKey = "RAIN";
		int ANonce = 0;
		int CNonce = 0;
		int Nonce = 0;
		byte[] TK = null;
		
		System.out.println("服务器启动...\n");    
		try {    
            ServerSocket serverSocket = new ServerSocket(PORT); 
            while (true) {    
                // 一旦有堵塞, 则表示服务器与客户端获得了连接    
                Socket socket = serverSocket.accept();
                Message msg = new Message();
                InetAddress ia = socket.getInetAddress().getLocalHost();
                byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
                // 处理这次连接    
                 
            	int r = 0;
            
            	ObjectInputStream ois = null;
            	ObjectOutputStream oos = null;
            	
                try {    
                	ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())); 
                	oos = new ObjectOutputStream(socket.getOutputStream());  
                	
                	Object obj = ois.readObject();
                	Message receive_msg = (Message)obj;
                	System.out.println("AP receive:" + receive_msg.type +"\t" + receive_msg.r + "\t" + receive_msg.code);
                	
                	Message Msg = new Message();
                	
                	if(receive_msg.type.compareTo("Authentication_request") == 0) {
                		Random random = new Random(2018);
                		Msg.type = "Msg1";
                		Msg.r = r;
                		ANonce = random.nextInt();
                		System.out.println(ANonce);
                		Msg.code = String.valueOf(ANonce);
                	}
                	else if(receive_msg.type.compareTo("Msg2") == 0) {
                		String type = receive_msg.type;
                		r = receive_msg.r;
                		String code = receive_msg.code;
                		CNonce = Integer.parseInt(code);
                		TK = Utils.Hash(ANonce, CNonce, MasterKey);
                		System.out.println(ANonce +"\t"+ CNonce +"\t" + MasterKey + "\t" + Arrays.toString(TK));
                		Msg.type = "Msg3";
                		Msg.r = r + 1;
                		Msg.code = "ACK";
                	}
                	else if(receive_msg.type.compareTo("Msg4") ==  0)
                	{
                		Msg.type = "data";
                	}
                	else if(receive_msg.type.compareTo("data") == 0) {
                		byte[] key = Utils.Hash(mac, Nonce, TK);
                		System.out.println(Nonce);
//                		System.out.println(Arrays.toString(key));
                		System.out.println(Arrays.toString(Utils.XOR(receive_msg.code.getBytes(), key)));
                		String plain = new String(Utils.XOR(receive_msg.code.getBytes(), key));
//                		System.out.println(Arrays.toString(receive_msg.code.getBytes()));
//                		System.out.println(plain);
//                		System.out.println(Arrays.toString(receive_msg.code.getBytes()));
                		while(true) {
                			Nonce++;
                			key = Utils.Hash(mac, Nonce, TK);
                			System.out.println(Nonce);
//                			System.out.println(Arrays.toString(key));
                			try {
                				oos.writeObject(Msg);
                				oos.flush();
//                				System.out.println("AP send:" + Msg.type);
                				ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())); 
                            	oos = new ObjectOutputStream(socket.getOutputStream());  
                				Object data = ois.readObject();
                            	Message receive_data = (Message)data;
                            	System.out.println(Arrays.toString(Utils.XOR(receive_data.code.getBytes(), key)));
//                            	System.out.println("AP receive:" + receive_data.type +"\t" + receive_data.r + "\t" + receive_data.code);
//                            	System.out.println(Arrays.toString(receive_data.code.getBytes()));
                            	plain = new String(Utils.XOR(receive_data.code.getBytes(), key));
//                            	System.out.println(plain);
//                            	System.out.println(Arrays.toString(receive_data.code.getBytes()));
                            	Thread.sleep(1000);
                            	if(receive_data.type.compareTo("over") == 0 ) {
                            		Msg.type = "OK";
                            		break;
                            	}
                			} catch (Exception e) {
                				System.out.println("服务器接收异常: " + e.getMessage());  
                				break;
                			} 
                		}
                		
                	}
                	
                	System.out.println("AP send:" + Msg.type);
                	oos.writeObject(Msg);
            		oos.flush();
            		
                	ois.close();
                	oos.close();
                	
                    /*// 读取客户端数据    
                    DataInputStream input = new DataInputStream(socket.getInputStream());  
                    String clientInputStr = input.readUTF();//这里要注意和客户端输出流的写方法对应,否则会抛 EOFException  
                    // 处理客户端数据    
                    System.out.println("客户端发过来的内容:" + clientInputStr);    
        
                    // 向客户端回复信息    
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());    
                    System.out.print("请输入:\t");    
                    // 发送键盘输入的一行    
                    String s = new BufferedReader(new InputStreamReader(System.in)).readLine();    
                    out.writeUTF(s);    
                      
                    out.close();    
                    input.close(); */   
                } catch (Exception e) {    
                    System.out.println("服务器 run 异常: " + e.getMessage());    
                    e.printStackTrace();
                } finally {    
                    if (socket != null) {    
                        try {    
                            socket.close();    
                        } catch (Exception e) {    
                            socket = null;    
                            System.out.println("服务端 finally 异常:" + e.getMessage());    
                        }    
                    }    
                       
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
		
		