package ao.shapefile.gp;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabase.esriSchemaLock;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CalculateField;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CopyFeatures;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.JoinField;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;

import ao.ArcUtils.ArcUtils;

/**
 * 东莞市政 POI 图层. 对相同道路名称的 POI 组做邻近分析.
 * @author YunquNet_233
 *
 */
public class GeoProcess_Near_Q1 {

    
    public static void main(String[] args) {
        ArcUtils.bootArcEnvironment();
        
        // GDB Path
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        // FeatureClass name
        String fcName = "DongChengStreet_GOVPOI";
        
        String roadFieldName = "所属街路巷代码";
        String oneRoadLayer  = "oneRoadLayer";
        String clause = " \"%s\" = '%s' ";
        
        /* GeoProcess tools */
        GeoProcessor gp = null;
        Near near = new Near();
        CopyFeatures copyFeatures = new CopyFeatures();
        MakeFeatureLayer makeFeatureLayer = new MakeFeatureLayer();
        JoinField joinField = new JoinField();
        CalculateField calField;
        IGeoProcessorResult result;
        
        
        try {
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));

            // GeoProcessor
            gp = new GeoProcessor();
            gp.setOverwriteOutput(true);
            
            // Open POI by FeatureClass
            FeatureClass poiFC = new FeatureClass(workspace.openFeatureClass(fcName));
            
            /* Add fields for the first time */
            addFields(poiFC);
            
            // Field index
//            int roadNameIdx = poiFC.findField(roadFieldName);
//            int guidIdx = poiFC.findField("GUID");
            
            /* Summarize road name and corresponding count */
            QueryFilter queryFilter = new QueryFilter();
            queryFilter.setSubFields(roadFieldName + ", COUNT(*)");
            queryFilter.setWhereClause(roadFieldName + " IS NOT NULL ");
            queryFilter.setPostfixClause(" GROUP BY " + roadFieldName);
            
            ICursor iCursor = poiFC.ITable_search(queryFilter, true);
            IRow nextRow;
            String roadName;
            int roadCount;
            
            while ((nextRow = iCursor.nextRow()) != null) {
                roadName = nextRow.getValue(0).toString();
                roadCount = (Integer) nextRow.getValue(1);
                
                if (roadCount == 1) continue;
                
            }
            
                nextRow = iCursor.nextRow();
                roadName = String.valueOf(nextRow.getValue(0));
                roadCount = (Integer) nextRow.getValue(1);
                System.out.println("  -- " + roadName + " - " + roadCount );
                
                /* Make one road feature layer */
                makeFeatureLayer.setInFeatures(poiFC);
                makeFeatureLayer.setOutLayer(oneRoadLayer);
                makeFeatureLayer.setWhereClause( String.format(clause, roadFieldName, roadName) );
                gp.execute(makeFeatureLayer, null);
                
                /* oneRoadLayer near oneRoadLayer */
                near.setInFeatures(oneRoadLayer);
                near.setNearFeatures(oneRoadLayer);
                result = gp.execute(near, null);
                
                calField = new CalculateField(oneRoadLayer, "NEAR_FEATUREID", "[NEAR_FID]");
                gp.execute(calField, null);
                
                calField.setField("NEAR_DISTANCE");
                calField.setExpression("[NEAR_DIST]");
                gp.execute(calField, null);
            
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ArcUtils.release();
        }
        
        System.out.println("---- Application exit ----");
    }

    
    private static void addFields(FeatureClass featureClass) throws UnknownHostException, IOException {
        featureClass.changeSchemaLock(esriSchemaLock.esriExclusiveSchemaLock);

        Field nearFeatureID = new Field();
        nearFeatureID.setName("NEAR_FEATUREID");
        nearFeatureID.setType(esriFieldType.esriFieldTypeString);
        nearFeatureID.setLength(32);
        featureClass.addField(nearFeatureID);
        
        Field nearDisField = new Field();
        nearDisField.setName("NEAR_DISTANCE");
        nearDisField.setType(esriFieldType.esriFieldTypeDouble);
        featureClass.addField(nearDisField);
        
        Field nearGUIDField = new Field();
        nearGUIDField.setName("NEAR_GUID");
        nearGUIDField.setType(esriFieldType.esriFieldTypeString);
        nearGUIDField.setLength(12);
        featureClass.addField(nearGUIDField);
        
        Field nearRoadname = new Field();
        nearRoadname.setName("NEAR_ROADNAME");
        nearRoadname.setType(esriFieldType.esriFieldTypeString);
        nearRoadname.setLength(128);
        featureClass.addField(nearRoadname);
        
        Field nearDoornum = new Field();
        nearDoornum.setName("NEAR_DOORNUM");
        nearDoornum.setType(esriFieldType.esriFieldTypeString);
        nearDoornum.setLength(128);
        featureClass.addField(nearDoornum);
        
        featureClass.changeSchemaLock(esriSchemaLock.esriSharedSchemaLock);
        
    }
    
    

}
