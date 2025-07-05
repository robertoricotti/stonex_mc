package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.List;

import utils.FullscreenActivity;
import utils.MyData;

public class CustomMenuLista {
    private Activity activity;
    private TextView title;
    private ListView listView;
    private String titolo;
    private ImageView chiudi;

    private ArrayAdapter<String> listAdapter;

    private Dialog alertDialog;
    public CustomMenuLista(Activity activity, String titolo) {
        this.activity = activity;
        this.titolo=titolo;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        listAdapter = new ArrayAdapter<>(activity, R.layout.simple_list_item);

    }
    public void show(List<String> listItems, final CustomMenu.OnItemSelectedListener itemSelectedListener) {
        alertDialog.create();
        alertDialog.setContentView(R.layout.layout_custom_menu_lista);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        if(Build.BRAND.equals("SRT8PROS")){
            alertDialog.getWindow().setLayout(1100, 650);}
        else {
            alertDialog.getWindow().setLayout(1000, 550);
        }

        FullscreenActivity.setFullScreen(alertDialog);
        alertDialog.show();
        findView();
        setupListView(listItems, itemSelectedListener);
    }
    private void findView(){
        title = alertDialog.findViewById(R.id.menuTitle);
        listView = alertDialog.findViewById(R.id.listView);
        chiudi=alertDialog.findViewById(R.id.chiudi);
        title.setText(String.valueOf(MyData.get_String("geoidPath")));

        chiudi.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

    }

    private void setupListView(List<String> listItems, final CustomMenu.OnItemSelectedListener itemSelectedListener) {
        listView.setAdapter(listAdapter);
        listAdapter.clear();
        listAdapter.addAll(listItems);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                String selectedItem = listAdapter.getItem(position);
                itemSelectedListener.onItemSelected(selectedItem); // Restituisci il valore selezionato
                alertDialog.dismiss(); // Chiudi la dialog
            }
        });
    }
    public interface OnItemSelectedListener {
        void onItemSelected(String selectedItem);
    }

}
