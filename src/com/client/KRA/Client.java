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

	public static final String IP_ADDR = "localhost";//��������ַ   
    public static final int PORT = 12345;//�������˿ں�
    
	public static void main(String[] args) {
		/*String ip = args[0];
		String port = args[1];
		String MasterKey = args[2];
		String filename = args[3];*/
		String MasterKey = "RAIN";
		String filename = "data.txt";
		
		System.out.println("�ͻ�������...");    
        System.out.println("�����յ����������ַ�Ϊ \"OK\" ��ʱ��, �ͻ��˽���ֹ\n");   
        
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
                //����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�  
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
                	System.out.println("�ͻ��˽��ر�����");    
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
                /*//��ȡ������������    
                DataInputStream input = new DataInputStream(socket.getInputStream());    
                //��������˷�������    
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());    
                System.out.print("������: \t");    
                String str = new BufferedReader(new InputStreamReader(System.in)).readLine();    
                out.writeUTF(str);    
                    
                String ret = input.readUTF();     
                System.out.println("�������˷��ع�������: " + ret);    
                // ����յ� "OK" ��Ͽ�����    
                if ("OK".equals(ret)) {    
                    System.out.println("�ͻ��˽��ر�����");    
                    Thread.sleep(500);    
                    break;    
                }    
                  
                out.close();  
                input.close();*/  
            } catch (Exception e) {  
                System.out.println("�ͻ����쳣:" + e.getMessage());   
            } finally {  
                if (socket != null) {  
                    try {  
                        socket.close();  
                    } catch (IOException e) {  
                        socket = null;   
                        System.out.println("�ͻ��� finally �쳣:" + e.getMessage());   
                    }  
                }  
            }  
        }    
    }
}
