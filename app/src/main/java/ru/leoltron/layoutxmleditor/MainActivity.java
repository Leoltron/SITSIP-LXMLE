package ru.leoltron.layoutxmleditor;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import ru.leoltron.layoutxmleditor.dialog.ClearConfirmDialog;
import ru.leoltron.layoutxmleditor.dialog.EditXMLDialog;
import ru.leoltron.layoutxmleditor.dialog.SelectViewDialog;
import ru.leoltron.layoutxmleditor.dialog.ViewPropertiesDialog;

public class MainActivity extends AppCompatActivity {

    public enum Mode{
        NORMAL(0),
        EDIT(1),
        EDITED(2);

        private int id;
        Mode(int i) {
            id = i;
        }
        public int getID(){
            return id;
        }
    }

    public static MainActivity instance;
    public static Mode mode = Mode.NORMAL;
    public static boolean isTreeViewMode;

    public static final String LOG_TAG = "LayoutXMLEditor";
    public static final String PREF_XML = "currentXML";

    public DialogFragment selectViewDialog;
    public ViewPropertiesDialog viewPropertiesDialog;
    public DialogFragment xmlViewDialog;
    public ClearConfirmDialog clearConfirmDialog;

    private SharedPreferences sPref;

    public static int selectedViewIdToCreate = -1;



    /**
     * Test XML String
     */
    public static final String base = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<LinearLayout\n" +
            " xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            " android:layout_width=\"match_parent\"\n" +
            " android:layout_height=\"match_parent\"\n" +
            " android:orientation=\"vertical\"\n> " +
            "<TextView\n" +
            "    android:layout_width=\"wrap_content\"\n" +
            "    android:layout_height=\"wrap_content\"\n" +
            "    android:text=\"Hello world!\"\n" +
            "    android:id=\"@+id/textView\"\n" +"/>"+
            "</LinearLayout>";

    public static FrameLayout layoutContainer;

    public static ScrollView treeScrollView;
    public static LinearLayout treeLinearLayout;

    public static UpperLayerView upperLayerView;

    public static String currentXML;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        instance = this;
        mode = Mode.NORMAL;
        isTreeViewMode = false;

        selectViewDialog = new SelectViewDialog();
        viewPropertiesDialog = new ViewPropertiesDialog();
        xmlViewDialog = new EditXMLDialog();
        clearConfirmDialog = new ClearConfirmDialog();

        upperLayerView = (UpperLayerView) findViewById(R.id.upperLayer);
        treeScrollView = (ScrollView) findViewById(R.id.treeScrollView);
        treeLinearLayout = (LinearLayout) findViewById(R.id.treeLL);
        layoutContainer = (FrameLayout) findViewById(R.id.layoutContainerFL);

        currentXML = loadXML();
        boolean error = false;
        try {
            ViewBuilder.buildLayoutContainer(this,layoutContainer,currentXML);
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }
        if(error){
            currentXML = base;
            try {
                ViewBuilder.buildLayoutContainer(this,layoutContainer,currentXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        upperLayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        upperLayerView.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveXML();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.groupFile, mode == Mode.NORMAL);
        menu.setGroupVisible(R.id.groupCancel, mode == Mode.EDIT || mode == Mode.EDITED);
        menu.setGroupVisible(R.id.groupConfirm, mode == Mode.EDITED);
        menu.setGroupVisible(R.id.groupScreenViewType, isTreeViewMode);
        menu.setGroupVisible(R.id.groupTreeViewType, !isTreeViewMode);
        onViewModeChange();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.action_new:
                clearConfirmDialog.show(getFragmentManager(),"clearConfirmDialog");
                break;
            case R.id.action_add:
                selectViewDialog.show(getFragmentManager(),"selectViewDialog");
                break;
            case R.id.action_view_xml:
                xmlViewDialog.show(getFragmentManager(),"xmlViewDialog");
                break;
            case R.id.action_cancel:
                setMode(Mode.NORMAL);
                break;
            case R.id.action_confirm:
                Log.d(LOG_TAG,upperLayerView.getSelectedView().toString());
                Log.d(LOG_TAG,String.valueOf(selectedViewIdToCreate));
                if(upperLayerView.getSelectedView() != null && upperLayerView.getSelectedView() instanceof ViewGroup && selectedViewIdToCreate >=0){
                    View v = ViewBuilder.getView(this, selectedViewIdToCreate);
                    v.setOnTouchListener(ViewBuilder.selectTouchListener);
                    v.setOnLongClickListener(ViewBuilder.showPropertiesListener);
                    ((ViewGroup)upperLayerView.getSelectedView()).addView(v);
                }
                setMode(Mode.NORMAL);
                if(isTreeViewMode)
                    buildTree();
                break;
            case R.id.action_tree_view_type:
            case R.id.action_screen_view_type:
                switchViewType();
                break;
        }
        return false;
    }

    public static void setMode(Mode mode){
        if(mode == Mode.NORMAL){
            upperLayerView.setSelectedAndFictionView(null);
            selectedViewIdToCreate = -1;
        }
        if(MainActivity.mode == Mode.NORMAL)
            upperLayerView.setSelectedAndFictionView(null);

        MainActivity.mode = mode;
        instance.invalidateOptionsMenu();
    }

    private static void switchViewType() {
        isTreeViewMode = !isTreeViewMode;
        onViewModeChange();
        instance.invalidateOptionsMenu();
    }

    public static void onViewModeChange(){
        treeScrollView.setVisibility(isTreeViewMode ? View.VISIBLE : View.GONE);
        layoutContainer.setVisibility(isTreeViewMode ? View.GONE : View.VISIBLE);
        if(isTreeViewMode)
            buildTree();
        else
            upperLayerView.setSelectedAndFictionView(upperLayerView.getSelectedView());
    }

    private static final String spacePerLayer = "   ";
    public static void buildTree(){
        treeLinearLayout.removeAllViews();
        int layer = 0;
        for(int i=0; i < layoutContainer.getChildCount(); i++){
            addToTree(treeLinearLayout,MainActivity.instance,layoutContainer.getChildAt(i),layer);
        }
    }

    public static void addToTree(LinearLayout container,Context context, View view, int layer){
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(view instanceof IRawAttributesContainer){
            IRawAttributesContainer rawAttributes = (IRawAttributesContainer) view;
            String id = rawAttributes.getRawAttributesMap().get("id");
            if(id == null)
                id = "";
            if(id.indexOf("/") >=0){
                id = id.substring(id.lastIndexOf("/")+1);
            }
            String pref = "";
            for(int i=0;  i < layer; i++)
                pref = pref + spacePerLayer;
            tv.setText(pref+rawAttributes.getName()+(id.length() > 0 ? " - "+id: ""));
        }
        tv.setTextColor(Color.BLACK);
        tv.setOnTouchListener(new ViewBuilder.FictionSelectTouchListener(view));
        tv.setOnLongClickListener(new ViewBuilder.FictionPropertiesListener(view));
        container.addView(tv);
        if(isTreeViewMode && view.equals(upperLayerView.getSelectedView()))
            upperLayerView.setFictionSelectedView(tv);

        if(view instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) view;
            for(int i=0;i < vg.getChildCount(); i++)
                addToTree(container,context,vg.getChildAt(i),layer+1);
        }
    }

    public void saveXML(){
        sPref = getPreferences(MODE_PRIVATE);
        try {
            currentXML = XMLWriter.toXMLString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = sPref.edit();
        if(!currentXML.equals(base))
            editor.putString(PREF_XML,currentXML);
        editor.commit();
    }

    public String loadXML(){
        final String none = "none";

        sPref = getPreferences(MODE_PRIVATE);
        String xml = sPref.getString(PREF_XML,none);
        if(xml.equals(none))
            return base;
        else
            return xml;
    }

}
