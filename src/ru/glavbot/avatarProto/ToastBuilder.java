package ru.glavbot.avatarProto;

import android.app.Activity;
import android.app.Application;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastBuilder {
	Activity owner;
	View toastWindow=null;
	TextView textView;
	ImageView iconView;
	
	public static final int ICON_NULL=-1;
	public static final int ICON_OK=1;
	public static final int ICON_WARN=2;
	public static final int ICON_ERROR=3;
	
	public static final int LENGTH_LONG = Toast.LENGTH_LONG;
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	
	
	
	public ToastBuilder(Activity owner)
	{
		this.owner=owner;
	}
	
	public void makeAndShowToast(int textResId, int errLvl, int length)
	{
		String text = owner.getString(textResId);
		makeAndShowToast(text,  errLvl,  length);
	}
	public void makeAndShowToast(String text, int errLvl, int length)
	{
		if(toastWindow==null)
		{
			init();
		}
		switch(errLvl)
		{
			//case ICON_NULL: iconView.setImageResource(R.drawable.ic_launcher);
			case ICON_OK: 
				iconView.setImageResource(R.drawable.icon_ok);
				break;
			case ICON_WARN: 
				iconView.setImageResource(R.drawable.icon_warning);
				break;
			case ICON_ERROR: 
				iconView.setImageResource(R.drawable.icon_error);
				break;
			default:
				iconView.setImageResource(R.drawable.ic_launcher);
		}
		
		
		
		textView.setText(text);

		Toast toast = new Toast(owner.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(length);
		toast.setView(toastWindow);
		toast.show();
	}
	
	
	
	private void init()
	{
		LayoutInflater inflater = owner.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
		                               (ViewGroup) owner.findViewById(R.id.toast_layout_root));

		iconView = (ImageView) layout.findViewById(R.id.imageViewErrLevel);
		//image.setImageResource(R.drawable.android);
		textView = (TextView) layout.findViewById(R.id.textViewText);

	}
}
