package breakthrough;
import java.util.ArrayList;

import game.*;
import game.GameState.Who;


// ABBreakthroughPlayer2 is identical to MiniMaxBreakthroughPlayer
// except for the search process, which uses alpha beta pruning.

public class ABBreakthroughPlayer2 extends GamePlayer {
	public final int MAX_DEPTH = 50;
	public final int MAX_SCORE = Integer.MAX_VALUE;
	public int depthLimit;
	private int origDepth;
	private double timeSpent = 0;
	
	protected ScoredBreakthroughMove [] mvStack;
	/**
	 * Initializes the stack of Moves.
	 */
	public void init()
	{
		mvStack = new ScoredBreakthroughMove [MAX_DEPTH];
		for (int i=0; i<MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}
	}
	
	protected class ScoredBreakthroughMove extends BreakthroughMove {
		int row;
		public double score;
		public ScoredBreakthroughMove(int srow, int scol, int erow, int ecol, double s)
		{
			super();
			startRow = srow;
			startCol = scol;
			endingRow = erow;
			endingCol = ecol;
			score = s;
		}
		public void set(int srow, int scol, int erow, int ecol, double s)
		{
			startRow = srow;
			startCol = scol;
			endingRow = erow;
			endingCol = ecol;
			score = s;
		}
	}
	
	public ABBreakthroughPlayer2(String nname, int d)
	{ 
		super(nname, new BreakthroughState(), true);
		origDepth = d;
	}

	/**
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw
	 * +X, for a HOME win and -X for an AWAY win.
	 * @param brd Connect4 board to be examined
	 * @param mv where to place the score information; column is irrelevant
	 * @return true if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv)
	{
		GameState.Status status = brd.getStatus();
		boolean isTerminal = true;
		
		if (status == GameState.Status.HOME_WIN) {
			mv.set(0,0,6,0, MAX_SCORE);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.set(6,0,6,6, - MAX_SCORE);
		}else {
			isTerminal = false;
		}
		return isTerminal;
	}
	
	private static int eval_numpieces(BreakthroughState brd) {
		int num_home = 0;
		int num_away = 0;
		for(int i=0; i<BreakthroughState.N; i++) {
			for(int j=0; j<BreakthroughState.N; j++) {
				if(brd.board[i][j] == BreakthroughState.homeSym)
					num_home++;
				else if(brd.board[i][j] == BreakthroughState.awaySym)
					num_away++;
			}
		}
		return num_home - num_away;
	}
	
	private static int eval_numus(BreakthroughState brd) {
		int num_home = 0;
		int num_away = 0;
		for(int i=0; i<BreakthroughState.N; i++) {
			for(int j=0; j<BreakthroughState.N; j++) {
				if(brd.board[i][j] == BreakthroughState.homeSym)
					num_home++;
				else if(brd.board[i][j] == BreakthroughState.awaySym)
					num_away++;
			}
		}
		if(brd.who == GameState.Who.HOME)
			return num_home;
			else return -num_away;
	}
	
	private static int eval_coverage(BreakthroughState brd) {
		int num_home = 0;
		int num_away = 0;
		int N = BreakthroughState.N;
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				if(brd.board[i][j] == BreakthroughState.homeSym){
					if(i < N-1 && j < N-1 && brd.board[i+1][j+1] == BreakthroughState.homeSym) {
						num_home++;
					} else if(i > 0 && j < N-1 && brd.board[i-1][j+1] == BreakthroughState.homeSym) {
						num_home++;
					} else if(i > 0 && j > 0 && brd.board[i-1][j-1] == BreakthroughState.homeSym) {
						num_home++;
					} else if(i < N-1 && j > 0 && brd.board[i+1][j-1] == BreakthroughState.homeSym) {
						num_home++;
					}
				} else if(brd.board[i][j] == BreakthroughState.awaySym) {
					if(i < N-1 && j < N-1 && brd.board[i+1][j+1] == BreakthroughState.awaySym) {
						num_away++;
					} else if(i > 0 && j < N-1 && brd.board[i-1][j+1] == BreakthroughState.awaySym) {
						num_away++;
					} else if(i > 0 && j > 0 && brd.board[i-1][j-1] == BreakthroughState.awaySym) {
						num_away++;
					} else if(i < N-1 && j > 0 && brd.board[i+1][j-1] == BreakthroughState.awaySym) {
						num_away++;
					}
				}
					
			}
		}
		return num_home - num_away;
	}
	
	private static int eval_win(BreakthroughState brd) {
		int home = 0, away = 0;
		int N = BreakthroughState.N;
		for(int i=0; i<N; i++) {
			if(brd.board[0][i] == BreakthroughState.homeSym){
				home++;
			}
			if(brd.board[0][i] == BreakthroughState.awaySym){
				away++;
			}
			
			if(brd.board[N-1][i] == BreakthroughState.homeSym){
				home++;
			}
			if(brd.board[N-1][i] == BreakthroughState.awaySym){
				away++;
			}
		}
		return home - away;
	}
	
	//Evaluation Function
	private static int evalBoard(BreakthroughState brd){
		int h1 = eval_numpieces(brd);
		int h3 = eval_coverage(brd);
		int h4 = eval_win(brd);
		return h1;
	}
	/**
	 * Performs alpha beta pruning.
	 * @param brd
	 * @param currDepth
	 * @param alpha
	 * @param beta
	 */
	private void alphaBeta(BreakthroughState brd, int currDepth,
										double alpha, double beta)
	{
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;
		
		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);
		
		if(currDepth == depthLimit){
			mvStack[currDepth].set(0,0,0,0,evalBoard(brd));
		}else if (!isTerminal){
			//ScoredBreakthroughMove tempMv = new ScoredBreakthroughMove(0,0,0,0, Double.MAX_VALUE);
			double bestScore = (toMaximize ? 
					Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth+1];
			
			bestMove.set(0,0,0,0, bestScore);
			GameState.Who currTurn = brd.getWho();
			char opp = brd.who == GameState.Who.HOME ?
					BreakthroughState.awaySym : BreakthroughState.homeSym;
			
			ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
			ArrayList<BreakthroughMove> genericMoves = new ArrayList<BreakthroughMove>();
			ArrayList<BreakthroughMove> takeMoves = new ArrayList<BreakthroughMove>();
			BreakthroughMove mv = new BreakthroughMove();
			int dir = brd.getWho() == GameState.Who.HOME ? +1 : -1;
			
			//Add all possible moves to arraylist so we can alphabeta them.
			for (int r=0; r<BreakthroughState.N; r++) {
				for (int c=0; c<BreakthroughState.N; c++) {
					mv.startRow = r;
					mv.startCol = c;
					mv.endingRow = r+dir; 
					mv.endingCol = c;
					if (brd.moveOK(mv)) {
						if (brd.board[mv.endingRow][mv.endingCol] == opp) {
							takeMoves.add((BreakthroughMove)mv.clone());
						}
						else genericMoves.add((BreakthroughMove)mv.clone());
					}
					mv.endingRow = r+dir; mv.endingCol = c+1;
					if (brd.moveOK(mv)) {
						if (brd.board[mv.endingRow][mv.endingCol] == opp) {
							takeMoves.add((BreakthroughMove)mv.clone());
						}
						else genericMoves.add((BreakthroughMove)mv.clone());
					}
					mv.endingRow = r+dir; mv.endingCol = c-1;
					if (brd.moveOK(mv)) {
						if (brd.board[mv.endingRow][mv.endingCol] == opp) {
							takeMoves.add((BreakthroughMove)mv.clone());
						}
						else genericMoves.add((BreakthroughMove)mv.clone());
					}
				}
			}
			
			moves.addAll(takeMoves);
			moves.addAll(genericMoves);
			
			for(int i=0; i<moves.size(); i++) {
				BreakthroughMove tmp = moves.get(i);
				char tmpchar = brd.board[tmp.endingRow][tmp.endingCol];
				brd.makeMove(tmp);
				
				alphaBeta(brd, currDepth+1, alpha, beta);
				
				//Undo Move
				/* What I will do instead to undo the move is set the board at the 
				 * temp's end row/end col that there is nothing there and at the
				 * temp's start row/col that there is a home or away piece, depending
				 * and then subtract 1 from the number of moves				
				 */
				brd.who = currTurn;
				char PLAYER = brd.who == GameState.Who.HOME ? BreakthroughState.homeSym : BreakthroughState.awaySym;
				
				brd.board[tmp.endingRow][tmp.endingCol] = tmpchar;
				brd.board[tmp.startRow][tmp.startCol] = PLAYER;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				
				
				// Check out the results, relative to what we've seen before
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(tmp.startRow, tmp.startCol, tmp.endingRow, tmp.endingCol, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(tmp.startRow, tmp.startCol, tmp.endingRow, tmp.endingCol, nextMove.score);
				}
				
				// Update alpha and beta. Perform pruning, if possible.
				if (toMinimize) {
					beta = Math.min(bestMove.score, beta);
					if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
						return;
					}
				} else {
					alpha = Math.max(bestMove.score, alpha);
					if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
						return;
					}
				}
			}
		}
	}
	public void timeOfLastMove(double secs){
		timeSpent += secs;
	}
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		int pieceDifference = eval_numpieces((BreakthroughState) brd);
		double timeRemaining = 420.000000 - timeSpent; 
		if(brd.numMoves < 10 ){
			depthLimit = 3;
		}else {
			if(pieceDifference == 2){
				depthLimit = 5;
			}else if(pieceDifference > 2){
				depthLimit = 3;
			}else if(pieceDifference < 0 && brd.numMoves > 20){
				depthLimit = 8;
			}else if(timeRemaining < 150.0){
				depthLimit = 6;
			}else if(timeRemaining < 100.0)
			{
				depthLimit = 5;
			}else if(timeRemaining < 50.0){
				depthLimit = 3;
			}else{
				depthLimit = origDepth;
			}
		}
		alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
										 Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
		System.out.println("Time remaining: " + (420.00 - timeSpent) );
		return mvStack[0];
	}
	
	public static char [] toChars(String x)
	{
		char [] res = new char [x.length()];
		for (int i=0; i<x.length(); i++)
			res[i] = x.charAt(i);
		return res;
	}
	
	public static void main(String [] args)
	{
		int depth = 10;
		GamePlayer p = new ABBreakthroughPlayer2("Normal", depth);
		p.compete(args);
	}
}
