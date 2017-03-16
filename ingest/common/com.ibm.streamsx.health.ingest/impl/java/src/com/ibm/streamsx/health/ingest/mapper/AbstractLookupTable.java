package com.ibm.streamsx.health.ingest.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class AbstractLookupTable implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String DELIM = "\\|";
	private static final Logger logger = Logger.getLogger(AbstractLookupTable.class);
	
	protected Map<String, List<String>> lookupTable;
	
	public AbstractLookupTable() {
		lookupTable = new HashMap<>();
	}
	
	/**
	 * Populate the table with the data from the inputStream
	 * 
	 * @param inputStream The inputStream to populate the table with
	 * @throws Exception Throws an exception is there is an error reading from the inputStream
	 * or if an attempt is made to load a duplicate key into the table
	 */
	public void loadCSV(InputStream inputStream) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		String line = "";
		int i = 0;
		while((line = reader.readLine()) != null) {
			i++; // increment row counter
			List<String> row = Arrays.asList(line.split(DELIM, -1));
			
			// check that row contains at least 2 columns
			if(row.size() < 2) {
				logger.warn("WARNING: row must contain at least 2 columns, skipping! [row=" + i + ", line=" + line + "]");
				continue;
			}
			
			try {
				String id = row.get(0);
				
				// duplicate keys in the lookup table are not supported
				if(contains(id)) {
					throw new Exception("Lookup table already contains an entry for ID: [id=" + id + ", row=" + i + "]");
				}
				
				lookupTable.put(id, row);
				logger.trace("Adding id=" + id + ", row=" + row);
			} catch (NumberFormatException e) {
				logger.error("Row " + i + " does not contain valid integer in the first column", e);
				e.printStackTrace();
				continue;
			}
		}
		
		reader.close();
	}

	/**
	 * Populate the lookup table with data from a CSV file
	 * @param path The path to the CSV file
	 * @throws Exception Throws an exception is there is an error reading from the inputStream
	 * or if an attempt is made to load a duplicate key into the table
	 */
	public void loadCSV(File path) throws Exception {
		loadCSV(new FileInputStream(path));
	}	
	
	public boolean contains(String id) {
		return lookupTable.containsKey(id);
	}
	
	public String lookup(String id, int columnIndex) {
		String value = null;
		
		List<String> row = lookupTable.get(id);
		if(row == null)
			return null;
		
		try {
			value = row.get(columnIndex);
		} catch(IndexOutOfBoundsException e) {
			IndexOutOfBoundsException ex = new IndexOutOfBoundsException("Invalid column index [id=" + id + ", num_columns=" + row.size() + ", attempted_index=" + columnIndex + "]");
			ex.addSuppressed(e);
			throw ex;
		}

		return value;
	}
}
