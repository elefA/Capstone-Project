package com.example.client_poker;
import java.io.Serializable;

public class GameState implements Serializable {
    private int chips1;
    private int chips2;
    private int fold;           // 1 if player1 folded 2 if player2 folded  0 if hand is still playing
    private int dealer;         // 1 if player1 is the dealer 2 if player 2
    private int pot;            // Size of the pot
    private int preflop;        // 1 if preflop just started , >1 if preflop has not just started, 0 if current hand has progressed further than preflop
    private int flop;           // 1 if flop just started , >1 if flop has not just started, 0 if current hand has progressed further than flop
    private int turn;           // 1 if turn just started , >1 if turn has not just started, 0 if current hand has progressed further than turn round
    private int river;          // 1 if river just started , >1 if river has not just started, 0 if current hand has progressed further than the river
    private int playerToMove;   // 0 if player1's turn 1 if player2's
    private int raise;          // Raise amount, 0 if player can check the hand
    private int smallBlind;
    private int allIn;          //If player1 goes all in then it is =1 else 2 and if none 0
    private int winner;         //if player1 won the hand then =1 else 2 if its a tie 0     /// TIE CAREFUL
    public GameState(int chips1, int chips2, int fold, int dealer, int pot, int preflop, int flop, int turn, int river, int playerToMove, int smallBlind, int raise,int allIn,int winner) {
        this.setChips1(chips1);
        this.setChips2(chips2);
        this.setFold(fold);
        this.setDealer(dealer);
        this.setPot(pot);
        this.setPreflop(preflop);
        this.setFlop(flop);
        this.setTurn(turn);
        this.setRiver(river);
        this.setPlayerToMove(playerToMove);
        this.setSmallBlind(smallBlind);
        this.setRaise(raise);
        this.setAllIn(allIn);
        this.setWinner(winner);

    }
    public void refreshVariables(int blind){
        fold=0;
        if (dealer==2)dealer=1;else dealer=2;
        preflop=1;
        flop=turn=river=1;
        playerToMove=dealer;
        smallBlind=blind;
        raise=blind;

    }

    public GameState(int chips1, int chips2, int fold, int pot, int dealer) {
        this.setChips1(chips1);
        this.setChips2(chips2);
        this.setFold(fold);
        this.setDealer(dealer);
        this.setPot(pot);
    }

    public int getChips1() {
        return chips1;
    }

    public void setChips1(int chips1) {
        this.chips1 = chips1;
    }

    public int getChips2() {
        return chips2;
    }

    public void setChips2(int chips2) {
        this.chips2 = chips2;
    }

    public int getFold() {
        return fold;
    }

    public void setFold(int fold) {
        this.fold = fold;
    }

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public int getPreflop() {
        return preflop;
    }

    public void setPreflop(int preflop) {
        this.preflop = preflop;
    }

    public int getFlop() {
        return flop;
    }

    public void setFlop(int flop) {
        this.flop = flop;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getRiver() {
        return river;
    }

    public void setRiver(int river) {
        this.river = river;
    }

    public int getPlayerToMove() {
        return playerToMove;
    }

    public void setPlayerToMove(int playerToMove) {
        this.playerToMove = playerToMove;
    }

    public int getRaise() {
        return raise;
    }

    public void setRaise(int raise) {
        this.raise = raise;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    public int getAllIn() {
        return allIn;
    }

    public void setAllIn(int allIn) {
        this.allIn = allIn;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}
