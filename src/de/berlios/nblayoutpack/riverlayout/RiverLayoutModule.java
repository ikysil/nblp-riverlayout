/*
 * RiverLayoutModule.java
 *
 * Created on December 30, 2005, 12:02 PM
 */

package de.berlios.nblayoutpack.riverlayout;

import org.openide.modules.ModuleInstall;

import org.netbeans.modules.form.layoutsupport.LayoutSupportRegistry;

/**
 *
 * @author Illya Kysil
 */
public class RiverLayoutModule extends ModuleInstall {

    /** Creates a new instance of RiverLayoutModule */
    public RiverLayoutModule() {
    }

    public void restored(){
        LayoutSupportRegistry.registerSupportForLayout(
                "se.datadosen.component.RiverLayout",
                "de.berlios.nblayoutpack.riverlayout.RiverLayoutSupport"
        );
    }

    public void close(){
        LayoutSupportRegistry.registerSupportForLayout(
                "se.datadosen.component.RiverLayout",
                LayoutSupportRegistry.DEFAULT_SUPPORT
        );
    }

}
