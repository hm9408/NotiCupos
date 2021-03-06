package com.hz.noticupos;

import java.io.Serializable;

public class Course implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3669307741695714657L;

	private String CRN;
	
	private String cod;
	
	private String depto;
	
	private String seccion;
	
	private String credits;
	
	private String titulo;
		
	private String cupo;
	
	private String inscritos;
	
	private String disponibles;
	
	private String lastUpd;
	
	
	public Course(String cRN, String cod, String seccion, String credits, String titulo, String cupo, String inscritos, String disponibles, String string) {
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
	
	public String getLastUpd() {
		return lastUpd;
	}


	public void setLastUpd(String lastUpd) {
		this.lastUpd = lastUpd;
	}


	public String toString()
	{
		return "C�digo: "+cod+", nombre: "+titulo+", CRN: "+CRN+", Departamento: "+depto;
	}
	
	
	
}
