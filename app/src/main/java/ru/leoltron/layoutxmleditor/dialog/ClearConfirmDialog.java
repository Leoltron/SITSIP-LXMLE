package ru.leoltron.layoutxmleditor.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import ru.leoltron.layoutxmleditor.MainActivity;
import ru.leoltron.layoutxmleditor.R;
import ru.leoltron.layoutxmleditor.ViewBuilder;

public class ClearConfirmDialog extends DialogFragment implements DialogInterface.OnClickListener {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.no, this)
                .setMessage(R.string.confirm_clear);
        return adb.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                MainActivity.upperLayerView.setSelectedAndFictionView(null);
                Toast.makeText(MainActivity.instance,R.string.layoutCleared,Toast.LENGTH_SHORT).show();
                try {
                    ViewBuilder.buildLayoutContainer(MainActivity.instance,MainActivity.layoutContainer,MainActivity.base);
                    if(MainActivity.isTreeViewMode)
                        MainActivity.buildTree();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dismiss();
                break;
            case Dialog.BUTTON_NEGATIVE:
                dismiss();
                break;
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
