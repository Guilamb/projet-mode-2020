package etu.univlille.fr.projetmodei3.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * La classe Parser, classe utilitaire permettant de creer des instances de Model3D a partir 
 * d'un fichier .ply
 * @author Leopold HUBERT, Maxime BOUTRY, Guilhane BOURGOING, Luca FAUBOURG
 *
 */
public class ParserUtils {

	/**
	 * La méthode principale convertissant un fichier .ply et en extrait les informations nécessaires
	 * à la création d'une instance de Model3D
	 * @param file Le fichier a transformer
	 * @return Le Model3D correspondant au fichier transformé, si ce dernier ne comporte pas d'erreur
	 * @throws Exception Les erreurs trouvés dans le fichier
	 */
	/**
	 * le nombre d'arguments
	 */
	private final static int NBARGUMENTS = 3;
	/**
	 * constucteur
	 * @param file le fichier à parser
	 * @return le modele 3D decrit dans le fichier
	 * @throws Exception
	 */
	public static Model3D parse(File file) throws Exception {
		List<List<String>> headerList = parseHeader(file);
		List<List<String>> list = new ArrayList<>();
		boolean body = false;
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				StringTokenizer tokenizer = new StringTokenizer(scanner.nextLine(), " ");
				int idx = 0;
				String key = null;
				List<String> elements = new ArrayList<>();
				while(tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if(token.length() > 0) {
						if(idx == 0) {
							key = token;
						}
						elements.add(token);
						idx++;
					}
				}
				if(body && key != null && !key.equalsIgnoreCase("comment"))list.add(elements);
				if(!body && key != null && key.equalsIgnoreCase("end_header"))body = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(scanner != null)scanner.close();
		}
		
		List<Element> elements = new ArrayList<>();
		for(List<String> headline:headerList) {
			if(headline.get(0).equalsIgnoreCase("element")) {
				if(headline.size() != NBARGUMENTS)throw new NoSuchElementException("an element must have at least and only 2 arguments");
				int count;
				try {
					count = Integer.parseInt(headline.get(2));
				}catch(Exception e) {
					throw new IllegalArgumentException("the element's count must be a positive Integer");
				}
				if(count < 0)throw new IllegalArgumentException("the element's count must be a positive Integer");
				elements.add(new Element(headline.get(1), count));
			}else if(headline.get(0).equalsIgnoreCase("property")) {
				if(elements.size() == 0)throw new NullPointerException("you must define an element before a property");
				if(headline.size() < NBARGUMENTS)throw new NoSuchElementException("a property must have at least 2 arguments");
				if(headline.get(1).equalsIgnoreCase("list") && headline.size() < 5)throw new NoSuchElementException("list property must have at least 4 arguments");
				Element element = elements.get(elements.size()-1);
				int fe = 1;
				if(headline.get(1).equalsIgnoreCase("list"))fe++;
				if(!element.addProp(headline.get(headline.size()-1), headline.subList(fe, headline.size()-1))) {
					throw new IllegalArgumentException(headline.get(headline.size()-1)+" already exist in the properties");
				}
			}
		}
		
		Model3D model = new Model3D();
		List<Point> vertexes = new ArrayList<>();
		
		int intidx = 0;
		for(Element el:elements) {
			System.out.println("parsing: "+el.name+" ["+el.count+"]");
			List<Entry<String, List<String>>> propList = new ArrayList<>();
			for(Entry<String, List<String>> prop:el.properties.entrySet()) {
				propList.add(prop);
			}
			if(list.size() < intidx+el.count) throw new NoSuchElementException("missing arguments for element "+el.name);
			for(int i = intidx; i < intidx+el.count; i++) {
				List<String> line = list.get(i);
				Point point = new Point();
				Face face = new Face();
				if(line.size() < el.properties.size()) throw new NoSuchElementException("missing elements on declared value for element "+el.name);
				if(el.name.equalsIgnoreCase("face")) {
					int lstsz = (int) getDoubleValueFromArg(propList.get(0).getValue().get(0).toLowerCase(), line.get(0));
					if(lstsz != line.size()-1) throw new NoSuchElementException("arguments given for the list didn't correspond to the declared size");
				}
				for(int e = 0; e < line.size(); e++) {
					if(el.name.equalsIgnoreCase("vertex")) {
						String datatype = propList.get(e).getValue().get(0).toLowerCase();
						String dataname = propList.get(e).getKey().toLowerCase();
						if(dataname.equals("x") || dataname.equals("y") || dataname.equals("z") || dataname.equals("nx") || dataname.equals("ny") || dataname.equals("nz")) {
							double value = getDoubleValueFromArg(datatype, line.get(e));
							switch(dataname) {
								case "x":
									point.setX(value);
									break;
								case "y":
									point.setY(value);
									break;
								case "z":
									point.setZ(value);
									break;
								default:
									break;
							}
						}
					}else if(el.name.equalsIgnoreCase("face")) {
						if(e != 0) {
							int ptidx = (int) getDoubleValueFromArg(propList.get(0).getValue().get(1).toLowerCase(), line.get(e));
							if(ptidx >= vertexes.size())throw new NullPointerException("argument \""+line.get(e)+"\" pointing to an unexisting vertex");
							face.addPoints(vertexes.get(ptidx));
						}
					}
				}
				if(el.name.equalsIgnoreCase("vertex")) {
					vertexes.add(point);
				}else if(el.name.equalsIgnoreCase("face")) {
					model.addFaces(face);
				}
			}
			intidx = intidx+el.count;
		}
		System.out.println("parsing completed!");
		
		return model;
	}
	
	
	/**
	 * Methode permettant la capture d'erreur en cas d'incohérence dans le fichier .ply
	 * Il compare le type attendu et le type de la chaine de caractère à tester
	 * La valeur retournée est la traduction de la chaine de caractère 
	 * @param type Le type attendu
	 * @param arg La chaine qui sera parser à l'aide du type attendu
	 * @return Resultat de la traduction de la chaine de caractère
	 */
	public static double getDoubleValueFromArg(String type, String arg) {
		double value = 0;
		if(type.startsWith("short") || type.startsWith("ushort")) {
			try {
				value = Short.parseShort(arg);
			} catch (Exception ex) {
				throw new IllegalArgumentException("non matching arg declared type \""+type+"\" and arg value \""+arg+"\"");
			}
		}else if(type.startsWith("char") || type.startsWith("int")) {
			try {
				value = Integer.parseInt(arg);
			} catch (Exception ex) {
				throw new IllegalArgumentException("non matching arg declared type \""+type+"\" and arg value \""+arg+"\"");
			}
		}else if(type.startsWith("uchar") || type.startsWith("uint")) {
			try {
				value = Integer.parseUnsignedInt(arg);
			} catch (Exception ex) {
				throw new IllegalArgumentException("non matching arg declared type \""+type+"\" and arg value \""+arg+"\"");
			}
		}else if(type.startsWith("float")) {
			try {
				Float f = Float.parseFloat(arg);
				value = f.doubleValue();
			} catch (Exception ex) {
				throw new IllegalArgumentException("non matching arg declared type \""+type+"\" and arg value \""+arg+"\"");
			}
		}else if(type.startsWith("double")) {
			try {
				value = Double.parseDouble(arg);
			} catch (Exception ex) {
				throw new IllegalArgumentException("non matching arg declared type \""+type+"\" and arg value \""+arg+"\"");
			}
		}
		return value;
	}
	/**
	 * methode servant à parcourir le header
	 * @param file le fichier à parcourir
	 * @return une list de list de String contenant les informations du header
	 */
	public static List<List<String>> parseHeader(File file){
		List<List<String>> list = new ArrayList<>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				StringTokenizer tokenizer = new StringTokenizer(scanner.nextLine(), " ");
				int idx = 0;
				String key = null;
				List<String> elements = new ArrayList<>();
				while(tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if(token.length() > 0) {
						if(idx == 0) {
							key = token;
						}
						elements.add(token);
						idx++;
					}
				}
				if(key != null && key.equalsIgnoreCase("end_header"))break;
				if(key != null)list.add(elements);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(scanner != null)scanner.close();
		}
		
		return list;
	}
	/**
	 * decrit un element
	 * @author grp I3
	 *
	 */
	public static class Element {
		/**
		 * nom de l'element
		 */
		public String name;
		/**
		 * nombre d'elements
		 */
		public int count;
		/**
		 * liste des proprietes 
		 */
		public LinkedHashMap<String, List<String>> properties = new LinkedHashMap<>();
		/**
		 * constructeur
		 * @param name des elements
		 * @param count nombre d'elements
		 */
		public Element(String name, int count) {
			this.name = name;
			this.count = count;
		}
		/**
		 * ajoute une propriete
		 * @param name nom de la propriete
		 * @param args liste d'arguments
		 * @return un boolean indiquant si l'ajout s'est bien passé
		 */
		public boolean addProp(String name, List<String> args) {
			boolean isAdded = true;
			if(name == null || args == null || args.size() == 0 || properties.containsKey(name))isAdded = false;
			properties.put(name, args);
			return isAdded;
		}
		
	}
	
	/**
	 * Methode permetant de parser le nombre de faces ou de point, servant pour les methodes tri des
	 * classes implémentants l'interface Tri
	 * @param file Fichier à trier
	 * @param element Element du ficher qu'on recherche : nombre de Face ou de Points
	 * @return Le nombre de l'élément recherché
	 */
	public static int parseNb(File file, String element) {
		Scanner sc = null;
		int nb = -1;
		try {
			sc = new Scanner(file);
			StringTokenizer st;
			while(sc.hasNextLine()) {
				st = new StringTokenizer(sc.nextLine());
				if( (st.hasMoreTokens() && st.nextToken().equals("element") )&& (st.hasMoreTokens() &&st.nextToken().equals(element) )) {
					if(st.hasMoreTokens()) {
						nb = Integer.parseInt(st.nextToken());
						System.out.println("On a "+nb+ " "+element +" pour le fichier" +file.getName());
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(sc != null) sc.close();
		}
		
		return nb;
	}
	
}
