import java.util.*;

public class Legend extends AIModule{
    
    private int player;
    private int opponent;
    private int DEPTH_LIMIT = Integer.MAX_VALUE;
    private int[] order = {3,2,4,1,5,0,6};
    
    
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
            return (winner == this.player) ? 1000000 - filled : (winner == this.opponent) ? -1000000 + filled : 0;
        }
        
        int center = countCenter(game,this.player);
        
        int winDirectly = directWin(game,this.player);
        int loseDirectly = directWin(game,this.opponent);
        
        int winnable = countWinnable(game, this.player);
        
        int open = 5*countColumns(game, this.opponent) + countDiagonals(game, this.opponent);
        int enemyopen = countColumns(game,this.player) + countDiagonals(game, this.player);

        int forbiddenMove = countForbiddenColumns(game, this.opponent);
        int enemyforbiddenMove = countForbiddenColumns(game, this.player);
        
        if(bot == this.player){
            if (loseDirectly > 0){
                return -1000000 + filled;
            }
            if (winDirectly > 1){
                return 1000000 - filled;
            }
        }else{
            if (winDirectly > 0){
                return 1000000 - filled;
            }
            if (loseDirectly > 1){
                return -1000000 + filled;
            }
        }
        // WTF is this ******************************************
        return center*(open-enemyopen)-70*forbiddenMove + 60*enemyforbiddenMove + 15*winnable;
    }
    
    // *******************************************************************************************************
    
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
            for(int i : order){
                if(!game.canMakeMove(i)){
                    continue;
                }
                game.makeMove(i);
                int min = minValue(game, d);
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
    
    private int maxValue(final GameStateModule game, int depth){
        if(game.isGameOver() || terminate || depth <= 0) {
            return evaluate(game, this.opponent);
        }
        int val = Integer.MIN_VALUE;
        for(int i : order){
            if(game.canMakeMove(i)){
                game.makeMove(i);
                val = Math.max(val, minValue(game, depth-1));
                game.unMakeMove();
            }
        }
        return val;
    }
    
    private int minValue(final GameStateModule game, int depth){
        if(game.isGameOver() || terminate || depth <=0){
            return evaluate(game, this.player);
        }
        int val = Integer.MAX_VALUE;
        for(int i : order){
            if(game.canMakeMove(i)){
                game.makeMove(i);
                val = Math.min(val, maxValue(game, depth-1));
                game.unMakeMove();
            }
        }
        return val;
    }
}
