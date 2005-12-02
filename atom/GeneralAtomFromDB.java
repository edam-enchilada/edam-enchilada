package atom;

import gui.MainFrame;

import java.util.ArrayList;
import java.util.Date;

import database.DynamicTable;
import database.SQLServerDatabase;

public class GeneralAtomFromDB {

	protected ArrayList<String> fieldNames;
	protected ArrayList<String> fieldTypes;
	protected ArrayList<String> fieldValues;
	protected String datatype;
	protected int atomID;
	protected SQLServerDatabase db;
	
	public GeneralAtomFromDB(int atomID,SQLServerDatabase database) {
		db = database;
		this.atomID = atomID;
		datatype = db.getAtomDatatype(atomID);
		ArrayList<ArrayList<String>> temp = db.getColNamesAndTypes(datatype, DynamicTable.AtomInfoDense);
		fieldNames = new ArrayList<String>();
		fieldTypes = new ArrayList<String>();
		for (int i = 0; i < temp.size(); i++) {
			fieldNames.add(temp.get(i).get(0));
			fieldTypes.add(temp.get(i).get(1));
		}
		// initialize field values when needed
		//fieldValues = db.getAtomInfoDense(atomID);
	}
	
	public GeneralAtomFromDB() {}
	
	public ArrayList<String> getFieldNames() {
		return fieldNames;
	}
	
	public ArrayList<String> getFieldValues() {
		return fieldValues;
	}
	public ATOFMSAtomFromDB toATOFMSAtom() {
		return new ATOFMSAtomFromDB(atomID, fieldValues.get(5), 
				Integer.parseInt(fieldValues.get(4)), 
				Float.parseFloat(fieldValues.get(2)),
				new Date(fieldValues.get(1)));
	}
}
