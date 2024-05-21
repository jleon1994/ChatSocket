package Cliente;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import Objetos.Mensajes;
import Objetos.TipoMensajes;

public class Cliente {

	// ************************************************************//
	// ********ATRIBUTOS ENCAPSULADOS DEL CLIENTE******************//
	// ************************************************************//

	private Socket socket;// **SOCKET DEL CLIENTE
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String nombreCliente;

	private JFrame frame;
	private JSplitPane splitPane;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JScrollPane globalChat;
	private JTextArea areaChat;
	private JScrollPane areaMensaje;
	private JTextArea textoMensaje;
	private JButton sendButton;
	private int numeroPuerto;
	private String servidor;

	/*
	 * main method of the Main-Class of the Client.jar file
	 */
	public static void main(String[] args) throws IOException {
		new Cliente();
	}

	// ************************************************************//
	// *****************CONSTRUCTOR DEL CLIENTE*******************//
	// ************************************************************//

	// **CONECTA AL SERVIDOR, FLUJOS INPUT/OUTPUT Y LA GUI DEL CLIENTE

	Cliente() {
		// **DESPACHO DE TAREAS EN EL HILO DE DESPACHO DE EVENTOS DE GUI
		SwingUtilities.invokeLater(new Runnable() {// **ENVIAMOS UNA TAREA EDT
			public void run() {// **AQUI ESTA EL CODIGO QUE INTERACTUA CON LA GUI
				try {
					iniciarConeccion();
					iniciarChatGUI();
				} catch (IOException ioe) {
					System.out.println("No se logro la coneccion al servidor: " + ioe.getMessage());
				}
			}
		});
	}

	// ************************************************************//
	// **METODO PARA INICIAR CONECCION CON EL SERVIDOR*************//
	// ************************************************************//

	// ** SE CONECTA A TRAVES DEL PUERTO
	// **LANZAMOS EXECPCIONES PARA EL HOST SI NO PUEDES SER RESULTO
	// **LANZAMOS EXEPCIONES SI OCURRE ALGUN ERROR DURANTE LA CONECCION

	private void iniciarConeccion() throws UnknownHostException, IOException {
		String str = "";// **NOMBRE DEL HOST
		String mensaje = "Ingrese el nombre del host del servidor en donde se va a conectar:";
		while (str.isEmpty()) {// **SE INICIA UN BUCLE HASTA QUE EL CLIENTE INGRESE UN NOMBRE
			str = (String) JOptionPane.showInputDialog(frame, mensaje, InetAddress.getLocalHost().getHostName());
			if (str == null)// **SI INGRESA CON UN NOMBRE DE HOST NULL SALE DEL SERVIDOR
				System.exit(0);
			mensaje = "El nombre del Host no puede estar vacio!\nIngrese el nombre del Host del servidor:";
		}
		str = str.trim();// **ELIMINAMOS ESPACIOS DEL NOMBRE DEL HOST
		servidor = str;// **PASAMOS EL NOMBRE DEL HOST A SERVIDOR
		System.out.println("Conectando al servidor...");
		mensaje = "Usando el puerto 8889\nSi deseas ingresar a un puerto diferente escribelo:\n";

		String str1 = "";

		while (str1.isEmpty()) {// **SI COLOCA UN PUERTO VACIO SOLICITA UN PUERTO CORRECTO
			str1 = (String) JOptionPane.showInputDialog(frame, mensaje, "8889");
			if (str1 == null)
				System.exit(0);
			mensaje = "No puedes ingresar un puerto nulo, por favor escribe un puerto correcto";
		}

		str1 = str1.trim();// **ELIMINAMOS ESPACIOS DEL PUERTO
		numeroPuerto = Integer.parseInt(str1);// **PASAMOS EL PUERTO A NUMERO

		new SwingWorker<Object, Object>() {// **ENCAPSULA LA EJECUACION DE UN EDT EN 2 PLANO

			@Override
			// **REALIZA EJECUACION DE TAREAS EN EL EDT MIENTRAS SE ACTUALIZA LA GUI
			protected Object doInBackground() throws Exception {
				// **CREACION DEL SOCKET CLIENTE(CON EL NUMERO DE PUERTO Y EL NOMBRE DEL HOST)
				socket = new Socket(servidor, numeroPuerto);// **PROCESO MAS IMPORTANTE DE LA CLASE**//
				System.out.println("Conectado en el puerto " + numeroPuerto);
				return null;
			}

		}.execute();// **LLAMAMOS EXECUTE PARA INICIAR EL METODO SWINGWORKER
	}

	// ************************************************************//
	// **METODO PARA LA INTERACION CON EL GUI DEL CLIENTE**********//
	// ************************************************************//

	private void iniciarChatGUI() throws IOException {

		/*
		 * showing input dialog box asking client to enter their name and sending it to
		 * server
		 */
		JFrame.setDefaultLookAndFeelDecorated(true);// **COLORACION DE LA VENTANA ACTIVA
		frame = new JFrame();// **INSTANCIAMOS UNA NUEVA VENTANA
		String str = "";
		String message = "Ingresa tu nombre para ingresar al chat:";
		while (str.isEmpty()) {// **SI ESCRIBE UN NOMBRE VACIO EL BUCLE SE SEGUIRA EJECUTANDO
			str = (String) JOptionPane.showInputDialog(frame, message);
			str = "Poligran-" + str;// **ADICIONAMOS POLIGRAN AL NOMBRE DEL CLIENTE
			if (str == null)
				System.exit(0);
			message = "Nombre no valido!\nIngresa tu nombre para ingresar al Chat:";
		}
		this.nombreCliente = str;// **AGREGAMOS A LA VARIBLE EL NOMBRE DEL CLIENTE
		System.out.println(socket == null);
		// **ENCAPSULAMOS LA EJECUCION EN 2 PLANO DE EDT
		new SwingWorker<Object, Object>() {

			@Override
			// **IMPORTANTE**//
			// **EJECUTAMOS EN UN HILO DE FONDO
			protected Object doInBackground() throws Exception {
				// **CREAMOS LOS FLUJOS DE ENTRADA Y SALIDA DE LOS OBJETOS
				// **RECIBIRIMOS Y ENVIAREMOS OBJETOS A TRAVES DE LA CONECCION
				Cliente.this.out = new ObjectOutputStream(socket.getOutputStream());
				Cliente.this.in = new ObjectInputStream(socket.getInputStream());
				// **VAMOS A ENVIAR EL OBJETO MENSAJES AL SERVIDOR
				// **ESTE OBJETO CONTIENE EL NOMBRE DEL CLIENTE Y EL TIPO DE MENSAJE
				// **ENVIA EL MENSAJE AL SERVIDOR
				out.writeObject(new Mensajes(nombreCliente, TipoMensajes.SEND_NAME));
				out.flush();
				return null;// **ESTE METODO NO NECESITA PROCESAR NINGUN PROCESO POSTERIOR
			}

		}.execute();// **EJECUTAMOS EL SWINGWORKER

		/*
		 * start a helper thread that keeps on reading messages from server and also
		 * sets up private chatting GUI
		 */
		new Thread(new AsistenteCliente(this)).start();

 
		//**GUI DEL CLIENTE**//

		// **TITULO DE LA VENTANA DEL GUI DEL CLIENTE(INDICA A QUE SERVIDOR SE ENCUENTRA
		frame.setTitle("Conectado al servidor: " + socket.getRemoteSocketAddress().toString());
		splitPane = new JSplitPane();// **CONTENEDOR QUE DIVIDE EN DOS COMPONENTES
		splitPane.setBorder(BorderFactory.createTitledBorder(str));// **BORDE CON TITULO
		leftPanel = new JPanel();// **PANEL DE CHAT GLOBAL
		rightPanel = new JPanel();// **PANEL DE LISTA DE USUARIOS
		globalChat = new JScrollPane();// **PERMITE EL DESPLAZAMIENTO POR SI EL CHAT ES MUY LARGO
		areaChat = new JTextArea();// **MOSTRAREMOS EL CHAT GLOBAL
		areaChat.setEditable(false);// **AREA DEL CHAT NO ES EDITABLE
		areaChat.setFont(areaChat.getFont().deriveFont(18f));
		areaChat.setBackground(new Color(30, 180, 220));// **COLOR DE FONDO DEL CHAT
		areaMensaje = new JScrollPane();// **PERMITE EL DESPLAZAMIENTO EN EL AREA QUE SE ESCRIBE EL MENSAJE
		textoMensaje = new JTextArea();//**SE CRE UNA NUEVA AREA DE TEXTO DE MENSAJE
		sendButton = new JButton("Enviar");//**CREACION DEL BOTON ENVIAR
		sendButton.setMargin(new Insets(2, 2, 2, 2));//**MARGENES DEL BOTON

		/*
		 * send the message to all the clients
		 */
		
		//**ACCION CUANDO LE DE CLICK A ENVIAR**//
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textoMensaje.getText().equals("")) {
					try {
						out.writeObject(new Mensajes(textoMensaje.getText(), TipoMensajes.CLIENT_GLOBAL_MESSAGE));
						out.flush();
					} catch (IOException ioe) {
						System.out.println("Error establishing connection: " + ioe.getMessage());
					}
					textoMensaje.setText("");
				}
			}
		});
		globalChat.setViewportView(areaChat);
		areaMensaje.setViewportView(textoMensaje);
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setBorder(BorderFactory.createTitledBorder("Global Chat"));
		leftPanel.setFont(leftPanel.getFont().deriveFont(18f));

		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(5, 5, 5, 5);

		addComponent(leftPanel, globalChat, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 0, 1, 2, 1,
				insets);
		addComponent(leftPanel, areaMensaje, gbc, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 0, 0, 2, 1, 1,
				insets);
		addComponent(leftPanel, sendButton, gbc, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0, 0, 1, 2, 1, 1,
				insets);

		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(300);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);

		frame.setPreferredSize(new Dimension(600, 400));
		frame.setLayout(new GridLayout());
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
		System.out.println("visible");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*
		 * request for client list from server initially to show online clients as the
		 * client joins chat
		 */
//		new SwingWorker<Object, Object>() {
//
//			@Override
//			protected Object doInBackground() throws Exception {

		out.writeObject(new Mensajes());
		out.flush();
//				return null;
//			}
//			
//		}.execute();

	}

	/*
	 * helper method for adding gridbaglayout constraints
	 */
	private void addComponent(Container parent, Component child, GridBagConstraints gbc, int fill, int anchor,
			double weightx, double weighty, int gridx, int gridy, int gridwidth, int gridheight, Insets insets) {
		gbc.fill = fill;
		gbc.anchor = anchor;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.insets = insets;
		parent.add(child, gbc);
	}

	/*
	 * getters
	 */
	public ObjectOutputStream getOut() {
		return out;
	}

	public String getName() {
		return nombreCliente;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public JTextArea getGlobalChatArea() {
		return areaChat;
	}

	public JFrame getFrame() {
		return frame;
	}

	/*
	 * global variables
	 */

}
