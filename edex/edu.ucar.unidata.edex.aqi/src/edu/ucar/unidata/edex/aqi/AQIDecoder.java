package edu.ucar.unidata.edex.aqi;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

public class AQIDecoder {
    private static final IUFStatusHandler logger = UFStatus.getHandler(AQIDecoder.class);
    
    public PluginDataObject[] decode(String input) throws Exception {

        logger.info("Starting AQI Decoder");
      //Create an empty list to hold all of our records
        List<AQIRecord> records = new ArrayList<AQIRecord>();
        GeometryFactory geomFact = new GeometryFactory();
        int start = input.indexOf("<");
        input = input.substring(start);
        
      //Read the string into an xml parser
        Document document = DocumentHelper.parseText(input);
        Element e = document.getRootElement();
        Element doc = e.element("Document");
        
      //Iterate through all Location tags
        Iterator<Element> i = doc.elementIterator("Placemark");

        while(i.hasNext()) {
            //Grab next placemark
            Element placemark = i.next();
            //extract everything and populate record and add it to the list.
            //pull out the aqi tag
            Element aqiElement  = placemark.element("aqi");
            //Decode the parts from the tags
            if (aqiElement != null) {
                //Get Snippet and point and coordinates
                Element snippetElement  = placemark.element("Snippet");
                Element point = placemark.element("Point");
                Element coordinates = point.element("coordinates");
                String name = snippetElement.getText();
                int aqi = Integer.parseInt(aqiElement.getText());
                String[] coordParts = coordinates.getText().split(",");
                Coordinate coord = new Coordinate(Double.parseDouble(coordParts[0].trim()),Double.parseDouble(coordParts[1].trim()));
                Geometry location = geomFact.createPoint(coord);
                //Create a AQIRecord and add it to the list
                //Create new record to populate
                AQIRecord record = new AQIRecord();
                //Set the name
                record.setName(name);
                //Set the AQI
                record.setAqi(aqi);
                record.setLocation(location);
                //Since the data file has no time in it, just use current time.
                record.setDataTime(new DataTime(new Date()));
                records.add(record);
            }

        }

      //When done with all records, convert the list to an array and return.
        logger.info("Finished AQI Decoder");
        return (records.toArray(new AQIRecord[0]));
    }

}
