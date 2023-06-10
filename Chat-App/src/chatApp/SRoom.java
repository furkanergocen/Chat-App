package chatApp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author fergocen
 */
//Serverside room representation
public class SRoom implements Serializable{
    public int room_id;
    public String room_name;
    public SClient room_creater;
    public ArrayList <SClient> clients = new ArrayList();
    
    @Override
    public String toString(){
        return this.room_name;
    }
}
