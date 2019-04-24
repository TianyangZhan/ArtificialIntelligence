// (c) Ian Davidson, Leo Shamis U.C. Davis 2019


/// Coordinates the game classes and runs the game.

import java.util.ArrayList;
import java.util.Arrays;
/**
 * This class is responsible for gluing together the AI and human input modules
 * to play a game of Connect Four.  You will not need to modify the contents
 * of this class, though it may be useful to glance over its contents before writing
 * your AI implementation.
 * 
 * @author Leonid Shamis
 */
public final class GameController
{
	/// State of the game.
	private final GameStateModule game;
	/// Controller for IO.
	private final IOModule io;
	/// AI game-players.
	private final AIModule[] players;
	/// Time alloted to any AI per turn.
	private final int AI_time;

	/// Primary Constructor.
	/**
	 * Constructs a new GameController that pulls together the various pieces of the
	 * program.
	 *
	 * @param game The game to arbitrate.
	 * @param io An IO module to use for graphics output and human input.
	 * @param players An array of two AIModules that will be pitted against each other.
	 * @param AI_time Maximum amount of time alotted per AI move.
	 */
	public GameController(final GameStateModule game, final IOModule io, final AIModule[] players, final int AI_time)
	{
		assert players.length == 2 : "Should only have two players.";

		this.game = game;
		this.io = io;
		this.players = players;
		this.AI_time = AI_time;
	}



	/// Start the gameplay.
	/**
	 * Runs the game.
	 */
	public void play()
	{
		// Draw the initial board
		io.drawBoard(game);

		// While not finished
		while(!game.isGameOver())
			// for both players
			for(int i = 0; i < 2; i++)
			{
				final AIModule player = players[i];
				// If the player is human than make a move request
				if(player == null)
					tryMakeMove(io.getHumanMove());
				else
					callAI(player, "Player " + (i + 1));

				// Update graphics
				io.drawBoard(game);
				// Break if done
				if(game.isGameOver())
					break;
			}


	}

	public void self_play(int draw_board)
	{
		// While not finished
		while(!game.isGameOver())
			// for both players
			for(int i = 0; i < 2; i++)
			{
				final AIModule player = players[i];
				// If the player is human than make a move request
				if(player == null)
					tryMakeMove(io.getHumanMove());
				else
					trainAI(player);

				// Update graphics
				if (draw_board == 1)
					io.drawBoard(game);
				// Break if done
				if(game.isGameOver())
					break;
			}
	}

	/// Gets the next move from the AI.
	/**
	 * Given an AI module, extracts the move from that module.  The AI will have
	 * a set period of time in which to choose a move.
	 *
	 * @param ai The AIModule to call.
	 * @param AIName Label given to the AI.
	 * @see AIModule
	 */
	private void callAI(final AIModule ai, final String AIName)
	{
		// Make a duplicate GameStateModule to avoid any unwanted changes to the board
		final GameStateModule duplicate = game.copy();
		ai.terminate = false;
		// Run the ai in another thread
		final Thread t = new Thread()
		{
			@Override
			public void run()
			{
				ai.getNextMove(duplicate);
			}
		};
		t.start();
		int move = -1;
		try
		{
			// Wait until the ai has finished or has timed out
			t.join(AI_time);
			// Set the terminate flag so that the AI knows it should wrap up its computations
			ai.terminate = true;
			if(t.isAlive())
			{
				// Allow 100ms margin before throwing the exception
				t.join(100);
				if(t.isAlive())
					throw new RuntimeException(AIName + " did not terminate when told to do so.");
			}
			// Get the selected move
			move = ai.chosenMove;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		// Try to make the move. If it fails then arbitrarily assign the move
		tryMakeMove(move);
	}

	private void trainAI(final AIModule ai)
	{
		// Make a duplicate GameStateModule to avoid any unwanted changes to the board
		final GameStateModule duplicate = game.copy();
		ai.getNextMove(duplicate);
		int move = ai.chosenMove;
		tryMakeMove(move);
	}

	public void print_2d_array(int[][] a){
		for(int[] row : a){
			System.out.println(Arrays.toString(row));
		}
	}


	public void printboardString(String board){
		System.out.println(board.substring(0, 4));
		System.out.println(board.substring(4, 8));
		System.out.println(board.substring(8, 12));
		System.out.println(board.substring(12, 16));
		System.out.println(board.substring(16, 20));
	}

	/// Attempts to make the given move, defaulting to an arbitrary move otherwise.
	/**
	 * Given a move, attempts to play that move.  If the move is illegal, the GameController
	 * will pick an arbitrary move instead and will report an error.
	 *
	 * @param move The column in which to drop the coin.
	 */
	private void tryMakeMove(final int move)
	{
		// If the move is illegal, make some valid move and write to System.err
		if(!game.canMakeMove(move))
		{
//			String currState = "";
//			String reverseState = "";
//			for(int i=0;i<game.getWidth();i++){
//				// current state
//				for(int j=0;j<game.getHeight();j++){
//					currState += String.valueOf(game.getAt(i, j));
////					reverseState += String.valueOf(game.getAt(game.getWidth() - i - 1, j));
//				}
//			}
//			printboardString(currState);
//			mctsAI.Stats stats = mctsAI.stateStats.get(currState);
//			for (int a : stats.legalActions)
//				System.out.println(a);
			for(int j = 0; j < game.getWidth(); j++)
				if(game.canMakeMove(j))
				{
					game.makeMove(j);
					System.err.println("Attempted to play illegal move " + move +
									   ".  Computer automatically played legal move " + j +
									   " instead.");
					break;
				}
		}
		else{
			game.makeMove(move);


//			System.out.println("move:" + move);
//			System.out.println(currState);
//			System.out.println(reverseState);
		}

	}
}
