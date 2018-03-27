package edu.ucar.unidata.common.dataplugin.aqi;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "aqi_seq")
@Table(name = "aqi", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "name", "refTime" }) })
@org.hibernate.annotations.Table(appliesTo = "aqi", indexes = { @Index(name = "aqi_refTimeIndex", columnNames = {
        "refTime", "forecastTime" }) })
@DynamicSerialize
public class AQIRecord extends PluginDataObject {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "aqi";

    @Column
    @DynamicSerializeElement
    @DataURI(position = 1)
    String name;

    @Column
    @DynamicSerializeElement
    int aqi;

    @Column
    @DynamicSerializeElement
    int random;

    @Column(name = "location", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Geometry geometry;

    public AQIRecord() {
    }

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

    public int getRandom() {
        return random;
    }

    public void setRandom(int random) {
        this.random = random;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
