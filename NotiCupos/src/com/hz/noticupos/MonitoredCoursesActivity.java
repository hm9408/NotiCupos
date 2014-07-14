package com.hz.noticupos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

public class MonitoredCoursesActivity extends ListActivity implements OnRefreshListener {

	private SwipeRefreshLayout swipeLayout;
	private ArrayList<Course> monitored;
	private String lastUpdate;
	private ListView myList;
	private MyListAdapter myListAdapter;
	Context context;
	//	private int freqUpdate;
	//	private String updateTime;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_main);
		if (loadState()==null) {
			monitored = new ArrayList<Course>();
		}
		myList=(ListView)findViewById(android.R.id.list);
		myListAdapter = new MyListAdapter(context, R.layout.row, monitored);
		View empty = findViewById(android.R.id.empty);
		myList.setEmptyView(empty);
		myList.setAdapter(myListAdapter);
		myList.setLongClickable(true);
		registerForContextMenu(myList);

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);
		//		IntentService updateService = new IntentService("updateService") {
		//
		//			@Override
		//			protected void onHandleIntent(Intent intent) {
		//				// TODO Auto-generated method stub
		//				while(true){
		//					if (updateTime.equals("Horas")) {
		//
		//						try {
		//							updateCoursesInfo();
		//							wait(freqUpdate*3600000);
		//						} catch (InterruptedException e) {
		//							// TODO Auto-generated catch block
		//							e.printStackTrace();
		//						}
		//					}
		//					else if (updateTime.equals("Minutos")) {						
		//						try {
		//							updateCoursesInfo();
		//							wait(freqUpdate*60000);
		//						} catch (InterruptedException e) {
		//							// TODO Auto-generated catch block
		//							e.printStackTrace();
		//						}
		//					}		
		//				}				
		//			}
		//		};
		//
		//		Intent serviceInt = new Intent(this, IntentService.class);
		//		updateService.startService(serviceInt);
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

			Button butSaveSettings = (Button) settingsDialog.findViewById(R.id.butSaveSettings);
			// if button is clicked, close the custom dialog
			butSaveSettings.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					saveState(monitored);
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
		//saveState(monitored); TODO
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
		Course c = searchCourseCRN(CRN, depto);
		if (c!=null) {
			monitored.add(c);
			System.out.println("Se agregó un curso ("+monitored.size()+" total):\n\r"+c.toString());
			//saveState(monitored); TODO
		}
		else
		{
			Toast.makeText(this, "No se encontró el curso.\n\rVerifique los datos e intente nuevamente.", Toast.LENGTH_SHORT).show();
		}
	}

	public void removeCourse(Course c)
	{
		monitored.remove(c);
		myListAdapter.notifyDataSetChanged();
		//saveState(monitored); TODO
	}

	public void updateCoursesInfo()
	{
		if (monitored.size()!=0) {
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
		//saveState(monitored); TODO
	}

	public void saveState(ArrayList<Course> courses){
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("notiCupos.data"));        
			out.writeObject(courses);
			out.close();
		} catch (Exception e) {e.printStackTrace();}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Course> loadState(){
		ObjectInput in;
		ArrayList<Course> saved=null;
		try {
			in = new ObjectInputStream(new FileInputStream("notiCupos.data"));       
			saved=(ArrayList<Course>) in.readObject();
			in.close();
			if (saved==null) {
				System.out.println("Se cargaron 0 cursos");
			}
			else
			{
				System.out.println("Se cargaron "+saved.size()+ " cursos");
			}
		} 
		catch (IOException e) {
			//the file didn't exist and will now be created
			e.printStackTrace();
			File fileSave = new File(Environment.getDataDirectory(),"notiCupos.data");
			try {
				fileSave.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Does the save file exist now? "+fileSave.exists());
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return saved;
	}



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
			URL url;
			try {
				url = new URL("http://registroapps.uniandes.edu.co/scripts/adm_con_horario1_joomla.php?depto="+params[0]);
				Tidy tidy = new Tidy();
				tidy.setQuiet(true);
				tidy.setXHTML(true);    
				tidy.setShowWarnings(false);
				Document doc = tidy.parseDOM(url.openStream(), System.out);

				// Use XPath to obtain whatever you want from the (X)HTML
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression expr = xpath.compile("//tr[td[normalize-space(font) = '"+params[1]+"']]/td/font/text()");
				NodeList result = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
				if (result!=null) {
					String crn = result.item(0).getNodeValue();
					String cod = result.item(1).getNodeValue();
					//				String seccion = result.item(2).getNodeValue(); //TODO: fix
					//				String credits = result.item(3).getNodeValue(); //TODO: fix
					//				String titulo = result.item(4).getNodeValue();
					//				String cupo = result.item(5).getNodeValue();
					//				String inscritos = result.item(6).getNodeValue();
					//				String disponibles = result.item(7).getNodeValue();
					//				searched = new Course(crn, cod, seccion, credits, titulo, cupo, inscritos, disponibles);
					String titulo = result.item(2).getNodeValue();
					String cupo = result.item(3).getNodeValue();
					String inscritos = result.item(4).getNodeValue();
					String disponibles = result.item(5).getNodeValue();
					searched = new Course(crn, cod, "1", "3", titulo, cupo,
							inscritos, disponibles);
				}
				else
				{
					Toast.makeText(context, "Problema al conectarse con Registro.\n\rIntente más tarde.", Toast.LENGTH_SHORT).show();
				}
					
			} 
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
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
		inflater.inflate(R.menu.context_menu_delete, menu);
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
		default:
			return super.onContextItemSelected(item);
		}
	}




















}