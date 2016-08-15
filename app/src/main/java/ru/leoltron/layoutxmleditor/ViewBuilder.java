package ru.leoltron.layoutxmleditor;


import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ru.leoltron.layoutxmleditor.dialog.ViewPropertiesDialog;

import static ru.leoltron.layoutxmleditor.MainActivity.LOG_TAG;

/**
 * Здесь располагаются классы и билдеры дял создания усложненных видов View, содержащих атрибуты в сыром, легком для обработки и использования виде.
 */
public class ViewBuilder {
    public static final String[] names = {"LinearLayout","Button","TextView","EditText"};

    public static Map<String,String> getDefaultAttributesMap(){
        Map<String,String>  defaultAttributesMap = new HashMap<String,String>();
        defaultAttributesMap.put("layout_width","wrap_content");
        defaultAttributesMap.put("layout_height","wrap_content");
        defaultAttributesMap.put("id","+@id/id");

        return defaultAttributesMap;
    }

    public static class FictionSelectTouchListener implements View.OnTouchListener{

        private View view;

        public FictionSelectTouchListener(View view) {
            this.view = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN ){
                if( !((MainActivity.mode == MainActivity.Mode.EDITED || MainActivity.mode == MainActivity.Mode.EDIT) && !(view instanceof ViewGroup)) ){
                    MainActivity.upperLayerView.setSelectedView(view);
                    MainActivity.upperLayerView.setFictionSelectedView(v);
                    if(MainActivity.mode == MainActivity.Mode.EDIT)
                        MainActivity.setMode(MainActivity.Mode.EDITED);
                }
            }

            return false;
        }
    }

    public static class FictionPropertiesListener implements View.OnLongClickListener{

        private View view;

        public FictionPropertiesListener(View view) {
            this.view = view;
        }

        @Override
        public boolean onLongClick(View v) {
            MainActivity.instance.viewPropertiesDialog = new ViewPropertiesDialog();
            MainActivity.instance.viewPropertiesDialog.setChangeableView(view);
            MainActivity.instance.viewPropertiesDialog.show(MainActivity.instance.getFragmentManager(),"viewPropertiesDialog");
            return false;
        }
    }

    public static View.OnTouchListener selectTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN ){
                if(!((MainActivity.mode == MainActivity.Mode.EDITED || MainActivity.mode == MainActivity.Mode.EDIT) && !(v instanceof ViewGroup))){
                    MainActivity.upperLayerView.setSelectedAndFictionView(v);
                    if(MainActivity.mode == MainActivity.Mode.EDIT)
                        MainActivity.setMode(MainActivity.Mode.EDITED);
                }
            }

            return false;
        }
    };

    public static View.OnLongClickListener showPropertiesListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            MainActivity.instance.viewPropertiesDialog = new ViewPropertiesDialog();
            MainActivity.instance.viewPropertiesDialog.setChangeableView(v);
            MainActivity.instance.viewPropertiesDialog.show(MainActivity.instance.getFragmentManager(),"viewPropertiesDialog");
            return false;
        }
    };

    /**
     * Fills layoutContainer with view XML file contains
     */
    public static void buildLayoutContainer(Context context,ViewGroup layoutContainer, File f){
        try {
            Scanner sc = new Scanner(f);
            String s = "";
            while(sc.hasNext())
                s = s + sc.next();
            buildLayoutContainer(context,layoutContainer,s);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Fills layoutContainer with view XML string contains
     */
    public static void buildLayoutContainer(Context context,ViewGroup layoutContainer, String s) throws IOException, XmlPullParserException {
        View currentView = layoutContainer;
        layoutContainer.removeAllViews();
            XmlPullParser xpp = prepareXpp(s);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        Log.d(LOG_TAG,"START_DOCUMENT");
                        break;
                    case XmlPullParser.START_TAG:
                        Log.d(LOG_TAG,"START_TAG");
                        String name = xpp.getName();
                        Log.d(LOG_TAG,"Tag name: "+name);
                        if(currentView instanceof ViewGroup){
                            View child = ViewBuilder.getView(context,xpp.getName());
                            child.setOnTouchListener(selectTouchListener);
                            child.setOnLongClickListener(showPropertiesListener);
                            ((ViewGroup)currentView).addView(child);
                            applyAttributes(child,xpp);
                            currentView = child;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(LOG_TAG,"END_TAG");
                        try {
                            if (currentView.getParent() instanceof View)
                                currentView = (View) currentView.getParent();
                        }catch(NullPointerException e){
                            e.printStackTrace();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    default:
                        break;
                }
                xpp.next();
            }

    }

    /**
     * Parses string into XmlPullParser
     * @throws XmlPullParserException
     */
    private static XmlPullParser prepareXpp(String s) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(s));
        return xpp;
    }


    /**
     * @param xpp
     * @return View depending on tag name and attributes inside
     */
    private static View getView(XmlPullParser xpp,Context context){
        View view = ViewBuilder.getView(context,xpp.getName());
        applyAttributes(view,xpp);
        return view;
    }

    /** Manually apply attributes to View
     * @param view View to apply attributes on
     * @param xpp XmlPullParser contains attributes
     * @throws NumberFormatException
     */
    private static void applyAttributes(View view, XmlPullParser xpp) throws NumberFormatException{
        HashMap<String, String> attributes = new HashMap<>();
        for(int i=0; i < xpp.getAttributeCount(); i++)
            attributes.put(xpp.getAttributeName(i),xpp.getAttributeValue(i));

        applyAttributes(view,attributes);
    }

    /**
     * Applies attributes from ac to view
     * @param view view attributes applied on
     * @param ac attributes container, may be view itself
     * @throws NumberFormatException
     */
    public static void applyAttributes(View view,IRawAttributesContainer ac) throws NumberFormatException{
        Log.d(LOG_TAG,"Reading and applying attributes from IRawAttributesContainer...");
        applyAttributes(view,ac.getRawAttributesMap());
    }

    public static void applyAttributes(View view, Map<String,String> attributesMap) throws NumberFormatException{
        Log.d(LOG_TAG,"Reading and applying attributes from attributesMap...");
        String name,value;

        boolean isButton = view instanceof Button;
        boolean isTextView = view instanceof TextView;

        boolean isIRawAttributesContainer = view instanceof IRawAttributesContainer;

        Map<String,String> rawAttributeMap = new HashMap<>();
        if(isIRawAttributesContainer)
            rawAttributeMap = ((IRawAttributesContainer)view).getRawAttributesMap();


        //Размеры View, будут применены позже
        int width = 0;
        int height = 0;
        float weight = 0;

        for(Map.Entry<String,String> entry: attributesMap.entrySet()){
            name = entry.getKey();
            value = entry.getValue();

            if(name.equals("layout_height")) {
                if (value.equalsIgnoreCase("MATCH_PARENT"))
                    height = ViewGroup.LayoutParams.MATCH_PARENT;
                else if (value.equalsIgnoreCase("WRAP_CONTENT"))
                    height = ViewGroup.LayoutParams.WRAP_CONTENT;
                else
                    height = getSize(view.getContext(),value);

            }else if(name.equals("layout_width")) {
                if (value.equalsIgnoreCase("MATCH_PARENT"))
                    width = ViewGroup.LayoutParams.MATCH_PARENT;
                else if (value.equalsIgnoreCase("WRAP_CONTENT"))
                    width = ViewGroup.LayoutParams.WRAP_CONTENT;
                else
                    width = getSize(view.getContext(),value);

            }else if (name.equals("layout_weight")){
                weight = Float.parseFloat(value);
            }else if(name.equals("id")) {
                int id = getResourceId(value, R.id.class);
                if (id != -1)
                    view.setId(id);
            }else if (name.equals("text")){
                if(isTextView)
                    ((TextView)view).setText(value);
                else if(isButton)
                    ((Button)view).setText(value);
            }else if (name.equals("textAppearance")) { //Скорее всего, не будет применено
                int id = getResourceId(value, R.attr.class);
                if (id != -1)
                    setTextAppearance(view, view.getContext(), id);
            }else if (name.equals("hint")){
                if(view instanceof EditText)
                    ((EditText)view).setHint(value);
            }else if (name.equals("background")){ //TODO:В данный момент не работает
//                int id = getResourceId(value, R.drawable.class);
                try {
//                    if (id == -1)
                        view.setBackgroundColor(Integer.decode(value));
//                    else
//                        view.setBackgroundResource(id);
                }catch(NumberFormatException e){
                    Log.e(LOG_TAG,e.getMessage());
                }
            }else if (name.equals("orientation") && view instanceof LinearLayout)
                ((LinearLayout)view).setOrientation(value.equalsIgnoreCase("HORIZONTAL") ? LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);


            if(isIRawAttributesContainer) rawAttributeMap.put(name,value);
        }

        if(view.getParent() != null)
            Log.d(MainActivity.LOG_TAG,view.getParent().toString());

        if(view.getParent() != null && view.getParent() instanceof LinearLayout)
            view.setLayoutParams(new LinearLayout.LayoutParams(width,height,weight));
//        else if(view.getParent() != null)
//            view.setLayoutParams(new ViewGroup.LayoutParams(width,height));

        if(isIRawAttributesContainer){
            ((IRawAttributesContainer)view).setRawAttributesMap(rawAttributeMap);
        }
    }

    @Nullable
    public static View getView(Context context, String name){
        for(int i=0; i  < names.length; i++)
            if(names[i].equals(name)) return getView(context,i);

        return null;
    }

    public static View getView(Context context, int id){
        View r = null;
        Map<String,String> map;
        switch(id){
            case 1:
                r = new RawAttributesButton(context);
                map = getDefaultAttributesMap();
                r.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                ((Button)r).setText("New Button");
                map.put("text","New Button");
                ((IRawAttributesContainer)r).setRawAttributesMap(map);
                break;
            case 2:
                r = new RawAttributesTextView(context);
                map = getDefaultAttributesMap();
                r.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                ((TextView)r).setText("New TextView");
                map.put("text","New TextView");
                ((IRawAttributesContainer)r).setRawAttributesMap(map);
                break;
            case 0:
                r = new RawAttributesLinearLayout(context);
                map = getDefaultAttributesMap();
                map.put("layout_width","match_parent");
                map.put("layout_height","match_parent");
                map.put("orientation","vertical");
                ((RawAttributesLinearLayout)r).setOrientation(LinearLayout.VERTICAL);
                r.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                ((IRawAttributesContainer)r).setRawAttributesMap(map);
                break;
            case 3:
                r = new RawAttributesEditText(context);
                map = getDefaultAttributesMap();
                map.put("hint","");
                map.put("layout_width","match_parent");
                ((IRawAttributesContainer)r).setRawAttributesMap(map);
                r.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
        }
        return r;
    }

    /**
     * @param resName resource name in file
     * @param c class- resource container, R.drawable, for example
     * @return Resource id if such exists in c, -1 otherwise
     */
    public static int getResourceId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets text appearance
     */
    public static void setTextAppearance(View view,Context context, int resId){
        if (Build.VERSION.SDK_INT < 23) {
            if(view instanceof TextView)
                ((TextView)view).setTextAppearance(context, resId);
            else if(view instanceof Button)
                ((Button)view).setTextAppearance(context, resId);
        } else {
            if(view instanceof TextView)
                ((TextView)view).setTextAppearance(resId);
            else if(view instanceof Button)
                ((Button) view).setTextAppearance(resId);

        }
    }

    public static int getSize(Context context, String s) throws NumberFormatException{
        boolean isDP = s.substring(s.length()-2).equalsIgnoreCase("dp");
        int size = Integer.valueOf(s.substring(0,s.length()-2));
        if(isDP)
            size = (int) (size * context.getResources().getDisplayMetrics().density+0.5f);
        return size;
    }

    public static class RawAttributesButton extends Button implements IRawAttributesContainer{
        private  Map<String,String> map;

        public RawAttributesButton(Context context) {
            super(context);
        }

        public RawAttributesButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RawAttributesButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public String getName() {
            return "Button";
        }

        public void setRawAttributesMap(Map<String,String> map){
            this.map = map;
        }

        @Override
        public  Map<String,String> getRawAttributesMap() {
            return map;
        }
    }

    public static class RawAttributesTextView extends TextView implements IRawAttributesContainer{
        private Map<String,String> map;

        public RawAttributesTextView(Context context) {
            super(context);
        }

        public RawAttributesTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RawAttributesTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public String getName() {
            return "TextView";
        }

        public void setRawAttributesMap( Map<String,String> map){
            this.map = map;
        }

        @Override
        public  Map<String,String> getRawAttributesMap() {
            return map;
        }
    }

    public static class RawAttributesLinearLayout extends LinearLayout implements IRawAttributesContainer{
        private Map<String,String> map;

        public RawAttributesLinearLayout(Context context) {
            super(context);
        }

        public RawAttributesLinearLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RawAttributesLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public String getName() {
            return "LinearLayout";
        }

        public void setRawAttributesMap(Map<String,String> map){
            this.map = map;
        }

        @Override
        public Map<String,String> getRawAttributesMap() {
            return map;
        }
    }

    public static class RawAttributesEditText extends EditText implements IRawAttributesContainer{
        private Map<String,String> map;

        public RawAttributesEditText(Context context) {
            super(context);
        }

        public RawAttributesEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RawAttributesEditText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public String getName() {
            return "EditText";
        }

        public void setRawAttributesMap(Map<String,String> map){
            this.map = map;
        }

        @Override
        public Map<String,String> getRawAttributesMap() {
            return map;
        }
    }
}
