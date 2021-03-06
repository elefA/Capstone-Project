import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PokerGame {
    private ServerSocket ss;
    private int numPlayers;

    private ArrayList<ServerSideConnection> players;
    private ArrayList<ServerSideConnection> playQueue;

    /*
    0,13,26,39 are aces
    Hearts are 0 through 12
    Diamonds 13 through 25
    Clubs 26 through 38
    Spades 39 through 51
     */
    public static void main(String[] args) {

        //Variable Initialization of the main class
        PokerGame gameServer = new PokerGame();

        //Method that loops constantly to accept new users that want to connect to the server
        gameServer.acceptConnections();

    }
    public void acceptConnections() {
        try {
            System.out.println("Waiting connections");
            WaitForPlayers wait = new WaitForPlayers();
            Thread a = new Thread(wait);
            a.start();
            while (true) {
                Socket s = ss.accept();
                numPlayers++;
                System.out.println("Player #" + numPlayers + " has connected");
                ServerSideConnection ssc = new ServerSideConnection(s, players.size());

                players.add(ssc);
                Thread t = new Thread(ssc);
                t.start();

            }
        } catch (IOException e) {
            System.out.println("IO Exception caught while accepting connections");
        }
    }
    //Waits for users to start a game and initializes their game.
    public class WaitForPlayers implements Runnable{

        @Override
        public void run() {
            while(true) {
                while (playQueue.size() < 2) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                Poker newGame = new Poker(playQueue.get(0), playQueue.get(1));
                System.out.println("GAME STARTED");
                Thread b=new Thread(newGame);
                b.start();
                playQueue.clear();
            }
        }
    }


    public class ServerSideConnection implements Runnable {

        private Socket socket;
        private ObjectInputStream dataIn;
        private ObjectOutputStream dataOut;
        private String username = "";
        public ServerSideConnection(Socket s, int id) {
            socket = s;
            try {
                dataOut = new ObjectOutputStream(s.getOutputStream());
                dataOut.flush();
                dataIn = new ObjectInputStream(s.getInputStream());
                System.out.println("Streams are set up");
            } catch (IOException e) {
                System.out.println("IOException from run() SSC");
                closeConnection(this);
            }

        }
        @Override
        public void run() {
            try {
                try {
                    String logInOption = (String) dataIn.readObject();
                    if (logInOption.equals("guest")) {
                        username = "guest";

                        String menuOption = (String) dataIn.readObject();
                        if (menuOption.equals("play"))
                        {
                            playQueue.add(this);
                        }
//This code is here for future increments
//                    } else if (logInOption.equals("register")) {
//                        username = (String) dataIn.readObject();
//                        String password = (String) dataIn.readObject();
//                        //Look in database to check if username already exists and that password
//                        //is not null and then if it does not exist set username back to empty7
//                        //code here
//
//                        //back to log in menu
//                        run();
//                    } else if (logInOption.equals("login")) {
//                        username = (String) dataIn.readObject();
//                        String password = (String) dataIn.readObject();
//                        //Look in database to check if username already exists and that password
//                        //is not null and then if it does not exist set username back to empty7
//                        //code here
                   }

                } catch (ClassNotFoundException ex) {
                    System.out.println("ClassNotFound ex in read in SSC");
                    playQueue.remove(playQueue.size()-1);
                    closeConnection(this);
                }

            } catch (IOException e) {
                e.printStackTrace();
                playQueue.remove(playQueue.size()-1);
                closeConnection(this);
                System.out.println("IOException from run ssc");
            }
        }
    }

    //Terminates Streams in a connection to a player and
    void closeConnection(ServerSideConnection player){

        try {
            System.out.println("Closing connection");
            player.dataOut.close();
            player.dataIn.close();
            player.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //This is where the game between the two players initialises and runs
    public class Poker implements Runnable {
        private int disconnected = 1;
        private ServerSideConnection player1;
        private ServerSideConnection player2;
        int[] player1Hand= new int[2];
        int[] player2Hand= new int[2];
        ArrayList<ServerSideConnection> players= new ArrayList<>();
        int[] board = new int[5];


        public Poker(ServerSideConnection player1, ServerSideConnection player2) {
            this.player1=player1;
            this.player2=player2;
        }

        @Override
        public void run() {
            ArrayList<Integer> deck = new ArrayList<>();
            players.add(player1);
            players.add(player2);
            try {
                player1.dataOut.writeObject(0);
                player2.dataOut.writeObject(1);
                player1.dataOut.flush();
                player2.dataOut.flush();

                String username1 = (String) player1.dataIn.readObject();
                String username2 = (String) player2.dataIn.readObject();


                player1.dataOut.writeObject(username2);
                player2.dataOut.writeObject(username1);

                player1.dataOut.flush();
                player2.dataOut.flush();

            }
            catch (IOException | ClassNotFoundException e){
                closeConnection(player1);
                closeConnection(player2);
                System.out.println("IOException in sending who is the dealer in the first hand");
            }
            //Initialize game variables
            State newGame = new State(0,0,-1,-1,0,0,-1);

            while (newGame.score1<5 && newGame.score2<5 && disconnected!=0) {
                for (int i =0;i<52;i++){
                    deck.add(i);
                }
                newHand(deck, newGame);
                if (newGame.dealer==1)newGame.dealer=0;else newGame.dealer=1;
            }
            closeConnection(player1);
            closeConnection(player2);
        }
        void newHand(ArrayList<Integer> deck,State game){

            dealHands(deck);

            
            try {
                if (game.dealer == 0) {
                    // If a move is == 1 then the player predicted a win else if == 0 a loss

                    //Read moves then send to the other player
                    game.move1=(int)player1.dataIn.readObject();
                    player2.dataOut.writeObject(game.move1);
                    player2.dataOut.flush();
                    game.move2=(int)player2.dataIn.readObject();
                    player1.dataOut.writeObject(game.move2);
                    player1.dataOut.flush();

                    //Send the hand of each player's opponent
                    player1.dataOut.writeObject(player2Hand[0]);
                    player1.dataOut.writeObject(player2Hand[1]);
                    player1.dataOut.flush();

                    player2.dataOut.writeObject(player1Hand[0]);
                    player2.dataOut.writeObject(player1Hand[1]);
                    player2.dataOut.flush();


                    //Open board cards
                    dealBoard(deck);


                    //If handWinner == 1 then Player1 won the hand else if == 0 Player2 Won else it's a tie
                    //Check Who won the hand
                    game.handWinner = checkWhoWon(player1Hand,player2Hand,board);

                    player1.dataOut.writeObject(game.handWinner);
                    player2.dataOut.writeObject(game.handWinner);

                    //Player1 predicted a win and won  Player1 wins a point
                    if (game.handWinner==1 && game.move1==1)game.score1++;
                    //Player1 predicted a loss and lost Player1 wins a point
                    else if (game.handWinner==0 && game.move1==0)game.score1++;

                    //Player2 predicted a win and won  Player2 wins a point
                    if (game.handWinner==0 && game.move2==1)game.score2++;
                    //Player2 predicted a loss and lost Player2 wins a point
                    else if (game.handWinner==1 && game.move2==0)game.score2++;






//                    //Player1 predicted a loss and lost while player2 also predicted a loss but won. Player1 wins a point
//                    else if (game.handWinner==0 && game.move1==0 && game.move2==0)game.score1++;
//                    // Player 2 predicted a loss and lost while player1 also predicted a loss but won. Player2 wins a point
//                    else if (game.handWinner==1 && game.move2==0 && game.move1!=1) game.score2++;
//                    // Player2 predicted a win and won while player1 predicted also a win. Player2 wins a point
//                    else if (game.handWinner==0 && game.move2==1 && game.move1!=0) game.score2++;
//                    //Both players predicted the wrong choice
//                    else if (game.handWinner==1 && game.move1==0 && game.move2==1);
//                    else if (game.handWinner==0 && game.move1==1 && game.move2==0);
//                    //It is a tie and both players won a point because they both predicted correctly
//                    else {game.score1++;game.score2++;}

                }

                else{
                    // If a move is == 1 then the player predicted a win else if == 0 a loss

                    //Read moves then send to the other player
                    game.move2=(int)player2.dataIn.readObject();
                    player1.dataOut.writeObject(game.move2);
                    player1.dataOut.flush();
                    game.move1=(int)player1.dataIn.readObject();
                    player2.dataOut.writeObject(game.move1);
                    player2.dataOut.flush();

                    //Send the hand of each player's opponent
                    player1.dataOut.writeObject(player2Hand[0]);
                    player1.dataOut.writeObject(player2Hand[1]);
                    player1.dataOut.flush();

                    player2.dataOut.writeObject(player1Hand[0]);
                    player2.dataOut.writeObject(player1Hand[1]);
                    player2.dataOut.flush();


                    //Open board cards
                    dealBoard(deck);


                    //If handWinner == 1 then Player1 won the hand else if == 0 Player2 Won else it's a tie
                    //Check Who won the hand
                    game.handWinner = checkWhoWon(player1Hand,player2Hand,board);

                    player1.dataOut.writeObject(game.handWinner);
                    player2.dataOut.writeObject(game.handWinner);

                    //Player1 predicted a win and won  Player1 wins a point
                    if (game.handWinner==1 && game.move1==1)game.score1++;
                        //Player1 predicted a loss and lost Player1 wins a point
                    else if (game.handWinner==0 && game.move1==0)game.score1++;

                    //Player2 predicted a win and won  Player2 wins a point
                    if (game.handWinner==0 && game.move2==1)game.score2++;
                        //Player2 predicted a loss and lost Player2 wins a point
                    else if (game.handWinner==1 && game.move2==0)game.score2++;


                }
            }
            catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
                closeConnection(player1);
                closeConnection(player2);
                disconnected = 0;
                return;

            }
        }
        void dealBoard(ArrayList<Integer> deck){
            board[0]=dealCard(deck);
            board[1]=dealCard(deck);
            board[2]=dealCard(deck);
            board[3]=dealCard(deck);
            board[4]=dealCard(deck);

            try {
                player1.dataOut.writeObject(board[0]);
                player1.dataOut.writeObject(board[1]);
                player1.dataOut.writeObject(board[2]);
                player1.dataOut.writeObject(board[3]);
                player1.dataOut.writeObject(board[4]);

                player2.dataOut.writeObject(board[0]);
                player2.dataOut.writeObject(board[1]);
                player2.dataOut.writeObject(board[2]);
                player2.dataOut.writeObject(board[3]);
                player2.dataOut.writeObject(board[4]);

                player1.dataOut.flush();
                player2.dataOut.flush();
                //Cards are sent

            } catch (IOException e) {
                e.printStackTrace();
                closeConnection(player1);
                closeConnection(player2);
            }

        }
        void dealHands(ArrayList<Integer> deck){
            player1Hand[0]=dealCard(deck);
            player2Hand[0]=dealCard(deck);
            player1Hand[1]=dealCard(deck);
            player2Hand[1]=dealCard(deck);

            try {
                player1.dataOut.writeObject(player1Hand[0]);
                player1.dataOut.writeObject(player1Hand[1]);
                player2.dataOut.writeObject(player2Hand[0]);
                player2.dataOut.writeObject(player2Hand[1]);

                player1.dataOut.flush();
                player2.dataOut.flush();
                //Cards are sent

            } catch (IOException e) {
                e.printStackTrace();
                closeConnection(player1);
                closeConnection(player2);
            }

        }



        private void dealRiver (ArrayList<Integer> deck){
            board[4] = dealCard(deck);
            try{
                player1.dataOut.writeObject(board[4]);
                player2.dataOut.writeObject(board[4]);
                player1.dataOut.flush();
                player2.dataOut.flush();
            }catch (IOException e){
                System.out.println("IOException in dealing River");
            }
        }


        private void dealTurn(ArrayList<Integer> deck) {
            board[3]=dealCard(deck);
            printCard(board[3]);
            try{
                player1.dataOut.writeObject(board[3]);
                player2.dataOut.writeObject(board[3]);
                player1.dataOut.flush();
                player2.dataOut.flush();
            }catch (IOException e){
                System.out.println("IOException in dealTurn");
            }
        }


        private void dealFlop(ArrayList<Integer> deck){
            board[0]=dealCard(deck);
            board[1]=dealCard(deck);
            board[2]=dealCard(deck);
            printCard(board[0]);
            printCard(board[1]);
            printCard(board[2]);
            try {
                player1.dataOut.writeObject(board[0]);
                player1.dataOut.flush();
                player1.dataOut.writeObject(board[1]);
                player1.dataOut.flush();
                player1.dataOut.writeObject(board[2]);

                player2.dataOut.writeObject(board[0]);
                player2.dataOut.flush();
                player2.dataOut.writeObject(board[1]);
                player2.dataOut.flush();
                player2.dataOut.writeObject(board[2]);

                player1.dataOut.flush();
                player2.dataOut.flush();
            }
            catch (IOException e){
                System.out.println("IOEException on dealing board cards");
            }
        }
        int allIn(ArrayList<Integer> deck){

            dealFlop(deck);
            dealTurn(deck);
            dealRiver(deck);



            return checkWhoWon(player1Hand,player2Hand,board);

        }
        int checkWhoWon(int[] hand1,int[] hand2,int[] board){
            int[] combined1 = combine(hand1,board);
            int[] combined2 = combine(hand2,board);
            int result1 = checkHand(combined1);
            int result2 = checkHand(combined2);
            return compareHands(result1,result2,combined1,combined2);
        }

        int dealCard(ArrayList<Integer> deck) {
            int random = ThreadLocalRandom.current().nextInt(0, deck.size());
            int card = deck.get(random);
            deck.remove(random);
            return card;
        }


        //Check who wins. If player1 wins return 1, if player2 wins return 0, and if it's a tie return -1
        public  int compareHands(int a, int b, int[] c1, int[] c2) {

            if (a == b) {
                if (a == 10) {
                    int temp = compareSFlush(straightFlush(c1), straightFlush(c2));

                    return temp;
                }
                if (a == 9) {
                    int temp = compareFullandKare(fourOfAKind(c1), fourOfAKind(c2));

                    return temp;
                }
                if (a==8){
                    int three1 = fourOfAKind(c1)[2];
                    int three2 = fourOfAKind(c2)[2];
                    int temp =compareFullandKare(fullHouse(c1,three1),fullHouse(c2,three2));
                    return temp;
                }
                if (a == 7) {
                    int temp = compareFlush(flush(c1), flush(c2));

                    return temp;
                }
                if (a == 6) {
                    int temp = compareStraight(straight(c1),straight(c2));

                    return temp;
                }
                if (a == 5) {
                    return compareThrees(threeOfAKind(c1), threeOfAKind(c2));
                }
                if (a == 4) {
                    return comparePairs(c1, c2);
                }
                if (a == 3) {
                    return comparePairs(c1, c2);
                }
                if (a == 2) {
                    int temp = compareHigh(highcards(c1), highcards(c2));

                    return temp;
                }
            }
            if (a > b) {

                return 1;
            }
            if (a < b) {
                return 0;
            }
            return -1;
        }


        public  int checkHand(int[] c) {
            ArrayList<Integer> flush = flush(c);


            int[] sflush = straightFlush(c);
            if (sflush[1] == -1) {
                int[] fOfAKind = fourOfAKind(c);
                switch (fOfAKind[1]) {
                    case -1:
                        if (flush.size() < 6) {
                            int[] straight = straight(c);
                            if (straight[1] == -1) {
                                int[] pairs = pairs(c);
                                if (pairs[1] == -1) {
                                    return 2;
                                } else if (pairs.length == 4) {
                                    return 4;
                                } else {
                                    return 3;
                                }
                            } else {
                                return 6;
                            }
                        } else {
                            return 7;
                        }
                    case -3:
                        if (fullHouse(c, fOfAKind[2])[2] == -1) {
                            if (straight(c)[1] == -1) {
                                return 6;
                            } else {
                                return 5;
                            }
                        } else {
                            return 8;
                        }
                    default:
                        return 9;
                }
            } else {
                return 10;
            }
        }



        //Checks for straight flush This does not need %13
        int[] straightFlush(int[] c) {
            Arrays.sort(c);
            int count2 = 0;
            int temp = -1;
            int[] temp1 = {10, temp};
            boolean ten = false;
            boolean jack = false;
            boolean queen = false;
            boolean king = false;
            int k;

            for (int i = 0; i < 3; i++) {

                for (k = 0; k < 4; k++) {
                    if ((c[k + i + 1]) - (c[k + i]) == 1) {
                        count2++;
                    }
                }
                if (count2 == 4) {
                    temp = c[k + i] % 13;
                    temp1[1] = temp;
                }
                count2 = 0;
            }
            if (c[0] % 13 == 0) {
                for (int o = 1; o < c.length - 1; o++) {
                    if (c[o] - c[0] == 9) {
                        ten = true;
                    } else {
                        break;
                    }
                    if (c[o] - c[0] == 10) {
                        jack = true;
                    } else {
                        break;
                    }
                    if (c[o] - c[0] == 11) {
                        queen = true;
                    } else {
                        break;
                    }
                    if (c[o] - c[0] == 12) {
                        king = true;
                    }
                }
                if (ten && jack && queen && king) {
                    temp = 0;
                    temp1[1] = temp;
                }
            }
            return temp1;
        }

        //Checks for Four and Three of a kind. This does need %13
        int[] fourOfAKind(int[] c1) {
            int[] c = new int[7];

            for (int i = 0; i < 7; i++) {

                c[i] = c1[i];
            }
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);

            int counter = 0;
            int d[] = {9, -1, -1};
            int three = -1;
            int i, j;
            for (i = 0; i < 4; i++) {
                for (j = 0; j < 3; j++) {
                    if (c[i + j] % 13 == c[i + j + 1] % 13) {
                        counter++;
                        if (counter == 2 && three != 0) {
                            three = c[i];
                        }
                    }
                }
                if (counter == 3) {
                    d[1] = c[3];
                    if (c[0] == 0 && c[0] != c[3]) {

                        d[2] = 0;
                        return d;
                    }
                    for (int k = 6; k > -1; k--) {
                        if (c[k] != c[3]) {
                            d[2] = c[k];
                            return d;
                        }
                    }
                }

                counter = 0;
            }

            if (three != -1) {
                d[1] = -3;
                d[2] = three;
                return d;
            }

            return d;
        }

        //Checks for Full House, This does need %13
        int[] fullHouse(int[] c1, int three) {
            int[] c = new int[7];

            for (int i = 0; i < 7; i++) {
                c[i] = c1[i];
            }
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);
            int[] d = {8, -1, -1};
            d[1] = three;
            if (c[0] == 0 && three != 0 && c[1] == 0) {
                d[1] = 0;
                return d;
            }
            for (int i = 6; i > 0; i--) {
                if (c[i] == c[i - 1] && c[i] != three) {
                    d[2] = c[i];
                    return d;
                }
            }
            return d;// This checks that there is a three of a kind and should be stored smw so that the straight and flush are checked first.
        }

        //Checks for flush, This does not need %13
        ArrayList<Integer> flush(int[] c) {

            Arrays.sort(c);
            ArrayList<Integer> hearts = new ArrayList<>();
            ArrayList<Integer> diamonds = new ArrayList<>();
            ArrayList<Integer> clubs = new ArrayList<>();
            ArrayList<Integer> spades = new ArrayList<>();
            hearts.add(7);
            spades.add(7);
            clubs.add(7);
            diamonds.add(7);

            for (int i = 0; i < c.length; i++) {
                if (c[i] < 13) {
                    hearts.add(c[i]);
                }
                if (c[i] > 12 && c[i] < 26) {
                    diamonds.add(c[i]);
                }
                if (c[i] > 25 && c[i] < 39) {
                    spades.add(c[i]);
                }
                if (c[i] > 38) {
                    clubs.add(c[i]);
                }
            }
            if (clubs.size() > 5) {
                return clubs;
            }
            if (hearts.size() > 5) {
                return hearts;
            }
            if (spades.size() > 5) {
                return spades;
            }
            if (diamonds.size() > 5) {
                return diamonds;
            }
            return clubs;
        }

        //Checks for straight, This also needs %13 on input
        int[] straight(int[] c1) {
            int[] c = new int[7];

            for (int i = 0; i < 7; i++) {
                c[i] = c1[i];
            }
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);
            int count2 = 0;
            int[] temp = {6, -1};
            boolean ten = false;
            boolean jack = false;
            boolean queen = false;
            int k;
            int p;
            HashSet<Integer> set = new LinkedHashSet<>();
            for (int num : c) {
                set.add(num);
            }
            Object[] c2 = set.toArray();

            switch (set.size()) {
                case 5:
                    p = 1;
                    break;
                case 6:
                    p = 2;
                    break;
                case 7:
                    p = 3;
                    break;
                default:
                    return temp;
            }
            for (int i = 0; i < p; i++) {
                for (k = 0; k < 4; k++) {
                    if (((int) c2[k + i + 1] % 13) - ((int) c2[k + i] % 13) == 1) {
                        count2++;
                    }
                }
                if (count2 == 4) {
                    temp[1] = (int) c2[k + i] % 13;
                }
                count2 = 0;
            }
            if (c[0] % 13 == 0 && c[6] % 13 == 12) {
                for (int o = 1; o < c.length - 1; o++) {
                    if (c[o] % 13 == 9) {
                        ten = true;
                    }
                    if (c[o] % 13 == 10) {
                        jack = true;
                    }
                    if (c[o] % 13 == 11) {
                        queen = true;
                    }
                }
                if (ten && jack && queen) {
                    temp[1] = 0;//Ace Kicker
                }
            }
            return temp;
        }

        //Checks for Three of A KInd
        int[] threeOfAKind(int[] c) {
            int three = -1;
            int highCard1 = -1;
            int highCard2 = -1;
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);
            int[] fivecards = {5, three, highCard1, highCard2};
            if (c[0] == c[1] && c[0] == c[2] && c[0] == 0) {
                three = 0;
                highCard1 = c[6];
                highCard2 = c[5];
                fivecards[1] = three;
                fivecards[2] = highCard1;
                fivecards[3] = highCard2;
                return fivecards;
            }
            for (int i = 6; i > 1; i--) {
                if (c[i] == c[i - 1] && c[i] == c[i - 2]) {
                    three = c[i];
                }
            }
            if (c[0] == 0 && c[0] != three) {
                highCard1 = 0;
            }
            for (int i = 6; i > -1; i--) {
                if (c[i] != three && highCard1 == -1) {
                    highCard1 = c[i];
                } else if (c[i] != three && highCard2 == -1) {
                    highCard2 = c[i];
                }

            }
            fivecards[1] = three;
            fivecards[2] = highCard1;
            fivecards[3] = highCard2;

            return fivecards;
        }

        //Checks for pairs,. This method requires a sorting of %13 cards
        int[] pairs(int[] c1) {
            int[] c = new int[7];

            for (int i = 0; i < 7; i++) {
                c[i] = c1[i];
            }
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);

            int pair1 = -1;
            int pair2 = -1;

            int j = 0;
            if (c[0] % 13 == 0 && c[1] % 13 == 0) {
                pair1 = 0;
                j = 2;
            }
            for (int i = 6; i > j; i--) {
                if (c[i] == c[i - 1]) {
                    if (pair1 == -1) {
                        pair1 = c[i] % 13;
                    } else if (pair2 == -1) {
                        pair2 = c[i] % 13;
                    }
                }
            }
            if (pair2 == -1 && pair1 == -1) {
                int[] d = {3, -1};
                return d;
            }
            if (pair2 != -1) {
                int[] fivecards = {4, pair1, pair2, -1};
                if (c[0] != pair1 && c[0] != pair2 && c[0] % 13 == 0) {
                    fivecards[3] = c[0] % 13;
                    return fivecards;
                } else {
                    for (int i = 6; i > 0; i--) {
                        if (c[i] != pair1 && c[i] != pair2 && fivecards[3] == -1) {
                            fivecards[3] = c[i] % 13;

                        }
                    }

                    return fivecards;
                }

            }
            //1 pair case
            int[] fivecards = {3, pair1, -1, -1, -1};
            int l = 2;
            //ace high card case
            if (c[0] % 13 == 0 && c[0] % 13 != pair1) {
                fivecards[2] = c[0] % 13;
                l++;

                for (int i = 6; i > 0 && l < 5; i--) {
                    if (c[i] != pair1) {
                        fivecards[l] = c[i] % 13;
                        l++;
                    }
                }
                return fivecards;
            }

            for (int i = 6; i > -1 && l < 5; i--) {
                if (c[i] != pair1) {
                    fivecards[l] = c[i % 13];
                    l++;
                }

            }
            return fivecards;

        }

        //Checks for High Card, This does need %13
        int[] highcards(int[] c) {
            for (int i = 0; i < 7; i++) {
                c[i] = c[i] % 13;
            }
            Arrays.sort(c);
            int[] fivecards = {2, -1, -1, -1, -1, -1};
            int j = -1;
            if (c[0] % 13 == 0) {
                fivecards[1] = 0;
                j = 2;
                for (int i = 6; i > 2; i--, j++) {
                    fivecards[j] = c[i];
                }
                return fivecards;
            }
            j = 1;
            for (int i = 6; i > 1; i--, j++) {
                fivecards[j] = c[i];
            }
            return fivecards;

        }

        //Compare high cards
        int compareHigh(int[] c1, int[] c2) {
            if (c1[1] % 13 == 0 && c2[1] % 13 != 0) {
                return 1;
            }
            if (c2[1] % 13 == 0 && c1[1] % 13 != 0) {
                return 0;
            }
            for (int i = 5; i > 0; i--) {
                if (c1[i] == c2[i]) {
                    continue;
                }
                if (c1[i] > c2[i]) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        }

        //Compare pairs
        int comparePairs(int[] comb1, int[] comb2) {
            int[] c1= pairs(comb1);
            int[] c2= pairs(comb2);
            //2 pairs vs 1 pair case
            if (c1.length == 4 && c2.length == 5) {
                return 1;
            }
            if (c2.length == 4 && c1.length == 5) {
                return 0;
            }
            if (c1.length == 5 && c2.length == 5) {
                //Cases with ace

                if (c1[1] == 0 && c2[1] != 0) {
                    return 1;
                }
                if (c1[1] != 0 && c2[1] == 0) {
                    return 0;
                }

                //Who has the highest pair
                if (c1[1] > c2[1]) {
                    return 1;
                }
                if (c1[1] < c2[1]) {
                    return 0;
                }
                //both players have same pair
                if (c1[1] == c2[1]) {
                    //Check if someone has high card ace
                    if (c1[2] == 0 && c2[2] != 0) {
                        return 1;
                    } else if (c1[2] != 0 && c2[2] == 0) {
                        return 0;
                    }
                    for (int i = 2; i < 5; i++) {
                        if (c1[i] > c2[i]) {
                            return 1;
                        } else if (c1[i] < c2[i]) {
                            return 0;
                        }
                    }
                }
            }
            //Both players have 2 pairs case



            if (c1.length == 4 && c2.length == 4) {
                //Identical hands
                if (c2[2] == c1[2] && c2[3] == c1[3] && c1[1] == c2[1]) {
                    return -1;
                }
                //Ace case
                if (c1[1] == 0 && c2[1] != 0) {
                    return 1;
                }
                if (c2[1] == 0 && c1[1] != 0) {
                    return 0;
                }
                //Both players have pairs of aces
                if (c1[1] == 0 /*&& c2[0]==0*/) {

                    if (c1[2] > c2[2]) {
                        return 1;
                    } else if (c2[2] > c1[2]) {
                        return 0;
                    }

                    if (c1[3] > c2[3]) {
                        return 1;
                    } else if (c2[3] > c1[3]) {
                        return 0;
                    }

                }
                //All other cases
                if (c1[1] > c2[1]) {
                    return 1;
                } else if (c2[1] > c1[1]) {
                    return 0;
                }

                if (c1[2] > c2[2]) {
                    return 1;
                } else if (c2[2] > c1[2]) {
                    return 0;
                }

                if (c1[3] > c2[3]) {
                    return 1;
                } else {
                    return 0;
                }
            }

            return -1;
        }

        //Compare Three of  akind
        int compareThrees(int[] c1, int[] c2) {
            if (c1[1] == c2[1]) {
                if (c1[2] == c2[2]) {
                    if (c1[3] == c2[3]) {
                        return -1;
                    }
                    if (c1[3] == 0) {
                        return 1;
                    }
                    if (c2[3] == 0) {
                        return 0;
                    }
                    if (c1[3] > c2[3]) {
                        return 1;
                    }
                    if (c1[3] < c2[3]) {
                        return 0;
                    }
                }
                if (c1[2] == 0) {
                    return 1;
                }
                if (c2[2] == 0) {
                    return 0;
                }
                if (c1[2] > c2[2]) {
                    return 1;
                }
                if (c1[2] < c2[2]) {
                    return 0;
                }
            }

            if (c1[1] == 0) {
                return 1;
            }
            if (c2[1] == 0) {
                return 0;
            }
            if (c1[1] > c2[1]) {
                return 1;
            }
            if (c1[1] < c2[1]) {
                return 0;
            }

            return -1;
        }

        //Compare Straight
        int compareStraight(int[] c1, int[] c2) {
            if (c1[1] == c2[1]) {
                return -1;
            }
            if (c1[1] == 0) {
                return 1;
            }
            if (c2[1] == 0) {
                return 0;
            }
            if (c1[1] > c2[1]) {
                return 1;
            }
            if (c2[1] > c1[1]) {
                return 0;
            }
            return -1;
        }

        //Compare Flush
        int compareFlush(ArrayList<Integer> c1, ArrayList<Integer> c2) {

            Collections.sort(c1.subList(1,c1.size()));
            Collections.sort(c2.subList(1,c2.size()));
            for (int i = 1; i < c1.size(); i++) {
                c1.set(i, c1.get(i) % 13);
            }

            for (int i = 1; i < c2.size(); i++) {
                c2.set(i, c2.get(i) % 13);
            }




            if (c1.get(1) != 0 && c2.get(1) == 0) {
                return 0;
            }
            if (c1.get(1) == 0 && c2.get(1) != 0) {
                return 1;
            }
            for (int i = 5; i > 0; i--) {

                if (c1.get(i) == c2.get(i)) {
                    continue;
                }
                if (c1.get(i) > c2.get(i)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        }

        //Compare Fullhouse
        int compareFullandKare(int[] c1, int[] c2) {

            if (c1[1] == 0 && c2[1] != 0) {
                return 1;
            }
            if (c1[1] != 0 && c2[1] == 0) {
                return 0;
            }
            if (c1[1] == c2[1]) {
                if (c1[2] == c2[2]) {
                    return -1;
                }
                if (c1[2] == 0) {
                    return 1;
                }
                if (c2[2] == 0) {
                    return 0;
                }
                if (c1[2] > c2[2]) {
                    return 1;
                }

                if (c1[2] < c2[2]) {
                    return 0;
                }
            }
            if (c1[1] > c2[1]) {
                return 1;
            }

            if (c1[1] < c2[1]) {
                return 0;
            }
            return -199;
        }

        //Compare Straight FLush
        int compareSFlush(int[] c1, int[] c2) {
            if (c1[1] == 0) {
                return 1;
            }
            if (c2[1] == 0) {
                return 0;
            }
            if (c1[1] > c2[1]) {
                return 1;
            }
            if (c2[1] > c1[1]) {
                return 0;
            }
            if (c1[1] == c2[1]) {
                return -1;
            }
            return -100;
        }

        //Combine hand and board cards
        int[] combine(int[] hand, int[] board) {
            int count = 0;
            int[] c = new int[hand.length + board.length];
            for (int i = 0; i < hand.length; i++) {
                c[i] = hand[i];
                count++;
            }

            for (int j = 0; j < board.length; j++) {
                c[count++] = board[j];
            }

            return c;
        }
        //Describes what card is represented by a number from the array
        String printCard(int card) {
            String figure = "";
            String color = "";

            if (card % 13 == 0) {
                figure = "Ace";
            }
            if (card % 13 == 1) {
                figure = "2";
            }
            if (card % 13 == 2) {
                figure = "3";
            }
            if (card % 13 == 3) {
                figure = "4";
            }
            if (card % 13 == 4) {
                figure = "5";
            }
            if (card % 13 == 5) {
                figure = "6";
            }
            if (card % 13 == 6) {
                figure = "7";
            }
            if (card % 13 == 7) {
                figure = "8";
            }
            if (card % 13 == 8) {
                figure = "9";
            }
            if (card % 13 == 9) {
                figure = "10";
            }
            if (card % 13 == 10) {
                figure = "Jack";
            }
            if (card % 13 == 11) {
                figure = "Queen";
            }
            if (card % 13 == 12) {
                figure = "King";
            }
            //Color
            if (card >= 1 && card <= 13) {
                color = "Hearts";
            }
            if (card >= 14 && card <= 26) {
                color = "Diamonds";
            }
            if (card >= 27 && card <= 39) {
                color = "Clubs";
            }
            if (card >= 40 && card <= 52) {
                color = "Spades";
            }

            return figure + " of " + color;
        }



    }


    public PokerGame() {
        System.out.println("---Game Server----");
        players = new ArrayList<>();
        playQueue = new ArrayList<>();

        numPlayers = 0;

        try {
            ss = new ServerSocket(6789);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//This code was an attempt to code a full gameof poker and I m very proud of this recursion for the game algorithm.
// Saddly I abandoned the idea of a full game since it required a lot more work than imagined

//    private GameState flopTurn(ArrayList<Integer> deck, GameState gameState) {
//
//        gameState.setRaise(0);
//        if (gameState.getDealer() == 0) {
//            gameState.setPlayerToMove(1);
//        } else gameState.setPlayerToMove(0);
//        //Send gamestate to the player who has to move if it is not just the start of the flop round
//        try{
//
//            players.get(gameState.getPlayerToMove()).dataOut.writeObject(gameState);
//            players.get(gameState.getPlayerToMove()).dataOut.flush();
//
//            if (gameState.getTurn()==1){
//                dealTurn(deck);
//            }
//
//            else if (gameState.getFlop()==1) {
//                dealFlop(deck);
//            }
//            String move = (String) players.get(gameState.getPlayerToMove()).dataIn.readObject();
//            if (move.equals("raise")) {
//                //Change game variables according to raise
//                gameState.setRaise((Integer) players.get(gameState.getPlayerToMove()).dataIn.readObject());
//                gameState.setPot(gameState.getPot()+gameState.getRaise());
//                if (gameState.getPlayerToMove()== 0) gameState.setChips1(gameState.getChips1()-gameState.getRaise());
//                else gameState.setChips2(gameState.getChips2()-gameState.getRaise());
//                if (gameState.getPlayerToMove() == 0) gameState.setPlayerToMove(1);
//                else gameState.setPlayerToMove(0);
//                gameState.setPreflop(gameState.getPreflop() + 1);
//                return flopTurn(deck,gameState);
//
//            }
//            if (move.equals("call") && gameState.getFlop() == 0) {
//                gameState.setPot(gameState.getPot()+smallBlind[indexBlind]);
//                gameState.setChips1(gameState.getChips1()-smallBlind[indexBlind]);
//                if (gameState.getPlayerToMove()==0) gameState.setPlayerToMove(1);
//                else gameState.setPlayerToMove(0);
//                gameState.setPreflop(gameState.getPreflop() + 1);
//                gameState.setRaise(0);
//                return flopTurn(deck, gameState);
//            }
//            if (move.equals("call")) {
//                gameState.setPot(gameState.getPot()+gameState.getRaise());
//                if (gameState.getPlayerToMove() == 0) gameState.setChips1(gameState.getChips1()-gameState.getRaise());
//                else gameState.setChips2(gameState.getChips2()-gameState.getRaise());
//                gameState.setFlop(1);
//                gameState.setRaise(0);
//                if (gameState.getTurn()>0){
//                    gameState.setRiver(1);
//                }
//                else{
//                    gameState.setTurn(1);
//                }
//
//                return gameState;
//            }
//            if (move.equals("fold")) {
//                if (gameState.getPlayerToMove() == 0) {
//                    gameState.setChips2(gameState.getChips2()+gameState.getPot());
//                    gameState.setFold(1);
//                    player2.dataOut.writeObject(gameState);
//                } else {
//                    gameState.setChips1(gameState.getChips1()+gameState.getPot());
//                    gameState.setFold(2);
//                    player1.dataOut.writeObject(gameState);
//                }
//                return gameState;
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
{
//private GameState river(ArrayList<Integer> deck,GameState gameState) {
//    gameState.setRaise(0);
//    if (gameState.getDealer() == 0) {
//        gameState.setPlayerToMove(1);
//    } else gameState.setPlayerToMove(0);
//    //Send gamestate to the player who has to move if it is not just the start of the flop round
//    try{
//
//        players.get(gameState.getPlayerToMove()).dataOut.writeObject(gameState);
//        players.get(gameState.getPlayerToMove()).dataOut.flush();
//
//
//
//
//
//        String move = (String) players.get(gameState.getPlayerToMove()).dataIn.readObject();
//        if (move.equals("raise")) {
//            //Change game variables according to raise
//            gameState.setRaise((Integer) players.get(gameState.getPlayerToMove()).dataIn.readObject());
//            gameState.setPot(gameState.getPot()+gameState.getRaise());
//            if (gameState.getPlayerToMove()== 0) gameState.setChips1(gameState.getChips1()-gameState.getRaise());
//            else gameState.setChips2(gameState.getChips2()-gameState.getRaise());
//            if (gameState.getPlayerToMove() == 0) gameState.setPlayerToMove(1);
//            else gameState.setPlayerToMove(0);
//            gameState.setPreflop(gameState.getPreflop() + 1);
//            return river(deck,gameState);
//
//        }
//        if (move.equals("call") && gameState.getFlop() == 0) {
//            gameState.setPot(gameState.getPot()+smallBlind[indexBlind]);
//            gameState.setChips1(gameState.getChips1()-smallBlind[indexBlind]);
//            if (gameState.getPlayerToMove()==0) gameState.setPlayerToMove(1);
//            else gameState.setPlayerToMove(0);
//            gameState.setPreflop(gameState.getPreflop() + 1);
//            gameState.setRaise(0);
//            return river(deck, gameState);
//        }
//        if (move.equals("call")) {
//            gameState.setPot(gameState.getPot()+gameState.getRaise());
//            if (gameState.getPlayerToMove() == 0) gameState.setChips1(gameState.getChips1()-gameState.getRaise());
//            else gameState.setChips2(gameState.getChips2()-gameState.getRaise());
//            gameState.setFlop(1);
//            gameState.setRaise(0);
//            int winner = checkWhoWon(player1Hand,player2Hand,board);
//            gameState.setWinner(winner+1);
//            return gameState;
//        }
//        if (move.equals("fold")) {
//            if (gameState.getPlayerToMove() == 0) {
//                gameState.setChips2(gameState.getChips2()+gameState.getPot());
//                gameState.setFold(1);
//                player2.dataOut.writeObject(gameState);
//            } else {
//                gameState.setChips1(gameState.getChips1()+gameState.getPot());
//                gameState.setFold(2);
//                player1.dataOut.writeObject(gameState);
//            }
//            return gameState;
//        }
//    } catch (IOException e){
//        System.out.println("IOException in river round");
//    }catch (ClassNotFoundException e){
//        System.out.println("ClassnotFoundException in river");
//    }
//    return null;
//}
}
}


