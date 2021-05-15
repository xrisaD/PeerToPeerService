import java.util.ArrayList;
import java.util.HashMap;

public class PeersUtils {

    // Counter -> peers with this counter
    public static HashMap<Integer, ArrayList<Info>> getPeersCounters(Peer p, ArrayList<Info> nonSeeders) {
        HashMap<Integer, ArrayList<Info>> peersForSelection = new HashMap<>();
        // get the counters
        for (int i = 0; i < nonSeeders.size(); i++) {
            if (p.usernameToDownloadedFiles.contains(nonSeeders.get(i).username)) {
                int counter = p.usernameToDownloadedFiles.get(nonSeeders.get(i).username);
                if (!peersForSelection.containsKey(counter)) {
                    peersForSelection.put(counter, new ArrayList<>());
                }
                peersForSelection.get(counter).add(nonSeeders.get(i));
            } else {
                if (!peersForSelection.containsKey(0)) {
                    peersForSelection.put(0, new ArrayList<>());
                }
                // we haven't received from this peer yet
                peersForSelection.get(0).add(nonSeeders.get(i));
            }
        }
        return peersForSelection;
    }

    // Counter -> peers with this counter
    public static HashMap<Integer, ArrayList<AnyToPeer>> getRequestsCounters(Peer p, ArrayList<AnyToPeer> requests) {
        HashMap<Integer, ArrayList<AnyToPeer>> peersForSelection = new HashMap<>();
        // get the counters
        for (int i = 0; i < requests.size(); i++) {
            if (p.usernameToDownloadedFiles.contains(requests.get(i).myInfo.username)) {
                int counter = p.usernameToDownloadedFiles.get(requests.get(i).myInfo.username);
                if (!peersForSelection.containsKey(counter)) {
                    peersForSelection.put(counter, new ArrayList<>());
                }
                peersForSelection.get(counter).add(requests.get(i));
            } else {
                if (!peersForSelection.containsKey(0)) {
                    peersForSelection.put(0, new ArrayList<>());
                }
                // we haven't received from this peer yet
                peersForSelection.get(0).add(requests.get(i));
            }
        }
        return peersForSelection;
    }
}
