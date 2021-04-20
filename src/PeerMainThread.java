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
        boolean registered = false;
        boolean loggedin = false;
        while(true){

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

            if(func>=0 && func<=3) {
                // REGISTER
                if (func == 0 && !registered) {
                    System.out.println();
                    System.out.println("START REGISTER MESSAGES");
                    StatusCode statusCode = p.register();
                    System.out.println(statusCode);
                    if (statusCode == StatusCode.SUCCESSFUL_REGISTER) {
                        registered = true;
                    } else{
                        p.askForNewUserName();
                    }
                    System.out.println("END REGISTER MESSAGES");
                    System.out.println();
                } else if (func == 0 && registered) {
                    System.out.println("You are already registered");
                }
                // LOGIN
                else if (func == 1 && !loggedin) {
                    System.out.println();
                    System.out.println("START LOGIN MESSAGES");

                    StatusCode statusCode = p.login();
                    System.out.println(statusCode);
                    if (statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                        loggedin = true;
                    } else {
                        p.askForNewUserNameAndPassword();
                    }
                    System.out.println("END LOGIN MESSAGES");
                    System.out.println();
                } else if (func == 1 && loggedin) {
                    System.out.println("You are already logged in");
                }
                //LOGOUT
                else if(func == 2 && loggedin) {
                    System.out.println();
                    System.out.println("START LOGOUT MESSAGES");

                    StatusCode statusCode = p.logout();
                    System.out.println(statusCode);
                    if (statusCode == StatusCode.SUCCESSFUL_LOGOUT) {
                        System.out.println("You logged out successfully");
                        loggedin = false;
                        p.setToken_id(-1);
                    }else {
                        System.out.println("You are still logged in.. Unsuccessful logout!");
                    }
                    System.out.println("END LOGOUT MESSAGES");
                    System.out.println();
                }
                // LIST
                else if (func == 3 &&  loggedin) {
                    System.out.println();
                    System.out.println("START LIST MESSAGES");

                    ArrayList<String> allFiles = p.list();
                    String fileName = printAllFilesListAndAskForASpecificFile(allFiles);
                    ArrayList<Info> peers = p.details(fileName);
                    if(peers!=null) {
                        HashMap<Double, Info> scores = p.computeScores(peers);
                        boolean successfulDownload = p.simpleDownload(fileName, scores);
                        System.out.println("Download completed successfully: " + successfulDownload);
                        if(successfulDownload){
                            p.fileTitles.add(fileName);
                        }
                    }else{
                        System.out.println("No peer with this file");
                    }

                    System.out.println("END LIST MESSAGES");
                    System.out.println();
                }
                else { //func=2,3
                    System.out.println("You are not logged in");
                }
            }else{
                System.out.println("You have to type a number in range [0,3]");
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
        // get a valid number of file
        while(fileNumber<0 || fileNumber>=allFiles.size()) {
            number = scanner.nextLine();
            fileNumber = Integer.parseInt(number);
        }
        return numberToFile.get(fileNumber);
    }
}
