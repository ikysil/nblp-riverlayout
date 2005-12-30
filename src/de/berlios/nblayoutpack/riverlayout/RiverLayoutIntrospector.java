/*
 * RiverLayoutIntrospector.java
 *
 * Created on December 30, 2005, 1:04 PM
 */

package de.berlios.nblayoutpack.riverlayout;

import java.awt.*;
import java.lang.reflect.*;

import org.openide.ErrorManager;

import se.datadosen.component.*;

/**
 *
 * @author Illya Kysil
 */
public class RiverLayoutIntrospector {

    private static Constructor layoutConstructor;

    private static Method setExtraInsetsMethod;

    /** Creates a new instance of RiverLayoutIntrospector */
    private RiverLayoutIntrospector() {
    }

    public static Constructor getLayoutConstructor() {
        if (layoutConstructor == null) {
            try {
                layoutConstructor = RiverLayout.class.getConstructor(new Class[]{int.class, int.class});
            }
            catch (NoSuchMethodException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return layoutConstructor;
    }

    public static Method getSetExtraInsetsMethod() {
        if (setExtraInsetsMethod == null) {
            try {
                setExtraInsetsMethod = RiverLayout.class.getMethod("setExtraInsets", new Class[]{Insets.class});
            }
            catch (NoSuchMethodException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return setExtraInsetsMethod;
    }

}
