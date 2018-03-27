package edu.ucar.unidata.edex.plugin.aqi;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

public class AQIDecoder {

    // Create an instance of Geometry Factory for use to to create Point objects
    // from coordinates
    GeometryFactory geomFact = new GeometryFactory();
    private IUFStatusHandler logger = UFStatus.getHandler(AQIDecoder.class);

    public PluginDataObject[] decode(byte[] data) throws Exception {
        logger.info("Starting AQI Decoder");
        // Convert data to a string from byte array
        String input = new String(data);
        // Remove funny character that sometimes appears.
        int start = input.indexOf("<");
        input = input.substring(start);
        // Create list to hold all of the plugin objects that get created.
        ArrayList<AQIRecord> list = new ArrayList<AQIRecord>();
        try {
            // Parse the text input into a Document so that we can extract out
            // the root element.
            Document document = DocumentHelper.parseText(input);
            // Get root element of the document in xml
            Element e = document.getRootElement();
            // grab the Document section of the kml file
            Element doc = e.element("Document");
            // iterate over the items in the Document section and only grab
            // Placemark items.
            Iterator<Element> i = doc.elementIterator("Placemark");
            while (i.hasNext()) {
                // Grab next placemark
                Element placemark = i.next();
                // pull out the aqi tag
                Element aqiElement = placemark.element("aqi");
                // If there actually is aqi data then process it.
                if (aqiElement != null) {
                    // Get Snippet and point and coordinates
                    Element snippetElement = placemark.element("Snippet");
                    Element point = placemark.element("Point");
                    Element coordinates = point.element("coordinates");
                    try {
                        // Create new record to populate
                        AQIRecord record = new AQIRecord();
                        // Set the name
                        record.setName(snippetElement.getText());
                        int aqi = Integer.parseInt(aqiElement.getText());
                        // Set the AQI
                        record.setAqi(aqi);
                        record.setRandom((int) (Math.random() * 100.));
                        // Process the coordinates into a point geometry object
                        String[] coordParts = coordinates.getText().split(",");
                        record.setGeometry(geomFact.createPoint(new Coordinate(
                                Double.parseDouble(coordParts[0].replaceAll(
                                        " ", "")), Double
                                        .parseDouble(coordParts[1].replaceAll(
                                                " ", "")))));
                        // Since the data file has no time in it, just use
                        // current time.
                        record.setDataTime(new DataTime(new Date()));
                        // Add it to the list
                        list.add(record);
                    } catch (Exception ex) {

                    }
                }

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        logger.info("Finished AQI Decoder");
        // Process the list and send back an array of the PluginDataObjects
        return (list.toArray(new PluginDataObject[list.size()]));
    }

}
