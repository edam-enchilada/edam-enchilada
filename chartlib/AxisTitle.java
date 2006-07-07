package chartlib;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.Math;

public class AxisTitle {
	private String title;
	private AxisPosition position;
	private Point anchorPoint;
	
	
	public static enum AxisPosition {
		LEFT(-Math.PI / 2), 
		RIGHT(Math.PI / 2), 
		BOTTOM(0);
		
		private double textAngle;
		
		AxisPosition(double textAngle) {
			this.textAngle = textAngle;
		}
		
		public double getRotationAngle() {
			return textAngle;
		}
		
		public AffineTransform getTransform(Point2D anchor) {
			AffineTransform trans = new AffineTransform();
			trans.rotate(textAngle, anchor.getX(), anchor.getY());
			return trans;
		}
		
		public AffineTransform getInverseTransform(Point2D anchor) {
			AffineTransform trans = new AffineTransform();
			trans.rotate(- textAngle, anchor.getX(), anchor.getY());
			return trans;
		}
	}
	
	
	public AxisTitle(String title, AxisPosition position, Point anchorPoint) {
		this.title = title;
		this.position = position;
		this.anchorPoint = anchorPoint;
	}
	
	// not done anything with this yet

	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.transform(position.getTransform(anchorPoint));
		g2d.drawString(title, (int) anchorPoint.getX(), (int) anchorPoint.getY());
		g2d.transform(position.getInverseTransform(anchorPoint));
	}
	
	
//	private void drawAxisTitleX(Graphics2D g2d) {
//		int xCoord;
//		java.awt.font.GlyphVector vector; //the visual representation of the string.
//	
//		xCoord = dataArea.x + dataArea.width/2;
//		// center the string by finding its graphical representation
//		vector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), axis.getTitle());
//		xCoord = xCoord - vector.getOutline().getBounds().width / 2;
//		g2d.drawString(axis.getTitle(), xCoord, size.height - getInsets().bottom - 3);
//	}
//
//	private void drawAxisTitleY(Graphics2D g2d) {
//		g2d.setColor(Color.BLACK);
//		
////		y axis title - rotated so as to read vertically
//		Rectangle dataArea = getDataAreaBounds();
//		int yCoord = dataArea.y + dataArea.height / 2;
//		int xCoord = V_TITLE_PADDING - getInsets().left - 4;
//		
//		java.awt.font.GlyphVector vector; //the visual representation of the string.
//		vector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), axis.getTitle());
//		yCoord = yCoord + vector.getOutline().getBounds().width / 2;
//		
//		g2d.rotate(- Math.PI/2, xCoord, yCoord);
//		g2d.drawString(axis.getTitle(), xCoord, yCoord);
//		g2d.rotate(Math.PI/2, xCoord, yCoord);
//		
//		g2d.setColor(Color.BLACK);
//	}


}
