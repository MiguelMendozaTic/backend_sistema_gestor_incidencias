package utp.edu.sistema_gestor_incidencias.dto;

public class ApiResponse<T>{
	private boolean success;
	private String message;
	private int status;
	private T dato;	
	
	public ApiResponse(boolean success, String message, int status, T dato) {
		this.success = success;
		this.message = message;
		this.status = status;
		this.dato = dato;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public T getDato() {
		return dato;
	}
	public void setDato(T dato) {
		this.dato = dato;
	}
	
	
}