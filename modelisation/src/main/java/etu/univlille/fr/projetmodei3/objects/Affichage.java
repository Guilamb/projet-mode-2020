package etu.univlille.fr.projetmodei3.objects;

import java.io.File;
import java.util.List;
import java.util.StringJoiner;
import java.util.Map.Entry;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Affichage extends VBox{
	
	MenuBar menu;
	Model3D modele;
	Canvas vue;
	AnchorPane commande;
	HBox vueCommande;
	
	
	private double sensibilite = 60.0/360.0;
	boolean voirFace = true;
	
	
	public Affichage() {
		this.menu = new MenuBar();
		this.modele = new Model3D();
		this.vue = new Canvas();
		this.vueCommande = new HBox();
		this.commande = new AnchorPane();
		parametrageTailles();
		parametrageMenu();
		parametrageCommande();
	}
	
	private void parametrageTailles() {
		this.setPrefWidth(1124);
		this.setPrefHeight(775);
		this.getChildren().add(menu);
		this.vueCommande.getChildren().add(vue);
		this.vueCommande.getChildren().add(commande);
		this.vueCommande.setPrefWidth(1124);
		this.vueCommande.setPrefHeight(720);
		this.getChildren().add(vueCommande);
		this.vue.setWidth(984);
		this.vue.setHeight(720);
		this.commande.setPrefWidth(140);
		this.commande.setPrefHeight(720);
	}
	
	
	private void parametrageMenu() {
		this.menu.getMenus().add(new Menu("fichier"));
		MenuItem fichier = new MenuItem("fichier");
		
		fichier.addEventHandler(ActionEvent.ACTION,e->{
			selectModel();
		});
		this.menu.getMenus().get(0).getItems().add(fichier);
		this.menu.getMenus().get(0).getItems().get(0);
	}
	
	private void parametrageCommande() {
		GridPane boutons = new GridPane();
		
		Button affichageFace = new Button("Ne pas voir les faces");
		affichageFace.addEventHandler(ActionEvent.ACTION, e->{
			if(voirFace) {
				voirFace = false;
				affichageFace.setText("Voir les faces");
			} else {
				voirFace = true;
				affichageFace.setText("Ne pas voir les faces");
			}
			affichage(modele);
		});
		affichageFace.setTranslateY(300);
		affichageFace.setPrefWidth(130);
		affichageFace.setPrefHeight(50);

		this.commande.getChildren().add(affichageFace);
		
		Button option = new Button("\\");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(sensibilite,-sensibilite,-sensibilite);
			affichage(modele);
		});
		boutons.add(option,0,0);
		
		option = new Button("|");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(sensibilite,0,0);
			affichage(modele);
		});
		boutons.add(option,1,0);
		
		option = new Button("/");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(sensibilite,sensibilite,sensibilite);
			affichage(modele);
		});
		boutons.add(option,2,0);
		
		option = new Button("-");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(0,-sensibilite,0);
			affichage(modele);
		});
		boutons.add(option,0,1);
		
		option = new Button("o");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(0,0,0);
			affichage(modele);
		});
		boutons.add(option,1,1);
		
		option = new Button("-");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(0,sensibilite,0);
			affichage(modele);
		});
		boutons.add(option,2,1);
		
		option = new Button("/");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(-sensibilite,-sensibilite,-sensibilite);
			affichage(modele);
		});
		boutons.add(option,0,2);
		
		option = new Button("|");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(-sensibilite,0,0);
			affichage(modele);
		});
		boutons.add(option,1,2);
		
		option = new Button("\\");
		option.addEventHandler(ActionEvent.ACTION,e->{
			modele.rotate(-sensibilite,sensibilite,sensibilite);
			affichage(modele);
		});
		boutons.add(option,2,2);
		
		commande.getChildren().add(boutons);
	}
	
	public void selectModel() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "/src/main/resources/"));
	    File selectedDirectory = directoryChooser.showDialog(null);
	    
	    if(selectedDirectory != null) {
	  	  Stage selStage = new Stage();
	  	  selStage.setTitle("select your model");
	  	  VBox vb = new VBox();
	  	  for(Entry<File, List<String>> entry:FolderParser.getCompatibleFiles(new File(System.getProperty("user.dir") + "/src/main/resources/")).entrySet()) {
	  		  Button btn = new Button(entry.getKey().getName());
	  		  StringJoiner sj = new StringJoiner("\n");
	  		  for(String line:entry.getValue()) {
	  			  sj.add(line);
	  		  }
	  		  btn.setTooltip(new Tooltip(sj.toString()));
	  		  btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						try {
							selStage.close();
							modele = Parser.parse(entry.getKey());
							Point centre = modele.getCenter();
							modele.translate(-centre.getX(),-centre.getY(),-centre.getZ());
							autoResize(vue.getWidth(), vue.getHeight());
							affichage(modele);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	  		  vb.getChildren().add(btn);
	  	  }
	  	  selStage.setScene(new Scene(vb));
	  	  selStage.show();
	    }
	}
	public void autoResize(double width, double height) {
		System.out.println("center: "+modele.getCenter());
		double mw = 0;
		double mh = 0;
		Point mcent = modele.getCenter();
		
		for(Point pt:modele.getPoints()) {
			if(Math.abs(pt.getX())-Math.abs(mcent.getX()) > mw)mw = Math.abs(pt.getX())-Math.abs(mcent.getX());
			if(Math.abs(pt.getY())-Math.abs(mcent.getY()) > mh)mh = Math.abs(pt.getY())-Math.abs(mcent.getY());
		}
		
		mw = (width/2)/mw/2;
		mh = (height/2)/mh/2;
		
		modele.zoom(mw < mh?mw:mh);
	}	
	public void affichage(Model3D modele) {
		GraphicsContext gc = this.vue.getGraphicsContext2D();
		this.modele = modele;
		
		gc.clearRect(0,0,this.vue.getWidth(),this.vue.getHeight());
		
		Polygon forme;
		//vue.setTranslateX(vue.getWidth()/2);
		//vue.setTranslateY(vue.getHeight()/2);
		
		int idx = 0;
		double[] xPoints,yPoints;
		
		for(Face f : this.modele.getFaces()) {
			forme = new Polygon();
			idx = 0;
			xPoints = new double[10];
			yPoints = new double[10];
			for(Point p : f.getPoints()) {
				//forme.getPoints().add(p.getX());
				//forme.getPoints().add(p.getY());
				xPoints[idx] = p.getX()+vue.getWidth()/2;
				yPoints[idx] = p.getY()+vue.getHeight()/2;
				idx++;
			}
			forme.setStroke(Color.BLACK);
			forme.setFill(Color.RED);
			
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.RED);
			
			if(voirFace) gc.fillPolygon(xPoints,yPoints,idx);
			gc.strokePolygon(xPoints,yPoints,idx);

			//vue.getChildren().add(forme);
		}

	}	
	
	public void setSensibilite(double angle) {
		this.sensibilite = angle/360.0;
	}
}
