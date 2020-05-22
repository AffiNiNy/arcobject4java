package junit.test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.esriSelectionResultEnum;
import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Feature;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.FeatureCursor;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.ISelectionSet;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriSelectionOption;
import com.esri.arcgis.geodatabase.esriSelectionType;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.SelectLayerByAttribute;
import com.esri.arcgis.interop.AutomationException;

import ao.ArcUtils.ArcUtils;

public class GpToolsTestment {

    
    @Before
    public void init() throws AutomationException, IOException {
        ArcUtils.bootArcEnvironment();
    }
    
    @Test
    public void testSelect() throws AutomationException, IOException {
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";

        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
        
        String featureClassName = "MakeFeatureLayer_Output";
        FeatureClass featureClass = new FeatureClass(workspace.openFeatureClass(featureClassName));
        
        QueryFilter filter = new QueryFilter();
        filter.setWhereClause("OBJECTID <= 10");
        
        FeatureLayer featureLayer = new FeatureLayer();
        
        featureLayer.setFeatureClassByRef(featureClass);
        System.out.println( featureLayer.rowCount(null) );
        
        featureLayer.selectFeatures(filter, esriSelectionResultEnum.esriSelectionResultNew, false);
        System.out.println( featureLayer.rowCount(null) );

        GeoProcessor gp = new GeoProcessor();
        MakeFeatureLayer makeFeatureLayer = new MakeFeatureLayer(featureLayer, "layer1");
        
        gp.execute(makeFeatureLayer, null);
        
//        Near near = new Near(featureLayer, featureLayer);
//        gp.execute(near, null);
    }
    
    @Test
    public void test() throws UnknownHostException, IOException {
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";

        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
        
        String featureClassName = "MakeFeatureLayer_Output";
        FeatureClass featureClass = new FeatureClass(workspace.openFeatureClass(featureClassName));
        
        /* Update field */
        IFields fields = featureClass.getFields();
        ArcUtils.printFieldsName(fields);
        int idx = fields.findField("门牌含小区名称");
        int nearDisIdx = fields.findField("NEAR_DIST");
        
        ITable openTable = workspace.openTable(featureClassName);
        
        
        /* System.out.println( "Before: " + featureClass.getFields().getFieldCount() );
        
        Field field = new Field();
        field.setName("MY_FIELD2");
        field.setAliasName("我添加的字段");
        field.setIsNullable(true);
        field.setType(esriFieldType.esriFieldTypeString);
        field.setLength(123);
        
//        IEnumSchemaLockInfo[] schemaLockInfo = new IEnumSchemaLockInfoProxy[9];
//        featureClass.getCurrentSchemaLocks( schemaLockInfo );
        featureClass.changeSchemaLock( esriSchemaLock.esriExclusiveSchemaLock );
        featureClass.addField( field );

        System.out.println( "After: " + featureClass.getFields().getFieldCount() );*/
    }

    @After
    public void end() {
        ArcUtils.release();
    }
}
