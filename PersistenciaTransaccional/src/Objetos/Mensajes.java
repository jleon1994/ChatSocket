package Objetos;

import java.io.Serializable;
import java.util.ArrayList;

public class Mensajes implements Serializable {

	// ************************************************************//
	// ********ATRIBUTOS ENCAPSULADOS DE MENSAJES******************//
	// ************************************************************//

	private static final long serialVersionUID = 1L;// VERSION DE SERIALIZACION DEL OBJETO
	private String mensaje;// **ATRIBUTO MENSAJE
	private String persona;// **ATRIBUTO PERSONA
	private ArrayList<String> clientes;// **LISTA DE CLIENTES QUE SE ENCUENTRAN CONECTADOS
	private TipoMensajes tipoMensaje;// **TIPOS DE MENSAJES DE LA ENUM

	// ************************************************************//
	// **********CLASES CONSTRUCTORAS DE MENSAJES******************//
	// ************************************************************//

	// ************************************************************//
	// ******SOLICITA LISTA DE CLIENTES AL SERVIDOR****************//
	// ************************************************************//

	public Mensajes() {
		this.tipoMensaje = TipoMensajes.REQUEST_CLIENT_LIST;
	}

	// ************************************************************//
	// *********MENSAJES PRIVADOS ENTRE CLIENTES*******************//
	// ************************************************************//

	public Mensajes(String mensaje, String persona) {
		this.tipoMensaje = TipoMensajes.CLIENT_PRIVATE_MESSAGE;
		this.mensaje = mensaje;
		this.persona = persona;
	}

	// ************************************************************//
	// ***ESTABLECE UN MENSAJE Y LOS PONE EN LOS CAMPOS CHAT*******//
	// ************************************************************//

	public Mensajes(String mensaje, TipoMensajes tipoMensaje) {
		this.tipoMensaje = tipoMensaje;
		this.mensaje = mensaje;
	}

	// ************************************************************//
	// ***ENVIA LISTA DE CLIENTES DESDE EL SERVIDOR A UN CLIENTE***//
	// ************************************************************//

	public Mensajes(ArrayList<String> clients) {
		this.tipoMensaje = TipoMensajes.SEND_CLIENT_LIST;
		this.clientes = clients;
	}

	// ************************************************************//
	// ***ESTABLECE UN MENSAJE Y LOS PONE EN LOS CAMPOS CHAT*******//
	// ************************************************************//

	public Mensajes(String mensaje, String person, TipoMensajes tipoMensaje) {
		this.tipoMensaje = tipoMensaje;
		this.mensaje = mensaje;
		this.persona = person;
	}

	// ************************************************************//
	// **DEVUELVE EL MENSAJE ALMACENADO EN EL OBJETO MENSAJES******//
	// ************************************************************//

	public String getMensajes() {
		return mensaje;
	}

	// ************************************************************//
	// **DEVUELVE LA PERSONA ALMACENADA EN EL OBJETO MENSAJES******//
	// ************************************************************//

	public String getPersonas() {
		return persona;
	}

	// **********************************************************************//
	// **DEVUELVE LA LISTA DE CLIENTES ALMACENADA EN EL OBJETO MENSAJES*****//
	// *********************************************************************//

	public ArrayList<String> getClientes() {
		return clientes;
	}

	// **********************************************************************//
	// **DEVUELVE EL TIPO DE MENSAJE ALMACENADO EN EL OBJETO MENSAJES********//
	// *********************************************************************//
	public TipoMensajes gettipoMensaje() {
		return tipoMensaje;
	}

}