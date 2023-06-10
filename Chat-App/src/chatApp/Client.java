package chatApp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import chatApp.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

    

class Listen extends Thread {

    // Mesaj türüne göre işlenen mesajlar
    public void run() {
        while (Client.socket.isConnected()) {
            try {
                Message received = (Message) (Client.socketInput.readObject());
                switch (received.type) {
                    case LIST:
                        addListToTable(received);
                        break;
                    case TEXT:
                        String text = received.nickname + " : " + (String) received.content;
                        Chatbox chatbox = Screen.getChatbox(received);
                        chatbox.list_model.addElement(text);
                        break;
                    case NOTIFICATION:
                        if (received.notf_type == Message.Notf_Type.SUCCES) {
                            Client.joined_rooms.add(((CRoom) received.content).room_id);
                            Screen.IntToRoomChatMap.get(((CRoom) received.content).room_id).chat_box_nickname = received.nickname;

                        }
                        break;
                    case INJECTION:
                        if (received.cast_type == Message.Cast_Type.ROOM) {
                            setChatBox((CRoom) received.content);
                        } else {
                            setChatBox((Client) received.content);
                        }
                        break;
                    case ROOM_CREATE_NOTF:
                        // Room created notf is only received when user succesfully creates a room and is granted with ownership of the room
                        if (received.notf_type == Message.Notf_Type.SUCCES) {
                            Client.joined_rooms.add(((CRoom) received.content).room_id);
                            Chatbox c = Screen.getRoomChatbox((CRoom) received.content);
                            c.chat_box_nickname = received.nickname;
                        }
                        break;
                    case FILE:
                        Chatbox chatbox_file = Screen.getChatbox(received);
                        chatbox_file.list_model.addElement(received.nickname + " has shared a file named : " + received.file_name);
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fileChooser.setDialogTitle("Choose a location to save");
                        if (fileChooser.showOpenDialog(Client.screen) == JFileChooser.APPROVE_OPTION) {
                            File file_to_send = fileChooser.getSelectedFile();
                            FileOutputStream fos = new FileOutputStream(file_to_send + "/" + received.file_name);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            bos.write(received.file_byte, 0, received.filesize);
                            bos.close();
                        }
                        break;

                    case DELETION:
                        int rowcount = Screen.client_table_model.getRowCount();
                        for (int i = 0; i < rowcount; i++) {
                            Chatbox chat_box = (Chatbox) Screen.client_table_model.getValueAt(i, 0);
                            System.out.println(i);
                            if (chat_box.cclient.client_id == ((Client) received.content).client_id) {
                                Screen.client_table_model.removeRow(i);
                                break;
                            }
                        }
                }

            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                Client.terminate();
                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                Client.terminate();
                break;
            }
        }

    }

    // Kullanıcı veya oda listesini ilgili tabloya ekler
    void addListToTable(Message message) {

        if (message.cast_type == Message.Cast_Type.ROOM_LIST) {
            ArrayList<CRoom> rooms = (ArrayList<CRoom>) message.content;
            for (CRoom room : rooms) {
                setChatBox(room);
            }
        } else {
            ArrayList<Client> cclients = (ArrayList<Client>) message.content;
            for (Client cClient : cclients) {
                setChatBox(cClient);
            }
        }
    }

    void setChatBox(CRoom cRoom) {
        Chatbox chatbox = new Chatbox(cRoom);
        chatbox.chat_box_nickname = "NONE";
        Screen.IntToRoomChatMap.put(cRoom.room_id, chatbox);
        Client.screen.room_table_model.addRow(new Object[]{chatbox});
    }

    void setChatBox(Client cClient) {
        Chatbox chatbox = new Chatbox(cClient);
        Screen.IntToClientChatMap.put(cClient.client_id, chatbox);
        Client.screen.client_table_model.addRow(new Object[]{chatbox});
    }

}

public class Client implements Serializable {
public int client_id;
    
    public String client_nickname; 
    public static ObjectInputStream socketInput;
    public static ObjectOutputStream socketOutput;
    public static Socket socket;
    public static Listen listenMe;
    public static Screen screen;
    public static String nick_name;
    public static Client cclient;
    public static ArrayList<Integer> joined_rooms;

    public static void Start(String ip, int port, String username) {
        try {

            joined_rooms = new ArrayList<>();
            Client.nick_name = username;
            screen = new Screen();
            screen.setVisible(true);
            Client.socket = new Socket(ip, port);
            Client.print("Connected");
            Client.socketInput = new ObjectInputStream(Client.socket.getInputStream());
            Client.socketOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();
            sendNickName();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void terminate() {
        try {
            if (Client.socket != null) {
                Client.listenMe.stop();
                Client.socket.close();
                Client.socketOutput.flush();
                Client.socketOutput.close();

                Client.socketInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Mesajı yazarak soketin çıkışına yazar ve gönderir
    public static void Send(Object msg) {
        try {
            Client.socketOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Sunucuya takma ad gönderir
    public static void sendNickName() {
        Message nickname = new Message(Message.Type.NICKNAME);
        nickname.content = Client.nick_name;
        Send(nickname);
    }

    public static void print(String msg) {

        System.out.println(msg);

    }

    public Client(int clientid, String nickname){
        this.client_id = clientid;
        this.client_nickname = nickname;
    }

  @Override
    public String toString(){
        return this.client_nickname;
    }
}
