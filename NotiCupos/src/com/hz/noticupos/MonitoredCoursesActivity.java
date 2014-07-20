package com.hz.noticupos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MonitoredCoursesActivity extends ListActivity implements OnRefreshListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -639702177724593006L;
	private SwipeRefreshLayout swipeLayout;
	private ArrayList<Course> monitored;
	private ListView myList;
	private MyListAdapter myListAdapter;
	private transient Context context;
	public static final String FILE = "noticupos.data";
	//	private NotificationService notifServ;
	private int freqUpdate;
	private String updateTime;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(255, 179, 0)));
		bar.setTitle(Html.fromHtml("<font color='#4F4F4F'>NotiCupos</font>"));
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_main);
		loadState();
		if (monitored==null) {
			monitored = new ArrayList<Course>();
		}
		loadPreferences();

		myList=(ListView)findViewById(android.R.id.list);
		myListAdapter = new MyListAdapter(context, R.layout.row, monitored);
		View empty = findViewById(android.R.id.empty);
		myList.setEmptyView(empty);
		myList.setAdapter(myListAdapter);
		myList.setLongClickable(true);
		registerForContextMenu(myList);
		myListAdapter.notifyDataSetChanged();
		SwipeDismissListViewTouchListener touchListener =
				new SwipeDismissListViewTouchListener(
						myList,
						new SwipeDismissListViewTouchListener.DismissCallbacks() {
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {
								for (int position : reverseSortedPositions) {
									Course obj = (Course) myList.getItemAtPosition(position);
									removeCourse(obj);
								}
								
							}

							@Override
							public boolean canDismiss(int position) {
								// TODO Auto-generated method stub
								return true;
							}
						});
		myList.setOnTouchListener(touchListener);
		myList.setOnScrollListener(touchListener.makeScrollListener());

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);
	}	

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
				//Refresh all courses in the list
				if (monitored.size()!=0) {
					for (int i = 0; i < monitored.size(); i++) {
						Course c = monitored.get(i);
						new SearchCourseTask(context).execute(c.getDepto(), c.getCRN(),""+i);
						System.out.println(i);
					}
				}
				swipeLayout.setRefreshing(false);
			}
		}, 10);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			final Dialog settingsDialog = new Dialog(this);
			settingsDialog.setContentView(R.layout.activity_settings);
			settingsDialog.setTitle(R.string.notif_freq);
			final NumberPicker numFreq = (NumberPicker) settingsDialog.findViewById(R.id.numFreq);
			final Spinner spinTime = (Spinner) settingsDialog.findViewById(R.id.spinnerTime);
			ArrayAdapter myAdap = (ArrayAdapter) spinTime.getAdapter();
			spinTime.setSelection(myAdap.getPosition(updateTime));
			String[] nums = new String[46];
			for(int i=0; i<nums.length; i++)
			       nums[i] = Integer.toString(i);
			numFreq.setMaxValue(45);
			numFreq.setMinValue(0);
			numFreq.setDisplayedValues(nums);
			numFreq.setWrapSelectorWheel(false);
			numFreq.setValue(freqUpdate);
			
			Button butSaveSettings = (Button) settingsDialog.findViewById(R.id.butSaveSettings);
			// if button is clicked, close the custom dialog
			butSaveSettings.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					System.out.println("Num: "+numFreq.getValue());
					System.out.println("Text: "+spinTime.getSelectedItem().toString());
					if (spinTime.getSelectedItem().toString().equals("minutos") && numFreq.getValue() < 15)
						Toast.makeText(context, "La frecuencia de actualización no puede ser tan alta."
								+ "\nIngrese un valor mayor a 15 minutos.", Toast.LENGTH_LONG).show();
					else if (numFreq.getValue()<0) Toast.makeText(context, "Ingrese un valor mayor a 0.", Toast.LENGTH_LONG).show(); 
					else
					{
						setFreqUpdate(numFreq.getValue());
						setUpdateTime(spinTime.getSelectedItem().toString());
						savePreferences();
						settingsDialog.dismiss();
					}
				}
			});	 
			settingsDialog.show();
			return true;

		case R.id.action_addCourse:
			final Dialog addCourseDialog = new Dialog(this);
			addCourseDialog.setContentView(R.layout.activity_add_course);
			addCourseDialog.setTitle("Agregar curso");
			final TextView txtCRN = (TextView) addCourseDialog.findViewById(R.id.txtNotificationFreq);
			final Spinner spinDptos = (Spinner) addCourseDialog.findViewById(R.id.spinnerDeptos);
			spinDptos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					String item = parent.getItemAtPosition(pos).toString();
				}
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			final EditText editCRN = (EditText) addCourseDialog.findViewById(R.id.editTimeFreq);
			Button dialogButton = (Button) addCourseDialog.findViewById(R.id.butSaveSettings);

			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String departamento = spinDptos.getSelectedItem().toString().substring(0, 4);
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!! "+departamento);
					if (editCRN.getText().toString().length()<5) {
						Toast.makeText(context, "El CRN debe tener 5 caracteres.\nVerifique los datos e intente nuevamente.", 
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						addCourseCRN(editCRN.getText().toString(), departamento);
						addCourseDialog.dismiss();
						myListAdapter.notifyDataSetChanged();
					}
				}
			});	 
			addCourseDialog.show();
			return true;


		case R.id.action_about:
			AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));


			LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_about,null);
			builder.setView(layout);

			builder.setTitle(R.string.about);
			builder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("SimpleDateFormat")
	public String updateLastUpdate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date d= new Date();
		return sdf.format(d);
	}

	public void addCourseCRN(String CRN, String depto)
	{
		boolean existe = false;
		for (int i = 0; i < monitored.size() && !existe; i++) {
			Course temp = monitored.get(i);
			if (temp.getCRN().equals(CRN)) {
				existe = true;
				Toast.makeText(context, "Ya has agregado el curso "+temp.getTitulo()+" a tu lista.", Toast.LENGTH_LONG).show();
			}
		}
		if (!existe) {
			//starts the AsyncTask with an index of -1 so it knows it doesn't exist in the array.
			new SearchCourseTask(context).execute(depto, CRN,"-1");			
		}
	}

	public void removeCourse(Course c)
	{
		String titulo = c.getTitulo();
		monitored.remove(c);
		myListAdapter.notifyDataSetChanged();
		Toast.makeText(context, "El curso \""+titulo+"\" se eliminó correctamente", Toast.LENGTH_SHORT).show();
		saveState();
	}

	public void saveState(){
		try {
			FileOutputStream fos = openFileOutput(FILE, MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(monitored);
			oos.close();
			fos.close();				
			System.out.println("Saved "+monitored.size()+" courses into the app.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public void loadState(){
		try {
			FileInputStream fis = openFileInput(FILE);
			ObjectInputStream ois = new ObjectInputStream(fis);
			monitored = (ArrayList<Course>) ois.readObject();
			ois.close();
			fis.close();
			if (monitored!=null) {
				System.out.println("Loaded "+monitored.size()+" courses into the app.");
				Toast.makeText(context, "Se cargaron "+monitored.size()+" cursos.", Toast.LENGTH_SHORT).show();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void savePreferences()
	{
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("Frecuencia de actualización", freqUpdate);
		editor.putString("Tiempo de actualización", updateTime);
		editor.commit();
		Toast.makeText(context, "Se actualizarán sus cursos cada "+freqUpdate+" "+updateTime, Toast.LENGTH_LONG).show();
	}
	
	public void loadPreferences()
	{
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		freqUpdate = sharedPref.getInt("Frecuencia de actualización", 0);
		updateTime = sharedPref.getString("Tiempo de actualización", "minutos");
	}

	public int getFreqUpdate() {
		return freqUpdate;
	}

	public void setFreqUpdate(int freqUpdate) {
		this.freqUpdate = freqUpdate;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_main);
		myListAdapter.notifyDataSetChanged();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////ASYNCTASK CLASS//////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	class SearchCourseTask extends AsyncTask<String, Void, Course> {

		ProgressDialog progDailog;

		private Context context;
		
		private int index;

		public SearchCourseTask(Context c)
		{
			context = c;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDailog = ProgressDialog.show(context, "Cursos", "Buscando cursos...", false, false);
			progDailog.show();
		}

		@Override
		protected Course doInBackground(String... params) {
			Course searched = null;
			Document doc;
			String depto = params[0].substring(0,4);
			String CRN = params[1];
			index = Integer.parseInt(params[2]);
			try {
				doc = Jsoup
						.connect("http://registroapps.uniandes.edu.co/scripts/adm_con_horario1_joomla.php?depto="+depto)
						.timeout(20000)
						.get();

				Elements links = doc.select("font:containsOwn("+CRN+")");
				if(links!=null && !links.isEmpty())
				{
					Elements tds = links.parents().first().siblingElements();
					System.out.println("Done");
					System.out.println();
					String crn = "";
					String cod = "";
					String titulo = "";
					String cupo = "";
					String inscritos = "";
					String disponibles = "";
					String seccion = "";
					String credits = "";
					System.out.println("tds size: "+tds.size());
					if (tds!=null) {
						if (!tds.isEmpty()) {
							crn = links.parents().first().text();
							cod = tds.get(0).text();
							seccion = tds.get(1).text();
							credits = tds.get(2).text();
							titulo = tds.get(3).text();
							cupo = tds.get(4).text();
							inscritos = tds.get(5).text();
							disponibles = tds.get(6).text();
							searched = new Course(crn, cod, seccion, credits, titulo, cupo, inscritos, disponibles,updateLastUpdate());
						}
					}
					System.out.println("CRN: "+crn+", Código: "+cod+", Sección: "+seccion+", Créditos: "+credits+
							", Título: "+titulo+"\n\tCupo total: "+cupo+", Inscritos: "+inscritos+", Disponibles: "+disponibles);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return searched;
		}

		@Override
		protected void onPostExecute(Course c) {
			super.onPostExecute(c);
			progDailog.dismiss();
			if (c!=null) { //if the HTML document does contain a course with the CRN and Depto given
				if (index ==-1) { //the index is -1, which means it's a new course it's going to add
					monitored.add(c);
					System.out.println("Se agregó un curso ("+monitored.size()+" total):\n"+c.toString());
					saveState();
				}
				else 
				{ //the index is something else, so it's updating the monitored.
					monitored.set(index, c);
					System.out.println("Se actualizó el curso ("+c.toString()+")");
					saveState();
				}
			}
			else //if the course is not found within the HTML document
				Toast.makeText(context, "No se encontró el curso.\nVerifique los datos e intente nuevamente.", Toast.LENGTH_LONG).show();
		}	

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Course obj = (Course) myList.getItemAtPosition(info.position);
		System.out.println("Curso seleccionado: "+obj.toString());
		switch (item.getItemId()) {
		case R.id.action_delete:
			removeCourse(obj);
			return true;
		case R.id.action_details:
			AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));

			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_course_details,null);
			builder.setView(layout);
			TextView txtTitle = (TextView) layout.findViewById(R.id.txtTituloDet);
			TextView txtCourseCode = (TextView) layout.findViewById(R.id.txtCourseCodeDet);
			TextView txtSection = (TextView) layout.findViewById(R.id.txtSectionDet);
			TextView txtTaken = (TextView) layout.findViewById(R.id.txtInscritosDet);
			TextView txtRemaining = (TextView) layout.findViewById(R.id.txtRemainingDet);
			TextView txtCRN = (TextView) layout.findViewById(R.id.txtCRNDet);
			TextView txtCredits = (TextView) layout.findViewById(R.id.txtCreditsDet);


			// 4. Set the text for textView 
			txtTitle.setText(obj.getTitulo());
			txtCourseCode.setText(obj.getCod());
			txtSection.setText(context.getString(R.string.section)+" "+obj.getSeccion());
			txtTaken.setText(context.getString(R.string.taken)+" "+obj.getInscritos());
			txtRemaining.setText(context.getString(R.string.remaining)+" "+obj.getDisponibles());
			txtCRN.setText(context.getString(R.string.add_crn)+" "+obj.getCRN());
			txtCredits.setText(context.getString(R.string.credits)+" "+obj.getCredits());
			builder.setTitle(R.string.details);
			builder.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}




















}