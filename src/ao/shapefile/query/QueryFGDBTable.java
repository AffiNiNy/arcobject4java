package ao.shapefile.query;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;

import ao.ArcUtils.ArcUtils;
import ao.ArcUtils.DemoData;

public class QueryFGDBTable {

    public static void main(String[] args) throws UnknownHostException, IOException {
        ArcUtils.bootArcEnvironment();

        // Input File GeoDatabase
        String inFGDB = DemoData.usaFGDB;

        FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
        Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
        
        String name = "wind";
        
        // Workspace gets ITable interface.
        ITable openTable = workspace.openTable(name);
            System.out.println( openTable.toString() );
            
        // new a Table to enclose ITable.
        Table windTable = new Table(openTable);
            System.out.println( windTable.toString() );
        
        // Fields type need force transform
        Fields fields = (Fields) windTable.getFields();
        for (int i = 0; i < fields.getFieldCount(); i++) {
            Field field = (Field) fields.getField(i);
            System.out.println( "    " + field.getName() );
        }
        
        // "ID", "VELOCITY", "DIRECTION"
        IQueryFilter queryFilter = new QueryFilter();
        queryFilter.setSubFields(", VELOCITY, DIRECTION");
        queryFilter.setWhereClause("ID is not null");
        
        ICursor iCursor = windTable.ITable_search(queryFilter, false);
        IRow row = iCursor.nextRow();
        System.out.println( row );
        System.out.println( row.getValue(0) ); // null
        System.out.println( row.getValue(1) ); // null
        System.out.println( row.getValue(2) ); // get value
        System.out.println( row.getValue(3) ); // get value
        System.out.println( row.getValue(4) + "\r\n\r\nFeature way:\r\n"); // get value
        
        /*IFeatureClass iFeatureClass = workspace.openFeatureClass("wind");
        IFeature feature = iFeatureClass.getFeature(1);
        System.out.println( feature.getValue(0) );
        System.out.println( feature.getValue(1) );
        System.out.println( feature.getValue(2) );
        System.out.println( feature.getValue(3) );
        System.out.println( feature.getValue(4) );*/
        
    }

}
