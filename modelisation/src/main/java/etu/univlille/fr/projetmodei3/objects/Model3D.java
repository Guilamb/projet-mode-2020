package etu.univlille.fr.projetmodei3.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import etu.univlille.fr.projetmodei3.interfaces.PointCloud;
import etu.univlille.fr.projetmodei3.utils.MathsUtils;
import etu.univlille.fr.projetmodei3.utils.Matrix;

public class Model3D implements PointCloud{

	private List<Face> faces;
	private Affichage vue;

	Point posLumiere = new Point(500,0,0);
	private boolean voirFace = true, voirArrete = true;
	
	
	public Model3D(Face... faces) {
		this.faces = new ArrayList<>();
		addFaces(faces);
	}

	public void addFaces(Face... faces) {
		for (Face face : faces) {
			if (face != null && !this.faces.contains(face))
				this.faces.add(face);
		}
	}

	public void remFaces(Face... faces) {
		this.faces.removeAll(Arrays.asList(faces));
	}

	public List<Face> getFaces() {
		Collections.sort(this.faces);
		List<Face> showfaces = new ArrayList<>();
		
		for(Face face:this.faces) {
			Vector3D vn = MathsUtils.getNormal(face.getPoints());
			Point center = face.getCenter();
			//if(center.getZ()>center.getZ()+vn.getZ()) {
				showfaces.add(face);
			//}
		}
		//System.out.println("Nombre de faces renvoy�s par getFaces() : "+showfaces.size());
		return showfaces;
	}

	public List<Point> getPoints() {
		List<Point> points = new ArrayList<>();
		for(Face face:this.faces) {
			for(Point point:face.getPoints()) {
				if(!points.contains(point))points.add(point);
			}
		}
		return points;
	}

	public Point getCenter() {
		List<Point> points = this.getPoints();
		if(points.size() == 0)return new Point(0, 0, 0);
		
		double minx = points.get(0).getX();
		double miny = points.get(0).getY();
		double minz = points.get(0).getZ();
		double maxx = points.get(0).getX();
		double maxy = points.get(0).getY();
		double maxz = points.get(0).getZ();
		
		for(Point point:points) {
			minx = point.getX()<minx?point.getX():minx;
			miny = point.getY()<miny?point.getY():miny;
			minz = point.getZ()<minz?point.getZ():minz;
			
			maxx = point.getX()>maxx?point.getX():maxx;
			maxy = point.getY()>maxy?point.getY():maxy;
			maxz = point.getZ()>maxz?point.getZ():maxz;
		}
		
		return new Point(MathsUtils.getSegmentCenter(minx, maxx), MathsUtils.getSegmentCenter(miny, maxy), MathsUtils.getSegmentCenter(minz, maxz));
	}

	public void rotate(double x, double y, double z) {

		Point centre = getCenter();
		System.out.println(centre);
		//this.translate(-centre.getX(),-centre.getY(),-centre.getZ());
		for(Point p : getPoints()) {
			if(x != 0) rotateX(p,x);
			if(y != 0) rotateY(p,y);
			if(z != 0) rotateZ(p,z);

		}
		//this.translate(centre.getX(),centre.getY(),centre.getZ());
		if(vue != null)
			vue.affichage();
	}

	public void translate(double x, double y, double z) {
		for (Point p : this.getPoints()) {
			p.setX(p.getX() + x);
			p.setY(p.getY() + y);
			p.setZ(p.getZ() + z);
		}
		if(vue != null)
			vue.affichage();
	}
	
	public void zoom(double valeur) { 
		
		double[][] pointsModele = new double[4][getPoints().size()];
		
		System.out.println("Centre avant : "+getCenter());
		Point centre = getCenter();
		this.translate(-centre.getX(),-centre.getY(),-centre.getZ());

		System.out.println("centre : "+getCenter());
		
		for(Point p : getPoints()) {
			p.setX(p.getX()*valeur);
			p.setY(p.getY()*valeur);
			p.setZ(p.getZ()*valeur);
		}
		
		this.translate(centre.getX(),centre.getY(),centre.getZ());
		
		
		System.out.println("Centre après : "+getCenter());
		if(vue != null)
			vue.affichage();
	}
	
	
	public void rotateX(Point p,double degree) { 
		Point psauv = new Point();
		psauv.setX(p.getX());
		psauv.setY(p.getY());
		psauv.setZ(p.getZ());
		p.setX(psauv.getX()) ;
		p.setY(psauv.getY() * Math.cos(degree) - psauv.getZ() * Math.sin(degree));
		p.setZ(psauv.getY()  * Math.sin(degree) + (psauv.getZ() * Math.cos(degree)));
	}
	public void rotateY(Point p, double degree) {
		Point psauv = new Point();
		psauv.setX(p.getX());
		psauv.setY(p.getY());
		psauv.setZ(p.getZ());
		p.setX(psauv.getX() *  Math.cos(degree) + psauv.getZ()*  Math.sin(degree)) ;
		p.setY(psauv.getY());
		p.setZ(-psauv.getX() * Math.sin(degree) + psauv.getZ() * Math.cos(degree));

	}
	public void rotateZ(Point p, double degree) {
		Point psauv = new Point();
		psauv.setX(p.getX());
		psauv.setY(p.getY());
		psauv.setZ(p.getZ());
		p.setX(psauv.getX()  * Math.cos(degree) - psauv.getY() * Math.sin(degree)) ;
		p.setY(psauv.getY() *  Math.cos(degree) + psauv.getX() *  Math.sin(degree));
		p.setZ(psauv.getZ());
	}
	
	public void reset() {
		Point centre = this.getCenter();
		this.translate(-centre.getX(),-centre.getY(),-centre.getZ());
	}

	
	public void setVue(Affichage vue) {
		this.vue = vue;
		vue.setModele(this);
		vue.affichage();
	}
	
	public Point getLumiere() {
		return this.posLumiere;
	}
	
	public boolean vueFaceOn() {
		return this.voirFace;
	}
	public boolean vueArreteOn() {
		return this.voirArrete;
	}
	public void setLumiere(double x, double y, double z) {
		this.posLumiere.setX(x+ vue.getWidth()/2);
		this.posLumiere.setY(y+ vue.getWidth()/2);
		this.posLumiere.setZ(z);
		vue.affichage();
	}
	
	public Affichage getVue() {
		return this.vue;
	}
	
	public void switchVueFace() {
		this.voirFace = !voirFace;
		vue.affichage();
	}
	public void switchVueArrete() {
		this.voirArrete = !voirArrete;
		vue.affichage();
	}
	@Override
	public String toString() {
		return "Model3D [faces=" + faces + "]";
	}

}
