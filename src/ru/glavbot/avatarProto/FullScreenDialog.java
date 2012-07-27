package ru.glavbot.avatarProto;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FullScreenDialog extends Dialog {

	Button DoSomethingButton;
	ImageView largeIcon;
	TextView description;
	View baseLayout;
	//Context context;
	
	
	// Шоб неповадно было!!!)))
	public abstract static class FullScreenDialogButtonListener implements View.OnClickListener
	{
		private FullScreenDialog fsd;

		public FullScreenDialogButtonListener(FullScreenDialog f)
		{
			fsd=f;
		}
	
		public final void onClick(View v) {
			
			doAction();
			fsd.dismiss();
		}
		
		public abstract void doAction();
		
		
	}
	
	
	public FullScreenDialog(Context context) {
		super(context,R.style.MyDialogStyleFS);

		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		baseLayout = inflater.inflate(R.layout.full_screen_dialog,
		                               (ViewGroup) findViewById(R.id.layout_fsd_root));

		description=(TextView)baseLayout.findViewById(R.id.textViewDescription);
		DoSomethingButton=(Button)baseLayout.findViewById(R.id.buttonAction);
		largeIcon=(ImageView)baseLayout.findViewById(R.id.imageViewIcon);
		
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);


		this.setContentView(baseLayout);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

	}

	
	
	public void setImage (int resId)
	{
		largeIcon.setBackgroundResource(resId);
	}
	
	public void setActionButton (int resId, FullScreenDialogButtonListener handler )
	{
		DoSomethingButton.setText(resId);
		DoSomethingButton.setOnClickListener(handler);
	}
	
	public void setActionButton (String text, View.OnClickListener handler )
	{
		DoSomethingButton.setText(text);
		DoSomethingButton.setOnClickListener(handler);
	}
	public void setDescription  (String text)
	{
		description.setText(text);
	}
	public void setDescription  (int resId)
	{
		description.setText(resId);
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
