package breakthrough;
import java.util.ArrayList;

import game.*;


// AlphaBetaBreakthroughPlayer is identical to MiniMaxBreakthroughPlayer
// except for the search process, which uses alpha beta pruning.

public class AlphaBetaBreakthroughPlayer extends GamePlayer {
	public final int MAX_DEPTH = 50;
	public final int MAX_SCORE = Integer.MAX_VALUE;
	public int depthLimit;
	
	
	protected ScoredBreakthroughMove [] mvStack;
	
	protected class ScoredBreakthroughMove extends BreakthroughMove {
		int row;
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
		public double score;
	}
	
	public AlphaBetaBreakthroughPlayer(String nname, int d)
	{ 
		super(nname, new BreakthroughState(), true);
		depthLimit = d;
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
	//Evaluation Function
	private static int evalBoard(BreakthroughState brd){
		return Integer.MAX_VALUE+1;
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
			ScoredBreakthroughMove tempMv = new ScoredBreakthroughMove(0,0,0,0, Double.MAX_VALUE);
			double bestScore = (toMaximize ? 
					Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth+1];
			bestMove.set(0,0,0,0, bestScore);
			GameState.Who currTurn = brd.getWho();
			
			ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
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
							moves.add((BreakthroughMove)mv.clone());
					}
					mv.endingRow = r+dir; mv.endingCol = c+1;
					if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove)mv.clone());
					}
					mv.endingRow = r+dir; mv.endingCol = c-1;
					if (brd.moveOK(mv)) {
							moves.add((BreakthroughMove)mv.clone());
					}
				}
			}
			
			for(int i=0; i<moves.size(); i++) {
				BreakthroughMove tmp = moves.get(i);
				brd.makeMove(tmp);
				
				alphaBeta(brd, currDepth+1, alpha, beta);
				
				//Undo Move
				BreakthroughMove undoMove = (BreakthroughMove)tmp.clone();
				undoMove.startCol = tmp.endingCol;
				undoMove.endingCol = tmp.startCol;
				undoMove.startRow = tmp.endingRow;
				undoMove.endingRow = tmp.startRow;
				
				brd.makeMove(undoMove);
				brd.numMoves -= 2;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;
				
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
		
	public GameMove getMove(GameState brd, String lastMove)
	{ 
		alphaBeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, 
										 Double.POSITIVE_INFINITY);
		System.out.println(mvStack[0].score);
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
		int depth = 8;
		GamePlayer p = new AlphaBetaBreakthroughPlayer("BT A-B F1 " + depth, depth);
		p.compete(args);
	}
}
