package baccarat.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//NUS\coding\day8workshop> javac -d classes --source-path src src/baccarat/server/*.java   
//NUS\coding\day8workshop> java -cp classes baccarat.server.ServerApp 12345 4
public class ServerApp {

    static volatile boolean running = true;

    public static void main(String[] args) throws IOException{
        int port = Integer.parseInt(args[0]);

        ExecutorService thrPool = Executors.newFixedThreadPool(3); // should allow 3 clients to connect
        Deck deck =  new Deck(Integer.parseInt(args[1]));   //create deck object with n decks of cards
        // for(int i = 0; i < 52 * 2; i++){
        //     Cards chosenCard = deck.get(i);
        //     System.out.printf("Card %d, suite: %s, name: %s, value: %d\n", i, chosenCard.getSuite(), chosenCard.getRank(), chosenCard.getValue());
        // }

        ServerSocket server = new ServerSocket(port);
        System.out.println("Accepting player connections...");

        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running) { //while running is true, we are continously scanning
                    if (scanner.nextLine().equalsIgnoreCase("shutdown")) {
                    System.out.println("shutting down the Baccarat server...");
                    running = false;
                    thrPool.shutdown(); //close all the thread resources
                    server.close();     //closes the serverside
                    scanner.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start(); //start the thread to scan

        while(running){
            try{
                Socket sock = server.accept();                      //continously accept client connection
                System.out.println("Player found and connected!");
                BaccaratEngine bacca = new BaccaratEngine(sock);

                // thrPool.execute(c); //for single thread
                thrPool.submit(bacca);                              //for multiple, submits cookie handler to the thread pool
            }catch (IOException e) {
                if (running) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            }
        }





    }
    
}
