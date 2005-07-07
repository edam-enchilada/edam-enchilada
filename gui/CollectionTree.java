/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's CollectionTree class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


/*
 * Created on Jul 20, 2004
 *
 */
package gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import atom.ATOFMSAtomFromDB;
import atom.GeneralAtomFromDB;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import collection.*;
import database.*;

/**
 * @author ritza
 *
 * CollectionTree contains the JTree of Collections.  Eventually, 
 * its functionality will include drag-and-drop.  It accesses all 
 * of the necessary information through a data model, talking to 
 * the database.
 * 
 * see http://java.sun.com/docs/books/tutorial/uiswing/
 * 											  components/tree.html
 * 
 */
public class CollectionTree extends JPanel 
	implements TreeSelectionListener 
{ 
	private JTree tree; //Collection tree

	private InfoWarehouse db;
	
	private MainFrame parentFrame = null;
	public CollectionTree(InfoWarehouse database, MainFrame pFrame) {
	
        super(new GridLayout(1,0));
        db = database;
        
        //Create a tree that allows one selection at a time.
        tree = new JTree(new CollectionModel(db));
        tree.setRootVisible(false); // hides the root.
        tree.setShowsRootHandles(true); // shows +/- expand handles on 
        								// root level nodes
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        parentFrame = pFrame;
        
        
        JScrollPane pane = new JScrollPane(tree);
        add(pane);
	}
	
    
    /* TreeSelectionListener Interface Implementation */
    
    public void valueChanged(TreeSelectionEvent e) {
        Collection node = 
        	(Collection)tree.getLastSelectedPathComponent();
        if (node == null) return;
        // Selection will display data in particles table - wire here.
        //System.out.println(node.getCollectionID());
        parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      
        // fast code
        ArrayList<String> colnames = db.getColNames(db.getCollection(node.getCollectionID()).getDatatype(), DynamicTable.AtomInfoDense);
        parentFrame.changeParticleTable(colnames);
        Vector<Vector<Object>> particleTable = parentFrame.getData();
        particleTable.clear();
        particleTable = db.updateParticleTable(db.getCollection(node.getCollectionID()), particleTable);
        
        parentFrame.getParticlesTable().tableChanged(new TableModelEvent(
        		parentFrame.getParticlesTable().getModel()));
        parentFrame.getParticlesTable().doLayout();
        parentFrame.validate();
        
        parentFrame.editText(MainFrame.DESCRIPTION,db.getCollectionDescription(node.getCollectionID()));
        parentFrame.setCursor(Cursor.getPredefinedCursor
                (Cursor.DEFAULT_CURSOR));
    }

    public void updateTree()
    {
    	((CollectionModel)tree.getModel()).fireTreeStructureChanged(
    			(Collection) tree.getModel().getRoot());
    }
    
    public Collection getSelectedCollection(){
    	return (Collection) tree.getSelectionPath().getLastPathComponent();
    }
}
