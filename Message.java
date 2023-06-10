package chatApp;

//This is the class to be sent in each message.
public class Message implements java.io.Serializable {

    public static enum Type {
        TEXT, CONN_REQ, FILE, LIST_REQUEST, LIST, INJECTION, DELETION, NOTIFICATION, CREATE, ROOM_CREATE_NOTF, NICKNAME
    }

    public static enum Notf_Type {
        SUCCES, FAIL, DISCONNECT
    }

    public static enum Cast_Type {
        ROOM_LIST, USER_LIST, ROOM, CLIENT
    }

    public static enum Chat_Type {
        ROOM_MESSAGE, PVP_MESSAGE
    }

    public Type type;
    public Cast_Type cast_type;
    public Chat_Type chat_type;
    public Notf_Type notf_type;
    public Object content;
    public Object receiver;
    public Object sender;

    public byte[] file_byte;
    public int filesize;
    public String file_dir;
    public String file_name;

    public String nickname;

    public Message(Type t) {
        this.type = t;
    }

}
