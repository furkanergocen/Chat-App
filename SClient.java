package chatApp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

//This is a server side client representation
public class SClient {

    int id;
    Socket socket;
    ObjectOutputStream socketOutput;
    ObjectInputStream socketInput;
    Listen listenThread;
    String nickname;
    
    public SClient(Socket incoming_socket, int id) {
        this.socket = incoming_socket;
        this.id = id;
        try {
            this.socketOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.socketInput = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.listenThread = new Listen(this);

    }

    //Handles the client message with the given message type
    class Listen extends Thread {

        SClient currentClient;

        Listen(SClient currentClient) {
            this.currentClient = currentClient;
        }

        public void run() {

            while (currentClient.socket.isConnected()) {

                try {
                    
                 Object received = currentClient.socketInput.readObject();
                 Message message = (Message)received;
                 switch (message.type){
                     case TEXT:
                         if(message.chat_type==Message.Chat_Type.ROOM_MESSAGE){
                         Server.broadcastToRoom((CRoom)message.receiver, currentClient, message);
                         }
                         else{
                         Server.SendToUser(currentClient,message);
                         }
                         break;
                     case CONN_REQ:
                         Server.handleRequest(currentClient, message);
                         break;
                     case CREATE:
                         Server.CreateRoom(currentClient,message);
                         break;
                     case NICKNAME:
                         nickname = (String)message.content;
                         Server.injectUser(SClient.this);
                         break;
                     case FILE:
                         if(message.chat_type==Message.Chat_Type.ROOM_MESSAGE){
                         Server.broadcastToRoom((CRoom)message.receiver, currentClient, message);
                         }
                         else{
                         Server.SendToUser(currentClient,message);
                         }
                         break;
                         
                 }
                    //Server.Send(currentClient.adversary,received);

                } catch (IOException e) {
                    Server.Clients.remove(currentClient);
                    Message deletion_message = new Message(Message.Type.DELETION);
                    deletion_message.cast_type=Message.Cast_Type.CLIENT;
                    deletion_message.content= Server.castClient(currentClient);
                    Server.BroadCast(deletion_message);
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e) {
                    Server.Clients.remove(currentClient);
                    Message deletion_message = new Message(Message.Type.DELETION);
                    deletion_message.cast_type=Message.Cast_Type.CLIENT;
                    deletion_message.content= Server.castClient(currentClient);
                    Server.BroadCast(deletion_message);
                    e.printStackTrace();
                    break;
                }

            }

        }

    }



}
