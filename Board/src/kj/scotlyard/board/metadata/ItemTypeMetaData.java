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

package kj.scotlyard.board.metadata;

import javax.swing.Icon;

import kj.scotlyard.game.model.item.Item;

public class ItemTypeMetaData {
	
	private String name;

	private Icon icon;

	public ItemTypeMetaData(String name, Icon icon) {
		this.name = name;
		this.icon = icon;
	}

	public ItemTypeMetaData(Class<? extends Item> itemType) {
		name = itemType.getSimpleName();
	}

	public String getName() {
		return name;
	}

	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}