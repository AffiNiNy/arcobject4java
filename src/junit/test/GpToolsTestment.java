package junit.test;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.esriSelectionResultEnum;
import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabase.esriSchemaLock;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CalculateField;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.JoinField;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.SelectLayerByAttribute;
import com.esri.arcgis.interop.AutomationException;

import ao.ArcUtils.ArcUtils;

public class GpToolsTestment {


    FileGDBWorkspaceFactory factory = null;
    Workspace workspace = null;
    
    @Before
    public void init() throws AutomationException, IOException {
        ArcUtils.bootArcEnvironment();
        factory = new FileGDBWorkspaceFactory();
    }
    
    /**
     * Equals field "Summarize" function in arrtibute table.  
     * @throws UnknownHostException
     * @throws IOException
     */
    @Test
    public void testSummarizeField() throws UnknownHostException, IOException {
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        String fcName = "DongChengStreet_GOVPOI";
        
        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));

        FeatureClass fc = new FeatureClass(workspace.openFeatureClass(fcName));

        String roadFieldName = "所属街路巷代码";
        
        QueryFilter queryFilter = new QueryFilter();
//        queryFilter.setPrefixClause("DISTINCT");
        queryFilter.setSubFields( roadFieldName + ", COUNT(*)" );
        queryFilter.setWhereClause(roadFieldName + " IS NOT NULL ");
        queryFilter.setPostfixClause(" GROUP BY " + roadFieldName);
        
        ICursor cursor = fc.ITable_search(queryFilter, false);
        IRow iRow;
        int cnt = 0;
        
        while((iRow = cursor.nextRow()) != null) {
            cnt++;
            System.out.println( cnt + "\t" + iRow.getValue(0) +" - " + (iRow.getValue(1) instanceof Integer) );
            
        }
        
    }
    
    @Test
    public void testUpdateCursor() throws AutomationException, IOException {
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        String fcName = "DongChengStreet_GOVPOI";
        
        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));

        FeatureClass fc = new FeatureClass(workspace.openFeatureClass(fcName));
        
        QueryFilter filter = new QueryFilter();
        filter.setWhereClause(" \"所属街路巷代码\" = '水阁坊二巷' ");
        
        ICursor update = fc.update(filter, false);
        IRow iRow;
        int fieldIdx = fc.findField("门牌地址别称");
        
        while ((iRow=update.nextRow()) != null) {
//            iRow.setValue(fieldIdx, iRow.getOID());
//            update.updateRow(iRow);
            System.out.println( iRow.getOID() +"\t"+ fc.getRow(iRow.getOID()).getValue(fc.findField("门牌含小区名称")));
        }
        
    }
    
    @Test
    public void testJoinFieldAndCalculateField() throws AutomationException, IOException {
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        String fcName = "TestOutput1";
        String inShp = inFGDB + File.separator + fcName;
        
        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));

        FeatureClass featureClass = new FeatureClass(workspace.openFeatureClass(fcName));
        
        GeoProcessor gp = new GeoProcessor();
        IGeoProcessorResult result = null;
        
        IField nearField = featureClass.getFields().getField(featureClass.findField("NEAR_FID"));
        
        ITable table = new Table(workspace.openTable("DongChengStreet_GOVPOI"));
        IField oidField = table.getFields().getField(table.findField("OBJECTID"));
        
        JoinField joinField = new JoinField();
        joinField.setInData(inFGDB + File.separator + fcName);
        joinField.setInField("NEAR_FID");
        joinField.setJoinTable(inFGDB + File.separator + "DongChengStreet_GOVPOI");
        joinField.setJoinField("OBJECTID");
        
        result = gp.execute(joinField, null);
        ArcUtils.printResult(result);
        
//        IFields fields = featureClass.getFields();
//        ArcUtils.printFieldsName(fields);
//        int guidIdx = fields.findField("GUID");
//        int guid_1Idx = fields.findField("GUID_1");
//        IField iField = fields.getField( guidIdx );
//        IField iField2 = fields.getField( guid_1Idx );
//        System.out.println( iField.getName() + " " + iField.getAliasName() );
//        System.out.println( iField2.getName() + " " + iField2.getAliasName() );
        
        featureClass.changeSchemaLock(esriSchemaLock.esriExclusiveSchemaLock);
        Field f = new Field();
        String newFieldName = "MY_GUID";
        f.setName(newFieldName);
        f.setType(esriFieldType.esriFieldTypeString);
        f.setLength(33);
        
        featureClass.addField(f);
        featureClass.changeSchemaLock(esriSchemaLock.esriSharedSchemaLock);
        
        String outLayer = "someRoadLayer";
        MakeFeatureLayer makeFeatureLayer = new MakeFeatureLayer(inShp, outLayer);
        gp.execute(makeFeatureLayer, null);
        
        SelectLayerByAttribute selByAttr = new SelectLayerByAttribute( outLayer );
        selByAttr.setWhereClause( " OBJECTID <= 10 " );
        result = gp.execute(selByAttr, null);
        ArcUtils.printResult(result);
        
        CalculateField calField = new CalculateField(outLayer, newFieldName, "["+"GUID_1"+"]");
        result = gp.execute(calField, null);
        ArcUtils.printResult(result);
        
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
        
        Near near = new Near(featureLayer, featureLayer);
        gp.execute(near, null);
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
