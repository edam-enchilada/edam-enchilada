package chartlib;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import static chartlib.AxisTitle.AxisPosition;

public class AxisTitleTest {
	JComponent testPanel;
	
	final Dimension panelSize = new Dimension(300, 100);

	Point[] anchorPoints = new Point[] {
		new Point(50, 50),
		new Point(150, 50),
		new Point(250, 50)
	};
	ArrayList<AxisTitle> titles = new ArrayList<AxisTitle>();

	public AxisTitleTest() throws Exception {
		JFrame frame = new JFrame("AxisTitle test");
		JPanel parent = new JPanel();
		parent.setPreferredSize(panelSize);
		frame.getContentPane().add(parent);
		testPanel = new JComponent() {
			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D)g.create();
				g2d.setColor(Color.RED);
				for (Point anchor : anchorPoints) {
					g2d.fillRect(anchor.x, anchor.y, 5, 5);
				}
				g2d.setColor(Color.BLACK);

				g2d.setFont(new Font("Courier", Font.PLAIN, 14));
				for (AxisTitle title : titles) {
					title.draw(g2d);
				}
			}
		};
		testPanel.setPreferredSize(panelSize);
		parent.add(testPanel);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(panelSize);

		frame.pack();
		frame.setVisible(true);
	}
	
	public void test() {
		titles.add(new AxisTitle("Title", AxisPosition.LEFT, anchorPoints[0]));
		titles.add(new AxisTitle("Title", AxisPosition.BOTTOM, anchorPoints[1]));
		titles.add(new AxisTitle("Title", AxisPosition.RIGHT, anchorPoints[2]));
		
	}

	public static void main(String[] args) throws Exception {
		AxisTitleTest t = new AxisTitleTest();
		t.test();
	}
}
