import java.util.*;

public class Yggdrasil extends AIModule{
    
    private int player;
    private int opponent;
    private int DEPTH_LIMIT = Integer.MAX_VALUE;
    private int[] playerOrder = {3,2,4,1,5,6,0};
    private int[] opponentOrder = {1,5,2,4,0,6,3};
    //private int[] opponentOrder = {0,1,2,3,4,5,6};
    
    private int boardWidth;
    private int boardHeight;
    
    // Utility functions
    private boolean valid(int i, int j){
        return (i >= 0 && i <= 6 && j >= 0 && j <= 5);
    }
    private boolean atPos(final GameStateModule game, int i, int j, int bot){
        return (this.valid(i, j) && game.getAt(i,j) == bot);
    }
    private boolean checkWin(final GameStateModule game, int i, int j, int bot){
        // 4-in-column
        if(atPos(game, i,j-3,bot) && atPos(game, i,j-2,bot) && atPos(game, i,j-1,bot)){
            return true;
        }
        // 4-in-row
        if((atPos(game, i-3,j,bot) && atPos(game, i-2,j,bot) && atPos(game, i-1,j,bot)) ||(atPos(game, i-2,j,bot) && atPos(game, i-1,j,bot) && atPos(game, i+1,j,bot)) ||(atPos(game, i-1,j,bot) && atPos(game, i+1,j,bot) && atPos(game, i+2,j,bot)) ||(atPos(game, i+1,j,bot) && atPos(game, i+2,j,bot) && atPos(game, i+3,j,bot))){
            return true;
        }
        // 3-in-row-and-2-empty
        if((atPos(game, i-3,j,0) && atPos(game, i-2,j,bot) && atPos(game, i-1,j,bot) && atPos(game, i+1,j,0)) || (atPos(game, i-2,j,0) && atPos(game, i-1,j,bot) && atPos(game, i+1,j,bot) && atPos(game, i+2,j,0)) || (atPos(game, i-1,j,0) && atPos(game, i+1,j,bot) && atPos(game, i+2,j,bot) && atPos(game, i+3,j,0))){
            return true;
        }
        // 4-in-\
        
        if((atPos(game, i-3,j+3,bot) && atPos(game, i-2,j+2,bot) && atPos(game, i-1,j+1,bot)) || (atPos(game, i-2,j+2,bot) && atPos(game, i-1,j+1,bot) && atPos(game, i+1,j-1,bot)) || (atPos(game, i-1,j+1,bot) && atPos(game, i+1,j-1,bot) && atPos(game, i+2,j-2,bot)) || (atPos(game, i+1,j-1,bot) && atPos(game, i+2,j-2,bot) && atPos(game, i+3,j-3,bot))){
            return true;
        }
        // 4-in-/
        if((atPos(game, i-3,j-3,bot) && atPos(game, i-2,j-2,bot) && atPos(game, i-1,j-1,bot)) || (atPos(game, i-2,j-2,bot) && atPos(game, i-1,j-1,bot) && atPos(game, i+1,j+1,bot)) || (atPos(game, i-1,j-1,bot) && atPos(game, i+1,j+1,bot) && atPos(game, i+2,j+2,bot)) || (atPos(game, i+1,j+1,bot) && atPos(game, i+2,j+2,bot) && atPos(game, i+3,j+3,bot))){
            return true;
        }
        return false;
    }
    
    // we are on top and possibe to get to 4
    private int countColumns(final GameStateModule game, int enemy){
        int numColumns = 0;
        int[] weight = {1,3,4,10,4,3,1};
        for(int i = 0; i < boardWidth; i++){
            if((game.getHeightAt(i) > 0 && game.getAt(i,game.getHeightAt(i)-1) == enemy) || game.getHeightAt(i) >= 6){ // we are on top
                continue;
            }
            if(game.getHeightAt(i) <= 3){ // XXX1__ or XXXX1_ or XXXXX1
                numColumns = numColumns + weight[i];
                continue;
            }
            // [X1__]__ or [XX1_]__
            if(game.getAt(i,2) != enemy && game.getAt(i,3) != enemy && game.getAt(i,4) != enemy && game.getAt(i,5) != enemy ){
                numColumns = numColumns + weight[i];
            }
        }
        return numColumns;
    }
    
    private int countDiagonals(final GameStateModule game, int enemy){
        
        int numDiagonals = 0;
        int bot = 2/enemy;
        // For diagnal like "/"
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 3; j++){
                if((game.getAt(i, j) != enemy) && (game.getAt(i+1, j+1) != enemy) && (game.getAt(i+2, j+2) != enemy) && (game.getAt(i+3, j+3) != enemy)){
                    numDiagonals++;
                }
            }
        }
        
        // For diagnal like "\"
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 3; j++){
                if((game.getAt(i, 5-j) != enemy) && (game.getAt(i+1, 5-j-1) != enemy) && (game.getAt(i+2, 5-j-2) != enemy) && (game.getAt(i+3, 5-j-3) != enemy)){
                    numDiagonals++;
                }
            }
        }
        return numDiagonals;
    }
    
    private int countForbiddenColumns(final GameStateModule game, int enemy){
        int forbiddenColumn = 0;
        for(int i = 0; i < boardWidth; i++){
            if (!game.canMakeMove(i)){
                continue;
            }
            for(int k = 0; k < boardWidth; k++){
                if (!game.canMakeMove(k)){
                    continue;
                }
                int j = game.getHeightAt(k);
                if(k == i && game.getHeightAt(k) < 6){
                    j++;
                }
                if(checkWin(game, k, j, enemy)){
                    forbiddenColumn++;
                }
            }
        }
        return forbiddenColumn;
    }
    
    
    private int directWin(final GameStateModule game, int bot){
        int win = 0;
        for(int i = 0; i < boardWidth; i++){
            if(!game.canMakeMove(i)){
                continue;
            }
            int j = game.getHeightAt(i);
            if(checkWin(game, i, j, bot)){
                win++;
            }
        }
        return win;
    }
    
    
    int countCenter(final GameStateModule game, int bot){
        int count = 1;
        for(int j = 2; j <= 4 ; j++){
            if(game.getAt(1,j) == bot){
                count++;
            }
            if(game.getAt(2,j) == bot){
                count++;
            }
            if(game.getAt(4,j) == bot){
                count++;
            }
            if(game.getAt(5,j) == bot){
                count++;
            }
        }
        for(int j = 0; j < boardHeight-1; j++){
            if(game.getAt(3,j) == bot){
                count = count + 4;
            }
        }
        return count;
    }

    private int numFilled(final GameStateModule game){
        int num = 0;
        for(int i = 0; i < boardWidth; i++){
            for(int j = 0; j < boardHeight; j++){
                if(game.getAt(i,j) != 0){
                    num++;
                }
            }
        }
        return num;
    }
    
    private int[] countThreats(final GameStateModule game, int bot){
        int[] value = {0,0,7};
        for (int i = 0; i < game.getWidth(); i++){
            if (!game.canMakeMove(i)){
                continue;
            }
            int j = game.getHeightAt(i);
            
            if(atPos(game, i, j-1, bot) && atPos(game, i, j-2, bot) && valid(i,j+1)){
                if(j%2==0){ //j+1 is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j+1);
            }
            
            // horizontal
            if((atPos(game, i-3, j,0) && atPos(game, i-2, j, bot) && atPos(game, i-1, j, bot)) ||
               (atPos(game, i-3, j, bot) && atPos(game, i-2, j,0) && atPos(game, i-1, j, bot)) ||
               (atPos(game, i-3, j, bot) && atPos(game, i-2, j, bot) && atPos(game, i-1, j,0)) ||
               (atPos(game, i-2, j,0) && atPos(game, i-1, j, bot) && atPos(game, i+1, j, bot)) ||
               (atPos(game, i-2, j, bot) && atPos(game, i-1, j,0) && atPos(game, i+1, j, bot)) ||
               (atPos(game, i-2, j, bot) && atPos(game, i-1, j, bot) && atPos(game, i+1, j,0)) ||
               (atPos(game, i-1, j,0) && atPos(game, i+1, j, bot) && atPos(game, i+2, j, bot)) ||
               (atPos(game, i-1, j, bot) && atPos(game, i+1, j,0) && atPos(game, i+2, j, bot)) ||
               (atPos(game, i-1, j, bot) && atPos(game, i+1, j, bot) && atPos(game, i+2, j,0)) ||
               (atPos(game, i+1, j,0) && atPos(game, i+2, j, bot) && atPos(game, i+3, j, bot)) ||
               (atPos(game, i+1, j, bot) && atPos(game, i+2, j,0) && atPos(game, i+3, j, bot)) ||
               (atPos(game, i+1, j, bot) && atPos(game, i+2, j, bot) && atPos(game, i+3, j,0))){
                
                if(j%2==1){ //j is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j);
            }
            
            if((atPos(game, i-3, j-3, bot) && atPos(game, i-2, j-2,0) && atPos(game, i-1, j-1, bot)) || (atPos(game, i-2, j-2,0) && atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1, bot))){
                
                if(j%2==1){ //j is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j-2);
            }
            if((atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2,0)) ||
               (atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2,0) && atPos(game, i+3, j+3, bot))){
                
                if(j%2==1){ //j is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j+1);
            }
            
            
            if((atPos(game, i-3, j-3,0) && atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1, bot))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j-3);
            }
            if((atPos(game, i-3, j-3, bot) && atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1,0))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j-1);
            }
            if((atPos(game, i-3, j-3, bot) && atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1,0)) ||
               (atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1,0) && atPos(game, i+1, j+1, bot)) ||
               (atPos(game, i-1, j-1,0) && atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2, bot))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j-1);
            }
            if((atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1,0)) ||
               (atPos(game, i+1, j+1,0) && atPos(game, i+2, j+2, bot) && atPos(game, i+3, j+3, bot)) ||
               (atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1,0) && atPos(game, i+2, j+2, bot))){
                   
                   if(j%2==1){ //j is even Threat
                       value[1]++;
                   }else{
                       value[0]++;
                   }
                   value[2] = Math.min(value[2],j+1);
            }
            if((atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2, bot) && atPos(game, i+3, j+3,0))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j+3);
            }
            
            
            if((atPos(game, i-3, j+3, bot) && atPos(game, i-2, j+2,0) && atPos(game, i-1, j+1, bot)) ||
               (atPos(game, i-2, j+2,0) && atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1, bot))){
                
                if(j%2==1){ //j is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j+2);
            }
            if((atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2,0)) ||
               (atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2,0) && atPos(game, i+3, j-3, bot))){
                
                if(j%2==1){ //j is odd Threat
                    value[0]++;
                }else{
                    value[1]++;
                }
                value[2] = Math.min(value[2],j-2);
            }
            
            
            if((atPos(game, i-3, j+3,0) && atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1, bot))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j+3);
            }
            if((atPos(game, i-3, j+3, bot) && atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1,0)) ||
               (atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1,0) && atPos(game, i+1, j-1, bot)) ||
               (atPos(game, i-1, j+1,0) && atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2, bot))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j+1);
            }
            if((atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1,0)) ||
               (atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1,0) && atPos(game, i+2, j-2, bot)) ||
               (atPos(game, i+1, j-1,0) && atPos(game, i+2, j-2, bot) && atPos(game, i+3, j-3, bot))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j-1);
            }
            if((atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2, bot) && atPos(game, i+3, j-3,0))){
                
                if(j%2==1){ //j is even Threat
                    value[1]++;
                }else{
                    value[0]++;
                }
                value[2] = Math.min(value[2],j-3);
            }
            
        }
        return value;
    }
    
    private int countWinnable(final GameStateModule game, int bot){
        int value = 0;
        for (int i = 0; i < game.getWidth(); i++){
            if (!game.canMakeMove(i)){
                continue;
            }
            int j = game.getHeightAt(i);
            
            if(atPos(game, i, j-1, bot) && atPos(game, i, j-2, bot) && valid(i,j+1)){
                value++;
            }
            
            if((atPos(game, i-3, j,0) && atPos(game, i-2, j, bot) && atPos(game, i-1, j, bot)) ||
               (atPos(game, i-3, j, bot) && atPos(game, i-2, j,0) && atPos(game, i-1, j, bot)) ||
               (atPos(game, i-3, j, bot) && atPos(game, i-2, j, bot) && atPos(game, i-1, j,0)) ||
               (atPos(game, i-2, j,0) && atPos(game, i-1, j, bot) && atPos(game, i+1, j, bot)) ||
               (atPos(game, i-2, j, bot) && atPos(game, i-1, j,0) && atPos(game, i+1, j, bot)) ||
               (atPos(game, i-2, j, bot) && atPos(game, i-1, j, bot) && atPos(game, i+1, j,0)) ||
               (atPos(game, i-1, j,0) && atPos(game, i+1, j, bot) && atPos(game, i+2, j, bot)) ||
               (atPos(game, i-1, j, bot) && atPos(game, i+1, j,0) && atPos(game, i+2, j, bot)) ||
               (atPos(game, i-1, j, bot) && atPos(game, i+1, j, bot) && atPos(game, i+2, j,0)) ||
               (atPos(game, i+1, j,0) && atPos(game, i+2, j, bot) && atPos(game, i+3, j, bot)) ||
               (atPos(game, i+1, j, bot) && atPos(game, i+2, j,0) && atPos(game, i+3, j, bot)) ||
               (atPos(game, i+1, j, bot) && atPos(game, i+2, j, bot) && atPos(game, i+3, j,0))){
                value++;
            }
            
            if((atPos(game, i-3, j-3,0) && atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1, bot)) ||
               (atPos(game, i-3, j-3, bot) && atPos(game, i-2, j-2,0) && atPos(game, i-1, j-1, bot)) ||
               (atPos(game, i-3, j-3, bot) && atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1,0)) ||
               (atPos(game, i-2, j-2,0) && atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1, bot)) ||
               (atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1,0) && atPos(game, i+1, j+1, bot)) ||
               (atPos(game, i-2, j-2, bot) && atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1,0)) ||
               (atPos(game, i-1, j-1,0) && atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2, bot)) ||
               (atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1,0) && atPos(game, i+2, j+2, bot)) ||
               (atPos(game, i-1, j-1, bot) && atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2,0)) ||
               (atPos(game, i+1, j+1,0) && atPos(game, i+2, j+2, bot) && atPos(game, i+3, j+3, bot)) ||
               (atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2,0) && atPos(game, i+3, j+3, bot)) ||
               (atPos(game, i+1, j+1, bot) && atPos(game, i+2, j+2, bot) && atPos(game, i+3, j+3,0))){
                value++;
            }
            
            if((atPos(game, i-3, j+3,0) && atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1, bot)) ||
               (atPos(game, i-3, j+3, bot) && atPos(game, i-2, j+2,0) && atPos(game, i-1, j+1, bot)) ||
               (atPos(game, i-3, j+3, bot) && atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1,0)) ||
               (atPos(game, i-2, j+2,0) && atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1, bot)) ||
               (atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1,0) && atPos(game, i+1, j-1, bot)) ||
               (atPos(game, i-2, j+2, bot) && atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1,0)) ||
               (atPos(game, i-1, j+1,0) && atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2, bot)) ||
               (atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1,0) && atPos(game, i+2, j-2, bot)) ||
               (atPos(game, i-1, j+1, bot) && atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2,0)) ||
               (atPos(game, i+1, j-1,0) && atPos(game, i+2, j-2, bot) && atPos(game, i+3, j-3, bot)) ||
               (atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2,0) && atPos(game, i+3, j-3, bot)) ||
               (atPos(game, i+1, j-1, bot) && atPos(game, i+2, j-2, bot) && atPos(game, i+3, j-3,0))){
                value++;
            }
            
        }
        return value;
    }
    
    private int evaluate(final GameStateModule game, int bot){
        int filled = numFilled(game);
        if(game.isGameOver()) {
            int winner = game.getWinner();
            return (winner == this.player) ? 10000000 - filled : (winner == this.opponent) ? -10000000 + filled : -500000 + filled;
        }
        
        int center = countCenter(game,this.player);
        
        int winDirectly = directWin(game,this.player);
        int loseDirectly = directWin(game,this.opponent);
        
        int ourThreats[] = countThreats(game, this.player);
        int enemyThreats[] = countThreats(game, this.opponent);
        
        if(this.player == 1){
            ourThreats[1] = (ourThreats[1] > 0 && ourThreats[2] < enemyThreats[2]) ? ourThreats[1] * 7 : 0;
            enemyThreats[0] = enemyThreats[0]*2;
            enemyThreats[1] = enemyThreats[1]*3;
        }else{
            ourThreats[1] = (ourThreats[1] > 0 && ourThreats[2] < enemyThreats[2]) ? ourThreats[1]*5 : 0;
            enemyThreats[0] = enemyThreats[0] *3;
        }
        int oT = ourThreats[0]+ourThreats[1];
        int eT = enemyThreats[0]+enemyThreats[1];
        
        int open = 5*countColumns(game, this.opponent) + countDiagonals(game, this.opponent);
        int enemyopen = countColumns(game, this.player) + countDiagonals(game, this.player);

        int forbiddenMove = countForbiddenColumns(game, this.opponent);
        int enemyforbiddenMove = countForbiddenColumns(game, this.player);
        
        if(bot == this.player){
            if (loseDirectly > 0){
                return -10000000 + filled;
            }
            if (winDirectly > 1){
                return 10000000 - filled;
            }
        }else{
            if (winDirectly > 0){
                return 10000000 - filled;
            }
            if (loseDirectly > 1){
                return -10000000 + filled;
            }
        }
        // WTF is this ******************************************
        if(this.player == 1){
            int winnable = countWinnable(game, this.player);
            return center*(open-enemyopen)-70*forbiddenMove + 60*enemyforbiddenMove + 15*winnable;
        }
        return center*(open-enemyopen)-70*forbiddenMove+30*enemyforbiddenMove+33*oT-40*eT; //33-40
    }
    
    // *******************************************************************************************************
    
    private int[] successorFunction(final GameStateModule game,int depth) {
        int num = numFilled(game);
        if(num <= 8){
            return playerOrder;
        }
        if(num <= 15){
            return opponentOrder;
        }
        //System.out.println(num);
        int successors[] = new int[boardWidth];
        int successorsVal[] = new int[boardWidth];
        
        for(int i = 0; i < boardWidth; i++) {
            if(!game.canMakeMove(i)) {
                successors[i] = -1;
                successorsVal[i] = -1;
            } else {
                successors[i] = i;
                successorsVal[i] = game.getHeightAt(i);
            }
        }
        
        for(int i = 1; i < boardWidth; i++) {
            int tempIdx = successors[i];
            int temp = successorsVal[i];
            int j = i - 1;
            while(j >= 0 && successorsVal[j] < temp) {
                successorsVal[j+1] = successorsVal[j];
                successors[j+1] = successors[j];
                j--;
            }
            successors[j+1] = tempIdx;
            successorsVal[j+1] = temp;
        }
        
        // for(int i = 0; i < boardWidth; i++){
        //     System.out.printf("%s ", successors[i]);
        // }
        // System.out.println();
        
        return successors;
    }
    
    public void getNextMove(final GameStateModule game){
        this.player = game.getActivePlayer();
        this.opponent = 2/this.player;
        
        boardHeight = game.getHeight();
        boardWidth = game.getWidth();
        
        for(int i = 0; i < boardWidth; i++){
            if(game.canMakeMove(i)){
                chosenMove = i;
                break;
            }
        }
        int val = Integer.MIN_VALUE;
        int depth = 0;
        int lastMove = chosenMove;
        for(int d = 4; d <= DEPTH_LIMIT; d++){
            for(int i : successorFunction(game,d)){
                if(!game.canMakeMove(i)){
                    continue;
                }
                game.makeMove(i);
                int min = minValue(game,Integer.MIN_VALUE,Integer.MAX_VALUE, d);
                if (min > val){
                    val = min;
                    chosenMove = i;
                }
                game.unMakeMove();
            }
            if(terminate && d > 4){
                chosenMove = lastMove;
                break;
            }
            lastMove = chosenMove;
            depth = d;
        }
        System.out.println(depth);
    }
    
    private int maxValue(final GameStateModule game, int alpha, int beta, int depth){
        if(game.isGameOver() || terminate || depth <= 0) {
            return evaluate(game, this.opponent);
        }

        int val = Integer.MIN_VALUE;
        for(int i : successorFunction(game,depth)){
            if(game.canMakeMove(i)){
                game.makeMove(i);
                val = Math.max(val, minValue(game,alpha,beta,depth-1));
                game.unMakeMove();
                if(val >= beta)
                    return val;
                alpha = Math.max(val, alpha);
            }
        }
        return val;
    }
    
    private int minValue(final GameStateModule game, int alpha, int beta, int depth){
        if(game.isGameOver() || terminate || depth <=0){
            return evaluate(game, this.player);
        }
        int val = Integer.MAX_VALUE;
        for(int i : successorFunction(game,depth)){
            if(game.canMakeMove(i)){
                game.makeMove(i);
                val = Math.min(val, maxValue(game,alpha,beta,depth-1));
                game.unMakeMove();
                if(val <= alpha)
                    return val;
                beta = Math.min(val, beta);
            }
        }
        return val;
    }
}
