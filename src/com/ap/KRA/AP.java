package com.ap.KRA;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Random;

import com.tools.KRA.Message;
import com.tools.KRA.Utils;

public class AP {

	public static final int PORT = 12345;//�����Ķ˿ں�   
	public int g_ANonce = 0;
	public int Nonce = 0;
	public byte[] TK = null;
	public Dictionary AddressPool = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String MasterKey = args[0];
		String port = args[1];*/
		
		System.out.println("����������...\n");    
        AP server = new AP();    
        server.init();    
    }    
    
    public void init() {    
        try {    
            ServerSocket serverSocket = new ServerSocket(PORT); 
            while (true) {    
                // һ���ж���, ���ʾ��������ͻ��˻��������    
                Socket client = serverSocket.accept();
                Message msg = new Message();
                InetAddress ia = client.getInetAddress();
                byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
                // �����������    
                new HandlerThread(client, g_ANonce);    
            }  
        } catch (Exception e) {    
            System.out.println("�������쳣: " + e.getMessage());    
        }    
    }    
    
    private class HandlerThread implements Runnable {    
    	private int ANonce;
		private Socket socket;    
        public HandlerThread(Socket client, int g_ANonce) {    
            socket = client; 
            this.ANonce = g_ANonce;
            new Thread(this).start();
        }    
    
        public void run() {  
        	int r = 0;
        	String MasterKey = "RAIN";
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
            		g_ANonce = ANonce;
            		System.out.println(ANonce);
            		Msg.code = String.valueOf(ANonce);
            	}
            	else if(receive_msg.type.compareTo("Msg2") == 0) {
            		String type = receive_msg.type;
            		r = receive_msg.r;
            		String code = receive_msg.code;
            		int CNonce = Integer.parseInt(code);
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
            		Msg.type = "OK";
            	}
            	
            	System.out.println("AP send:" + Msg.type);
            	oos.writeObject(Msg);
        		oos.flush();
        		
            	ois.close();
            	oos.close();
            	
                /*// ��ȡ�ͻ�������    
                DataInputStream input = new DataInputStream(socket.getInputStream());  
                String clientInputStr = input.readUTF();//����Ҫע��Ϳͻ����������д������Ӧ,������� EOFException  
                // ����ͻ�������    
                System.out.println("�ͻ��˷�����������:" + clientInputStr);    
    
                // ��ͻ��˻ظ���Ϣ    
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());    
                System.out.print("������:\t");    
                // ���ͼ��������һ��    
                String s = new BufferedReader(new InputStreamReader(System.in)).readLine();    
                out.writeUTF(s);    
                  
                out.close();    
                input.close(); */   
            } catch (Exception e) {    
                System.out.println("������ run �쳣: " + e.getMessage());    
            } finally {    
                if (socket != null) {    
                    try {    
                        socket.close();    
                    } catch (Exception e) {    
                        socket = null;    
                        System.out.println("����� finally �쳣:" + e.getMessage());    
                    }    
                }    
            }   
        }    
	}
}
