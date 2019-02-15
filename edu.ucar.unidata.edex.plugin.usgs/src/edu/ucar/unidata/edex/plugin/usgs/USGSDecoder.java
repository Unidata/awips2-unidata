package edu.ucar.unidata.edex.plugin.usgs;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.ucar.unidata.common.dataplugin.usgs.USGSRecord;

public class USGSDecoder {

    GeometryFactory geomFact = new GeometryFactory();
    
    private IUFStatusHandler logger = UFStatus.getHandler(USGSDecoder.class);

    public PluginDataObject[] decode(byte[] data) throws Exception {
        logger.info("Starting USGS Decoder");
        
        String SEASONAL = "Ssn";
        
        ArrayList<USGSRecord> list = new ArrayList<USGSRecord>();

        String input = new String(data);
        String[] lines = input.split("\n");

        for(String line : lines) {
        	
        	if (line.trim().startsWith("#")) {
                continue;
        	} else {
            	if (line.trim().startsWith("USGS")) {
            		// This is a record line, not a header or descriptor line
            		String[] values = line.split("\t");
            		USGSRecord record = new USGSRecord();
            		String name = values[1];
            		String cfs = values[4];
            		String height = values[6];
            		if (!cfs.equals(SEASONAL)) {
            			record.setName(name);
                		record.setCfs(Float.parseFloat(cfs));
                		record.setHeight(Float.parseFloat(height));
                		record.setDataTime(new DataTime(new Date()));
                		
                		/*
                		 * TODO: lookup table for USGS streamgauge station location to get full name and lat/lon
                		 */
                		
//                      String[] coordParts = coordinates.getText().split(",");
//                      record.setGeometry(geomFact.createPoint(new Coordinate(
//                              Double.parseDouble(coordParts[0].replaceAll(
//                                      " ", "")), Double
//                                      .parseDouble(coordParts[1].replaceAll(
//                                              " ", "")))));
                		list.add(record);
            		}
            	}
            }
        }
        logger.info("Finished USGS Decoder");
        return (list.toArray(new PluginDataObject[list.size()]));
    }
}
