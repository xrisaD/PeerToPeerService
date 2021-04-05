import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers;
    ConcurrentHashMap<int, Info> TokenId_toInfo;
    ConcurrentHashMap<String, ArrayList<int>> Files_toToken;
    ArrayList<int> All_tokenids;
    ArrayList<String> All_files;


}
