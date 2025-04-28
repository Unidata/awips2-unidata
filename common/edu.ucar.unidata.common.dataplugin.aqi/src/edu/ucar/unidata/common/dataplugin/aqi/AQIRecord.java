package edu.ucar.unidata.common.dataplugin.aqi;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.locationtech.jts.geom.Geometry;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

@DynamicSerialize
@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "aqi_seq")
@Table(name = "aqi", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "refTime" }) })
@org.hibernate.annotations.Table(appliesTo = "aqi", indexes = { @Index(name = "aqi_refTimeIndex", columnNames = {  "refTime", "forecastTime" }) })

public class AQIRecord extends PluginDataObject {

    public static final String PLUGINNAME="aqi";

    @DynamicSerializeElement
    @DataURI(position = 1)
    @Column
    String name;
    
    @DynamicSerializeElement
    @Column
    int aqi;

    @DynamicSerializeElement
    @Column(name = "location", columnDefinition = "geometry") 
    @XmlJavaTypeAdapter(value = GeometryAdapter.class) 
    Geometry location;
    
    //Don't need to declare the dataTime, because that already exists in the PluginDataObject that we're extending

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    @Override
    public String getPluginName() {

        return PLUGINNAME;
    }

}
