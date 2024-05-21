package Servidor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Objetos.Flujos;

public class Servidor {

	// ************************************************************//
	// ********ATRIBUTOS ENCAPSULADOS DEL SERVIDOR*****************//
	// ************************************************************//

	private Hashtable<String, Flujos> clientes; // **ALMACENAMOS LOS FLUJOS DE DATOS DE ENTRADA Y SALIDA
	private JFrame frame;// **CLASE PARA CREAR VENTADAS (BIBLIOTECA SWING)
	private ServerSocket servidor;// **ATRIBUTO SERVERSOCKET**
	private JTextArea sLabel;
	private JScrollPane sLabelPane;

	private int NumeroPuerto;

	// ************************************************************//
	// *****************CONSTRUCTOR DEL SERVIDOR*******************//
	// ************************************************************//

	Servidor() {

		clientes = new Hashtable<String, Flujos>();

		// **CREAMOS UNA VARIABLE MENSAJE INICIAL Y DE CONFIRMACION DEL PUERTO**
		String mensaje = "Usando el puerto 8889\nSi deseas ingresar con otro puerto, por favor escribelo:\n";
		String confirmacion = "El puerto ingresado es ";

		// **PROGRAMCION Y DESPACHO DE TAREAS EN EL HILO DE DESPACHO DE EVENTOS DE GUI
		SwingUtilities.invokeLater(new Runnable() {// ENVIAMOS UNA TAREA EDT
			public void run() {// **DENTRO DEL RUN ESTA EL CODIGO QUE INTERACTUA CON LA INTERFAZ GRAFICA**/

				// **CREACION DE UN CUADRO DE UN CUADRO DE DIALOGO CON DATOS DE ENTRADA (PUERTO)
				String puerto = (String) JOptionPane.showInputDialog(frame, mensaje, "8889");
				// **CREACION DEL PUERTO Y SOCKET SERVER
				if (puerto == null) {
					System.exit(0);// **SI LE DA SALIR A LA SOLICITUD CIERRA EL SERVIDOR
				} else {
					// **CONFIRMACION DE PUERTO
					int opcion = JOptionPane.showConfirmDialog(null, confirmacion + puerto,
							"!Confirmacion de puerto ingresado!", JOptionPane.YES_NO_OPTION);
					if (opcion == 0) {
						System.out.println("Acepto el puerto " + puerto + " ,procesando la solicitud...");
						puerto = puerto.trim();// **ELIMINAMOS ESPACION AL TEXTO(PUERTO)
						Servidor.this.NumeroPuerto = Integer.parseInt(puerto);// **CONVERTIMOS A NUMERO EL PUERTO

						// **CREACION DEL SERVERSOCKET PARA ESTAR A LA ESPERA DE SOLICITUDES**//
						try {
							Servidor.this.servidor = new ServerSocket(NumeroPuerto);
						} catch (IOException e) {
							System.out.println("Sin coneccion al servidor mediante el puerto " + NumeroPuerto);
						}

						if (servidor == null) {
							JOptionPane.showMessageDialog(frame, "Puerto en uso!", "Error!", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}

						// **DESPUES DE LA CREACION DEL SOCKETSERVER SE INICIA EL METODO SETGUI(INTERFAZ
						// GRAFICA)**//
						setGUI();

					} else {
						System.exit(0);// **CUANDO EL CLIENTE NO ACEPTA LA CONFIRMACION DEL PUERTO
					}

				}
			}
		});
	}

	// ************************************************************//
	// ************INTERFAZ GRAFICA DEL SERVIDOR*******************//
	// ************************************************************//

	private void setGUI() {
		frame = new JFrame("Servidor Chat Politecnico Grancolombiano");// **INSTACIONAMOS UNA NUEVA VENTANA
		frame.setUndecorated(false); // **ELIMINAR DECOLORACION DE LA VENTANA
		frame.setResizable(true); // **LA VENTANA SE PUEDE DIMENSIONAR

		JPanel panelInicial = new JPanel(new BorderLayout());// **INSTANCIAMOS UN NUEVO PANEL
		panelInicial.setBorder(new EmptyBorder(20, 20, 20, 20));// **BORDES DE LA VENTANA CON BORDERLAYOUT

		JPanel encabezadoPanel = new JPanel(new BorderLayout());// **INSTANCIAMOS OTRO PANEL
		encabezadoPanel.setBackground(new Color(15, 55, 90));// **COLOR DEL PANEL

		JLabel textoEncabezado = new JLabel("Log Servidor Chat Politecnico Grancolombiano");// **INSTANCIAMOS UN TEXTO
		textoEncabezado.setForeground(Color.white);// **COLOR DEL TEXTO
		textoEncabezado.setFont(new Font("Arial", Font.CENTER_BASELINE , 14));// **PROPIEDADES DEL TEXTO
		textoEncabezado.setBorder(new EmptyBorder(1, 1, 1, 1));
		encabezadoPanel.add(textoEncabezado, BorderLayout.WEST);// **ADICIONAMOS AL ENCAB. EL TEXTO

		sLabel = new JTextArea();// **CREACION DE OBJETO PARA PONER Y EDITAR TEXTO EN GUI
		sLabel.setEditable(false);// **TEXTO DEL TEXAREA NO EDITABLE
		sLabel.setLineWrap(false);// **AJUSTE DEL TEXTO
		sLabel.setWrapStyleWord(true);
		sLabelPane = new JScrollPane(sLabel);

		// **AGREGAMOS AL PANEL PRINCIPAL EL ENCABEZADO Y TEXTO
		panelInicial.add(encabezadoPanel, BorderLayout.NORTH);
		panelInicial.add(sLabelPane, BorderLayout.CENTER);

		// **TEXTO DEL SLABEL**//
		try {
			sLabel.setText("Servidor disponible en la IP \n" + InetAddress.getLocalHost() + "\nPuerto: " + NumeroPuerto
					+ "\n\nSi el cliente está en el mismo equipo,\n Use el mismo Localhost como IP\n\nSi está en otro equipo,\nUsar la IP y puerto anteriores");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		frame.add(panelInicial);
		frame.setPreferredSize(new Dimension(600, 500));// **INSTANCIAMOS UNA NUEVA DIMENSION DE LA VENTANA
		frame.setResizable(true);// **METODO QUE NOS PERMITE REDIMENSIONAR LA VENTANA
		frame.pack();// **METODO QUE ACOMODO AL TAMAÑO MINIMO NECESARIO PARA LA VISUALIZACION
		frame.setVisible(true);// **LA VENTANA ES VISIBLE
		frame.setLocationRelativeTo(null); // **CENTRAR LA VENTANA
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// **CIERRA EL SERVIDOR SI SE CIERRA
		escucharClientes();
	}

	// ************************************************************//
	// ******METODO PARA QUE EL SOCKET ESCUCHE USUARIOS************//
	// ************************************************************//

	public void escucharClientes() {
		// **ACTUALIZACION DE LA GUI**//
		new SwingWorker<Object, Object>() {// **REALIZA TAREAS EN SEGUNDO PLANO
			// **MIENTRAS EJECUTAMOS LA INTERFAZ DE USUARIO**//
			@Override
			protected Object doInBackground() throws Exception {// **REALIZA TAREAS EN SEGUNDO PLANO
				while (true) {// **BUCLE INFINITO PARA ESCUCHAR CONTINUAMENTE CONECCIONES
					try {
						Socket socket = servidor.accept();
						new Thread(new ListaClientes(Servidor.this, socket)).start();
						System.out.println("Cliente conectado en: " + socket.getRemoteSocketAddress());
					} catch (IOException ioe) {
						System.out.println("Error estableciendo la coneccion: " + ioe.getMessage());
					}

				}
			}
		}.execute();// **SE INICIA LA EJECUACION EN SEGUNDO PLANO

	}

	// ********************************************************************//
	// ******METODO PARA OBTENER LA LISTA DE CLIENTES GUARDADOS************//
	// ********************************************************************//

	public Hashtable<String, Flujos> getClientes() {
		return clientes;
	}

	// *************************************************************************//
	// ******ESCRIBE EN EL TEXAREA DE GUI SERVER LOS CLIENTES CONECC************//
	// *************************************************************************//

	public JTextArea getsLabel() {
		return sLabel;
	}

	// ************************************************************//
	// ******METODO PARA QUE EL SOCKET ESCUCHE USUARIOS************//
	// ************************************************************//

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Servidor();
	}
}
