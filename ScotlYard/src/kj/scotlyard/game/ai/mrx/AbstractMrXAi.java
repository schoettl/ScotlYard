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

package kj.scotlyard.game.ai.mrx;

import java.util.List;

import kj.scotlyard.game.ai.AbstractAi;
import kj.scotlyard.game.graph.GameGraph;
import kj.scotlyard.game.model.Move;

public abstract class AbstractMrXAi extends AbstractAi implements MrXAi {

	protected AbstractMrXAi(GameGraph gameGraph) {
		super(gameGraph);
	}

	@Override
	protected void moveUndone(Move move) {
		if (move.getPlayer() instanceof MrXPlayer) {
			// Zug von MrX Player rückgängig gemacht
			// -> nochmal neu berechnen
			startCalculation();
		}
		// TODO calc auch abbrechen ??
	}

	@Override
	protected void moveDone(Move move) {
		List<DetectivePlayer> ds = getGameState.getDetectives();
		if (move.getPlayer() == ds.get(ds.size() - 1)) {
			// Letzter Detective hat gezogen
			// -> Berechnung für MrX Zug starten
			startCalculation();
		}
		// TODO calc auch abbrechen ??
	}

}
