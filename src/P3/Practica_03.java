package P3;
/*
 * Autores: 
 * Miguel Torres Pérez
 * Samuel Moreno Vincent
 * 
 */

import java.io.*;
import java.security.*;
import java.util.Scanner;
import javax.crypto.Cipher;

public class Practica_03 {
	public static Scanner input;
	public static KeyPair keyPair;
	public static Options options;
	public static Header header;
	static BufferedReader bufferedReader;

	private static boolean salir() throws IOException {
		boolean salir = false;
		System.out.print("0) Salir. 1) Seguir > ");
		String salirInt = bufferedReader.readLine();
		if (salirInt.equals("0")) {
			System.out.println("Saliendo.");
			salir = true;
		}
		return salir;

	}

	private static String Menu() throws Exception {
		// MENU
		String opcion;
		boolean bool = false;

		// Opciones del menu
		while (!bool) {
			System.out.println("Selecciona la opcion:");
			System.out.println("1. Generar claves nuevas.");
			System.out.println("2. Ver claves actuales.");
			System.out.println("3. Opciones de claves/firmas.");
			System.out.println("4. Cifrar.");
			System.out.println("5. Descifrar.");
			System.out.println("6. Firmar.");
			System.out.println("7. Verificar firma.");
			System.out.println("8. Salir.");
			System.out.print("> ");
			// opcion = input.nextInt(); // getting a String value
			opcion = bufferedReader.readLine();
			switch (opcion) {
			case "1":
				System.out.println("Accediendo a Generar claves nuevas.");
				generarClaves();
				bool = salir();
				break;
			case "2":
				System.out.println("Accediendo a Ver claves actuales.");
				verClaves();
				bool = salir();
				break;
			case "3":
				System.out.println("Accediendo a Opciones de claves/firmas.");
				opcionesClaves();
				bool = salir();
				break;
			case "4":
				System.out.println("Accediendo a Cifrar.");
				cifrar();
				bool = salir();
				break;
			case "5":
				System.out.println("Accediendo a Descifrar.");
				descifrar();
				bool = salir();
				break;
			case "6":
				System.out.println("Accediendo a Firmar.");
				firmar();
				bool = salir();
				break;
			case "7":
				System.out.println("Accediendo a Verificar firma.");
				verificarfirma();
				bool = salir();
				break;
			case "8":
				System.out.println("Saliendo.");
				bool = true;
				break;
			default:
				System.out.println("Numero '" + opcion + "' incorrecto.");
				break;

			}
		}
		return " ";
	}

	private static void generarClaves() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(512); // bits

		keyPair = kpg.generateKeyPair();

		PublicKey pku = keyPair.getPublic();
		PrivateKey pkr = keyPair.getPrivate();
		//System.out.println("publica: " + pku + "\nprivada: " + pkr);

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("p3.key"));
			oos.writeObject(keyPair);
			oos.close();
			System.out.println("Claves generadas.");
		} catch (Exception localException) {
			System.out.println("Error al guardar la KEY");
		}
	}

	private static void verClaves() {

		PublicKey pkuReaded;
		PrivateKey pkrReaded;
		KeyPair aux;

		try {

			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("p3.key"));
			aux = ((KeyPair) ois.readObject());
			ois.close();

			pkrReaded = aux.getPrivate();
			pkuReaded = aux.getPublic();

			System.out.println("publica: " + pkuReaded + "\nprivada: " + pkrReaded);
			keyPair = aux;

		} catch (Exception localException) {
			System.out.println("Error al cargar la KEY");
		}
	}

	private static boolean cargarClaves() {
		KeyPair aux;

		try {

			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("p3.key"));
			aux = ((KeyPair) ois.readObject());
			ois.close();

			keyPair = aux;

		} catch (Exception localException) {
			System.out.print("Error al cargar la KEY. ");
			return false;
		}
		return true;
	}

	private static void opcionesClaves() throws IOException {
		System.out.print("1. SHA1withRSA\n" + "2. MD2withRSA\n" + "3. MD5withRSA\n" + "> ");
		String opciones = bufferedReader.readLine();
		switch (opciones) {
		case "1":
			options.setSigner(options.signers[0]);
			break;
		case "2":
			options.setSigner(options.signers[1]);
			break;
		case "3":
			options.setSigner(options.signers[2]);
			break;
		default:
			options.setSigner(options.signers[0]);
			break;
		}
		System.out.println("Seleccionada la opcion " + options.getSigner() + ".");
	}

	private static String menuFicheroSin() throws Exception {
		// MENU
		String ficheroEntrada = " ";

		System.out.println("Introducir nombre del fichero de entrada.");
		System.out.println("Para usar archivo por defecto (ejemplo.txt) pulsar:  1.");
		// Opciones del menu
		System.out.print("> ");
		ficheroEntrada = bufferedReader.readLine();
		if (ficheroEntrada.equals("1")) {
			System.out.println("Utilizando archivo por defecto.");
			ficheroEntrada = "ejemplo.txt";
		}
		return ficheroEntrada;
	}

	private static String menuFicheroCon(String extension) throws Exception {
		// MENU
		String ficheroEntrada = " ";

		boolean bool = false;
		System.out.println("Introducir nombre del fichero de entrada.");
		System.out.println("El fichero debe acabar en '" + extension+"'");
		System.out.println("Para usar archivo por defecto (ejemplo.txt" + extension + ") pulsar:  1.");
		// Opciones del menu
		while (!bool) {
			System.out.print("> ");
			ficheroEntrada = bufferedReader.readLine();
			if (ficheroEntrada.equals("1")) {
				System.out.println("Utilizando archivo por defecto.");
				ficheroEntrada = "ejemplo.txt" + extension;
				bool = true;
			}
			if (ficheroEntrada.endsWith(extension)) {
				bool = true;
			}
			if (!bool) {
				System.out.println("Nombre '" + ficheroEntrada + "' incorrecto.");
			}
		}
		return ficheroEntrada;
	}

	private static void cifrar() throws Exception {

		if (!cargarClaves()) {
			System.out.println("Para cifrar, generar KEY.");
			return;
		}

		String ficheroEntrada = menuFicheroSin();
		String ficheroSalida = ficheroEntrada + ".cif";

		// Abrimos los ficheros
		System.out.print("Abriendo fichero... ");
		FileInputStream fileInput = new FileInputStream(ficheroEntrada);
		FileOutputStream fileOutput = new FileOutputStream(ficheroSalida);
		System.out.println("Fichero abierto.");

		header = new Header(options.getCipher());

		Cipher c = Cipher.getInstance(options.getCipher());
		c.init(c.ENCRYPT_MODE, keyPair.getPublic());

		int blockSize = 53;

		// Desencriptamos el contenido del ficheroencriptado y lo pasamos al
		// fichero plano
		byte[] buffer = new byte[blockSize];
		header.save(fileOutput);

		int bytes_leidos = fileInput.read(buffer);
		while (bytes_leidos != -1) {

			byte out[] = c.doFinal(buffer, 0, bytes_leidos);
			// System.out.println(bytes_leidos);

			fileOutput.write(out);
			bytes_leidos = fileInput.read(buffer);
		}
		// Cerramos los ficheros
		fileOutput.close();
		fileInput.close();

		System.out.println("\nFichero cifrado.");
	}

	private static void descifrar() throws Exception {
		if (!cargarClaves()) {
			System.out.println("Para descifrar, obtener KEY.");
			return;
		}

		String ficheroEntrada = menuFicheroCon(".cif");
		String ficheroSalida = ficheroEntrada + ".des";

		// Abrimos los ficheros
		System.out.print("Abriendo fichero... ");
		FileInputStream fileInput = new FileInputStream(ficheroEntrada);
		FileOutputStream fileOutput = new FileOutputStream(ficheroSalida);
		System.out.println("Fichero abierto.");

		header = new Header();

		header.load(fileInput);

		Cipher c = Cipher.getInstance(header.getCipher());
		c.init(c.DECRYPT_MODE, keyPair.getPrivate());

		int blockSizeDes = 64;

		// Desencriptamos el contenido del ficheroencriptado y lo pasamos al
		// fichero plano
		byte[] buffer = new byte[blockSizeDes];

		int bytes_leidos = fileInput.read(buffer);

		while (bytes_leidos != -1) {
			byte out[] = c.doFinal(buffer, 0, bytes_leidos);
			// System.out.println(bytes_leidos);

			fileOutput.write(out);
			bytes_leidos = fileInput.read(buffer);
		}

		// Cerramos los ficheros
		fileOutput.close();
		fileInput.close();

		System.out.println("\nFichero descifrado.");
	}

	private static void firmar() throws Exception {
		if (!cargarClaves()) {
			System.out.println("Para firmar, generar KEY.");
			return;
		}
		String ficheroEntrada = menuFicheroSin();
		String ficheroSalida = ficheroEntrada + ".fir";

		// Abrimos los ficheros
		System.out.print("Abriendo fichero... ");
		FileInputStream fileInput = new FileInputStream(ficheroEntrada);
		System.out.println("Fichero abierto.");

		// Creación de firma
		// Obtener instancia del objeto
		Signature dsa = Signature.getInstance(options.getSigner());

		// Iniciar para crear firma con Clave Privada
		dsa.initSign(keyPair.getPrivate());

		int blockSize = 53;

		byte[] buffer = new byte[blockSize];

		int bytes_leidos = fileInput.read(buffer);
		while (bytes_leidos != -1) {

			// Procesar información a firmar
			dsa.update(buffer, 0, bytes_leidos);

			// System.out.println(bytes_leidos);

			bytes_leidos = fileInput.read(buffer);
		}

		// Obtener la firma
		byte[] sig = dsa.sign();

		fileInput.close();
		FileInputStream fileInput2 = new FileInputStream(ficheroEntrada);
		FileOutputStream fileOutput = new FileOutputStream(ficheroSalida);

		// Guardamos la firma en la cabecera y la escribimos
		header = new Header(options.getSigner(), sig);
		header.save(fileOutput);

		// Copiamos el resto del fichero
		bytes_leidos = fileInput2.read(buffer);
		while (bytes_leidos != -1) {

			// System.out.println(bytes_leidos);
			fileOutput.write(buffer, 0, bytes_leidos);

			bytes_leidos = fileInput2.read(buffer);
		}

		// Cerramos los ficheros
		fileOutput.close();
		fileInput2.close();

		System.out.println("\nFichero firmado.");
	}

	private static void verificarfirma() throws Exception {
		if (!cargarClaves()) {
			System.out.println("Para comprobar firma, generar KEY.");
			return;
		}
		String ficheroEntrada = menuFicheroCon(".fir");
		String ficheroSalida = ficheroEntrada + ".cla";

		// Abrimos los ficheros
		System.out.print("Abriendo fichero... ");
		FileInputStream fileInput = new FileInputStream(ficheroEntrada);
		FileOutputStream fileOutput = new FileOutputStream(ficheroSalida);
		System.out.println("Fichero abierto.");

		header = new Header();

		header.load(fileInput);

		// Creación de firma
		// Obtener instancia del objeto
		Signature dsa = Signature.getInstance(header.getSigner());

		// Iniciar para verificar firma con Clave Pública:
		dsa.initVerify(keyPair.getPublic());

		int blockSize = 53;

		byte[] buffer = new byte[blockSize];

		int bytes_leidos = fileInput.read(buffer);
		while (bytes_leidos != -1) {

			// Procesar información a firmada
			dsa.update(buffer, 0, bytes_leidos);

			//System.out.println(bytes_leidos);
			fileOutput.write(buffer, 0, bytes_leidos);

			bytes_leidos = fileInput.read(buffer);
		}

		// Verificar la firma:
		System.out.println((dsa.verify(header.getSign())) ? "\nFirma válida" : "\nFirma no válida.");

		// Cerramos los ficheros
		fileOutput.close();
		fileInput.close();
	}

	public static void main(String args[]) throws Exception {
		bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		options = new Options();
		cargarClaves();
		Menu();
	}

}
