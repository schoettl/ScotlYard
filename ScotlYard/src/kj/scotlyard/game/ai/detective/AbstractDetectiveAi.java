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

package kj.scotlyard.game.ai.detective;

import kj.scotlyard.game.ai.AbstractAi;
import kj.scotlyard.game.model.Move;
import kj.scotlyard.game.model.Player;

public abstract class AbstractDetectiveAi extends AbstractAi implements DetectiveAi {

	@Override
	protected void moveUndone(Move move) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void moveDone(Move move) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void currentPlayerChanged(Player oldPlayer, Player newPlayer) {
		// TODO Auto-generated method stub
		
	}
}