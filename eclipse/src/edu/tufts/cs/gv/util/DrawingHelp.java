package edu.tufts.cs.gv.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class DrawingHelp {
	public static void renderHelpText(Component target, List<String> helpString, Graphics g) {
		int height = 0;
		int width = 0;
		for (String line : helpString) {
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(line, g);
			height += bounds.getHeight();
			width = Math.max(width, (int)bounds.getWidth());
		}
		height += 20;
		width += 20;
		g.setColor(Color.YELLOW);
		g.fillRect(target.getWidth() - width - 1, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(target.getWidth() - width - 1, 0, width, height);
		int y = 7;
		for (String line : helpString) {
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(line, g);
			y += bounds.getHeight();
			g.drawString(line, target.getWidth() - width + 10, y);
		}
	}
}
