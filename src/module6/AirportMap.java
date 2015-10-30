package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {
		// setting up PAppler
		size(850,650, OPENGL);
		AbstractMapProvider provider = new Google.GoogleTerrainProvider();
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550,provider);
		map.zoomAndPanTo(5, new Location(1.3f, 103.8f));
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);

			m.setRadius(5);
			airportList.add(m);
			//System.out.println(m.getProperty("code"));
			//System.out.println(m.getProperty("city"));
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
		}


		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds

			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));


			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
			//System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}


		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);
		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
		
		map.addMarkers(airportList);
		
	}
	
	public void draw() {
		background(0);
		map.draw();
		
	}

	@Override
	public void mouseMoved() {
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;

		}
		selectMarkerIfHover(airportList);
	}
	private void selectMarkerIfHover(List<Marker> markers) {
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers)
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}

	}
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null)
		{
			checkAirportClick();

		}
	}
	private void unhideMarkers() {
		for(Marker marker : airportList) {
			marker.setHidden(false);
		}

		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
	}
	private void checkAirportClick(){
		if (lastClicked != null) return;
		for (Marker m : airportList) {
			if (!m.isHidden() && m.isInside(map, mouseX, mouseY)) {
				//System.out.println(m.getProperty("source"));
				lastClicked = (CommonMarker) m;
				for (Marker mhide : airportList) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
						lastClicked.setClicked(true);
					}

				}
				System.out.println(m.getProperty("source"));
				for(Marker r:routeList){
					if (r.getProperty("source").equals(m.getProperty("source")) ||
							r.getProperty("destination").equals(m.getProperty("source"))){
						r.setHidden(false);
						for (Marker mhide : airportList) {
							if (mhide.getProperty("source").equals(r.getProperty("destination"))){
								mhide.setHidden(false);
							}
						}

					}
					//System.out.println(r.getProperty("source"));
				}

			}

		}


	}

}
