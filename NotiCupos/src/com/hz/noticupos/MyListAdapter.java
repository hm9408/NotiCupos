package com.hz.noticupos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("rawtypes")
public class MyListAdapter extends ArrayAdapter {

	private final Context context;
	private final ArrayList<Course> itemsArrayList;
	private int layoutResourceId;

	@SuppressWarnings("unchecked")
	public MyListAdapter(Context context, int row, ArrayList<Course> itemsArrayList) {

		super(context, row, itemsArrayList);

		this.context = context;
		this.layoutResourceId = row;
		this.itemsArrayList = itemsArrayList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		//        // 1. Create inflater 
		//        LayoutInflater inflater = (LayoutInflater) context
		//            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//
		//        // 2. Get rowView from inflater
		//        View rowView = inflater.inflate(R.layout.row, parent, false);
		//
		//        // 3. Get the two text view from the rowView

		if(convertView==null){
			// inflate the layout
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);
		}


		TextView txtCourseCode = (TextView) convertView.findViewById(R.id.txtCourseCode);
		TextView txtRemaining = (TextView) convertView.findViewById(R.id.txtRemaining);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitulo);
		TextView txtSection = (TextView) convertView.findViewById(R.id.txtSection);
		TextView txtCRN = (TextView) convertView.findViewById(R.id.txtCRN);
		TextView txtFecha = (TextView) convertView.findViewById(R.id.txtFecha);


		// 4. Set the text for textView 
		txtCourseCode.setText(itemsArrayList.get(position).getCod());
		txtRemaining.setText(context.getString(R.string.remaining)+" "+itemsArrayList.get(position).getDisponibles());
		txtTitle.setText(itemsArrayList.get(position).getTitulo());
		txtSection.setText(context.getString(R.string.section)+" "+itemsArrayList.get(position).getSeccion());
		txtCRN.setText(context.getString(R.string.add_crn)+" "+itemsArrayList.get(position).getCRN());
		txtFecha.setText(context.getString(R.string.last_update)+" "+lastUpdate());

		// 5. return rowView
		return convertView;
	}

	public String lastUpdate()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date d= new Date();
		return sdf.format(d);
	}

}
