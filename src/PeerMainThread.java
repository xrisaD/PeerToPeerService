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
        System.out.println("Start...");
        while(true){

            //print menu
            System.out.println();
            System.out.println("----------------------------");
            System.out.println("MENU: \n0: REGISTER \n1: LOGIN \n 2: LOGOUT \n 3: DETAILS \n 4: SIMPLE_DOWNLOAD");
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
                    if (statusCode != null) {
                        if (statusCode == StatusCode.SUCCESSFUL_REGISTER) {
                            registered = true;
                        } else
                            p.askForNewUserName();
                    }
                } else if (func == 0 && registered) {
                    System.out.println("You are already registered");
                }
                // LOGIN
                else if (func == 1 && !loggedin) {
                    StatusCode statusCode = p.login();
                    if (statusCode != null) {
                        if (statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                            loggedin = true;
                        } else {
                            p.askForNewUserNameAndPassword();
                        }
                    }
                } else if (func == 1 && loggedin) {
                    System.out.println("You are already logged in");
                }
                // DOWNLOAD
                else if (loggedin) {
                    //katevasma file
                }
            }
        }
    }
}
