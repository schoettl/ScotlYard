package kj.scotlyard.game.control.impl;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import kj.scotlyard.game.control.GameStatus;
import kj.scotlyard.game.graph.GameGraph;
import kj.scotlyard.game.graph.StationVertex;
import kj.scotlyard.game.model.DetectivePlayer;
import kj.scotlyard.game.model.Game;
import kj.scotlyard.game.model.GameState;
import kj.scotlyard.game.model.Move;
import kj.scotlyard.game.model.MrXPlayer;
import kj.scotlyard.game.model.Player;
import kj.scotlyard.game.model.TheMoveProducer;
import kj.scotlyard.game.rules.GameInitPolicy;
import kj.scotlyard.game.rules.GameWin;
import kj.scotlyard.game.rules.Rules;
import kj.scotlyard.game.rules.TurnPolicy;

class NotInGameControllerState extends GameControllerState {
	
	@SuppressWarnings("serial")
	private class NewMrXEdit extends AbstractUndoableEdit {
		
		private MrXPlayer oldMrX;
		
		private MrXPlayer newMrX;
		
		public NewMrXEdit(MrXPlayer oldMrX) {
			this.oldMrX = oldMrX;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			newMrX = game.getMrX();
			game.setMrX(oldMrX);
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			game.setMrX(newMrX);
		}
		
	}
	
	@SuppressWarnings("serial")
	private class NewDetectiveEdit extends AbstractUndoableEdit {
		
		private DetectivePlayer detective;
		
		private int index;
		
		public NewDetectiveEdit(DetectivePlayer detective) {
			this.detective = detective;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			index = game.getDetectives().indexOf(detective);
			game.getDetectives().remove(detective);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			game.getDetectives().add(index, detective);
		}
		
	}
	
	@SuppressWarnings("serial")
	private class RemoveDetectiveEdit extends AbstractUndoableEdit {
		
		private DetectivePlayer detective;
		
		private int index;
		
		public RemoveDetectiveEdit(int index, DetectivePlayer detective) {
			this.detective = detective;
			this.index = index;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			game.getDetectives().add(index, detective);
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();			
			game.getDetectives().remove(detective);
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ShiftUpDetectiveEdit extends AbstractUndoableEdit {
		
		private DetectivePlayer detective;
		
		private int oldIndex;
		
		public ShiftUpDetectiveEdit(int oldIndex, DetectivePlayer detective) {
			this.detective = detective;
			this.oldIndex = oldIndex;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			game.getDetectives().remove(detective);
			game.getDetectives().add(oldIndex, detective);
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();			
			game.getDetectives().remove(detective);
			game.getDetectives().add(oldIndex - 1, detective);
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ShiftDownDetectiveEdit extends AbstractUndoableEdit {
		
		private DetectivePlayer detective;
		
		private int oldIndex;
		
		public ShiftDownDetectiveEdit(int oldIndex, DetectivePlayer detective) {
			this.detective = detective;
			this.oldIndex = oldIndex;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			game.getDetectives().remove(detective);
			game.getDetectives().add(oldIndex, detective);
		}
		
		@Override
		public void redo() throws CannotRedoException {
			super.redo();			
			game.getDetectives().remove(detective);
			game.getDetectives().add(oldIndex + 1, detective);
		}
		
	}
	
	
	
	private final Game game;
	
	private final GameGraph gameGraph;
	
	private final UndoManager undoManager;
	
	protected NotInGameControllerState(TheGameController controller) {
		super(controller);
		game = controller.getGame();
		gameGraph = controller.getGameGraph();
		undoManager = controller.getUndoManager();
	}

	private void raiseIllegalStateException() {
		throw new IllegalStateException("We are currently NOT_IN_GAME.");
	}
	
	@Override
	public GameStatus getStatus() {
		return GameStatus.NOT_IN_GAME;
	}

	@Override
	public void newGame() {
		getController().getGame().getMoves().clear();
		// Das reicht schon aus.
		// Andere Werte werden bei Initialisierung (start) ueberschrieben.
		undoManager.discardAllEdits();
	}

	@Override
	public void clearPlayers() {
		getController().getGame().setMrX(null);
		getController().getGame().getDetectives().clear();
		undoManager.discardAllEdits(); // TODO oder auch Undoable machen?
	}

	@Override
	public void newMrX() {
		undoManager.addEdit(new NewMrXEdit(game.getMrX()));
		game.setMrX(new MrXPlayer());
	}

	@Override
	public void newDetective() {
		// TODO regeln beachten?
		DetectivePlayer d = new DetectivePlayer();
		game.getDetectives().add(d);
		undoManager.addEdit(new NewDetectiveEdit(d));
	}

	@Override
	public void removeDetective(DetectivePlayer detective) {
		// TODO regeln beachten?
		undoManager.addEdit(new RemoveDetectiveEdit(game.getDetectives().indexOf(detective), detective));
		game.getDetectives().remove(detective);
	}

	@Override
	public void shiftUpDetective(DetectivePlayer detective) {
		List<DetectivePlayer> ds = getController().getGame().getDetectives();
		int i = ds.indexOf(detective);
		if (i > 0) {
			ds.remove(detective);
			ds.add(i - 1, detective);
			undoManager.addEdit(new ShiftUpDetectiveEdit(i, detective));
		}		
	}

	@Override
	public void shiftDownDetective(DetectivePlayer detective) {
		List<DetectivePlayer> ds = getController().getGame().getDetectives();
		int i = ds.indexOf(detective);
		if (i < ds.size()) {
			ds.remove(detective);
			ds.add(i + 1, detective);
			undoManager.addEdit(new ShiftDownDetectiveEdit(i, detective));
		}
	}

	@Override
	public void start() {
		TheMoveProducer moveProducer = TheMoveProducer.createInstance();
				
		if (!game.getMoves().isEmpty()) {
			throw new IllegalStateException("Cannot start game, while Move list is not cleared. Call newGame and try again.");
		}
		
		// TODO rules zu detective count beachten?
		
		// Valid GameState -> proceed with initialization
		Rules rules = getController().getRules();
		GameInitPolicy initPolicy = rules.getGameInitPolicy();
		TurnPolicy turnPolicy = rules.getTurnPolicy();
		
		game.setCurrentRoundNumber(GameState.INITIAL_ROUND_NUMBER);
		while (turnPolicy.getNextRoundNumber(game, gameGraph) == GameState.INITIAL_ROUND_NUMBER) {
			Player player = turnPolicy.getNextPlayer(game, gameGraph);
			game.setCurrentPlayer(player);
			game.setItems(player, initPolicy.createItemSet(game, player));
			
			StationVertex station = initPolicy.suggestInitialStation(game, gameGraph, getController().getInitialPositions(), player);
			Move initMove = moveProducer.createInitialMove(player, station);
			game.getMoves().add(initMove);
		}
		game.setCurrentRoundNumber(turnPolicy.getNextRoundNumber(game, gameGraph));

		GameWin win = getController().getRules().getGameWinPolicy().isGameWon(game, gameGraph);
		getController().setState(this, (win == GameWin.NO) ? GameStatus.IN_GAME : GameStatus.NOT_IN_GAME, win);
		
	}

	@Override
	public void abort() {
		raiseIllegalStateException();
	}

	@Override
	public void move(Move move) {
		raiseIllegalStateException();
	}
	
}
