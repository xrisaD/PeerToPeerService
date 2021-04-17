import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class PeerMainThread extends Thread {
    Peer p;
    PeerMainThread(Peer p){
        this.p = p;
    }
    @Override
    public void run() {
        System.out.println("Start Peer's thread for command line requests... ");
        boolean registered = false;
        boolean loggedin = false;
        System.out.println("Start PeerMainThread...");
        while(true){

            System.out.println(p.getIp()+p.getPort());
            //print menu
            System.out.println();
            System.out.println("----------------------------");
            System.out.println("MENU: \n0: REGISTER \n1: LOGIN \n2: LOGOUT \n3: LIST");
            System.out.println("----------------------------");
            System.out.println();

            // get input
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            int func = parseInt(input);

            if(func>=0 && func<=1) {
                // REGISTER
                if (func == 0 && !registered) {
                    StatusCode statusCode = p.register();
                    if (statusCode == StatusCode.SUCCESSFUL_REGISTER) {
                        registered = true;
                    } else
                        p.askForNewUserName();
                } else if (func == 0 && registered) {
                    System.out.println("You are already registered");
                }
                // LOGIN
                else if (func == 1 && !loggedin) {
                    StatusCode statusCode = p.login();
                    if (statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                        System.out.println("SUCCESSFUL_LOGIN");
                        loggedin = true;
                    } else {
                        p.askForNewUserNameAndPassword();
                    }
                } else if (func == 1 && loggedin) {
                    System.out.println("You are already logged in");
                }
                //LOGOUT
                else if(func == 2) {
                    System.out.println("OKKKK");
                    StatusCode statusCode = p.logout();
                    if (statusCode == StatusCode.SUCCESSFUL_LOGOUT) {
                        System.out.println("You logged out successfully");
                        loggedin = false;
                        p.setToken_id(-1);
                    }else {
                        System.out.println("You are still logged in.. Unsuccessful logout!");
                    }
                }
                // LIST
                else if (func == 3 &&  loggedin) {
                    ArrayList<String> allFiles = p.list();
                    String fileName = printAllFilesListAndAskForASpecificFile(allFiles);
                    ArrayList<Info> peers = p.details(fileName);
                    HashMap<Double, Info> scores = p.computeScores(peers);
                    boolean successfulDownload = p.simpleDownload(fileName, scores);
                    System.out.println("Download completed successfully: " + successfulDownload);
                }
                else if(!loggedin){ //func=2,3
                    System.out.println("You are not logged in");
                }
            }
        }
    }
    public String printAllFilesListAndAskForASpecificFile(ArrayList<String> allFiles){
        HashMap<Integer,String> numberToFile = new HashMap<Integer,String>();
        System.out.println("Available files: (Enter the number of the file you want to download)");
        for(int i = 0; i < allFiles.size(); i++){
            String fileName = allFiles.get(i);
            numberToFile.put(i, fileName);
            System.out.println(i + " : " + fileName);
        }

        Scanner scanner = new Scanner(System.in);

        String number = scanner.nextLine();
        int fileNumber = Integer.parseInt(number);
        while(fileNumber<0 || fileNumber>=allFiles.size()) {
            number = scanner.nextLine();
            fileNumber = Integer.parseInt(number);
        }
        return numberToFile.get(fileNumber);
    }
}
