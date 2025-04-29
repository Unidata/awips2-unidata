package edu.ucar.unidata.viz.aqi.rsc;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import com.raytheon.uf.common.dataplugin.PluginDataObject;


@XmlAccessorType(XmlAccessType.NONE)
public class AQIResourceData extends AbstractRequestableResourceData {

    @Override
    protected AbstractVizResource<?, ?> constructResource(LoadProperties loadProperties, PluginDataObject[] objects) throws VizException {
            
        AQIResource rsc = new AQIResource(this,loadProperties);
        for (PluginDataObject o : objects) {
                 if (o instanceof AQIRecord) {
               AQIRecord rec = (AQIRecord) o;
               rsc.addRecord(rec);
             }
        }
        return rsc;

    }

}
