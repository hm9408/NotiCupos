package com.hz.noticupos;

public class Course {
	
	private String CRN;
	
	private String cod;
	
	private String depto;
	
	private String seccion;
	
	private String credits;
	
	private String titulo;
		
	private String cupo;
	
	private String inscritos;
	
	private String disponibles;
	
	
	public Course(String cRN, String cod, String seccion, String credits, String titulo, String cupo, String inscritos, String disponibles) {
		CRN = cRN;
		this.cod = cod;
		this.depto = cod.substring(0, 4);
		this.seccion = seccion;
		this.credits = credits;
		this.titulo = titulo;
		this.cupo = cupo;
		this.inscritos = inscritos;
		this.disponibles = disponibles;
	}


	public String getDepto() {
		return depto;
	}


	public String getInscritos() {
		return inscritos;
	}


	public void setInscritos(String inscritos) {
		this.inscritos = inscritos;
	}


	public String getDisponibles() {
		return disponibles;
	}


	public void setDisponibles(String disponibles) {
		this.disponibles = disponibles;
	}
	

	public String getCRN() {
		return CRN;
	}


	public String getCod() {
		return cod;
	}


	public String getSeccion() {
		return seccion;
	}


	public String getCredits() {
		return credits;
	}


	public String getTitulo() {
		return titulo;
	}


	public String getCupo() {
		return cupo;
	}
	
	public String toString()
	{
		return "Código: "+cod+", nombre: "+titulo+", CRN: "+CRN+", Departamento: "+depto;
	}
	
	
	
}
