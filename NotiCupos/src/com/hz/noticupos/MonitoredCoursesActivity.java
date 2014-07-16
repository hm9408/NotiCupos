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
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
	private String lastUpdate;
	private ListView myList;
	private MyListAdapter myListAdapter;
	private transient Context context;
	public static final String FILE = "noticupos.data";
//	private NotificationService notifServ;
	private int freqUpdate;
	private String updateTime;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_main);
		loadState();
		if (monitored==null) {
			monitored = new ArrayList<Course>();
		}

		myList=(ListView)findViewById(android.R.id.list);
		myListAdapter = new MyListAdapter(context, R.layout.row, monitored);
		View empty = findViewById(android.R.id.empty);
		myList.setEmptyView(empty);
		myList.setAdapter(myListAdapter);
		myList.setLongClickable(true);
		registerForContextMenu(myList);
		myListAdapter.notifyDataSetChanged();

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
				updateCoursesInfo();
				swipeLayout.setRefreshing(false);
			}
		}, 10000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			final Dialog settingsDialog = new Dialog(this);
			settingsDialog.setContentView(R.layout.activity_settings);
			settingsDialog.setTitle("Opciones");
			TextView txtNotifFreq = (TextView) settingsDialog.findViewById(R.id.txtNotificationFreq);
			final Spinner spinTime = (Spinner) settingsDialog.findViewById(R.id.spinnerTime);
			final EditText editNotifFreq = (EditText) settingsDialog.findViewById(R.id.editTimeFreq);
			final int freq = Integer.parseInt(editNotifFreq.getText().toString());
			final String selectedTime= spinTime.getSelectedItem().toString();
			Button butSaveSettings = (Button) settingsDialog.findViewById(R.id.butSaveSettings);
			
			// if button is clicked, close the custom dialog
			butSaveSettings.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedTime.equals("Minutos") && freq < 15) {
						Toast.makeText(context, "La frecuencia de actualización no puede ser tan alta."
								+ "\n\rIngrese un valor mayor a 15 minutos.", Toast.LENGTH_LONG).show();
					}
					else
					{
						setFreqUpdate(freq);
						setUpdateTime(selectedTime);
						saveState(monitored);
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
						Toast.makeText(context, "El CRN debe tener 5 caracteres.\n\rVerifique los datos e intente nuevamente.", 
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

	public String getLastUpdate() {
		return lastUpdate;
	}

	@SuppressLint("SimpleDateFormat")
	public void updateLastUpdate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date d= new Date();
		this.lastUpdate = sdf.format(d);
		saveState(monitored);
	}

	public Course searchCourseCRN(String CRN, String depto)
	{
		Course c = null;
		try {
			c = new SearchCourseTask(context).execute(depto, CRN).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
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
			Course c = searchCourseCRN(CRN, depto);
			if (c!=null) {
				monitored.add(c);
				System.out.println("Se agregó un curso ("+monitored.size()+" total):\n\r"+c.toString());
				saveState(monitored);
			}
			else
				Toast.makeText(context, "No se encontró el curso.\n\rVerifique los datos e intente nuevamente.", Toast.LENGTH_LONG).show();
		}
	}

	public void removeCourse(Course c)
	{
		String titulo = c.getTitulo();
		monitored.remove(c);
		myListAdapter.notifyDataSetChanged();
		Toast.makeText(context, "El curso \""+titulo+"\" se eliminó correctamente", Toast.LENGTH_SHORT).show();
		saveState(monitored);
	}

	public void updateCoursesInfo()
	{
		if (monitored.size()!=0) {
			Toast.makeText(context, "Actualizando cursos...", Toast.LENGTH_SHORT).show();
			for (int i = 0; i < monitored.size(); i++) {
				Course c = monitored.get(i);
				c = searchCourseCRN(c.getCRN(), c.getDepto()); //updates every value
			}
			updateLastUpdate();
			myListAdapter.notifyDataSetChanged();
		}
		else
		{
			Toast.makeText(context, "No hay cursos para actualizar.", Toast.LENGTH_SHORT).show();
		}
		saveState(monitored);
	}

	public void saveState(ArrayList<Course> courses){
		try {
			FileOutputStream fos = openFileOutput(FILE, MODE_PRIVATE);
			System.out.println("Is the fos null? "+fos==null);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			System.out.println("Is the oos null? "+oos==null);
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
			System.out.println("Is the fis null? "+fis==null);
			ObjectInputStream ois = new ObjectInputStream(fis);
			System.out.println("Is the ois null? "+ois==null);
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

		public SearchCourseTask(Context c)
		{
			context = c;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDailog = ProgressDialog.show(context, "Agregar curso", "Buscando curso...", false, false);
			progDailog.show();
		}

		@Override
		protected Course doInBackground(String... params) {
			Course searched = null;
			Document doc;
			try {
				doc = Jsoup
				        .connect("http://registroapps.uniandes.edu.co/scripts/adm_con_horario1_joomla.php?depto=IIND")
				        .timeout(20000)
				        .get();

		        Elements links = doc.select("font:containsOwn(10110)");
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
						searched = new Course(crn, cod, seccion, credits, titulo, cupo, inscritos, disponibles);
					}
				}
				System.out.println("CRN: "+crn+", Código: "+cod+", Sección: "+seccion+", Créditos: "+credits+
						", Título: "+titulo+"\n\tCupo total: "+cupo+", Inscritos: "+inscritos+", Disponibles: "+disponibles);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return searched;
		}

		@Override
		protected void onPostExecute(Course unused) {
			super.onPostExecute(unused);
			progDailog.dismiss();
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

			builder.setTitle(R.string.details);
			builder.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}




















}