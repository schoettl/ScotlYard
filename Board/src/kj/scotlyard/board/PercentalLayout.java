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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.geom.Rectangle2D;

/**
 * A layout manager that behaves like the <code>null</code> layout
 * for normal components, but lays out <code>PercentalBounds</code>
 * objects according to it's percental location and size hints.
 * @author jakob190590
 *
 */
public class PercentalLayout implements LayoutManager {

	@Override
	public void addLayoutComponent(String name, Component comp) { }

	@Override
	public void removeLayoutComponent(Component comp) { }

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		// Greatest coordinates of non-PercentalBounds objects
		int x = 0;
		int y = 0;
		for (Component c : parent.getComponents()) {
			// Jedes "normale" Objekt wird nicht gelayouted
			// Und preferred size wird so berechnet, dass diese noch reinpassen!
			if (!(c instanceof PercentalBounds)) {
				int cx = c.getLocation().x + c.getWidth();
				int cy = c.getLocation().y + c.getHeight();
				if (cx > x) x = cx;
				if (cy > y) y = cy;
			}
		}		
		// Insets addieren (siehe http://docs.oracle.com/javase/tutorial/uiswing/layout/custom.html)
		Insets insets = parent.getInsets();
		x += insets.left + insets.right;
		y += insets.top + insets.bottom;
		
		// TODO Was, wenn keine non-PercentalBounds objects da sind?
		// Standardgroesse? 0?
		if (x == 0 || y == 0) {
			x = y = 200;
		}
		
		return new Dimension(x, y);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension();
	}

	@Override
	public void layoutContainer(Container parent) {
		for (Component c : parent.getComponents()) {
			if (c instanceof PercentalBounds) {
				PercentalBounds pb = (PercentalBounds) c;
				Rectangle2D.Double bounds = pb.getBounds2();
				int w = parent.getWidth();
				int h = parent.getHeight();
				c.setBounds((int) (w * bounds.x), (int) (h * bounds.y), 
						(int) (w * bounds.width), (int) (h * bounds.height));
//				System.out.println(c.getBounds());
			}
			// "normal" components will not be laid out
		}
	}

}
