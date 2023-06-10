package chatApp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Bu sınıf, chat odasının id'sini, adını ve oluşturan kişiyi saklar.
 * 
 * @author fergocen
 */

public class CRoom implements Serializable {
    
    public int room_id; 
    
    public String room_name;
    
    public String room_creator; 
    
    @Override
    public String toString() {
        return this.room_name;
    }
}
