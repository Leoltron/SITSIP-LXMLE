package ru.leoltron.layoutxmleditor.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.leoltron.layoutxmleditor.IRawAttributesContainer;
import ru.leoltron.layoutxmleditor.MainActivity;
import ru.leoltron.layoutxmleditor.R;
import ru.leoltron.layoutxmleditor.ViewBuilder;

public class ViewPropertiesDialog extends DialogFragment implements View.OnClickListener {

    public static ViewGroup containerView;

    public LinearLayout propertiesLL;

    public LinearLayout layoutWidthLL;
    public LinearLayout customLayoutWidthLL;
    public RadioButton widthMatchParentRB;
    public RadioButton widthWrapContentRB;
    public RadioButton widthCustomSizeRB;

    public RadioButton widthCustomSizeDPRB;
    public RadioButton widthCustomSizePXRB;
    public EditText widthCustomSizeValue;


    public LinearLayout layoutHeightLL;
    public LinearLayout customLayoutHeightLL;
    public RadioButton heightMatchParentRB;
    public RadioButton heightWrapContentRB;
    public RadioButton heightCustomSizeRB;

    public RadioButton heightCustomSizeDPRB;
    public RadioButton heightCustomSizePXRB;
    public EditText heightCustomSizeValue;

    public Button applyButton;
    public Button cancelButton;
    public Button deleteButton;

    public View getChangeableView() {
        return changeableView;
    }

    public void setChangeableView(View view) {
        this.changeableView = view;
    }

    private View changeableView;

    private List<EditText> attributesETList;
    private Map<String,String> attributes;

    private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateVisibility();
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(MainActivity.LOG_TAG,"ViewPropertiesDialog onCreateView called");
        String prefix =  changeableView instanceof IRawAttributesContainer ? ((IRawAttributesContainer)changeableView).getName()+" ":"";
        getDialog().setTitle(prefix+getResources().getString(R.string.properties));

        containerView = (ViewGroup)inflater.inflate(R.layout.view_properties, null);

        propertiesLL = (LinearLayout) containerView.findViewById(R.id.propertiesLL);

        layoutWidthLL = (LinearLayout) containerView.findViewById(R.id.LayoutParamsWidthLL);
        customLayoutWidthLL = (LinearLayout) containerView.findViewById(R.id.widthCustomSizeLL);
        widthMatchParentRB = (RadioButton) containerView.findViewById(R.id.widthMatchParentRB);
        widthMatchParentRB.setOnCheckedChangeListener(onCheckedChangeListener);
        widthWrapContentRB = (RadioButton) containerView.findViewById(R.id.widthWrapContentRB);
        widthWrapContentRB.setOnCheckedChangeListener(onCheckedChangeListener);
        widthCustomSizeRB = (RadioButton) containerView.findViewById(R.id.widthCustomSizeRB);
        widthCustomSizeRB.setOnCheckedChangeListener(onCheckedChangeListener);

        widthCustomSizeDPRB = (RadioButton) containerView.findViewById(R.id.widthDPRB);
        widthCustomSizePXRB = (RadioButton) containerView.findViewById(R.id.widthPXRB);
        widthCustomSizeValue = (EditText) containerView.findViewById(R.id.widthEditText);


        layoutHeightLL = (LinearLayout) containerView.findViewById(R.id.LayoutParamsHeightLL);
        customLayoutHeightLL = (LinearLayout) containerView.findViewById(R.id.heightCustomSizeLL);
        heightMatchParentRB = (RadioButton) containerView.findViewById(R.id.heightMatchParentRB);
        heightMatchParentRB.setOnCheckedChangeListener(onCheckedChangeListener);
        heightWrapContentRB = (RadioButton) containerView.findViewById(R.id.heightWrapContentRB);
        heightWrapContentRB.setOnCheckedChangeListener(onCheckedChangeListener);
        heightCustomSizeRB = (RadioButton) containerView.findViewById(R.id.heightCustomSizeRB);
        heightCustomSizeRB.setOnCheckedChangeListener(onCheckedChangeListener);

        heightCustomSizeDPRB = (RadioButton) containerView.findViewById(R.id.heightDPRB);
        heightCustomSizePXRB = (RadioButton) containerView.findViewById(R.id.heightPXRB);
        heightCustomSizeValue = (EditText) containerView.findViewById(R.id.heightEditText);

        applyButton = (Button) containerView.findViewById(R.id.applyVPButton);
        applyButton.setOnClickListener(this);
        cancelButton = (Button) containerView.findViewById(R.id.cancelVPButton);
        cancelButton.setOnClickListener(this);
        deleteButton = (Button) containerView.findViewById(R.id.deleteViewButton);
        deleteButton.setOnClickListener(this);

        if(changeableView.getParent() != null && changeableView.getParent().equals(MainActivity.layoutContainer))
            deleteButton.setVisibility(View.GONE);

        resetPropertiesListView();

        return containerView;
    }

    public void resetPropertiesListView(){

        String value;

        if(changeableView instanceof IRawAttributesContainer)
            attributes = new HashMap<>(((IRawAttributesContainer) changeableView).getRawAttributesMap());
        else
            attributes = new HashMap<>();

        if(changeableView.getParent() != null && changeableView.getParent() instanceof LinearLayout && !attributes.containsKey("layout_weight"))
            attributes.put("layout_weight","0");

        attributesETList = new ArrayList<>();

        if(!(changeableView.getParent() != null && changeableView.getParent() instanceof LinearLayout)){
            layoutWidthLL.setVisibility(View.GONE);
            layoutHeightLL.setVisibility(View.GONE);
        }else{
            layoutWidthLL.setVisibility(View.VISIBLE);
            layoutHeightLL.setVisibility(View.VISIBLE);
        }

        for(Map.Entry<String,String> entry: attributes.entrySet()) {
            Log.d(MainActivity.LOG_TAG,entry.getKey()+" "+entry.getValue());

            if (entry.getKey().equals("layout_height")){
                value = entry.getValue();
                if(value != null && value.length() > 0){
                    heightMatchParentRB.setChecked(value.equalsIgnoreCase("match_parent"));
                    heightWrapContentRB.setChecked(value.equalsIgnoreCase("wrap_content"));
                    if(!(value.equalsIgnoreCase("match_parent") || value.equalsIgnoreCase("wrap_content"))){
                        heightCustomSizeRB.setChecked(true);
                        String postfix = value.substring(value.length()-2);
                        if(postfix.equals("px"))
                            heightCustomSizePXRB.setChecked(true);
                        else
                            heightCustomSizeDPRB.setChecked(true);

                        if(!(postfix.equals("px") || postfix.equals("dp")))
                            heightCustomSizeValue.setText(value);
                        else
                            heightCustomSizeValue.setText(value.substring(0,value.length()-2));
                    }
                }
                updateVisibility();
                continue;
            }else if(entry.getKey().equals("layout_width")){
                value = entry.getValue();
                if(value != null && value.length() > 0) {
                    widthMatchParentRB.setChecked(value.equalsIgnoreCase("match_parent"));
                    widthWrapContentRB.setChecked(value.equalsIgnoreCase("wrap_content"));
                    if (!(value.equalsIgnoreCase("match_parent") || value.equalsIgnoreCase("wrap_content"))) {
                        widthCustomSizeRB.setChecked(true);
                        String postfix = value.substring(value.length() - 2);
                        if (postfix.equals("px"))
                            widthCustomSizePXRB.setChecked(true);
                        else
                            widthCustomSizeDPRB.setChecked(true);

                        if (!(postfix.equals("px") || postfix.equals("dp")))
                            widthCustomSizeValue.setText(value);
                        else
                            widthCustomSizeValue.setText(value.substring(0, value.length() - 2));
                    }
                }
                updateVisibility();
                continue;
            }

            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llParams.setMargins(5, 0, 5, 0);
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setLayoutParams(llParams);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setText(entry.getKey());
            ll.addView(tv);
            EditText et = new EditText(getActivity()) {
                public boolean isSuggestionsEnabled() {
                    return false;
                }
            };
            et.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f));
            et.setHint(entry.getKey());
            et.setText(entry.getValue());
            ll.addView(et);
            attributesETList.add(et);

            propertiesLL.addView(ll);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.applyVPButton:

                for(EditText et : attributesETList){
                    attributes.put(et.getHint().toString(),et.getText().toString());
                }

                if((changeableView.getParent() != null && changeableView.getParent() instanceof LinearLayout)) {
                    //Сохраняем ширину View
                    if (widthMatchParentRB.isChecked())
                        attributes.put("layout_width", "match_parent");
                    else if (widthWrapContentRB.isChecked())
                        attributes.put("layout_width", "wrap_content");
                    else if (widthCustomSizeRB.isChecked()) {
                        int size;
                        try {
                            size = widthCustomSizeValue.getText().length() > 0 ? Integer.parseInt(widthCustomSizeValue.getText().toString()) : 0;
                        } catch (Exception e) {
                            size = 0;
                        }
                        if (widthCustomSizePXRB.isChecked())
                            attributes.put("layout_width", String.valueOf(size) + "px");
                        else
                            attributes.put("layout_width", String.valueOf(size) + "dp");
                    }
                    //Сохраняем высоту View
                    if (heightMatchParentRB.isChecked())
                        attributes.put("layout_height", "match_parent");
                    else if (heightWrapContentRB.isChecked())
                        attributes.put("layout_height", "wrap_content");
                    else if (heightCustomSizeRB.isChecked()) {
                        int size;
                        try {
                            size = heightCustomSizeValue.getText().length() > 0 ? Integer.parseInt(heightCustomSizeValue.getText().toString()) : 0;
                        } catch (Exception e) {
                            size = 0;
                        }
                        if (heightCustomSizePXRB.isChecked())
                            attributes.put("layout_height", String.valueOf(size) + "px");
                        else
                            attributes.put("layout_height", String.valueOf(size) + "dp");
                    }
                }

                if(changeableView instanceof IRawAttributesContainer){
                    ((IRawAttributesContainer) changeableView).setRawAttributesMap(attributes);
                    ViewBuilder.applyAttributes(changeableView,((IRawAttributesContainer) changeableView));
                }
                if(MainActivity.isTreeViewMode)
                    MainActivity.buildTree();
                dismiss();
                break;
            case R.id.cancelVPButton:
                dismiss();
                break;
            case R.id.deleteViewButton:
                if(changeableView.getParent() instanceof ViewGroup)
                    ((ViewGroup) changeableView.getParent()).removeView(changeableView);
                MainActivity.upperLayerView.setSelectedAndFictionView(null);
                if(MainActivity.isTreeViewMode)
                    MainActivity.buildTree();
                dismiss();
                break;
        }
    }

    public void updateVisibility(){
        try {
            customLayoutWidthLL.setVisibility(widthCustomSizeRB.isChecked() ? View.VISIBLE : View.GONE);
            customLayoutHeightLL.setVisibility(heightCustomSizeRB.isChecked() ? View.VISIBLE : View.GONE);
        }catch(NullPointerException e){
            Log.e(MainActivity.LOG_TAG,e.getMessage());
        }
    }
}
