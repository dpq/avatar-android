package ru.glavbot.avatarProto;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;



public class CustomAutoCompleteTextView extends AutoCompleteTextView {

	public CustomAutoCompleteTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public CustomAutoCompleteTextView(Context context, AttributeSet attrs)
	{
		super(context,attrs);
	}
	public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context,attrs,defStyle);
	}
	@Override
	public boolean enoughToFilter()
	{
		boolean isEnough=(getThreshold()<=this.getText().length());
		
		if(isEnough)
		{
			if(this.getAdapter()!=null)
			{
				int itemsCount=0;
				int matchIndex=0;
				String txt = this.getText().toString();
				for (int i=0; i< this.getAdapter().getCount();i++) 
				{
					String dat = (String)this.getAdapter().getItem(i);
					if(dat.startsWith(txt))
					{
						itemsCount++;
						matchIndex=i;
					}
				}
				if(itemsCount == 1)
				{
					 if(((String)getAdapter().getItem(matchIndex)).equals(txt))
					 {
						 isEnough=false;
					 }
						 
				}
			}
		}
		return isEnough;
		
	}
	
	
}
