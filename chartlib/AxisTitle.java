package chartlib;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import java.awt.font.GlyphVector;
import java.lang.Math;

public class AxisTitle {
	private String title;
	private AxisPosition position;
	private Point anchorPoint;
//	g2d.transform(getTransform(anchor));
//	g2d.transform(getInverseTransform(anchor));


	public static enum AxisPosition {
		LEFT(-Math.PI / 2) {
			public Point2D getCoords(Rectangle bounds, Point2D anchor, Graphics2D g2d) {
				return new Point2D.Double(
						anchor.getX(),
						anchor.getY() + (bounds.width / 2));
			}
		},
		RIGHT(Math.PI / 2) {
			public Point2D getCoords(Rectangle bounds, Point2D anchor, Graphics2D g2d) {
				return new Point2D.Double(
						anchor.getX(),
						anchor.getY() - (bounds.width / 2));
			} 
		},
		BOTTOM(0) {
			public Point2D getCoords(Rectangle bounds, Point2D anchor, Graphics2D g2d) {
				return new Point2D.Double(
						anchor.getX() - (bounds.width / 2),
						anchor.getY() + bounds.height);
			}
		};

		public abstract Point2D getCoords(Rectangle bounds, Point2D anchor, Graphics2D g2d);

		private final double textAngle;

		AxisPosition(double textAngle) {
			this.textAngle = textAngle;
		}
		
		public double getRotationAngle() {
			return textAngle;
		}
	}
	
	
	public AxisTitle(String title, AxisPosition position, Point anchorPoint) {
		this.title = title;
		this.position = position;
		this.anchorPoint = anchorPoint;
	}
	
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		GlyphVector text = glyphVec(title, g2d);
		Point2D fixed = position.getCoords(
				text.getOutline().getBounds(), anchorPoint, g2d);
		
		g2d.rotate(position.textAngle, fixed.getX(), fixed.getY());
		
		g2d.drawGlyphVector(text, (float) fixed.getX(), (float) fixed.getY());
		
		g2d.rotate(- position.textAngle, fixed.getX(), fixed.getY());
	}
	
	private static GlyphVector glyphVec(String title, Graphics2D g2d) {
		return g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), title);
	}

	public Point getAnchorPoint() {
		return anchorPoint;
	}

	public void setAnchorPoint(Point anchorPoint) {
		this.anchorPoint = anchorPoint;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
