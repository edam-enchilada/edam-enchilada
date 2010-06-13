package chartlib.tree;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;

import collection.Collection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import chartlib.*;
import database.InfoWarehouse;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * A window which contains a tree view of a hierarchy.
 * 
 * 
 * @author jtbigwoo
 *
 */

public class TreeViewWindow extends JFrame {

	private InfoWarehouse db;
	
	private JPanel buttonPanel;

	private Collection rootCollection;
	
	private GraphBuilder builder;
	
    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Collection, String> vv;

    Layout<Collection, String> layout;

    TreeCollapser collapser;

	
	public TreeViewWindow(InfoWarehouse db, int collID) {
		super("Hierarchy Tree View");
		this.db = db;

		setLayout(new BorderLayout());
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		this.add(buttonPanel, BorderLayout.EAST);
		
		rootCollection = db.getCollection(collID);
		builder = new GraphBuilder(db, rootCollection);
//		Tree<Collection, String> tree = builder.getFullGraph();
		Tree<Collection, String> tree = builder.getSubGraph(6);
		
//		Layout<Collection, String> layout = new TreeLayout<Collection, String>(tree, 25, 25);
//		Layout<Collection, String> layout = new CircleLayout<Collection, String>(tree);
//		layout.setSize(new Dimension(500, 500));
//		VisualizationViewer<Collection, String> vv = new VisualizationViewer<Collection, String>(layout);
//		vv.setPreferredSize(new Dimension(700, 700));
//		DefaultModalGraphMouse mouse = new DefaultModalGraphMouse();
//		mouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
//		vv.setGraphMouse(mouse);

//		layout = new BalloonLayout<Collection, String>(tree);
//		layout.setSize(new Dimension(900,900));
		
		layout = new RadialTreeLayout<Collection, String>(tree);
		layout.setSize(new Dimension(600,600));

//      layout = new TreeLayout<Collection, String>(tree, 15, 15);

		collapser = new TreeCollapser();

        vv =  new VisualizationViewer<Collection, String>(layout, new Dimension(600,600));
        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
//        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction());
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new CollectionLabeller());

        final GraphZoomScrollPane treePanel = new GraphZoomScrollPane(vv);

        this.add(treePanel, BorderLayout.CENTER);

        final DefaultModalGraphMouse<Collection, String> graphMouse = new DefaultModalGraphMouse<Collection, String>();

        vv.setGraphMouse(graphMouse);

        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });

        JButton collapse = new JButton("Collapse");
        collapse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Set picked =new HashSet(vv.getPickedVertexState().getPicked());
                if(picked.size() == 1) {
                	Object root = picked.iterator().next();
                    Forest inGraph = (Forest)layout.getGraph();

                    try {
						collapser.collapse(vv.getGraphLayout(), inGraph, root);
					} catch (InstantiationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

                    vv.getPickedVertexState().clear();
                    vv.repaint();
                }
            }});

        JButton expand = new JButton("Expand");
        expand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Set picked = vv.getPickedVertexState().getPicked();
                for(Object v : picked) {
                    if(v instanceof Forest) {
                        Forest inGraph = (Forest)layout.getGraph();
            			collapser.expand(inGraph, (Forest)v);
                    }
                    vv.getPickedVertexState().clear();
                   vv.repaint();
                }
            }});

        JButton moveCenter = new JButton("Recenter Graph on Particle");
        moveCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Set picked =new HashSet(vv.getPickedVertexState().getPicked());
                Collection newRoot;
                if(picked.size() == 1) {
                	Object newRootObj = picked.iterator().next();
                	if (newRootObj instanceof Collection) {
                    	newRoot = (Collection) newRootObj;
                		RadialTreeLayout<Collection, String> newLayout = new RadialTreeLayout<Collection, String>(builder.getSubGraph(newRoot, 5));
                		newLayout.setSize(new Dimension(600,600));

                		vv.setGraphLayout(newLayout);
                		layout = newLayout;
//                        vv.getPickedVertexState().clear();
                        vv.repaint();
                	}
                	else if (newRootObj instanceof Tree)  {
                		Tree littleTree = (Tree) newRootObj;
                		newRoot = (Collection) littleTree.getRoot();
                		RadialTreeLayout<Collection, String> newLayout = new RadialTreeLayout<Collection, String>(builder.getSubGraph(newRoot, 5));
                		newLayout.setSize(new Dimension(600,600));

                		vv.setGraphLayout(newLayout);
                		layout = newLayout;
                        vv.getPickedVertexState().clear();
                        vv.repaint();
                	}
                }
            }});

        JButton reCenter = new JButton("Recenter Graph on Home");
        reCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
        		RadialTreeLayout<Collection, String> newLayout = new RadialTreeLayout<Collection, String>(builder.getSubGraph(rootCollection, 5));
        		newLayout.setSize(new Dimension(600,600));

        		vv.setGraphLayout(newLayout);
        		layout = newLayout;
                vv.getPickedVertexState().clear();
                vv.repaint();
            }});

        JPanel controls = new JPanel();
        controls.add(collapse);
        controls.add(expand);
        controls.add(moveCenter);
        controls.add(reCenter);
        buttonPanel.add(controls, BorderLayout.SOUTH);

		
		treePanel.add(vv);
			
		buttonPanel.add(Box.createHorizontalStrut(150));
		
//		setPreferredSize(new Dimension(700, 700));
		validate();
		pack();
	}

    private void setLtoR(VisualizationViewer<Collection, String> vv) {
    	Layout<Collection, String> layout = vv.getModel().getGraphLayout();
    	Dimension d = layout.getSize();
    	Point2D center = new Point2D.Double(d.width/2, d.height/2);
    	vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).rotate(-Math.PI/2, center);
    }

    private boolean isBig(Collection coll) {
    	int rootSize = this.rootCollection.getCollectionSize();
    	return rootSize / 10 <= coll.getCollectionSize();
    }

    private boolean isMedium(Collection coll) {
    	int rootSize = this.rootCollection.getCollectionSize();
    	return rootSize / 100 <= coll.getCollectionSize() && rootSize / 10 > coll.getCollectionSize();
    }

    class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {
        ClusterVertexShapeFunction() {
            setSizeTransformer(new ClusterVertexSizeFunction<V>());
        }
        
        public Shape transform(V v) {
            if(v instanceof Graph<?,?>) {
            	return factory.getRegularPolygon(v, 6);
            }
            return super.transform(v);
        }
    }

    class ClusterVertexSizeFunction<V> implements Transformer<V,Integer> {
        public ClusterVertexSizeFunction() {
        }

        public Integer transform(V v) {
            if(v instanceof Graph<?,?>) {
            	return 18;
            }
        	if (v instanceof Collection) {
        		Collection coll = (Collection) v;
        		if (isBig(coll)) {
        			return 18;
        		}
        		if (isMedium(coll)) {
        			return 15;
        		}
        	}
            return 12;
        }
    }

    class CollectionLabeller<V> implements Transformer<V,String> {
        public CollectionLabeller() {

        }

        public String transform(V v) {
        	if (v instanceof Collection) {
        		Collection coll = (Collection) v;
        		return coll.getName() + " : " + coll.getCollectionSize() + " particles";
        	}
        	else if (v instanceof Tree)  {
        		Tree littleTree = (Tree) v;
        		Collection coll = (Collection) littleTree.getRoot();
        		return coll.getName() + " : " + coll.getCollectionSize() + " particles";
        	}

            return v.toString();
        }
    }
}
