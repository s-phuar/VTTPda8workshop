package baccarat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BaccaratEngine implements Runnable{
    private final Socket sock;

    public BaccaratEngine(Socket s){ //pass freshly created socket from server
        sock = s;
    }
    Path cardPath = Paths.get("src/baccarat/server/cards.db");
    File cardFile = cardPath.toFile();
    ArrayList<String> cards = new ArrayList<>();
    ArrayList<String> playerHand = new ArrayList<>();
    ArrayList<String> bankerHand = new ArrayList<>();

    
    @Override
    public void run() {
        //client detect Login kenneth 100
        //client send login|kenneth|100 
        try (//opening stream to transmit baccarat data over to client
            OutputStream os = sock.getOutputStream()) {
            Writer writer = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(writer);

            //get the input stream to receive commands from client
            InputStream is = sock.getInputStream();
            Reader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);

            int balance = 0;            
            int betAmount = 0;            
            int betOnTable = 0;
            String user = "unknown";
            File playerFile = null;
            FileWriter fw = null;
            boolean open = true;
            while(open){
                String cmd = br.readLine(); //read instructions from clients
                String[] cmdSplit = cmd.split("\\|");

                switch(cmdSplit[0]){
                    case "login":
                        user = cmdSplit[1];
                        Path playerPath = Paths.get("src/baccarat/server/" + cmdSplit[1] + ".db");
                        playerFile = playerPath.toFile();
                        if(!playerFile.exists()){
                        playerFile.createNewFile(); 
                        }
                        //write to file
                        fw = new FileWriter(playerFile, false);
                        fw.write(cmdSplit[2]); //update balance.db
                        balance = Integer.parseInt(cmdSplit[2]);
                        fw.flush();
                        System.out.printf("%s has logged in with a balance of: %d\n", user, balance);
                    break;
    
                    case "bet":
                        betAmount = Integer.parseInt(cmdSplit[1]);
                        if(betAmount > balance){
                            System.out.println("Player tried to bet more funds than they have");
                            betAmount = 0;
                        }else{
                            if(fw != null){
                                balance -= betAmount;
                                betOnTable += betAmount;
                                //write to user db file
                                fw = new FileWriter(playerFile, false);
                                fw.write(String.valueOf(balance));
                                fw.flush();

                                System.out.printf("%s bet %s at the table, updated balance is: %d\n", user, betAmount, balance);
                                System.out.printf("bet on the table is: %d\n", betOnTable);
                            }else{
                                System.out.println("Please login first!");
                            }
                        }

                    break;
    
                    case "deal":
                        String side = cmdSplit[1];
                        loadDeck(cardFile);
                        String result = dealCards(cards);
                        // System.out.println(result); //testing

                        if(betOnTable == 0){
                            System.out.println("No valid bet");
                            break;
                        }   

                        try{
                        if((side.equals("b") && result.equals("banker"))||(side.equals("p") && result.equals("player"))){

                            System.out.printf("%s won\n", user);
                            betOnTable = betOnTable * 2;
                            balance += betOnTable;
                            betOnTable = 0;
                            betAmount = 0;
                            System.out.printf("%s won\n", user);
                            String outcome = String.join("|", "P", seeHand(playerHand, 0), seeHand(playerHand, 1), seeHand(playerHand, 2)+ ",B|" + seeHand(bankerHand, 0), seeHand(bankerHand, 1), seeHand(bankerHand, 2));
                            System.out.println(outcome);
                            fw = new FileWriter(playerFile, false);
                            fw.write(String.valueOf(balance));
                            bw.write(outcome);
                            bw.newLine();
                            fw.flush();
                            bw.flush();
                            
                            //write to client side about win P|1|10|3,B|10|10|7
                            //write to client side, they won and how much.
                            //balance increased by XX to XX
                            //update balance.db
                            clearHand();
                            
                        }else if(result.equals("draw")){
                            System.out.printf("%s tied\n", user);
                            balance += betOnTable;
                            betOnTable = 0;
                            betAmount = 0;
                            System.out.printf("%s tied\n", user);
                            String outcome = String.join("|", "P", seeHand(playerHand, 0), seeHand(playerHand, 1), seeHand(playerHand, 2)+ ",B|" + seeHand(bankerHand, 0), seeHand(bankerHand, 1), seeHand(bankerHand, 2));
                            System.out.println(outcome);
                            fw = new FileWriter(playerFile, false);
                            fw.write(String.valueOf(balance));
                            bw.write(outcome);
                            bw.newLine();
                            fw.flush();
                            bw.flush();
                            clearHand();
                            //write to client side about draw, current balance stay the same

                        }else{ //lose
                            System.out.printf("%s lost\n", user);
                            betOnTable = 0;
                            betAmount = 0;
                            //write to client side about lost P|1|10|3,B|10|10|7
                            //write current balance, balance increased by XX to 
                            System.out.printf("%s lost\n", user);
                            String outcome = String.join("|", "P", seeHand(playerHand, 0), seeHand(playerHand, 1), seeHand(playerHand, 2)+ ",B|" + seeHand(bankerHand, 0), seeHand(bankerHand, 1), seeHand(bankerHand, 2));
                            System.out.println(outcome);
                            fw = new FileWriter(playerFile, false);
                            fw.write(String.valueOf(balance));
                            bw.write(outcome);
                            bw.newLine();
                            fw.flush();
                            bw.flush();
                            clearHand();
                        }

                        }catch(IOException e){
                            System.err.println("Something wrong");
                        }
                    break;

                    case "exit":
                        //assuming user leaving was previously playing
                        user = cmdSplit[1];
                        System.out.printf("%s has left the table\n", user);
                    break;
    
                    default:
                    System.out.println("try again");
    
                }
                }
            
            
            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDeck(File cardFile) throws IOException{
        BufferedReader brc = new BufferedReader(new FileReader(cardFile));
        String line = brc.readLine();
        cards = new ArrayList<>();
       
        // populate arraylist with cards.db
        while (line != null) {
            cards.add(line);
            line = brc.readLine();
        }
        brc.close();
    }

    public void updateDeck(File cardFile, ArrayList<String> cards) throws IOException{
        BufferedWriter bwc = new BufferedWriter(new FileWriter(cardFile));
        for(String str: cards){
            bwc.write(str + System.lineSeparator());
        }
        bwc.flush();
        bwc.close();
    }


    public String dealCards(ArrayList<String> cards) throws IOException{
        int playerSum = 0;
        int bankerSum = 0;
        String result = null;

        if(cards.size()< 4){
            System.out.println("not enough cards in the deck");
        }else{
            for(int i = 0; i < 2; i++){
            playerHand.add(cards.get(0));
            cards.remove(0);
            bankerHand.add(cards.get(0));
            cards.remove(0);
            }
        }
        updateDeck(cardFile, cards);

        // ****************
        if(checkHand(playerHand) < 15){
            playerHand.add(cards.get(0));
            cards.remove(0);
        }   

        if(checkHand(bankerHand) < 15){
            bankerHand.add(cards.get(0));
            cards.remove(0);
        }
        updateDeck(cardFile, cards);

        playerSum = checkHand(playerHand);
        bankerSum = checkHand(bankerHand);

        // System.out.println(playerSum);
        // System.out.println(bankerSum);

        playerSum %= 10;
        bankerSum %= 10;

        if(playerSum > bankerSum){
            result = "player";
        }
        if(playerSum < bankerSum){
            result =  "banker";
        }
        if(playerSum == bankerSum){
            result = "draw";
        }
        return result;
    }


    public int checkHand(ArrayList<String> currentHand){
        int value = 0;
        int temporary = 0;
        // System.out.println(value);
        for(int i = 0; i < currentHand.size(); i++){
            String card = currentHand.get(i);

            temporary = Integer.parseInt(card.split("\\.")[0]);
            if(temporary > 10){
                temporary = 10;
            }
            value += temporary;            
        }
        return value;
    }

    public String seeHand(ArrayList<String> currentHand, int pos){
        int temp = 0;
        if(pos < 0 || pos >= currentHand.size()){ // extremely important to check whether we drew a third card or not
            return "0";
        }

        if(currentHand.get(pos) == null){
            return "0";
        }

        if(Integer.parseInt(currentHand.get(pos).split("\\.")[0]) > 10){
            temp = 10;
        }else{
            temp = Integer.parseInt(currentHand.get(pos).split("\\.")[0]);
        }
            return Integer.toString(temp);
    }


    public void clearHand(){
        playerHand.clear();
        bankerHand.clear();
    }




}
