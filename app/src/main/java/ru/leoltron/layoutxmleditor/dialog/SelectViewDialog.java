package ru.leoltron.layoutxmleditor.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import ru.leoltron.layoutxmleditor.MainActivity;
import ru.leoltron.layoutxmleditor.R;
import ru.leoltron.layoutxmleditor.ViewBuilder;

public class SelectViewDialog extends DialogFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(R.string.select_view);
        View v = inflater.inflate(R.layout.list, null);
        ListView listView = (ListView) v.findViewById(R.id.palleteListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.instance,android.R.layout.simple_list_item_1, ViewBuilder.names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i < MainActivity.layoutContainer.getChildCount();i++){
                    View child = MainActivity.layoutContainer.getChildAt(i);
                    if(child instanceof ViewGroup){
                        View newChild = ViewBuilder.getView(MainActivity.instance, (int) id);
                        if(newChild != null){
                            newChild.setEnabled(!(newChild instanceof Button));
                            ((ViewGroup)child).addView(newChild);
                            return;
                        }
                    }
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.selectedViewIdToCreate = (int)id;
                MainActivity.instance.setMode(MainActivity.Mode.EDIT);
                Toast.makeText(MainActivity.instance,R.string.select_view_group,Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        return v;
    }
}
