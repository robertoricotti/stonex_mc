package gui.dialogs_and_toast;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stx_dig.R;
public class CustomToast {
    Activity activity;
    String msg;


    public CustomToast(Activity activity, String msg){
        this.activity = activity;
        this.msg = msg;
    }
    public void show() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 150);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    public void show_long() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast_2, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 150);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    public void show_alert() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast_w, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -150);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    public void show_error() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast_e, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void show_added() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast_2, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    public void show_removed() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_toast_e, (ViewGroup) activity.findViewById(R.id.custom_toast_layout));
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setText(msg);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}