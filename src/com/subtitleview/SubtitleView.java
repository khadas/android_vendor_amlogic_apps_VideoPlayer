package com.subtitleview;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.subtitleparser.*;
import android.graphics.Matrix;
import android.graphics.Canvas;

public class SubtitleView extends TextView {
	private static final String LOG_TAG = SubtitleView.class.getSimpleName();
	private SubtitleFile subFile = null;
	private boolean needSubTitleShow= true;
	private Subtitle.SUBTYPE type=Subtitle.SUBTYPE.SUB_INVALID;
	private boolean InsubStatus=false;
	private Bitmap inter_bitmap = null;
	
	public void setInsubStatus(boolean flag)
	{
		InsubStatus=flag;
		
		if(InsubStatus)
		{
			setText("");
			subFile = null;
		}
	}
	

	public SubtitleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SubtitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SubtitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void setViewStatus(boolean flag )
	{
		needSubTitleShow=flag;
		if(flag==false)
		{
			setText("");
			setCompoundDrawablesWithIntrinsicBounds(null, null, null,null); 

		}
	}
	public void tick(int millisec) {

		if (needSubTitleShow==false) {
			return;
		}
		if(InsubStatus==true)
		{
			inter_bitmap=Subtitle.getBitmap(millisec);
			Log.i(LOG_TAG,
			"return bitmap is " + inter_bitmap);
			if(inter_bitmap!=null)
			{
				invalidate(); 
			}
			return;
		}
		
		if (subFile == null)
			return;
		SubtitleLine cur = subFile.curSubtitle();
		try {
			if (millisec >= cur.getBegin().getMilValue()
					&& millisec <= cur.getEnd().getMilValue()) {
				setText(subFile.curSubtitle().getText());
			} else {
				subFile.matchSubtitle(millisec);
				cur = subFile.curSubtitle();
				if (millisec > cur.getEnd().getMilValue()) {
					subFile.toNextSubtitle();
				}
				setText("");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) 
	{
		 super.onDraw(canvas); 
		 /*
		 if(mbmpTest!=null)
		 {
		     Rect rtSource = new Rect(0,0,320,240);
		     Rect rtDst = new Rect(0,0,320,240);
		     canvas.drawBitmap(mbmpTest, rtSource,rtDst, mPaint);
		 }
		*/
		
		Log.i(LOG_TAG,
				"start draw bitmap");
		 if(inter_bitmap!=null)
		 {
		   Matrix matrix = new Matrix();
           matrix.postScale(1.0f, 1.0f);
           //matrix.setRotate(90,120,120);
           canvas.drawBitmap(inter_bitmap, 0, 0, null);
           Log.i(LOG_TAG,
			"end draw bitmap ");
           inter_bitmap.recycle();
           inter_bitmap = null;
		 } 				 
    }

	public Subtitle.SUBTYPE setFile(String file, String enc) throws Exception {
		subFile = null;
		InsubStatus=false;
		// load Input File
		try {
		    Log.i("SubView", "------------parseSubtitleFile-----------" );
		    type=Subtitle.fileType(file);
		    if(type==Subtitle.SUBTYPE.SUB_IDXSUB)
		    {
		    
		    }
		    else if (type==Subtitle.SUBTYPE.SUB_INVALID) 
		    {
		    	subFile =null;
		    }
		    else
		    {
				subFile = Subtitle.parseSubtitleFile(file, enc);
	    	}
		} catch (Exception e) {
		    Log.i("SubView", "------------err-----------" );
			throw e;
		}

		Log.i(LOG_TAG,
				"Parsed: " + file + " total subtitle lins:" + subFile.size());
		return type;
	}

	public SubtitleFile getSubtitleFile() {
		return subFile;
	}

	public void reSet() {
		setText("");
		setCompoundDrawablesWithIntrinsicBounds(null, null, null,null); 
		if (subFile != null) {
			subFile.setCurSubtitleIndex(0);
		}
	}

	public void showPrevSubtitle() {
		if (subFile == null||InsubStatus==false) {
			return;
		}

		subFile.toPrevSubtitle();

		setText(subFile.curSubtitle().getText());
	}

	public void showNextSubtitle() {
		if (subFile == null||InsubStatus==false) {
			return;
		}

		subFile.toNextSubtitle();

		setText(subFile.curSubtitle().getText());
	}
}
