/*
 * RiverLayoutSupport.java
 *
 * Created on December 30, 2005, 12:05 PM
 */

package de.berlios.nblayoutpack.riverlayout;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.*;

import se.datadosen.component.*;
import se.datadosen.util.*;

import de.berlios.nblayoutpack.riverlayout.editors.*;

/**
 *
 * @author Illya Kysil
 */
public class RiverLayoutSupport extends AbstractLayoutSupport {

    /** The icon for RiverLayout. */
    private static String iconURL =
            "de/berlios/nblayoutpack/riverlayout/resources/riverlayout16.png"; // NOI18N
    /** The icon for RiverLayout. */
    private static String icon32URL =
            "de/berlios/nblayoutpack/riverlayout/resources/riverlayout32.png"; // NOI18N

    private int hgap;

    private int vgap;

    private Insets extraInsets;

    /** Creates a new instance of RiverLayoutSupport */
    public RiverLayoutSupport() {
        RiverLayout riverLayout = new RiverLayout();
        hgap = riverLayout.getHgap(); 
        vgap = riverLayout.getVgap(); 
        extraInsets = riverLayout.getExtraInsets(); 
    }

    public Class getSupportedClass() {
        return se.datadosen.component.RiverLayout.class;
    }

    public int getHgap() {
        return hgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public int getVgap() {
        return vgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    public Insets getExtraInsets() {
        return extraInsets;
    }

    public void setExtraInsets(Insets extraInsets) {
        this.extraInsets = extraInsets;
    }

    /** Provides an icon to be used for the layout node in Component
     * Inspector. Only 16x16 color icon is required.
     * @param type is one of BeanInfo constants: ICON_COLOR_16x16,
     *        ICON_COLOR_32x32, ICON_MONO_16x16, ICON_MONO_32x32
     * @return icon to be displayed for node in Component Inspector
     */
    public Image getIcon(int type) {
        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return Utilities.loadImage(iconURL);
            default:
                return Utilities.loadImage(icon32URL);
        }
    }

    public static ResourceBundle getBundle() {
        return org.openide.util.NbBundle.getBundle(RiverLayoutSupport.class);
    }

    /** Sets up the layout (without adding components) on a real container,
     * according to the internal metadata representation. This method must
     * override AbstractLayoutSupport because FormLayout instance cannot
     * be used universally - new instance must be created for each container.
     * @param container instance of a real container to be set
     * @param containerDelegate effective container delegate of the container;
     *        for layout managers we always use container delegate instead of
     *        the container
     */
    public void setLayoutToContainer(Container container, Container containerDelegate) {
        try {
            containerDelegate.setLayout(cloneLayoutInstance(container, containerDelegate));
        }
        catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }

    protected LayoutManager cloneLayoutInstance(Container container, Container containerDelegate) {
        RiverLayout result = new RiverLayout(hgap, vgap);
        result.setExtraInsets(extraInsets);
        return result;
    }

    protected LayoutManager createDefaultLayoutInstance() throws Exception {
        return new RiverLayout();
    }

    private static final String baseVarName = "_riverLayoutInstance";

    /** Creates code structures for a new layout manager (opposite to
     * readInitLayoutCode). As the RiverLayout is not a bean, this method must
     * override from AbstractLayoutSupport.
     * @param layoutCode CodeGroup to be filled with relevant
     *        initialization code;
     * @return new CodeExpression representing the FormLayout
     */
    protected CodeExpression createInitLayoutCode(CodeGroup layoutCode){
        CodeStructure codeStructure = getCodeStructure();
        FormProperty[] properties = getProperties();
        CodeExpression[] constrParams = new CodeExpression[2];
        constrParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0]));
        constrParams[1] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[1]));
        CodeExpression varExpression = codeStructure.createExpression(RiverLayoutIntrospector.getLayoutConstructor(), constrParams);
        String varName = baseVarName;
        CodeVariable var = codeStructure.getVariable(varName);
        int i = 1;
        while(var != null){
            varName = baseVarName + (i++);
            var = codeStructure.getVariable(varName);
        };
        var = codeStructure.createVariable(CodeVariable.LOCAL /*| CodeVariable.EXPLICIT_DECLARATION*/, RiverLayout.class, varName);
        codeStructure.attachExpressionToVariable(varExpression, var);
        layoutCode.addStatement(0, var.getAssignment(varExpression));
        CodeExpression[] setExtraInsetsParams = new CodeExpression[1];
        setExtraInsetsParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[2]));
        layoutCode.addStatement(codeStructure.createStatement(varExpression, RiverLayoutIntrospector.getSetExtraInsetsMethod(), setExtraInsetsParams));
        return varExpression;
    }

    protected void readInitLayoutCode(CodeExpression codeExpression, CodeGroup layoutCode){
        CodeVariable var = codeExpression.getVariable();
        layoutCode.addStatement(0, var.getAssignment(codeExpression));
        CodeExpression[] constructorParams = var.getAssignment(codeExpression).getStatementParameters();
        FormProperty[] properties = getProperties();
        FormCodeSupport.readPropertyExpression(constructorParams[0], properties[0], false);
        FormCodeSupport.readPropertyExpression(constructorParams[1], properties[1], true);
        Iterator it = CodeStructure.getDefinedStatementsIterator(codeExpression);
        while(it.hasNext()){
            CodeStatement statement = (CodeStatement)it.next();
            if(isMethod(statement, RiverLayoutIntrospector.getSetExtraInsetsMethod())){
                FormCodeSupport.readPropertyStatement(statement, properties[2], true);
            }
            layoutCode.addStatement(statement);
        }
        updateLayoutInstance();
    }

    public boolean isMethod(CodeStatement statement, Method method){
        Object obj = statement.getMetaObject();
        if (obj != null && obj instanceof Method) {
            Method other = (Method)obj;
            // Compare class names only since classes can be loaded by different ClassLoaders
            if ((method.getDeclaringClass().getName().equals(other.getDeclaringClass().getName()))
                && (method.getName() == other.getName())) {
                if (!method.getReturnType().equals(other.getReturnType())) {
                    return false;
                }
                Class[] params1 = method.getParameterTypes();
                Class[] params2 = other.getParameterTypes();
                if (params1.length == params2.length) {
                    for (int i = 0; i < params1.length; i++) {
                        if (params1[i] != params2[i])
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Called from createComponentCode method, creates code for a component
     * layout constraints (opposite to readConstraintsCode).
     * @param constrCode CodeGroup to be filled with constraints code; not
     *        needed here because AbsoluteConstraints object is represented
     *        only by a single constructor code expression and no statements
     * @param constr layout constraints metaobject representing the constraints
     * @param compExp CodeExpression object representing the component; not
     *        needed here
     * @return created CodeExpression representing the layout constraints
     */
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
            LayoutConstraints constr, CodeExpression compExp, int index) {
        if (!(constr instanceof RiverLayoutSupportConstraints))
            return null;
        
        RiverLayoutSupportConstraints rlsConstr = (RiverLayoutSupportConstraints) constr;
        // code expressions for constructor parameters are created in
        // RiverLayoutSupportConstraints
        CodeExpression[] params = rlsConstr.createPropertyExpressions(getCodeStructure());
        return params[0];
    }
    
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp, CodeGroup constrCode, CodeExpression compExp) {
        RiverLayoutSupportConstraints constr = new RiverLayoutSupportConstraints();
        // reading is done in RiverLayoutSupportConstraints
        constr.readCodeExpression(constrExp, constrCode);
        return constr;
    }
    
    private FormProperty[] properties;
    
    protected FormProperty[] getProperties() {
        if (properties == null){
            properties = createProperties();
        }
        return properties;
    }
    
    protected FormProperty[] createProperties() {
        FormProperty[] properties = new FormProperty[] {
            new FormProperty("hgap", // NOI18N
                    Integer.class,
                    getBundle().getString("PROP_hGap"), // NOI18N
                    getBundle().getString("HINT_hGap")) { // NOI18N
                
                public Object getTargetValue() {
                    return new Integer(hgap);
                }
                public void setTargetValue(Object value) {
                    hgap = ((Integer) value).intValue();
                }
                public void setPropertyContext(FormPropertyContext ctx) {
                    // disabling this method due to limited persistence
                    // capabilities (compatibility with previous versions)
                }
            },
            
            new FormProperty("vgap", // NOI18N
                    Integer.class,
                    getBundle().getString("PROP_vGap"), // NOI18N
                    getBundle().getString("HINT_vGap")) { // NOI18N
                
                public Object getTargetValue() {
                    return new Integer(vgap);
                }
                public void setTargetValue(Object value) {
                    vgap = ((Integer) value).intValue();
                }
                public void setPropertyContext(FormPropertyContext ctx) {
                    // disabling this method due to limited persistence
                    // capabilities (compatibility with previous versions)
                }
            },
            
            new FormProperty("extraInsets", // NOI18N
                    Insets.class,
                    getBundle().getString("PROP_extraInsets"), // NOI18N
                    getBundle().getString("HINT_extraInsets")) { // NOI18N
                
                public Object getTargetValue() {
                    return extraInsets;
                }
                public void setTargetValue(Object value) {
                    extraInsets = (Insets) value;
                }
                public void setPropertyContext(FormPropertyContext ctx) {
                    // disabling this method due to limited persistence
                    // capabilities (compatibility with previous versions)
                }
            }
        };
        return properties;
    }
    
    public LayoutConstraints createDefaultConstraints() {
        return new RiverLayoutSupportConstraints();
    }

    /** Provides resizing options for given component. It can combine the
     * bit-flag constants RESIZE_UP, RESIZE_DOWN, RESIZE_LEFT, RESIZE_RIGHT.
     * @param container instance of a real container in which the
     *        component is to be resized
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component real component to be resized
     * @param index position of the component in its container
     * @return resizing options for the component; 0 if no resizing is possible
     */
    public int getResizableDirections(Container container, Container containerDelegate,
            Component component, int index) {
        //TODO: evaluate feasibility
        return 0;
    }

    /** This method should paint a feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container).
     * In principle, it should present given component layout constraints or
     * index graphically.
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame) - here the feedback is painted
     * @param component the real component being dragged, can be null
     * @param newConstraints component layout constraints to be presented
     * @param newIndex component's index position to be presented
     *        (if newConstraints == null)
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (may return false if the
     *         constraints or index are invalid, or if the painting is not
     *         implemented)
     */
    public boolean paintDragFeedback(Container container, Container containerDelegate,
            Component component, LayoutConstraints newConstraints, int newIndex, Graphics g) {
        //TODO: evaluate feasibility
        return false;
    }

    /** This method should calculate layout constraints for a component dragged
     * over a container (or just for mouse cursor being moved over container).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component the real component being dragged, can be null
     * @param index position (index) of the component in its current container;
     *        -1 if there's no dragged component
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component; null if
     *        there's no dragged component
     * @return new LayoutConstraints object corresponding to the position of
     *         the component in the container; may return null if the layout
     *         does not use component constraints, or if default constraints
     *         should be used
     */
    public LayoutConstraints getNewConstraints(Container container, Container containerDelegate,
            Component component, int index, Point posInCont, Point posInComp) {
        //TODO: evaluate feasibility
        return null;
    }

    /** This method should calculate layout constraints for a component being
     * resized.
     * @param container instance of a real container in which the
     *        component is to be resized
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component real component to be resized
     * @param index position of the component in its container
     * @param sizeChanges Insets object with size differences
     * @param posInCont position of mouse in the container delegate
     * @return component layout constraints for resized component; null if
     *         resizing is not possible or not implemented
     */
    public LayoutConstraints getResizedConstraints(Container container, Container containerDelegate,
            Component component, int index, Insets sizeChanges, Point posInCont) {
        //TODO: evaluate feasibility
        return null;
    }

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). The default implementation does nothing.
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    public void convertConstraints(LayoutConstraints[] previousConstraints,
            LayoutConstraints[] currentConstraints, Component[] components) {
        if ((components == null) || (components.length == 0)){
            return;
        }
        //TODO: evaluate feasibility
//        ConstraintsConverter converter = new DefaultConstraintsConverter();
//        converter.convertConstraints(getLayoutContext(), this, previousConstraints, currentConstraints, components);
    }
    
    public static String encodeConstraints(RiverLayoutSupportConstraints rlsc) {
        StringBuffer sb = new StringBuffer();
        appendIf(sb, rlsc.lineBreak, RiverLayout.LINE_BREAK);
        appendIf(sb, rlsc.parBreak, RiverLayout.PARAGRAPH_BREAK);
        appendIf(sb, rlsc.tabStop, RiverLayout.TAB_STOP);
        appendIf(sb, rlsc.hFill, RiverLayout.HFILL);
        appendIf(sb, rlsc.vFill, RiverLayout.VFILL);
        appendIf(sb, rlsc.hAlign == RiverLayoutSupportConstraints.HALIGN_LEFT, RiverLayout.LEFT);
        appendIf(sb, rlsc.hAlign == RiverLayoutSupportConstraints.HALIGN_CENTER, RiverLayout.CENTER);
        appendIf(sb, rlsc.hAlign == RiverLayoutSupportConstraints.HALIGN_RIGHT, RiverLayout.RIGHT);
        appendIf(sb, rlsc.vAlign == RiverLayoutSupportConstraints.VALIGN_CENTER, RiverLayout.VCENTER);
        appendIf(sb, rlsc.vAlign == RiverLayoutSupportConstraints.VALIGN_TOP, RiverLayout.VTOP);
        return sb.toString().trim();
    }

    private static StringBuffer appendIf(StringBuffer sb, boolean flag, String value) {
        if (flag) {
            sb.append(value).append(" ");
        }
        return sb;
    }

    public static RiverLayoutSupportConstraints decodeConstraints(String value) {
        RiverLayoutSupportConstraints result = new RiverLayoutSupportConstraints();
        StringTokenizer st = new StringTokenizer(value.trim());
        for (; st.hasMoreTokens(); ) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase(RiverLayout.LINE_BREAK)) {
                result.lineBreak = true;
            }
            if (token.equalsIgnoreCase(RiverLayout.PARAGRAPH_BREAK)) {
                result.parBreak = true;
            }
            if (token.equalsIgnoreCase(RiverLayout.TAB_STOP)) {
                result.tabStop = true;
            }
            if (token.equalsIgnoreCase(RiverLayout.HFILL)) {
                result.hFill = true;
            }
            if (token.equalsIgnoreCase(RiverLayout.VFILL)) {
                result.vFill = true;
            }
            if (token.equalsIgnoreCase(RiverLayout.LEFT)) {
                result.hAlign = RiverLayoutSupportConstraints.HALIGN_LEFT;
            }
            if (token.equalsIgnoreCase(RiverLayout.CENTER)) {
                result.hAlign = RiverLayoutSupportConstraints.HALIGN_CENTER;
            }
            if (token.equalsIgnoreCase(RiverLayout.RIGHT)) {
                result.hAlign = RiverLayoutSupportConstraints.HALIGN_RIGHT;
            }
            if (token.equalsIgnoreCase(RiverLayout.VCENTER)) {
                result.vAlign = RiverLayoutSupportConstraints.VALIGN_CENTER;
            }
            if (token.equalsIgnoreCase(RiverLayout.VTOP)) {
                result.vAlign = RiverLayoutSupportConstraints.VALIGN_TOP;
            }
        }
        return result;
    }

    public static class RiverLayoutSupportConstraints implements LayoutConstraints {

        public static final int HALIGN_DEFAULT = 0;
        public static final int HALIGN_LEFT    = 1;
        public static final int HALIGN_CENTER  = 2;
        public static final int HALIGN_RIGHT   = 3;

        public static final int VALIGN_DEFAULT = 0;
        public static final int VALIGN_TOP     = 1;
        public static final int VALIGN_CENTER  = 2;

        private Node.Property[] properties;

        // br - Add a line break
        public boolean lineBreak;
        // p - Add a paragraph break
        public boolean parBreak;
        // tab - Add a tab stop (handy for constructing forms with labels followed by fields)
        public boolean tabStop;

        // hfill - Extend component horizontally
        // vfill - Extent component vertically (currently only one allowed)
        public boolean hFill;
        public boolean vFill;
        
        // left - Align following components to the left (default)
        // center - Align following components horizontally centered
        // right - Align following components to the right
        public int hAlign = HALIGN_DEFAULT;

        // vtop - Align following components vertically top aligned
        // vcenter - Align following components vertically centered (default) 
        public int vAlign = VALIGN_DEFAULT;

        /**
         * Creates a new instance of RiverLayoutSupportConstraints
         */
        public RiverLayoutSupportConstraints() {
        }

        public RiverLayoutSupportConstraints(RiverLayoutSupportConstraints prototype) {
            this (prototype.lineBreak, prototype.parBreak, prototype.tabStop,
                    prototype.hFill, prototype.vFill, prototype.hAlign, prototype.vAlign);
        }

        public RiverLayoutSupportConstraints(boolean lineBreak, boolean parBreak,
                boolean tabStop, boolean hFill, boolean vFill, int hAlign, int vAlign) {
            this.lineBreak = lineBreak;
            this.parBreak = parBreak;
            this.tabStop = tabStop;
            this.hFill = hFill;
            this.vFill = vFill;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
        }

        public void copyFrom(RiverLayoutSupportConstraints prototype) {
            this.lineBreak = prototype.lineBreak;
            this.parBreak = prototype.parBreak;
            this.tabStop = prototype.tabStop;
            this.hFill = prototype.hFill;
            this.vFill = prototype.vFill;
            this.hAlign = prototype.hAlign;
            this.vAlign = prototype.vAlign;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer(getClass().getName()).append("[");
            sb.append("lineBreak=").append(lineBreak).append(",");
            sb.append("parBreak=").append(parBreak).append(",");
            sb.append("tabStop=").append(tabStop).append(",");
            sb.append("hFill=").append(hFill).append(",");
            sb.append("vFill=").append(vFill).append(",");
            sb.append("hAlign=").append(hAlign).append(",");
            sb.append("vAlign=").append(vAlign).append("]");
            return sb.toString();
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                properties = createProperties();
                reinstateProperties();
            }
            return properties;
        }

        public Object getConstraintsObject() {
            return encodeConstraints(this);
        }

        public LayoutConstraints cloneConstraints() {
            return new RiverLayoutSupportConstraints(this);
        }

        protected Node.Property[] createProperties() {
            return new Node.Property[] {
                new FormProperty("RiverLayoutConstraints.constraints", // NOI18N
                        String.class,
                        getBundle().getString("PROP_constraints"), // NOI18N
                        getBundle().getString("HINT_constraints")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return encodeConstraints(RiverLayoutSupportConstraints.this);
                    }
                    public void setTargetValue(Object value) {
                        copyFrom(decodeConstraints((String) value));
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.lineBreak", // NOI18N
                        Boolean.class,
                        getBundle().getString("PROP_lineBreak"), // NOI18N
                        getBundle().getString("HINT_lineBreak")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return new Boolean(lineBreak);
                    }
                    public void setTargetValue(Object value) {
                        lineBreak = ((Boolean) value).booleanValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.parBreak", // NOI18N
                        Boolean.class,
                        getBundle().getString("PROP_parBreak"), // NOI18N
                        getBundle().getString("HINT_parBreak")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return new Boolean(parBreak);
                    }
                    public void setTargetValue(Object value) {
                        parBreak = ((Boolean) value).booleanValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.tabStop", // NOI18N
                        Boolean.class,
                        getBundle().getString("PROP_tabStop"), // NOI18N
                        getBundle().getString("HINT_tabStop")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return new Boolean(tabStop);
                    }
                    public void setTargetValue(Object value) {
                        tabStop = ((Boolean) value).booleanValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.hFill", // NOI18N
                        Boolean.class,
                        getBundle().getString("PROP_hFill"), // NOI18N
                        getBundle().getString("HINT_hFill")){ // NOI18N
                    
                    public Object getTargetValue() {
                        return new Boolean(hFill);
                    }
                    public void setTargetValue(Object value) {
                        hFill = ((Boolean) value).booleanValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.vFill", // NOI18N
                        Boolean.class,
                        getBundle().getString("PROP_vFill"), // NOI18N
                        getBundle().getString("HINT_vFill")){ // NOI18N
                    
                    public Object getTargetValue() {
                        return new Boolean(vFill);
                    }
                    public void setTargetValue(Object value) {
                        vFill = ((Boolean) value).booleanValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                },

                new FormProperty("RiverLayoutConstraints.hAlign", // NOI18N
                        Integer.class,
                        getBundle().getString("PROP_hAlign"), // NOI18N
                        getBundle().getString("HINT_hAlign")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return new Integer(hAlign);
                    }
                    public void setTargetValue(Object value) {
                        hAlign = ((Integer) value).intValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new HAlignEditor();
                    }
                },

                new FormProperty("RiverLayoutConstraints.vAlign", // NOI18N
                        Integer.class,
                        getBundle().getString("PROP_vAlign"), // NOI18N
                        getBundle().getString("HINT_vAlign")) { // NOI18N
                    
                    public Object getTargetValue() {
                        return new Integer(vAlign);
                    }
                    public void setTargetValue(Object value) {
                        vAlign = ((Integer) value).intValue();
                    }
                    public void setPropertyContext(FormPropertyContext ctx) {
                        // disabling this method due to limited persistence
                        // capabilities (compatibility with previous versions)
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new VAlignEditor();
                    }
                }
            };
        }

        private void reinstateProperties() {
            try {
                for (int i = 0; i < properties.length; i++) {
                    FormProperty prop = (FormProperty) properties[i];
                    prop.reinstateProperty();
                }
            }
            catch (IllegalAccessException e1) {} // should not happen
            catch (java.lang.reflect.InvocationTargetException e2) {} // should not happen
        }

        protected final CodeExpression[] createPropertyExpressions(CodeStructure codeStructure) {
            // first make sure properties are created...
            getProperties();
            
            // ...then create code expressions based on the properties
            CodeExpression expr = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0]));
            return new CodeExpression[]{expr};
        }

        protected final void readCodeExpression(CodeExpression constrExp, CodeGroup codeGroup) {
            // first make sure properties are created...
            getProperties();
            FormCodeSupport.readPropertyExpression(constrExp, properties[0], false);
        }

    }

}
