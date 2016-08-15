package ru.leoltron.layoutxmleditor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import ru.leoltron.layoutxmleditor.MainActivity;
import ru.leoltron.layoutxmleditor.R;
import ru.leoltron.layoutxmleditor.ViewBuilder;
import ru.leoltron.layoutxmleditor.XMLWriter;

public class EditXMLDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public EditText xmlEditText;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        xmlEditText = new EditText(this.getActivity()) {
            public boolean isSuggestionsEnabled() {
                return false;
            }
        };
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle("XML")
                .setPositiveButton(R.string.apply,this)
                .setNeutralButton(R.string.cancel,this).setView(xmlEditText);

        try {
            xmlEditText.setText(XMLWriter.toXMLString());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return adb.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                boolean ok = true;
                try{
                    ViewBuilder.buildLayoutContainer(MainActivity.instance,MainActivity.layoutContainer,xmlEditText.getText().toString());
                    if(MainActivity.isTreeViewMode)
                        MainActivity.buildTree();
                } catch (Exception e) {
                    Toast.makeText(this.getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    ok = false;
                    e.printStackTrace();
                }
                if(ok){
                    MainActivity.currentXML = xmlEditText.getText().toString();
                    dismiss();
                }else{
                    try {
                        ViewBuilder.buildLayoutContainer(MainActivity.instance,MainActivity.layoutContainer,MainActivity.currentXML);
                        if(MainActivity.isTreeViewMode)
                            MainActivity.buildTree();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case Dialog.BUTTON_NEUTRAL:
                dismiss();
                break;
        }
    }
}
