package ao.ArcUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;

public class BootUtils {

    public static void bootstrapArcobjectsJar() {
        // Get the ArcGIS Engine runtime, if it is available
        String arcObjectsHome = System.getenv("AGSENGINEJAVA");

        // If the ArcGIS Engine runtime is not available, then we can try ArcGIS Desktop
        // runtime
        if (arcObjectsHome == null) {
            arcObjectsHome = System.getenv("AGSDESKTOPJAVA");
        }

        // If no runtime is available, exit application gracefully
        if (arcObjectsHome == null) {
            if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
                System.err.println("You must have ArcGIS Engine Runtime or ArcGIS Desktop "
                        + "installed in order to execute this sample.");
                System.err.println("Install one of the products above, then re-run this sample.");
                System.err.println("Exiting execution of this sample...");
                System.exit(0);
            } else {
                System.err.println("You must have ArcGIS Engine Runtime installed " + "in order to execute this sample.");
                System.err.println("Install the product above, then re-run this sample.");
                System.err.println("Exiting execution of this sample...");
                System.exit(0);
            }
        }

        // Obtain the relative path to the arcobjects.jar file
        String jarPath = arcObjectsHome + "java" + File.separator + "lib" + File.separator + "arcobjects.jar";

        // Create a new file
        File jarFile = new File(jarPath);

        // Test for file existence
        if (!jarFile.exists()) {
            System.err.println("The arcobjects.jar was not found in the following location: " + jarFile.getParent());
            System.err.println("Verify that arcobjects.jar can be located in the specified folder.");
            System.err.println("If not present, try uninstalling your ArcGIS software and reinstalling it.");
            System.err.println("Exiting execution of this sample...");
            System.exit(0);
        }

        // Helps load classes and resources from a search path of URLs
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { jarFile.toURI().toURL() });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.err.println("Could not add arcobjects.jar to system classloader");
            System.err.println("Exiting execution of this sample...");
            System.exit(0);
        }
        
        System.out.println( "--- Bootstrapped ArcObject jar ---");
    }

    public static void initLicense() throws AutomationException, IOException {
        // Initialize engine console application
        EngineInitializer.initializeEngine();

        /*
         * Initialize ArcGIS license.
         * Checks to see if an ArcGIS Engine Runtime license or an Basic License is
         * available. If so, then the appropriate ArcGIS License is initialized.
         */
        AoInitialize aoInit = new AoInitialize();
        
        if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) 
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
            System.out.println( "**** use engine ****");}
        else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeBasic) 
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic);
            System.out.println( "**** use basic ****");}
        else {
            System.err.println("Could not initialize an Engine or Basic License. Exiting application.");
            System.exit(-1);
        }
        
        System.out.println( "--- ArcGIS License initialized. ---");
    }

}
