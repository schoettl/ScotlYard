/*
 * ScotlYard -- A software implementation of the Scotland Yard board game
 * Copyright (C) 2012  Jakob Sch�ttl
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

import javax.swing.JComponent;

import kj.scotlyard.game.model.Player;

@SuppressWarnings("serial")
public abstract class Piece extends JComponent {

	private Player player;
	
	private String playerName;

	public Piece(Player player, String playerName) {
		this.player = player;
		this.playerName = playerName;
	}
	
	
	
	// TODO Optimierung: Nur berechnen on setWidth/Height
	protected int getHalfWidth() {
		return getWidth() / 2;
	}
	
	protected int getHalfHeight() {
		return getHeight() / 2;
	}
	
	

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
}