package ao.shapefile.query;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.IDatasetName;
import com.esri.arcgis.geodatabase.IEnumDatasetName;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriDatasetType;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Point;

import ao.ArcUtils.BootUtils;
import ao.ArcUtils.DemoData;

public class AccessFGDB {

    public AccessFGDB() {}
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        BootUtils.bootstrapArcobjectsJar();
        BootUtils.initLicense();
        
        // Input File Geodatabase
        String inFGDB = DemoData.inFGDB;
        
        AccessFGDB access = new AccessFGDB();
//        access.browseShpName( inFGDB );
        access.openFeature(inFGDB);
        
    }

    private void openFeature(String inFGDB) throws Exception {
        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
        
        // Get specific feature in feature class
        IFeatureClass fc = workspace.openFeatureClass("wind");
        System.out.println( "featureCount: " + fc.featureCount(null) );
        
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setWhereClause("OBJECTID <= 10");
        System.out.println( "featureCount filter: " + fc.featureCount(queryFilter) );
        
        IFeature iFeature = fc.getFeature( 5 );
        // Feature name
        System.out.println( "AliasName: " + fc.getAliasName() );
        // esriGeometryPoint = 1
        System.out.println( "Feature type: " + iFeature.getFeatureType() );
        // Print fields info
        IFields fields = iFeature.getFields();
        System.out.println( "Fields: " + fields.toString() );
        System.out.println( "Fields count: " + fields.getFieldCount() );
        System.out.println( "Specific field: " + fields.getField(fields.findField("Ãû³Æ")).getAliasName() + " - " + fields.getField(fields.findField("±àÂë")).getName());
        System.out.println( "Value 1: "+iFeature.getValue(0) );
        Point p = (Point) iFeature.getValue(1);
        System.out.println( "Value 2: "+iFeature.getValue(1)+" - "+p.getX()+" - "+p.getY() );
        System.out.println( "T/F " + (p instanceof com.esri.arcgis.geometry.Point) );
        System.out.println( "Value 3: "+iFeature.getValue(2) );
        System.out.println( "Value 4: "+iFeature.getValue(3) );
        // Print shape info
        IGeometry iGeometry = iFeature.getShape();
        System.out.println( "IGeometry dimension: " + iGeometry.getDimension());
        System.out.println( "IGeometry type: " + iGeometry.getGeometryType() );
        
    }
    
    /**
     * Browse the file geodatabase and print shapefile name.
     * @param inputFGDB Input the file geodatabase wanted.
     */
    @SuppressWarnings("unused")
    private void browseShpName(String inputFGDB) {
        try {
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inputFGDB, 0));
            
            //Get all dataset names in the workspace
            IEnumDatasetName enumDatasetName = workspace.getDatasetNames(esriDatasetType.esriDTFeatureClass);
            
            //Get the first name in the dataset
            IDatasetName dsName;
            int cnt = 0;
            while( (dsName = enumDatasetName.next()) != null ){
                cnt ++;
                //Print out the dataset name to the console
                System.out.println("Dataset Name: " + dsName.getName());
            }
            System.out.println( "Datasets: " + cnt );
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
}
