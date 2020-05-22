package ao.shapefile.gp;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Cursor;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IFeatureDataset;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geoprocessing.GPFeatureLayer;
import com.esri.arcgis.geoprocessing.GPTableView;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;

import ao.ArcUtils.ArcUtils;

/**
 * 东莞市政 POI 图层. 对相同道路名称的 POI 组做邻近分析.
 * @author YunquNet_233
 *
 */
public class GeoProcess_Near_Q1 {

    private static GeoProcessor gp;
    
    public static void main(String[] args) {
        ArcUtils.bootArcEnvironment();
        
        // GDB Path
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        // FeatureClass name
        String fcName = "DongChengStreet_GOVPOI";
        
        String roadFieldName = "所属街路巷代码";
        int roadNameIdx;
        String clause = "\"%s\" = '%s' ";
        
        try {
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
            
            // Open POI by table
            Table poiTable = new Table(workspace.openTable(fcName));
            // Open POI by FeatureClass
            FeatureClass poiFC = new FeatureClass(workspace.openFeatureClass(fcName));
            
            
            roadNameIdx = poiTable.findField(roadFieldName);
            
            /* Iterate unique roadname r1. */
            QueryFilter queryFilter = new QueryFilter();
            queryFilter.setPrefixClause("DISTINCT");
            queryFilter.setSubFields(roadFieldName);
            queryFilter.setWhereClause(roadFieldName + " IS NOT NULL ");
            
            ICursor iCursor = poiTable.ITable_search(queryFilter, true);
            IRow nextRow = iCursor.nextRow();
            String roadName = nextRow.getValue(roadNameIdx).toString();  System.out.println("The first road name: " + roadName);
            
            
            FeatureLayer featureLayer = new FeatureLayer();
            
            
            // GeoProcessor to execute 
            gp = new GeoProcessor();
            Near near = new Near();
            
            /*
            near.setInFeatures( layerName );
            near.setNearFeatures( layerName );
            gp.execute(near, null);
            IGeoProcessorResult execute = gp.execute(near, null);
            ArcUtils.printResult( execute);*/
            
            
            // Check whether contains NEAR_* fields or not..
//            IField field = featureClass.getFields().getField(featureClass.findField("NEAR_FID"));
//            System.out.println( field.getName() );
//            System.out.println( field.getLength() );
            
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ArcUtils.release();
        }
        
        System.out.println("---- Application exit ----");
    }
    

    @SuppressWarnings("unused")
    private static MakeFeatureLayer makeFeatureLayer(String inFGDB, String fcName, String roadFieldName, String clause,
            String roadName) {
        // Make specific feature layer based on r1
        MakeFeatureLayer roadFeatureLayer = null;
        String layerName = "oneRoadLayer";
        roadFeatureLayer = new MakeFeatureLayer(inFGDB + File.separator + fcName, layerName);
        // -- Feature layer set where clause
        roadFeatureLayer.setWhereClause( String.format(clause, roadFieldName, roadName) );
        return roadFeatureLayer;
    }

}
