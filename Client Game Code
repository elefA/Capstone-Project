package com.example.client_poker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class Pokeroom extends AppCompatActivity {
    private static final String TAG = Pokeroom.class.getSimpleName();
    //Variable Declaration

    TextView announcement;
    TextView pot;
    TextView moveText;

    ImageView card1;
    ImageView card2;
    ImageView card3;
    ImageView card4;
    TextView scoreText1;
    TextView scoreText2;
    static TextView player2;
    TextView player1;
    Handler mHandler;
    Handler nHandler;

    ImageView board1;
    ImageView board2;
    ImageView board3;
    ImageView board4;
    ImageView board5;
    ImageView dealer1;
    ImageView dealer2;

    Button win;
    Button lose;

    static int score1 = 0;
    static int score2 = 0;
    static boolean wPrediction;
    static boolean buttonClicked = false;
    static State state;
    static int winner;
    //Create an object of the Async classes to send and receive data

//    static ReceiveStringMessage stringReceiver;
//    static SendMessage sender;

    static ClientSideConnection csc;

    static String opponent;
    static int opponentMove = -1;
    static int number;
    static int playerNumber = -1;
    static String username;

    boolean roundFinished = true;
    boolean waiting = true;

    String[] cards = {
            "ha", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "h9", "h10", "hj", "hq", "hk",
            "da", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "d10", "dj", "dq", "dk",
            "ca", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "c10", "cj", "cq", "ck",
            "sa", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "sj", "sq", "sk"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokeroom);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        referenceElements();





        SharedPreferences sharedPref = getSharedPreferences("sharePref", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "Guest");
        player1.setText(username);
        //Refenerce UI elements and initializing the connection variable

        csc = MainActivity.csc;

        //Sending intent to play
        sendMessage("play");
        state = new State(0, 0, -1, -1, 0, 0, -1);


        System.out.println("Reached before reading player number");
        ReceivePlayerNumber receivePlayerNumber = new ReceivePlayerNumber();
        receivePlayerNumber.execute();

        hideButtons();


        waitOpponent();


    }

    private void waitOpponent() {
        mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (playerNumber == -1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "run: 1 ");
                sendMessage(username);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        announcement.setText("Let the games begin");
                        moveText.setText("");
                        ReceiveOpponentUsername receiver = new ReceiveOpponentUsername();
                        receiver.execute();


                    }
                });
                SystemClock.sleep(4000);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        announcement.setText("");
                    }
                });
                waiting = false;
                playGame();


            }
        }).start();

    }

    private void playGame() {
        nHandler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (score1 < 5 && score2 < 5) {

                    if (state.dealer==playerNumber){
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dealer1.setVisibility(View.VISIBLE);
                                dealer2.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else{
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dealer2.setVisibility(View.VISIBLE);
                                dealer1.setVisibility(View.INVISIBLE);
                            }
                        });

                    }

                    int tempcard2 = 0;
                    int tempcard1 = 0;
                    try {
                        int a = (int) csc.getIn().readObject();
                        int b = (int) csc.getIn().readObject();
                        tempcard2 = getImageId(MyApplication.getAppContext(), cards[a]);
                        tempcard1 = getImageId(MyApplication.getAppContext(), cards[b]);

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int[] hand = {tempcard1, tempcard2};
                    // This post method shows the hand cards that were received from the server on the UI thread.
                    MyRunnable1 data = new MyRunnable1(hand, card1, card2);
                    nHandler.post(data);
                    //UNTIL HERE IT WORKS PERFECTLY.
                    if (state.dealer == playerNumber) {

                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showButtons();
                                announcement.setText("Time to move");
                            }
                        });
                        while (!buttonClicked) {
                            SystemClock.sleep(1000);
                        }
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                moveText.setText("Waiting for opponent to predict");
                            }
                        });
                        try {
                            opponentMove = (int) csc.getIn().readObject();
                            if (opponentMove == 1) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("Opponent Predicted a win");
                                        announcement.setText("Cards will open now");
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("Opponent Predicted a loss");
                                        announcement.setText("Cards will open now");
                                    }
                                });
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        SystemClock.sleep(1200);
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                announcement.setText("");
                            }
                        });

                    } else {
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                announcement.setText("Wait for opponent's prediction");
                            }
                        });
                        try {
                            opponentMove = (int) csc.getIn().readObject();

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (opponentMove == 1) {
                                    moveText.setText("Opponent predicted a win");
                                } else moveText.setText("Opponent Predicted a loss");
                                showButtons();
                            }
                        });

                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                announcement.setText("Cards will open now");
                            }
                        });
                        SystemClock.sleep(1200);
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                announcement.setText("");
                            }
                        });
                    }

                    int[] cards1 = openBoard();

                    int[] oppHand = new int[2];
                    oppHand[0] = cards1[0];
                    oppHand[1] = cards1[1];

                    int[] flop = new int[3];
                    flop[0] = cards1[2];
                    flop[1] = cards1[3];
                    flop[2] = cards1[4];

                    int[] turnRiver = new int[2];
                    turnRiver[0] = cards1[5];
                    turnRiver[1] = cards1[6];

                    SystemClock.sleep(1000);
                    MyRunnable1 showOppHand = new MyRunnable1(oppHand, card3, card4);
                    nHandler.post(showOppHand);
                    SystemClock.sleep(2200);
                    MyRunnable2 showFlop = new MyRunnable2(flop, board1, board2, board3);
                    nHandler.post(showFlop);
                    SystemClock.sleep(2200);
                    MyRunnable3 showTurn = new MyRunnable3(turnRiver[0], board4);
                    nHandler.post(showTurn);
                    SystemClock.sleep(2200);
                    MyRunnable3 showRiver = new MyRunnable3(turnRiver[1], board5);
                    nHandler.post(showRiver);


                    try {
                        winner = (int) csc.getIn().readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (winner == -1) {
                        nHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                moveText.setText("It is a tie noone wins a point");
                            }
                        });
                    }
                    if (playerNumber == 0 && winner != -1) {
                        if (winner == 1) {
                            if (wPrediction) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You predicted correctly.");
                                        score1++;
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You lost this round");
                                    }
                                });
                            }
                            if (opponentMove == 1) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " lost this round.";
                                        announcement.setText(temp);
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " predicted correctly.";
                                        announcement.setText(temp);
                                        score2++;
                                    }
                                });
                            }

                        } else /*winner=0 (player2 won the hand)*/ {
                            if (wPrediction) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You lost this round");
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You predicted correctly");
                                        score1++;
                                    }
                                });
                            }
                            if (opponentMove == 1) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " predicted correctly.";
                                        announcement.setText(temp);
                                        score2++;
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " lost this round.";
                                        announcement.setText(temp);

                                    }
                                });
                            }
                        }

                    } else if (playerNumber == 1 && winner != -1) {
                        //Player1 won and this block is about the player2
                        if (winner == 1) {
                            if (wPrediction) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You lost this round");
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You predicted correctly.");
                                        score1++;
                                    }
                                });
                            }
                            if (opponentMove == 1) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " predicted correctly.";
                                        announcement.setText(temp);
                                        score2++;
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " lost this round";
                                        announcement.setText(temp);
                                    }
                                });
                            }

                        } else {
                            if (wPrediction) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You predicted correctly.");
                                        score1++;
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveText.setText("You lost this round.");
                                    }
                                });
                            }
                            if (opponentMove == 1) {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " lost this round";
                                        announcement.setText(temp);
                                    }
                                });
                            } else {
                                nHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temp = opponent + " predicted correctly.";
                                        announcement.setText(temp);
                                        score2++;
                                    }
                                });
                            }
                        }
                    }
                    nHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String tempScore1 = "" + score1;
                            String tempScore2 = "" + score2;
                            scoreText1.setText(tempScore1);
                            scoreText2.setText(tempScore2);
                        }
                    });
                    if (state.dealer == 0) state.dealer = 1;
                    else state.dealer = 0;

                    SystemClock.sleep(4000);
                    nHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            card1.setImageResource(R.drawable.down);
                            card2.setImageResource(R.drawable.down);
                            card3.setImageResource(R.drawable.down);
                            card4.setImageResource(R.drawable.down);

                            board5.setImageResource(R.drawable.down);
                            board4.setImageResource(R.drawable.down);
                            board3.setImageResource(R.drawable.down);
                            board2.setImageResource(R.drawable.down);
                            board1.setImageResource(R.drawable.down);
                        }
                    });
                }
                //UPDATE WIN RATIO CLOSE CONNECTION SEND BACK TO MAIN MENU
                SharedPreferences sp = getSharedPreferences("your_prefs", Context.MODE_PRIVATE);
                int games = sp.getInt("totalGames",0);
                int wins = sp.getInt("wins",0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("totalGames",++games);
                if (score1==5){
                    editor.putInt("wins", ++wins);
                    editor.apply();
                    nHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            moveText.setText("");
                            announcement.setText("YOU WON!!!");
                        }
                    });
                }
                else{
                    nHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            moveText.setText("You lost.");
                            announcement.setText("Better luck next time");
                        }
                    });
                }
                SystemClock.sleep(3000);
                MainActivity.csc = null;
                nHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Pokeroom.this.recreate();

                    }
                });
                startActivity(new Intent(Pokeroom.this, MainActivity.class));



            }

        }).start();

    }
    public void onGameFinish(){
        //Same as the menu button, changing activities.
        startActivity(new Intent(this, MainActivity.class));
        //Ending this activity when we are done with it.
        this.finish();
    }

    public int[] openBoard() {
        int[] cards = new int[7];
        try {
            cards[0] = (int) csc.getIn().readObject();
            cards[1] = (int) csc.getIn().readObject();

            cards[2] = (int) csc.getIn().readObject();
            cards[3] = (int) csc.getIn().readObject();
            cards[4] = (int) csc.getIn().readObject();
            cards[5] = (int) csc.getIn().readObject();
            cards[6] = (int) csc.getIn().readObject();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[] tempcards = new int[7];
        for (int i = 0; i < 7; i++) {
            tempcards[i] = getImageId(MyApplication.getAppContext(), this.cards[cards[i]]);
        }
        return tempcards;
    }

    public void win(View v) {
        sendNumber(1);
        //boolean to continue with the execution of the code inside the game loop
        buttonClicked = true;
        announcement.setText("Good luck on your win");
        hideButtons();
        //Boolean variable winPrediction is set to True to compare it when the server sends who actually won
        wPrediction = true;

    }

    public void lose(View v) {
        sendNumber(0);
        //boolean to continue with the execution of the code inside the game loop
        buttonClicked = true;
        announcement.setText("Not very optimistic are we?");
        hideButtons();
        //Boolean variable winPrediction is set to false to compare it when the server sends who actually won
        wPrediction = false;

    }

    private void hideButtons() {
        win.setVisibility(View.INVISIBLE);
        lose.setVisibility(View.INVISIBLE);
        win.setEnabled(false);
        lose.setEnabled(false);
    }

    private void showButtons() {
        win.setVisibility(View.VISIBLE);
        lose.setVisibility(View.VISIBLE);
        win.setEnabled(true);
        lose.setEnabled(true);
    }

    static class ReceivePlayerNumber extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                playerNumber = (int) csc.getIn().readObject();
                System.out.println("Got the number");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    void referenceElements() {
        player1 = findViewById(R.id.player1);
        player2 = findViewById(R.id.player2);
        announcement = findViewById(R.id.announcement);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        board1 = findViewById(R.id.board1);
        board2 = findViewById(R.id.board2);
        board3 = findViewById(R.id.board3);
        board4 = findViewById(R.id.board4);
        board5 = findViewById(R.id.board5);
        dealer1 = findViewById(R.id.dealer1);
        dealer2 = findViewById(R.id.dealer2);
        moveText = findViewById(R.id.move);
        win = findViewById(R.id.win);
        lose = findViewById(R.id.lose);
        scoreText1 = findViewById(R.id.score1);
        scoreText2 = findViewById(R.id.score2);
    }

    static void sendMessage(String message) {
        SendMessage sender = new SendMessage(message);
        sender.execute();

        sender = null;
    }

    void sendNumber(int number) {
        SendNumber sender = new SendNumber(number);
        sender.execute();
    }

    static class SendMessage extends AsyncTask<Void, Void, Void> {
        String message;

        private SendMessage(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sendMessage(message);
            System.out.println(message);
            return null;
        }

        void sendMessage(String message) {
            try {
                csc.getOut().writeObject(message);
                csc.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ReceiveOpponentUsername extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {

            try {
                opponent = (String) csc.getIn().readObject();
                System.out.println(opponent + " is the name of the opponent");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1;
        }

        protected void onPostExecute(Integer a) {
            player2.setText(opponent);
        }

    }

    static class SendNumber extends AsyncTask<Void, Void, Void> {
        int number;

        private SendNumber(int number) {
            this.number = number;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sendNumber(number);
            return null;
        }

        void sendNumber(int number) {
            try {
                csc.getOut().writeObject(number);
                csc.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

