/*
 * ScotlYard -- A software implementation of the Scotland Yard board game
 * Copyright (C) 2012  Jakob Schöttl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kj.scotlyard.board;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.UndoManager;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.ButtonGroup;

import kj.scotlyard.board.board.BoardPanel;
import kj.scotlyard.board.layout.AspectRatioGridLayout;
import kj.scotlyard.board.metadata.GameMetaData;
import kj.scotlyard.game.control.GameController;
import kj.scotlyard.game.control.GameStatus;
import kj.scotlyard.game.control.impl.DefaultGameController;
import kj.scotlyard.game.graph.GameGraph;
import kj.scotlyard.game.graph.StationVertex;
import kj.scotlyard.game.model.DefaultGame;
import kj.scotlyard.game.model.DefaultGameState;
import kj.scotlyard.game.model.DetectivePlayer;
import kj.scotlyard.game.model.Game;
import kj.scotlyard.game.model.GameState;
import kj.scotlyard.game.model.Move;
import kj.scotlyard.game.model.MoveListener;
import kj.scotlyard.game.model.MrXPlayer;
import kj.scotlyard.game.model.Player;
import kj.scotlyard.game.model.TurnListener;
import kj.scotlyard.game.model.item.Ticket;
import kj.scotlyard.game.rules.GameWin;
import kj.scotlyard.game.rules.Rules;
import kj.scotlyard.game.rules.TheRules;
import kj.scotlyard.game.util.MoveProducer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import static kj.scotlyard.board.ActionTools.isSelected;
import static kj.scotlyard.board.ActionTools.setSelected;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Component;

@SuppressWarnings("serial")
public class Board extends JFrame {
	
	private static final Logger logger = Logger.getLogger(Board.class);
	
	private static final double ZOMMING_FACTOR = 1.2; 
	
	private Image img;

	private JPanel contentPane;
	
	private BoardPanel boardPanel;
	
	private JLabel lblCurrentPlayerVal;
	
	private JLabel lblMoveVal;
	
	private JPanel boardPanelContainer;
	
	private JScrollPane boardPanelScrollPane;
	
	private MovePreparationBar movePreparationBar;
	
	private final ButtonGroup modeButtonGroup = new ButtonGroup();
	
	UndoManager undoManager = new UndoManager();
	Rules r;
	GameController gc;
	Game g;
	GameState gs;
	GameGraph gg;
	MovePreparer mPrep;
	Map<Integer, StationVertex> nsm; // Number Station Map
	
	private Dimension originalImageSize;
	
	private TicketSelectionDialog ticketSelectionDialogMrX = new TicketSelectionDialog(this, MrXPlayer.class);
	private TicketSelectionDialog ticketSelectionDialogDetectives = new TicketSelectionDialog(this, DetectivePlayer.class);
	
	// Fuer Action MoveDetectivesNow, die alle Detectives ziehen laesst (sofern Moves vorbereitet sind)
	private boolean doMoveDetectives = false;
	private TurnListener moveDetectivesTurnListener = new TurnListener() {
		@Override
		public void currentRoundChanged(GameState gameState, int oldRoundNumber,
				int newRoundNumber) {
			// Runde vorbei / Neue Runde
			doMoveDetectives = false;
		}
		@Override
		public void currentPlayerChanged(GameState gameState, Player oldPlayer,
				Player newPlayer) {
			if (doMoveDetectives) {
				Move m = mPrep.getMove(newPlayer);
				if (m != null) {
					move(m);
				}
			}
		}
	};
	
	// TODO beide muessen beim laden der Board def gesetzt werden
	private double normalZoomFactor = 0.2; // kann sein was will, nur 1 macht keinen sinn, weil das bild dann viel zu riessig ist!
	private double zoomFactor = normalZoomFactor;
	
	private final Action newGameAction = new NewGameAction();
	private final Action clearPlayersAction = new ClearPlayersAction();
	private final Action newMrXAction = new NewMrXAction();
	private final Action newDetectiveAction = new NewDetectiveAction();
	private final Action startAction = new StartAction();
	private final Action abortAction = new AbortAction();
	private final Action moveAction = new MoveAction();
	private final Action gameStatusWinAction = new GameStatusWinAction();
	private final Action removeDetectiveAction = new RemoveDetectiveAction();
	private final Action shiftDetectiveUpAction = new ShiftDetectiveUpAction();
	private final Action shiftDetectiveDownAction = new ShiftDetectiveDownAction();
	private final Action newGameWithPlayersAction = new NewGameWithPlayersAction();
	private final Action undoAction = new UndoAction();
	private final Action redoAction = new RedoAction();
	private final Action suggestMoveAction = new SuggestMoveAction();
	private final Action moveNowAction = new MoveNowAction();
	private final Action moveDetectivesNowAction = new MoveDetectivesNowAction();
	private final Action quickPlayAction = new QuickPlayAction();
	private final Action fitBoardAction = new FitBoardAction();
	private final Action zoomInAction = new ZoomInAction();
	private final Action zoomOutAction = new ZoomOutAction();
	private final Action zoomNormalAction = new ZoomNormalAction();
	private final Action selectCurrentPlayerAction = new SelectCurrentPlayerAction();
	private final Action jointMoving = new JointMoving();
	private final Action mrXAlwaysVisibleAction = new MrXAlwaysVisibleAction();
	private JLabel lblCurrentRoundNumberVal;



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Board frame = new Board();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * Create the frame.
	 */
	public Board() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnGameController = new JMenu("GameController");
		mnGameController.setMnemonic(KeyEvent.VK_C);
		menuBar.add(mnGameController);
		
		JMenuItem mntmNewGame = new JMenuItem("New Game");
		mntmNewGame.setAction(newGameAction);
		mnGameController.add(mntmNewGame);
		
		JMenuItem mntmNewGameWith = new JMenuItem("New Game with Player");
		mntmNewGameWith.setAction(newGameWithPlayersAction);
		mnGameController.add(mntmNewGameWith);
		
		mnGameController.addSeparator();
		
		JMenuItem mntmNewMrx = new JMenuItem("New MrX");
		mntmNewMrx.setAction(newMrXAction);
		mnGameController.add(mntmNewMrx);
		
		JMenuItem mntmNewDetective = new JMenuItem("New Detective");
		mntmNewDetective.setAction(newDetectiveAction);
		mnGameController.add(mntmNewDetective);
		
		JMenu mnDetectives = new JMenu("Detectives");
		mnDetectives.setMnemonic(KeyEvent.VK_V);
		mnGameController.add(mnDetectives);
		
		JMenuItem mntmRemoveDetective = new JMenuItem("Remove Detective...");
		mntmRemoveDetective.setAction(removeDetectiveAction);
		mnDetectives.add(mntmRemoveDetective);
		
		mnDetectives.addSeparator();
		
		JMenuItem mntmShiftDetectiveUp = new JMenuItem("Shift Detective Up...");
		mntmShiftDetectiveUp.setAction(shiftDetectiveUpAction);
		mnDetectives.add(mntmShiftDetectiveUp);
		
		JMenuItem mntmShiftDetectiveDown = new JMenuItem("Shift Detective Down...");
		mntmShiftDetectiveDown.setAction(shiftDetectiveDownAction);
		mnDetectives.add(mntmShiftDetectiveDown);
		
		JMenuItem mntmClearPlayers = new JMenuItem("Clear Players");
		mntmClearPlayers.setAction(clearPlayersAction);
		mnGameController.add(mntmClearPlayers);
		
		mnGameController.addSeparator();
		
		JMenuItem mntmStart = new JMenuItem("Start");
		mntmStart.setAction(startAction);
		mnGameController.add(mntmStart);
		
		JMenuItem mntmAbort = new JMenuItem("Abort");
		mntmAbort.setAction(abortAction);
		mnGameController.add(mntmAbort);
		
		mnGameController.addSeparator();
		
		JMenuItem mntmMove = new JMenuItem("Move...");
		mntmMove.setAction(moveAction);
		mnGameController.add(mntmMove);
		
		JMenu mnUndoRedo = new JMenu("Undo/Redo");
		mnUndoRedo.setMnemonic(KeyEvent.VK_U);
		mnGameController.add(mnUndoRedo);
		
		JMenuItem mntmUndo = new JMenuItem("Undo");
		mntmUndo.setAction(undoAction);
		mnUndoRedo.add(mntmUndo);
		
		JMenuItem mntmRedo = new JMenuItem("Redo");
		mntmRedo.setAction(redoAction);
		mnUndoRedo.add(mntmRedo);
		
		mnGameController.addSeparator();
		
		JMenuItem mntmGameStatusAnd = new JMenuItem("Game Status and Win...");
		mntmGameStatusAnd.setAction(gameStatusWinAction);
		mnGameController.add(mntmGameStatusAnd);
		
		
		JMenu mnBoardPanel = new JMenu("BoardPanel");
		mnBoardPanel.setMnemonic(KeyEvent.VK_B);
		menuBar.add(mnBoardPanel);
		
		// Um zu ueberpruefen, ob das neu setzen waehrend dem Spiel funktioniert
		JMenuItem mntmSetGameStateNull = new JMenuItem("Set GameState to null");
		mntmSetGameStateNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setGameState(null);
			}
		});
		mntmSetGameStateNull.setMnemonic(KeyEvent.VK_N);
		mnBoardPanel.add(mntmSetGameStateNull);
		
		JMenuItem mntmSetGameState = new JMenuItem("Set GameState");
		mntmSetGameState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setGameState(gs);
			}
		});
		mntmSetGameState.setMnemonic(KeyEvent.VK_G);
		mnBoardPanel.add(mntmSetGameState);
		
		mnBoardPanel.addSeparator();
		
		JMenuItem mntmSetRulesNull = new JMenuItem("Set Rules to null");
		mntmSetRulesNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setRules(null);
			}
		});
		mntmSetRulesNull.setMnemonic(KeyEvent.VK_U);
		mnBoardPanel.add(mntmSetRulesNull);
		
		JMenuItem mntmSetRules = new JMenuItem("Set Rules");
		mntmSetRules.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setRules(r);
			}
		});
		mntmSetRules.setMnemonic(KeyEvent.VK_R);
		mnBoardPanel.add(mntmSetRules);
		
		mnBoardPanel.addSeparator();
		
		JMenuItem mntmSetImageToNull = new JMenuItem("Set Image to null");
		mntmSetImageToNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setImage(null);
			}
		});
		mntmSetImageToNull.setMnemonic(KeyEvent.VK_L);
		mnBoardPanel.add(mntmSetImageToNull);
		
		JMenuItem mntmSetImage = new JMenuItem("Set Image");
		mntmSetImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boardPanel.setImage(img);
			}
		});
		mntmSetImage.setMnemonic(KeyEvent.VK_I);
		mnBoardPanel.add(mntmSetImage);
		
		JMenu mnErnsthaft = new JMenu("Ernsthaft");
		menuBar.add(mnErnsthaft);
		
		JCheckBoxMenuItem mntmQuickPlay = new JCheckBoxMenuItem("Quick Play");
		mntmQuickPlay.setAction(quickPlayAction);
		mnErnsthaft.add(mntmQuickPlay);
		
		JCheckBoxMenuItem chckbxmntmJointMoving = new JCheckBoxMenuItem("Joint Moving");
		chckbxmntmJointMoving.setAction(jointMoving);
		mnErnsthaft.add(chckbxmntmJointMoving);
		
		JMenuItem mntmShowMrx = new JMenuItem("Show MrX");
		mnErnsthaft.add(mntmShowMrx);
		
		JMenu mnZoom = new JMenu("Zoom");
		mnZoom.setMnemonic(KeyEvent.VK_Z);
		mnErnsthaft.add(mnZoom);
		
		JCheckBoxMenuItem chckbxmntmFitBoard = new JCheckBoxMenuItem("Fit Board");
		chckbxmntmFitBoard.setAction(fitBoardAction);
		mnZoom.add(chckbxmntmFitBoard);
		
		JMenuItem mntmZoomIn = new JMenuItem("Zoom In");
		mntmZoomIn.setAction(zoomInAction);
		mnZoom.add(mntmZoomIn);
		
		JMenuItem mntmZoomOut = new JMenuItem("Zoom Out");
		mntmZoomOut.setAction(zoomOutAction);
		mnZoom.add(mntmZoomOut);
		
		JMenuItem mntmZoomNormal = new JMenuItem("Zoom Normal");
		mntmZoomNormal.setAction(zoomNormalAction);
		mnZoom.add(mntmZoomNormal);
		
		JMenu mnMode = new JMenu("Mode");
		mnMode.setMnemonic(KeyEvent.VK_M);
		menuBar.add(mnMode);
		
		JRadioButtonMenuItem mntmNormalGame = new JRadioButtonMenuItem("Normal Game");
		modeButtonGroup.add(mntmNormalGame);
		mnMode.add(mntmNormalGame);
		
		// Besser, wenn NormalGame standardmäßig auch Server wäre
		JRadioButtonMenuItem mntmBeServer = new JRadioButtonMenuItem("Be Server");
		modeButtonGroup.add(mntmBeServer);
		mnMode.add(mntmBeServer);
		
		// Connect to Server müsste eigentlich eigene Funktion sein
		JRadioButtonMenuItem mntmBeClient = new JRadioButtonMenuItem("Be Client / Connect to Server");
		modeButtonGroup.add(mntmBeClient);
		mnMode.add(mntmBeClient);
		
		JRadioButtonMenuItem mntmMrXTracking = new JRadioButtonMenuItem("MrX Tracking");
		modeButtonGroup.add(mntmMrXTracking);
		mnMode.add(mntmMrXTracking);
		
		mnMode.addSeparator();
		
		JCheckBoxMenuItem mntmMrXAlwaysVisible = new JCheckBoxMenuItem("MrX Always Visible");
		mntmMrXAlwaysVisible.setAction(mrXAlwaysVisibleAction);
		mnMode.add(mntmMrXAlwaysVisible);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Board laden
		BoardGraphLoader bgl = new BoardGraphLoader();
		try {
			bgl.load("graph-description", "initial-stations");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		boardPanelContainer = new JPanel(new AspectRatioGridLayout());
		boardPanel = new BoardPanel();
		
		for (JComponent c : bgl.getVisualComponents()) {
			boardPanel.add(c);
		}
		boardPanel.buildVisualStationMap();
		nsm = bgl.getNumberStationMap();
		
		
		
		
		img = null;
		// Variante 1
		try {
			img = ImageIO.read(new File("original-scotland-yard-board.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Variante 2 (ungefaehr)
//		img = Toolkit.getDefaultToolkit().getImage("original-scotland-yard-board.png");
//		private final ImageObserver imageSizeObserver = new ImageObserver() {
//			@Override
//			public boolean imageUpdate(Image img, int infoflags, int x, int y,
//					int width, int height) {
//				// width und height koennten einzeln oder gleichzeitig ankommen
//				if ((infoflags & WIDTH) != 0) {				
//					imageWidth = width;
//				}
//				if ((infoflags & HEIGHT) != 0) {				
//					imageHeight = height;
//				}
//				if ((imageWidth >= 0) && (imageHeight >= 0)) {
//					preferredSize = new Dimension(imageWidth, imageHeight);
//					return false; // required information has been acquired
//				}
//				return true; // further updates are needed
//			}
//		};
//		imageWidth = image.getWidth(imageSizeObserver);
//		imageHeight = image.getHeight(imageSizeObserver);
//		if ((imageWidth >= 0) && (imageHeight >= 0)) {
//			preferredSize = new Dimension(imageWidth, imageHeight);
//		}		
		
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w < 0 || h < 0) {
			throw new IllegalArgumentException("The image seems to be not loaded " +
					"completely: Cannot determine image's width and/or height.");
		}
		originalImageSize = new Dimension(w, h);
		boardPanel.setImage(img);	
		boardPanel.setPreferredSize(originalImageSize);
		
		// GameState
		r = new TheRules();
		g = new DefaultGame();
		gs = new DefaultGameState(g);
		gg = bgl.getGameGraph();
		gc = new DefaultGameController(g, gg, r);
		gc.setUndoManager(undoManager);
		gc.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				GameController c = (GameController) o;
				
				movePreparationBar.setEnabled(c.getStatus() == GameStatus.IN_GAME);
				setGameControllerActionsEnabled(c.getStatus());
				if (c.getStatus() != GameStatus.IN_GAME)
					showGameStatusAndWin(c.getStatus(), c.getWin());
			}
		});
		mPrep = new MovePreparer(gs, gg) {
			@Override
			protected void errorSelectingPlayer(Player player) {
				logger.error("dieser player kann (jetzt) nicht ausgewaehlt werden");
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
			
			@Override
			protected void errorImpossibleNextStation(StationVertex station,
					Player player) {
				logger.error("impossible next station for: " + player);
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}

			@Override
			protected Ticket selectTicket(Set<Ticket> tickets, Player player) {
				if (player instanceof MrXPlayer)
					return ticketSelectionDialogMrX.show(tickets, player);
				else // DetectivePlayer
					return ticketSelectionDialogDetectives.show(tickets, player);
//				return tickets.iterator().next(); // gleich das erste
			}
		};
		mPrep.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				logger.debug("MovePreparator Ereignis");
				if (arg instanceof Move) {
					logger.debug("move prepared");
					Player player = ((Move) arg).getPlayer();
					
					if (player == gs.getCurrentPlayer()) {
						lblMoveVal.setText(arg.toString());
					}
					
					boolean furtherMoves = (player instanceof MrXPlayer) ? 
							ticketSelectionDialogMrX.isFurtherMovesSelected() : false;
					if (isSelected(quickPlayAction)
							&& player == gs.getCurrentPlayer() // selected player's turn
							&& !furtherMoves) { // no further moves
						logger.debug("Move fertig vorbereitet, QuickPlay, no further moves and Turn");
						move((Move) arg);
					}
				}
			}
		});
		
		gs.addMoveListener(new MoveListener() {
			@Override
			public void movesCleard(GameState gameState) {
				mPrep.resetAll();
			}
			@Override
			public void moveUndone(GameState gameState, Move move) {
				mPrep.reset(move.getPlayer());				
			}
			@Override
			public void moveDone(GameState gameState, Move move) {
//				mPrep.reset(move.getPlayer());
				// Unnoetig, da zum Rundenwechsel eh resetAll() aufgerufen wird
				// Stoerend, dadurch das (nicht ganz zuverlaessige) counting nicht funktioniert (MoveDetectivesNowAction)
			}
		});
		gs.addTurnListener(new TurnListener() {
			@Override
			public void currentRoundChanged(GameState gameState, int oldRoundNumber,
					int newRoundNumber) {
				// Beim Wechsel in naechste Runde
				logger.debug(String.format("round changed: %d -> %d", oldRoundNumber, newRoundNumber));
				
				lblCurrentRoundNumberVal.setText(String.valueOf(newRoundNumber));
				
				mPrep.resetAll();
			}
			@Override
			public void currentPlayerChanged(GameState gameState, Player oldPlayer,
					Player newPlayer) {
				lblCurrentPlayerVal.setText((newPlayer == null) ? "(None)"
						: GameMetaData.getForPlayer(newPlayer).getName());

				// TODO nicht fuer current, sondern fuer selected player
				Move m = mPrep.getMove(newPlayer);
				lblMoveVal.setText((m == null) ? "Noch kein Move vorbereitet" : m.toString());
			}
		});
		gs.addTurnListener(moveDetectivesTurnListener);
		
		setGameControllerActionsEnabled(gc.getStatus());
		boardPanel.setGameState(gs);
		boardPanel.setGameGraph(gg);
		boardPanel.setMovePreparer(mPrep);
		boardPanel.setRules(r);
		
		boardPanelContainer.add(boardPanel);
		// mit preferredSize ist es so:
		// 1. vorher nicht gesetzt heisst: nimm' vom layoutmanager
		// 2. setzen heisst: nimm' ab jetzt nur noch das angegebene
		// 3. wieder auf null setzen heisst: nicht gesetzt, siehe 1.
		boardPanelContainer.setPreferredSize(new Dimension());
		
		boardPanelScrollPane = new JScrollPane(boardPanelContainer);
		
		contentPane.add(boardPanelScrollPane, BorderLayout.CENTER);
		
		JPanel toolbarContainer = new JPanel();
		contentPane.add(toolbarContainer, BorderLayout.NORTH);
		toolbarContainer.setLayout(new BoxLayout(toolbarContainer, BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		toolbarContainer.add(panel);
		
		JLabel label = new JLabel("GameController");
		panel.add(label);
		
		JButton button = new JButton("New Game with Players");
		button.setAction(newGameWithPlayersAction);
		panel.add(button);
		
		JButton button_1 = new JButton("Start");
		button_1.setAction(startAction);
		panel.add(button_1);
		
		JButton button_2 = new JButton("Abort");
		button_2.setAction(abortAction);
		panel.add(button_2);
		
		JButton button_3 = new JButton("Move...");
		button_3.setAction(moveAction);
		panel.add(button_3);
		
		movePreparationBar = new MovePreparationBar(gs, mPrep, nsm);
		movePreparationBar.setEnabled(false);
		toolbarContainer.add(movePreparationBar);
		
		JPanel moveControlBar = new JPanel();
		toolbarContainer.add(moveControlBar);
		
		lblMoveVal = new JLabel("MoveVal");
		moveControlBar.add(lblMoveVal);
		
		JButton btnMove = new JButton("Move!");
		btnMove.setAction(moveNowAction);
		moveControlBar.add(btnMove);
		
		JButton btnSuggestMove = new JButton("Suggest Move");
		btnSuggestMove.setAction(suggestMoveAction);
		moveControlBar.add(btnSuggestMove);
		
		JButton btnMoveRound = new JButton("Move Detectives!");
		btnMoveRound.setAction(moveDetectivesNowAction);
		moveControlBar.add(btnMoveRound);
		
		JPanel panelLeft = new JPanel();
		contentPane.add(panelLeft, BorderLayout.WEST);
		panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.Y_AXIS));
		
		JLabel lblCurrentRoundNumber = new JLabel("Current Round Number");
		lblCurrentRoundNumber.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelLeft.add(lblCurrentRoundNumber);
		
		lblCurrentRoundNumberVal = new JLabel("R#");
		lblCurrentRoundNumberVal.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblCurrentRoundNumber.setLabelFor(lblCurrentRoundNumberVal);
		lblCurrentRoundNumberVal.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentRoundNumberVal.setFont(new Font("Tahoma", Font.BOLD, 36));
		panelLeft.add(lblCurrentRoundNumberVal);
		
		JLabel lblSeparator = new JLabel(" ");
		lblSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelLeft.add(lblSeparator);
		
		JLabel lblCurrentPlayer = new JLabel("Current Player");
		lblCurrentPlayer.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelLeft.add(lblCurrentPlayer);
		
		lblCurrentPlayerVal = new JLabel("CurrentPlayerVal");
		lblCurrentPlayer.setLabelFor(lblCurrentPlayerVal);
		lblCurrentPlayerVal.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelLeft.add(lblCurrentPlayerVal);
		lblCurrentPlayerVal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectCurrentPlayerAction.actionPerformed(null);
			}
		});
		
		
//		pack();
	}
	
	
	
	private void showErrorMessage(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass()
				.getSimpleName(), JOptionPane.ERROR_MESSAGE);
	}	
	private void showGameStatusAndWin(GameStatus status, GameWin win) {
		JOptionPane.showMessageDialog(Board.this, String.format(
				"Status: %s\nWin: %s", status, win));
	}
	private void setGameControllerActionsEnabled(GameStatus status) {
		boolean inGame = (status == GameStatus.IN_GAME);
		newGameAction.setEnabled(!inGame);
		//clearPlayersAction.setEnabled(inGame);
		//newMrXAction.setEnabled(inGame);
		//newDetectiveAction.setEnabled(inGame);
		startAction.setEnabled(!inGame);
		abortAction.setEnabled(inGame);
		moveAction.setEnabled(inGame);
		
		//removeDetectiveAction.setEnabled(inGame);
		//shiftDetectiveUpAction.setEnabled(inGame);
		//shiftDetectiveDownAction.setEnabled(inGame);
		newGameWithPlayersAction.setEnabled(!inGame);
	}
	private void updateBoardPanelZoom() {
		int w = (int) (originalImageSize.width * zoomFactor);
		int h = (int) (originalImageSize.height * zoomFactor);
		boardPanelContainer.setPreferredSize(new Dimension(w, h));
		boardPanelScrollPane.getViewport().revalidate();
		boardPanelScrollPane.getViewport().repaint();
		
		// Zusatz, dass nicht kleiner gezoomt werden kann, als das Fenster ist:
		if (w < boardPanelScrollPane.getViewport().getWidth()
				&& h < boardPanelScrollPane.getViewport().getHeight()) {
			w = boardPanelScrollPane.getViewport().getWidth();
			h = boardPanelScrollPane.getViewport().getHeight();
			boardPanelContainer.setPreferredSize(new Dimension(w, h));
		}
	}

	/**
	 * Try to carry out the specified move.
	 * When the MovePolicy throws an Exception,
	 * an error message dialog pops up.
	 * @param move
	 * @return <code>true</code> when successful
	 */
	private boolean move(Move move) {
		boolean success = true;
		try {
			gc.move(move);
		} catch (Exception e2) {
			success = false;
			doMoveDetectives = false;
			e2.printStackTrace();
			showErrorMessage(e2);
		}
		return success;
	}
	
	
	
	// Actions *******************************************
	
	
	private class NewGameAction extends AbstractAction {
		public NewGameAction() {
			putValue(NAME, "New Game");
			putValue(SHORT_DESCRIPTION, "Create a new game");
			putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		}
		public void actionPerformed(ActionEvent e) {
			gc.newGame();
		}
	}
	private class ClearPlayersAction extends AbstractAction {
		public ClearPlayersAction() {
			putValue(NAME, "Clear Players");
			putValue(SHORT_DESCRIPTION, "Clear all players");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		}
		public void actionPerformed(ActionEvent e) {
			gc.clearPlayers();
		}
	}
	private class NewMrXAction extends AbstractAction {
		public NewMrXAction() {
			putValue(NAME, "New MrX");
			putValue(SHORT_DESCRIPTION, "Set new Mr. X");
			putValue(MNEMONIC_KEY, KeyEvent.VK_X);
		}
		public void actionPerformed(ActionEvent e) {
			gc.newMrX();
		}
	}
	private class NewDetectiveAction extends AbstractAction {
		public NewDetectiveAction() {
			putValue(NAME, "New Detective");
			putValue(SHORT_DESCRIPTION, "Add a new detective");
			putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		}
		public void actionPerformed(ActionEvent e) {
			gc.newDetective();
		}
	}
	private class StartAction extends AbstractAction {
		public StartAction() {
			putValue(NAME, "Start");
			putValue(SHORT_DESCRIPTION, "Start the game");
			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		}
		public void actionPerformed(ActionEvent e) {
			try {
				gc.start();
			} catch (Exception e2) {
				showErrorMessage(e2);
			}
		}
	}
	private class AbortAction extends AbstractAction {
		public AbortAction() {
			putValue(NAME, "Abort");
			putValue(SHORT_DESCRIPTION, "Abort the game");
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		}
		public void actionPerformed(ActionEvent e) {
			gc.abort();
		}
	}
	private class MoveAction extends AbstractAction {
		public MoveAction() {
			putValue(NAME, "Move...");
			putValue(SHORT_DESCRIPTION, "Make a move");
			putValue(MNEMONIC_KEY, KeyEvent.VK_M);
		}
		public void actionPerformed(ActionEvent e) {
			Move move = null;
			if (gc.getStatus() == GameStatus.IN_GAME) {
				move = MoveProducer.createRandomSingleMove(gs, gg);
			}
			move(move);
		}
	}
	private class GameStatusWinAction extends AbstractAction {
		public GameStatusWinAction() {
			putValue(NAME, "Game Status and Win...");
			putValue(SHORT_DESCRIPTION, "Show the game status and win");
			putValue(MNEMONIC_KEY, KeyEvent.VK_I); // i as information
		}
		public void actionPerformed(ActionEvent e) {
			showGameStatusAndWin(gc.getStatus(), gc.getWin());
		}
	}
	private class RemoveDetectiveAction extends AbstractAction {
		public RemoveDetectiveAction() {
			putValue(NAME, "Remove Detective...");
			putValue(SHORT_DESCRIPTION, "Remove a detective");
			putValue(MNEMONIC_KEY, KeyEvent.VK_R);
		}
		public void actionPerformed(ActionEvent e) {
			String s = "0";
			while ((s = JOptionPane.showInputDialog(Board.this, "Enter the index of a detective", s)) != null) {
				DetectivePlayer d = null;
				try {
					d = g.getDetectives().get(Integer.parseInt(s));
				} catch (Exception e2) {
					if (JOptionPane.showConfirmDialog(Board.this, "Invalid index: " + e2.getMessage(), 
							e2.getClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION, 
							JOptionPane.ERROR_MESSAGE) != JOptionPane.OK_OPTION) {
						break;
					}
					continue;
				}
				try {
					gc.removeDetective(d);
				} catch (Exception e2) {
					showErrorMessage(e2);					
				}
				// Erfolgreich oder nicht am Ende -> Raus aus Schleife
				break;
			}
		}
	}
	private class ShiftDetectiveUpAction extends AbstractAction {
		public ShiftDetectiveUpAction() {
			putValue(NAME, "Shift Detective Up...");
			putValue(SHORT_DESCRIPTION, "Shift up a detective in the list");
			putValue(MNEMONIC_KEY, KeyEvent.VK_U);
		}
		public void actionPerformed(ActionEvent e) {
			String s = "0";
			while ((s = JOptionPane.showInputDialog(Board.this, "Enter the index of a detective", s)) != null) {
				DetectivePlayer d = null;
				try {
					d = g.getDetectives().get(Integer.parseInt(s));
				} catch (Exception e2) {
					if (JOptionPane.showConfirmDialog(Board.this, "Invalid index: " + e2.getMessage(), 
							e2.getClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION, 
							JOptionPane.ERROR_MESSAGE) != JOptionPane.OK_OPTION) {
						break;
					}
					continue;
				}
				try {
					gc.shiftUpDetective(d);
				} catch (Exception e2) {
					showErrorMessage(e2);					
				}
				// Erfolgreich oder nicht am Ende -> Raus aus Schleife
				break;
			}
		}
	}
	private class ShiftDetectiveDownAction extends AbstractAction {
		public ShiftDetectiveDownAction() {
			putValue(NAME, "Shift Detective Down...");
			putValue(SHORT_DESCRIPTION, "Shift down a detective in the list");
			putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 16);
		}
		public void actionPerformed(ActionEvent e) {
			String s = "0";
			while ((s = JOptionPane.showInputDialog(Board.this, "Enter the index of a detective", s)) != null) {
				DetectivePlayer d = null;
				try {
					d = g.getDetectives().get(Integer.parseInt(s));
				} catch (Exception e2) {
					if (JOptionPane.showConfirmDialog(Board.this, "Invalid index: " + e2.getMessage(), 
							e2.getClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION, 
							JOptionPane.ERROR_MESSAGE) != JOptionPane.OK_OPTION) {
						break;
					}
					continue;
				}
				try {
					gc.shiftDownDetective(d);
				} catch (Exception e2) {
					showErrorMessage(e2);					
				}
				// Erfolgreich oder nicht am Ende -> Raus aus Schleife
				break;
			}
		}
	}
	private class NewGameWithPlayersAction extends AbstractAction {
		public NewGameWithPlayersAction() {
			putValue(NAME, "New Game with Players");
			putValue(SHORT_DESCRIPTION, "Create a new game with new players");
			putValue(MNEMONIC_KEY, KeyEvent.VK_W);
		}
		public void actionPerformed(ActionEvent e) {
			logger.info("new game with players");
			gc.clearPlayers();
			gc.newMrX();
			for (int i = 0; i < 4; i++)
				gc.newDetective();
			
			gc.newGame();
		}
	}
	private class UndoAction extends AbstractAction {
		public UndoAction() {
			putValue(NAME, "Undo");
			putValue(SHORT_DESCRIPTION, "Some short description");
			putValue(MNEMONIC_KEY, KeyEvent.VK_U);
		}
		public void actionPerformed(ActionEvent e) {
			undoManager.undo();
			setEnabled(undoManager.canUndo()); // TODO das kanns doch nicht sein: ueberall, wo UndoManger veraendert werden koennte, muesste sowas stehn! wir brauchen nen listener!!
			setEnabled(undoManager.canRedo());
		}
	}
	private class RedoAction extends AbstractAction {
		public RedoAction() {
			putValue(NAME, "Redo");
			putValue(SHORT_DESCRIPTION, "Some short description");
			putValue(MNEMONIC_KEY, KeyEvent.VK_R);
		}
		public void actionPerformed(ActionEvent e) {
			undoManager.redo();
			setEnabled(undoManager.canUndo()); // TODO das kanns doch nicht sein: ueberall, wo UndoManger veraendert werden koennte, muesste sowas stehn! wir brauchen nen listener!!
		    setEnabled(undoManager.canRedo());
		}
	}
	private class SuggestMoveAction extends AbstractAction {
		public SuggestMoveAction() {
			putValue(NAME, "Suggest move");
			putValue(SHORT_DESCRIPTION, "Some short description");
			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		}
		public void actionPerformed(ActionEvent e) {
			// Suggest an AI move!
			if (gc.getStatus() == GameStatus.IN_GAME) {
				Move move = MoveProducer.createRandomSingleMove(gs, gg);
				// ... not yet
				mPrep.nextMove(move);
			}
		}
	}
	private class MoveNowAction extends AbstractAction {
		public MoveNowAction() {
			putValue(NAME, "Move!"); // or "Move now"
			putValue(SHORT_DESCRIPTION, "Move now");
			putValue(MNEMONIC_KEY, KeyEvent.VK_M);
		}
		public void actionPerformed(ActionEvent e) {
			move(mPrep.getMove(gs.getCurrentPlayer()));
		}
	}
	private class MoveDetectivesNowAction extends AbstractAction {
		public MoveDetectivesNowAction() {
			putValue(NAME, "Move Detectives!"); // alternative to simple "Move now"
			putValue(SHORT_DESCRIPTION, "Move all Detectives now");
			putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		}
		public void actionPerformed(ActionEvent e) {
			Player currentPlayer = gs.getCurrentPlayer();
			if (currentPlayer instanceof DetectivePlayer) {
				Move currentMove = mPrep.getMove(currentPlayer);				
				
				// Detectives zählen, die schon gezogen sind, oder einen Zug vorbereitet haben.
				int nDetectivesReady = 0;
				for (DetectivePlayer d : gs.getDetectives()) {
					// TODO schon ausgefuehrte moves einbeziehen! die werden momentan ignoriert! dann meldung unten ausbessern!
					Move m = mPrep.getMove(d);
					if (m != null) {
						nDetectivesReady++;
					}
				}
				logger.debug("result of prepared detective moves counting: " + nDetectivesReady);
				
				if (currentMove == null) {
					JOptionPane.showMessageDialog(Board.this, "The current player has not prepared it's move yet."); // TODO warnung?, dass currentPlayer noch keinen Zug vorbereitet hat
				} else if (nDetectivesReady == gs.getDetectives().size() // Alle Detectives haben gezogen oder Zug vorbereitet
						|| JOptionPane.showConfirmDialog(Board.this,
								"It seems that not all detectives have prepared " +
								"a move yet.\nBegin moving as far as possible?",
								"Move Detectives", JOptionPane.YES_NO_OPTION) 
								== JOptionPane.YES_OPTION) {
					// Moving anstoßen
					doMoveDetectives = true;
					move(currentMove);
				}
			}
		}
	}
	private class QuickPlayAction extends AbstractAction {
		public QuickPlayAction() {
			putValue(NAME, "Quick Play");
			putValue(SHORT_DESCRIPTION, "Toogle Quick Play mode");
			setSelected(this, true);
		}
		public void actionPerformed(ActionEvent e) {
			ticketSelectionDialogMrX.setQuickPlay(isSelected(quickPlayAction));
			ticketSelectionDialogDetectives.setQuickPlay(isSelected(quickPlayAction));
		}
	}
	private class FitBoardAction extends AbstractAction {
		public FitBoardAction() {
			putValue(NAME, "Fit Board");
			putValue(SHORT_DESCRIPTION, "Fit the board to the window");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
			putValue(MNEMONIC_KEY, KeyEvent.VK_F);
			setSelected(this, true); // default behavior
		}
		public void actionPerformed(ActionEvent e) {
			setSelected(this, true);
			// pref size auf 0 setzen, damit is es immer angepasst an Viewport. 
			boardPanelContainer.setPreferredSize(new Dimension());	// vorher: getImageScrollPane().getViewport().getSize());
			// Wenn man's aber an Viewport anpassen wuerde, waere es fix,
			// je nach dem, wie gross der Viewport war zu dem Zeitpunkt.
			
			boardPanelScrollPane.revalidate();
			boardPanelScrollPane.repaint();
		}
	}
	private class ZoomInAction extends AbstractAction {
		public ZoomInAction() {
			putValue(NAME, "Zoom In");
			putValue(SHORT_DESCRIPTION, "Zoom into the board");
			putValue(MNEMONIC_KEY, KeyEvent.VK_I);
			
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke((Character) '+', KeyEvent.CTRL_DOWN_MASK)); // dont work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl typed +")); // dont work
			
			// KeyStroke.getKeyStroke('+', KeyEvent.CTRL_DOWN_MASK)
			// wird aufgefasst als getKeyStroke(int, int) -- wtf?? java castet einen char lieber automatisch als int, anstatt auto boxing zu Character.
			//  http://stackoverflow.com/questions/7931862/java-int-and-char
			// was werd ich wohl eher mit dem character '-' meinen? den character oder eine Zahl...
			
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK)); // (numpad) would work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK)); // (non-numpad) would work
			// dabei will ich doch, dass es einfach funktioniert, wenn ein + getippt wird. dafuer ist doch auch getKeyStroke("ctrl typed ...") da, oder? 
		}
		public void actionPerformed(ActionEvent e) {
			setSelected(fitBoardAction, false);
			zoomFactor = (double) boardPanel.getWidth() / originalImageSize.width;
			logger.debug(String.format("zoomFactor old: %f, new: %f", zoomFactor, zoomFactor * ZOMMING_FACTOR));			
			zoomFactor *= ZOMMING_FACTOR;
			updateBoardPanelZoom();
		}
	}
	private class ZoomOutAction extends AbstractAction {
		public ZoomOutAction() {
			putValue(NAME, "Zoom Out");
			putValue(SHORT_DESCRIPTION, "Zoom out of the board");
			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
			putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 5);
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke((Character) '-', KeyEvent.CTRL_DOWN_MASK)); // dont work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl typed -")); // dont work
			
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK)); // (numpad) would work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK)); // (non-numpad) would work
		}
		public void actionPerformed(ActionEvent e) {
			setSelected(fitBoardAction, false);
			zoomFactor = (double) boardPanel.getWidth() / originalImageSize.width;
			logger.debug(String.format("zoomFactor old: %f, new: %f", zoomFactor, zoomFactor * ZOMMING_FACTOR));			
			zoomFactor /= ZOMMING_FACTOR;
			updateBoardPanelZoom();
		}
	}
	private class ZoomNormalAction extends AbstractAction {
		public ZoomNormalAction() {
			putValue(NAME, "Zoom Normal");
			putValue(SHORT_DESCRIPTION, "Zoom normal");
			putValue(MNEMONIC_KEY, KeyEvent.VK_N);
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke((Character) '-', KeyEvent.CTRL_DOWN_MASK)); // dont work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl typed -")); // dont work
			
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.CTRL_DOWN_MASK)); // (numpad) would work
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK)); // (non-numpad) would work
		}
		public void actionPerformed(ActionEvent e) {
			setSelected(fitBoardAction, false);
			zoomFactor = normalZoomFactor;
			logger.debug(String.format("zoomFactor: %f", zoomFactor));			
			updateBoardPanelZoom();
		}
	}
	/** Selects the current player to prepare a move for */
	private class SelectCurrentPlayerAction extends AbstractAction {
		public SelectCurrentPlayerAction() {
			putValue(NAME, "Select Current Player");
			putValue(SHORT_DESCRIPTION, "Select the current player to prepare a move for");
		}
		public void actionPerformed(ActionEvent e) {
			mPrep.selectPlayer(gs.getCurrentPlayer());
		}
	}
	private class JointMoving extends AbstractAction {
		public JointMoving() {
			putValue(NAME, "Joint Moving");
			putValue(SHORT_DESCRIPTION, "Joint moving for detectives");
			setSelected(this, true);
		}
		public void actionPerformed(ActionEvent e) {
			mPrep.setFixedTurnOrder(!isSelected(this));
		}
	}	
	private class MrXAlwaysVisibleAction extends AbstractAction {
		public MrXAlwaysVisibleAction() {
			putValue(NAME, "MrX Always Visible");
			putValue(SHORT_DESCRIPTION, "Keep MrX always visible");
			putValue(MNEMONIC_KEY, KeyEvent.VK_X); // oder M, A oder V ...
			setSelected(this, true);
		}
		public void actionPerformed(ActionEvent e) {
			boardPanel.setMrXAlwaysVisible(isSelected(this));
		}
	}
}
