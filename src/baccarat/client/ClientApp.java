package baccarat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

//NUS\coding\day8workshop> javac -d classes --source-path src src/baccarat/client/*.java
//NUS\coding\day8workshop> java -cp classes baccarat.client.ClientApp localhost:12345

public class ClientApp {

    public static String getResults(BufferedReader br) throws IOException{
        String fromServer = br.readLine(); //get server message

        if (fromServer == null || !fromServer.contains(",")) {
            return "Invalid server response";
        }

        //transform server output
        String[] fromServerArr = fromServer.split( ",");
        String[] playerHand = fromServerArr[0].split("\\|");
        String[] bankerHand = fromServerArr[1].split("\\|");

        if (fromServerArr.length < 2) {
            return "Invalid result format"; // Ensure we have both sides
        }

        int playerResult = 0;
        int BankerResult = 0;
        String finalResult = null;

        for(int i = 1; i < playerHand.length; i++){
            playerResult += Integer.parseInt(playerHand[i]);
            BankerResult += Integer.parseInt(bankerHand[i]);
        }

        playerResult %= 10;
        BankerResult %= 10;

        if (playerResult > BankerResult){
            finalResult = String.format("Player wins with %d points", playerResult);
        }

        if (playerResult < BankerResult){
            finalResult = String.format("Banker wins with %d points", BankerResult);
        }
        if (playerResult == BankerResult){
            finalResult = String.format("Player ties with Banker with %d points", BankerResult);
        }

        return finalResult;
    }

    public static void main(String[] args) throws UnknownHostException, IOException{
        String[] host = args[0].split(":");
        int port = Integer.parseInt(host[1]);

        System.out.println("Connecting to the Baccarat server...");
        Socket sock = new Socket(host[0], port);
        System.out.println("Connected!");


        //get the input stream to receive from server
        InputStream is = sock.getInputStream();
        Reader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);

        //outputsteam to send command to baccarat server
        OutputStream os = sock.getOutputStream();
        Writer writer = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(writer);

        String user = "unknown";
        boolean open = true;

        while(open){
        //switch case + console input
        Console cons = System.console();
        String cmd = cons.readLine("Enter command> ").trim().toLowerCase();
        String[] cmdSplit = cmd.split(" ");

        switch(cmdSplit[0]){
            case "login":
                user = cmdSplit[1];
                bw.write(String.join("|", "login", cmdSplit[1], cmdSplit[2])); // send command to server
                bw.newLine();
                bw.flush();
            break;
    
            case "bet":
            System.out.println("working on bet");
                bw.write(String.join("|", "bet", cmdSplit[1]));
                bw.newLine();
                bw.flush();


            break;

            case "deal":
            System.out.println("working on deal");

                bw.write(String.join("|", "deal", cmdSplit[1]));
                bw.newLine();
                bw.flush();
                System.out.println(getResults(br));


            break;

            case "exit":
                bw.write(String.join("|", "exit", cmdSplit[1]));
                bw.newLine();
                bw.flush();
                System.out.printf("%s has left the table\n", user);
                bw.close();
                os.close();
                br.close();
                is.close();
                sock.close();
                open = false;
                // System.exit(0);
            break;

            default:
                System.out.println("Please use a valid command:");
                System.out.println("1. Login: to create a user database with specified value");
                System.out.println("2. Bet: to play at the table");
                System.out.println("3. Deal: to choose who to bet on");
                System.out.println("4. Exit: to leave table");
    }
    }
    }
}
