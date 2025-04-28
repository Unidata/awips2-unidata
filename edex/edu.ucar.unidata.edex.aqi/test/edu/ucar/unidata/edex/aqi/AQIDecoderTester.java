package edu.ucar.unidata.edex.aqi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.raytheon.uf.common.dataplugin.PluginDataObject;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

public class AQIDecoderTester {

    public static void main(String[] args) {
        try {
                String fileString = new String(Files.readAllBytes(Paths.get("/home/awips/airnow_conditions.kml")), StandardCharsets.UTF_8);

            AQIDecoder decoder = new AQIDecoder();
            try {
                    PluginDataObject[] dataObjects = decoder.decode(fileString);
                for (PluginDataObject obj: dataObjects) {
                    if (obj instanceof AQIRecord) {
                        AQIRecord record = (AQIRecord)obj;
                        System.out.println(record.getName()+"  "+record.getAqi()+"  "+record.getLocation());
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}
