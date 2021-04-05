import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers;
    ConcurrentHashMap<Integer, Info> TokenId_toInfo;
    ConcurrentHashMap<String, ArrayList<Integer>> Files_toToken;
    ArrayList<Integer> All_tokenids;
    ArrayList<String> All_files;


}
