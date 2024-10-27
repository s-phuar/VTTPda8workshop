package baccarat.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


public class Deck {
    public static final String[] SUITE = {"Hearts", "Diamonds", "Spades", "Clubs"};
    public static final String[] SUITEValue = {"1", "2", "3", "4"};
    public static final String[] NAMES =
    {"Ace", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King"};
    public static final int[] VALUES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

    //Cards array holding each of the cards for 1 or more decks
    //use this keyword to refer to the cards ArrayList tied  to the current instance of the Deck class
    ArrayList<Cards> cards = new ArrayList<>();
    int idx = 0; //count number of cards

    public Deck(int n) throws IOException{
        int count = 0; //count number of decks
        while(count < n){
        for(int i = 0; i < SUITE.length; i++){
            String suite = SUITE[i];
            String suitevalue = SUITEValue[i];
            for(int j = 0;  j < NAMES.length; j++){
                String name = NAMES[j];
                int value = VALUES[j];
                Cards card = new Cards(suite, suitevalue, name, value);
                //index for all 52 cards
                cards.add(card);
                idx++;
            }
        }
        count++;
    }
    Collections.shuffle(cards);
    storeShuffledDeck();
    }

    //obtain Cards object in 'this' current instance of the Deck class's ArrayList(cards)
    //e.g
        //Deck deck1 = new Deck(1);
        //Deck deck2 = new Deck(2);
        //deck1.getCard(0) returns the Cards object at pos(n) generated in deck1, where this refers to deck1
        //deck2.getCard(0) returns the Cards object at pos(n) generated in deck2, where this refers to deck2
    public Cards getCard(int pos){  //this getCard is a method I created part of the Deck class
        return this.cards.get(pos); //this get is a method part of the ArrayList class
    }

    public void storeShuffledDeck() throws IOException{
        Path cardPath = Paths.get("src/baccarat/server/cards.db");
        File cardFile = cardPath.toFile();
            if(!cardFile.exists()){
                cardFile.createNewFile(); 
            }
           
        Writer writer = new FileWriter(cardFile, false);
        //treatment of old data
            //append true appends new data to old data in the existing file
            //appned false overwrites old data in the existing file with new data
            //treatment happens only ONCE at the time the FileWriter is instantiated
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        for(int i = 0; i < cards.size(); i++){
            //for every Cards object in the cards ArrayList, this(referring to instantiated deck object) stored as chosenCard
            Cards chosenCard = this.getCard(i); //this.cards.get(pos)
            bufferedWriter.write(chosenCard.getValue() + "." + chosenCard.getSuiteValue() + "\n");
            bufferedWriter.flush();
            
        }
        bufferedWriter.close();
    }


}
