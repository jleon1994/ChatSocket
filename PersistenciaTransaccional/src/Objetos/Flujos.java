package Objetos;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Flujos {

	// **ATRIBUTOS ENCAPSULADOS**//

	//**FLUJOS DE DATOS**//
	private ObjectInputStream lecturaDatos;// **LEER DATOS SERIALIZADOS - ENTRADA
	private ObjectOutputStream escribirDatos;// **ESCRIBIR DATOS SERIALIZADOS- SALIDA

	// **METODO CONSTRUCTOR**//
	public Flujos(ObjectInputStream lecturaDatos, ObjectOutputStream escribirDatos) {
		this.lecturaDatos = lecturaDatos;
		this.escribirDatos = escribirDatos;
	}

	// **METODOS GETTER PARA OBTENER LOS DATOS TANTO DE ENTRADA Y SALIDA**//
	public ObjectInputStream getLectura() {
		return this.lecturaDatos;
	}

	public ObjectOutputStream getEscribir() {
		return this.escribirDatos;
	}

}
