/*
 * VAlignEditor.java
 *
 * Created on December 30, 2005, 2:46 PM
 */

package de.berlios.nblayoutpack.riverlayout.editors;

import java.beans.*;
import java.io.IOException;

import org.w3c.dom.*;

import org.openide.explorer.propertysheet.editors.*;

import org.netbeans.modules.form.*;

import de.berlios.nblayoutpack.riverlayout.*;

/**
 *
 * @author Illya Kysil
 */
public class VAlignEditor extends PropertyEditorSupport {

    /** Creates a new instance of VAlignEditor */
    public VAlignEditor() {
    }

    private final String[] tags = {
        RiverLayoutSupport.getBundle().getString("VALUE_vAlign_CENTER"), // NOI18N
        RiverLayoutSupport.getBundle().getString("VALUE_vAlign_DEFAULT"), // NOI18N
        RiverLayoutSupport.getBundle().getString("VALUE_vAlign_TOP") // NOI18N
    };

    private final Integer[] values = {
        new Integer(RiverLayoutSupport.RiverLayoutSupportConstraints.VALIGN_CENTER),
        new Integer(RiverLayoutSupport.RiverLayoutSupportConstraints.VALIGN_DEFAULT),
        new Integer(RiverLayoutSupport.RiverLayoutSupportConstraints.VALIGN_TOP)
    };

    public String[] getTags() {
        return tags;
    }

    public String getAsText() {
        Object value = getValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return tags[i];
            }
        }
        return null;
    }

    public void setAsText(String str) {
        for (int i=0; i < tags.length; i++) {
            if (tags[i].equals(str)) {
                setValue(values[i]);
            }
        }
    }

    public String getJavaInitializationString() {
        return null;
    }

}
