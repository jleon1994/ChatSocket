package Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import Objetos.Flujos;
import Objetos.Mensajes;
import Objetos.TipoMensajes;

public class ListaClientes implements Runnable {

	// ************************************************************//
	// ******ATRIBUTOS ENCAPSULADOS LA LISTA DE CLIENTES***********//
	// ************************************************************//

	private ObjectInputStream in;// **LEER DATOS SERIALIZADOS
	private ObjectOutputStream out;// **ESCRIBIR DATOS SERIALIZADOS
	private String nombreCliente;// **NOMBRE DEL CLIENTE
	private Socket socket;// **CONECCION SOCKET CLIENTE
	private Servidor servidor;// **ATRIBUTO SERVIDOR DE LA CLASE

	// ************************************************************//
	// ***********CONSTRUCTOR DE LA LISTA DE CLIENTES**************//
	// ************************************************************//

	// **TENER COMO PARAMETRO AL SERVIDOR NOS PERMITE ACCEDER A EL **//
	public ListaClientes(Servidor servidor, Socket socket) {
		try {
			this.servidor = servidor;
			this.socket = socket;
			// **CONECTA EL FLUJO DE SALIDA DEL SOCKET (ENVIARA DATOS AL CLIENTE)
			// **LEE LO QUE EL CLIENTE LE ESTA ENVIANDO(SALIDA DE DATOS)
			this.out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();// **ASEGURA QUE CUALQUIER DATO EN BUFFER SE ENVIE AL CLIENTE

			// **CONECTA EL FLUJO DE ENTRADA DEL SOCKET (RECIBIR DATOS DEL CLIENTE)
			this.in = new ObjectInputStream(socket.getInputStream());

		} catch (SocketException se) {
			System.out.println("Error estableciendo la coneccion: " + se.getMessage());
		} catch (IOException ioe) {
			System.out.println("Error estableciendo la coneccion: " + ioe.getMessage());
		}
	}

	// *************************************************************************//
	// ***********LOGICA DE LA COMUNICACION ENTRE CLIENTE SERVIDOR**************//
	// ************************************************************************//

	public void run() {// **CUANDO EL THREAD SEA INICIADO SE EJECUTARA EL RUN
		System.out.println("en thread");
		try {

			// **EL PRIMER MENSAJE QUE ENVIA EL CLIENTE ES EL NOMBRE**//

			Mensajes mensaje = (Mensajes) in.readObject();// **OBTENEMOS EL NOMBRE DEL CLIENTE
			String solicitudNombre = mensaje.getMensajes();// **SE LEE EL PRIMER MENSAJE QUE ENVIA EL CLIENTE (NOMBRE)
			nombreCliente = solicitudNombre;// **GUARDA EN LA VARIABLE NOMBRECLIENTE EL RESULTADO

			// **AGREGA AL HASHARRAY CLIENTES EL NOMBRE Y OBJETO ENTRADA Y SALIDA
			// **ACTUALIZAMOS LA ETIQUETA EN LA INTERFAZ DE USUARIO
			servidor.getClientes().put(nombreCliente, new Flujos(in, out));
			System.out.println(
					nombreCliente + " en " + socket.getInetAddress().getHostAddress() + " se conecto al Chat!");
			servidor.getsLabel().setText(servidor.getsLabel().getText() + "\n\n" + nombreCliente + " en "
					+ socket.getRemoteSocketAddress() + " conectado al Chat!");

			// **SE ENVIA UN MENSAJE A TODOS LOS CLIENTES QUE UN NUEVO CLIENTE SE HA UNIDO
			servidor.getClientes().forEach((k, v) -> {// **DEVUELVE LA LISTA CLIENTES SUS CLAVES - FLUJOS
				if (!k.equals(nombreCliente)) {// **EL CLIENTE NO RECIBIRA UNA NOTIFICACION DE SU MISMA LLEGADA
					try {
						v.getEscribir()// **EL OBJETO MENSAJE CONTIENE UNA LISTA DE TODOS LOS CLIENTES
								.writeObject(new Mensajes(new ArrayList<String>(servidor.getClientes().keySet())));
						v.getEscribir().flush();// **ASEGURAR QUE LOS DATOS SE ENVIEN DE INMEDIATO
					} catch (IOException ioe) {
						System.out.println("Error estableciendo la coneccion: " + ioe.getMessage());
					}
				}
			});

			// **SE INICIA UN BUCLE INFINITO PARA LEER LOS MENSAJES INDEFINIDAMENTE**/
			// **BUSCA EL TIPO DE MENSAJE QUE ESTA LLEGANDO EN LA SOLICITUD**//
			while (true) {

				// **SE LEE EL MENSAJE ENVIADO POR EL CLIENTE**//
				mensaje = (Mensajes) in.readObject();
				final Mensajes msg = mensaje;

				// **VERIFICAMOS EL TIPO DE MENSAJE**//
				if (mensaje.gettipoMensaje() == TipoMensajes.REQUEST_CLIENT_LIST) {

					// **ENVIAMOS UNA LISTA DE CLIENTES AL CLIENTE QUE SE CONECTO**//
					out.writeObject(new Mensajes(new ArrayList<String>(servidor.getClientes().keySet())));
					out.flush();// **ASEGURAMOS QUE LA LISTA SE ENVIE DE INMEDIATO
				} else if (msg.gettipoMensaje() == TipoMensajes.CLIENT_GLOBAL_MESSAGE) {
					// **SI EL MENSAJE, ES UN MENSAJE GLOBAL, SE TRASMITE EL MENSAJE A TODOS
					servidor.getClientes().forEach((k, v) -> {
						try {
							v.getEscribir().writeObject(new Mensajes(msg.getMensajes(), this.nombreCliente,
									TipoMensajes.SERVER_GLOBAL_MESSAGE));
							v.getEscribir().flush();
						} catch (IOException ioe) {
							System.out.println("Error estableciendo coneccion: " + ioe.getMessage());
						}
					});
				} else if (mensaje.gettipoMensaje() == TipoMensajes.CLIENT_PRIVATE_MESSAGE) {
					// **CUANDO EL CLIENTE ENVIA UN MENSAJE PRIVADO
					ObjectOutputStream out_ = servidor.getClientes().get(mensaje.getPersonas()).getEscribir();
					out_.writeObject(new Mensajes(mensaje.getMensajes(), this.nombreCliente,
							TipoMensajes.SERVER_PRIVATE_MESSAGE));
					out_.flush();
				}

			}

			// **EXEPCIONES DURANTE LA EJECUCION DEL HILO**//
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Error al establecer la coneccion: " + cnfe.getMessage());
		} catch (SocketException se) {
			// **CLIENTE DESCONECTADO PARA REALIZAR LA LIMPIEZA
			System.out.println(nombreCliente + " en " + socket.getInetAddress().getHostAddress() + " salio del Chat!");
			servidor.getsLabel().setText(servidor.getsLabel().getText() + "\n\n" + nombreCliente + " at "
					+ socket.getRemoteSocketAddress() + " salio del Chat!");
			servidor.getClientes().remove(nombreCliente);
			servidor.getClientes().forEach((k, v) -> {
				try {
					v.getEscribir().writeObject(new Mensajes(nombreCliente, TipoMensajes.SEND_CLIENT_LIST_LEFT));
					v.getEscribir().flush();
				} catch (IOException ioe) {
					System.out.println("Error estableciendo coneccion: " + ioe.getMessage());
				}
			});
		} catch (IOException ioe) {
			System.out.println("Error estableciendo coneccion: " + ioe.getMessage());
		}
	}

}
