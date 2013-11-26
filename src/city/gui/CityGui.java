package city.gui;

import interfaces.Restaurant2Waiter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import restaurant1.Restaurant1CustomerRole;
import restaurant1.Restaurant1WaiterRole;
import restaurant1.gui.Restaurant1AnimationPanel;
import restaurant1.gui.Restaurant1CustomerGui;
import city.Restaurant2.Restaurant2CashierRole;
import city.Restaurant2.Restaurant2CookRole;
import city.Restaurant2.Restaurant2CustomerRole;
import city.Restaurant2.Restaurant2HostRole;
import city.Restaurant2.Restaurant2WaiterRole;
import city.gui.Bank.BankAnimationPanel;
import city.gui.House.ApartmentAnimationPanel;
import city.gui.House.HouseAnimationPanel;
import city.gui.Market.MarketAnimationPanel;
import Role.Role;
import astar.AStarTraversal;
import city.Restaurant2.Restaurant2;
import city.gui.CityClock;
import city.gui.restaurant4.AnimationPanel4;
import city.CityMap;
import city.House;
import city.Market;
import city.PersonAgent;
import city.gui.restaurant2.Restaurant2AnimationPanel;
import city.gui.restaurant2.Restaurant2CustomerGui;
import city.transportation.BusAgent;
import city.transportation.Vehicle;
import city.Restaurant3.*;
import city.gui.Restaurant3.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class CityGui extends JFrame implements ActionListener, ChangeListener {

	AnimationPanel animationPanel = new AnimationPanel();

	ControlPanel controlPanel = new ControlPanel();

	// Restaurants
	
		// Restaurant 1 (Trevor)
		Restaurant1AnimationPanel restaurant1 = new Restaurant1AnimationPanel();

		// Restaurant 2 (Holly)
		Restaurant2 rest2 = new Restaurant2();
		Restaurant2AnimationPanel restaurant2 = new Restaurant2AnimationPanel(rest2);
		
		// Restaurant 3 (Grant)
		//Restaurant3 rest3 = new Restaurant3();
		AnimationPanel3 restaurant3 = new AnimationPanel3();
		
		// Restaurant 4 (Justine)
		AnimationPanel4 restaurant4 = new AnimationPanel4();
		
		// Restaurant 5 (Tom)
		

	// Market Animation Panels
	MarketAnimationPanel market1Animation = new MarketAnimationPanel(this);
	MarketAnimationPanel market2Animation = new MarketAnimationPanel(this);
	MarketAnimationPanel market3Animation = new MarketAnimationPanel(this);
	
	// Bank Animation Panels
	BankAnimationPanel bank1Animation = new BankAnimationPanel(this);
	
	// Apartment Animation Panels
	ApartmentAnimationPanel apt1= new ApartmentAnimationPanel(1);
	ArrayList<HouseAnimationPanel> apt1List= new ArrayList<HouseAnimationPanel>();
	ApartmentAnimationPanel apt2= new ApartmentAnimationPanel(2);
	ArrayList<HouseAnimationPanel> apt2List= new ArrayList<HouseAnimationPanel>();
	
	//HouseAnimationPanel house1= new HouseAnimationPanel();
	ArrayList<HouseAnimationPanel> houses = new ArrayList<HouseAnimationPanel>();
	
	// Master list of city buildings
	List<BuildingPanel> buildingPanels = new ArrayList<BuildingPanel>();

	private JPanel infoPanel;

	private final int WINDOWX = 1300;
	private final int WINDOWY = 750;
	private final int ANIMATIONX = 900;
	private final int WINDOW_X_COORD = 50;
	private final int WINDOW_Y_COORD = 50;

	ArrayList<Gui> guis = new ArrayList<Gui>();
	ArrayList<PersonAgent> people = new ArrayList<PersonAgent>();

	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

	/**
	 * Constructor for RestaurantGui class.
	 * Sets up all the gui components.
	 */
	public CityGui() {            
		controlPanel.setCityGui(this);

		//testPerson.startThread();
		//testPerson.setGui(testPersonGui);
		//testPersonGui.addAnimationPanel(restaurant2);
		//guis.add(testPersonGui);
		//cityPanel.addGui(testPersonGui);

		setBounds(WINDOW_X_COORD, WINDOW_Y_COORD, WINDOWX, WINDOWY);

		setLayout(new BorderLayout());

		animationPanel.setCityGui(this);
		animationPanel.setPreferredSize(new Dimension(ANIMATIONX, WINDOWY));

		//Add all building animation panels to the building panel list!
		//This automatically sets dimensions and cityGui references
		restaurant2.setBackground(new Color(150, 20, 60));
		addBuildingPanel(restaurant2);
		controlPanel.addRest2ToCityMap(rest2);
		restaurant1.setBackground(Color.LIGHT_GRAY);
		addBuildingPanel(restaurant1);

		addBuildingPanel(restaurant4);

		addBuildingPanel(bank1Animation);
		addBuildingPanel(market1Animation);
		addBuildingPanel(market2Animation);
		addBuildingPanel(market3Animation);

		add(animationPanel, BorderLayout.EAST);

		//Set up and populate apartment 1
		addBuildingPanel(apt1);
		for(int i=0; i<10; i++){
			apt1List.add(new HouseAnimationPanel());
			addBuildingPanel(apt1List.get(i));
			buildingPanels.add(apt1List.get(i));
		}
		//Set up and populate apartment 2
		addBuildingPanel(apt2);
		for(int i=0; i<10; i++){
			apt2List.add(new HouseAnimationPanel());
			addBuildingPanel(apt2List.get(i));
			buildingPanels.add(apt2List.get(i));
		}
		
		//Set up all of the houses
		for(int i=0; i<26; i++){
			houses.add(new HouseAnimationPanel());
			addBuildingPanel(houses.get(i));
		}
		//addBuildingPanel(house1);
		//End of adding building panels!

		Dimension panelDim = new Dimension(WINDOWX - ANIMATIONX, WINDOWY);
		infoPanel = new JPanel();
		infoPanel.setPreferredSize(panelDim);
		infoPanel.setMinimumSize(panelDim);
		infoPanel.setMaximumSize(panelDim);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

		infoPanel.setLayout(new FlowLayout());

		//add(infoPanel, BorderLayout.WEST);
		add(controlPanel, BorderLayout.WEST);

		CityClock masterClock = new CityClock(this);
		masterClock.startTime();

	}

	public void timerTick(int timeOfDay, int hourOfDayHumanTime, long minuteOfDay, String dayState, String amPm, String displayTime) {
		for (PersonAgent person : people) {
			person.msgTimeUpdate(timeOfDay);
		}
		controlPanel.setTimeDisplay(displayTime);
	}

	public void addGui(Gui g){
		guis.add(g);
	}

	public void addPerson(PersonAgent p){
		people.add(p);
	}

	public void actionPerformed(ActionEvent e) {
		//if(e.getSource() == 
	}

	/**
	 * Main routine to get gui started
	 */
	public static void main(String[] args) {
		CityGui gui = new CityGui();
		gui.setTitle("SimCity201");
		gui.setVisible(true);
		gui.setResizable(true);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void stateChanged(ChangeEvent e) {
		//if(e.getSource() ==
		//(slider)
	}

	public void changeView(String building){
		if(building.equals("City")){
			for(BuildingPanel bp : buildingPanels) {
				bp.setVisible(false);
			}
			animationPanel.setVisible(true);
			animationPanel.setEnabled(true);
			add(animationPanel, BorderLayout.EAST);
			return;
		}  
		
		controlPanel.enableBackToCity(); //If not switching to city, enable "Change back to city view" button
		
		if(building.equals("Restaurant2")){
			animationPanel.setVisible(false);
			add(restaurant2, BorderLayout.EAST);
			restaurant2.setVisible(true);
			return;
		}     
		if(building.equals("Restaurant1")){
			animationPanel.setVisible(false);
			add(restaurant1, BorderLayout.EAST);
			restaurant1.setVisible(true);
			return;
		}
		if(building.equals("Restaurant4")){
			animationPanel.setVisible(false);
			add(restaurant4, BorderLayout.EAST);
			restaurant4.setVisible(true);
			return;
		}
		if(building.equals("Market1")){
			animationPanel.setVisible(false);
			add(market1Animation, BorderLayout.EAST);
			market1Animation.setVisible(true);
			return;
		}
		if(building.equals("Market2")){
			animationPanel.setVisible(false);
			add(market2Animation, BorderLayout.EAST);
			market2Animation.setVisible(true);
			return;
		}
		if(building.equals("Market3")){
			animationPanel.setVisible(false);
			add(market3Animation, BorderLayout.EAST);
			market3Animation.setVisible(true);
			return;
		}
		if(building.equals("Bank1")){
			animationPanel.setVisible(false);
			add(bank1Animation, BorderLayout.EAST);
			bank1Animation.setVisible(true);
			return;
		}
		if(building.equals("Apartment1")){
			animationPanel.setVisible(false);
			add(apt1, BorderLayout.EAST);
			apt1.setVisible(true);
			return;
		}
		if(building.equals("Apartment2")){
			animationPanel.setVisible(false);
			add(apt2, BorderLayout.EAST);
			apt2.setVisible(true);
			return;
		}
	}

	public void changeView(int building, int num){
		if(building == 0) {
			animationPanel.setVisible(false);
			add(houses.get(num), BorderLayout.EAST);
			houses.get(num).setVisible(true);
		}
		if(building == 1) {
			apt1.setVisible(false);
			for(int i = 0; i < 10; i++)
				apt1List.get(i).setVisible(false);
			add(apt1List.get(num), BorderLayout.EAST);
			apt1List.get(num).setVisible(true);
		}
		if(building == 2){
			apt2.setVisible(false);
			for(int i = 0; i < 10; i++)
				apt2List.get(i).setVisible(false);
			add(apt2List.get(num), BorderLayout.EAST);
			apt2List.get(num).setVisible(true);
		}
		controlPanel.enableBackToCity();
	}

	public void addPerson(String name, AStarTraversal aStarTraversal, String job, CityMap map, House h){
		PersonAgent newPerson = new PersonAgent(name, aStarTraversal, map, h);
		
		personFactory(newPerson, job);
		
		people.add(newPerson);
		PersonGui g = new PersonGui(newPerson);
		newPerson.setGui(g);
		if(job.equals("No job") || job.equals("Restaurant2 Waiter")){
			animationPanel.addGui(g);
		}
		guis.add(g);
		g.addAnimationPanel(restaurant2);
		
		newPerson.startThread();

		if(name.equals("rest2Test")){
			//newPerson.msgImHungry();
		}
		
		if(name.equals("rest1test")) {
			//newPerson.msgImHungry();
		}
	}

	public void enableComeBack(Restaurant2Waiter agent) {
		// TODO Auto-generated method stub

	}

	public void setEnabled(Restaurant2Waiter agent) {
		// TODO Auto-generated method stub

	}        

	public void addVehicle(String type, AStarTraversal aStarTraversal) {
		if(type == "bus") {
			BusAgent newBus = new BusAgent(aStarTraversal);
			newBus.addBusStops(controlPanel.getBusStops());
			vehicles.add(newBus);
			VehicleGui g = new VehicleGui(newBus);
			newBus.setGui(g);
			guis.add(g);
			animationPanel.addGui(g);
			g.setMainAnimationPanel(animationPanel);

			newBus.startThread();   
		}
	}   
	
	private void personFactory(PersonAgent p, int i) {
		if(i == 1) {
			Restaurant2CustomerRole customerRole = new Restaurant2CustomerRole(p);
			Restaurant2CustomerGui customerGui = new Restaurant2CustomerGui(customerRole, "cust", 1);
			restaurant2.addGui(customerGui);
			Restaurant2WaiterRole waiterRole = new Restaurant2WaiterRole("waiter", p);
			p.addFirstJob(waiterRole, "rest2", restaurant2);
			customerRole.setGui(customerGui);
			p.addRole(customerRole, false);
		}
		if(i == 2) {
			Restaurant1CustomerRole customerRole = new Restaurant1CustomerRole(p.getName(), p);
			Restaurant1CustomerGui customerGui = new Restaurant1CustomerGui(customerRole);
			restaurant1.addGui(customerGui);
			Restaurant1WaiterRole waiterRole = new Restaurant1WaiterRole("waiter", p);
			p.addFirstJob(waiterRole, "rest2", restaurant2);
			customerRole.setGui(customerGui);
			p.addRole(customerRole, false);
		}
	}
	
	private void personFactory(PersonAgent p, String job) {
		Restaurant2CustomerRole customerRole = new Restaurant2CustomerRole(p);
		Restaurant2CustomerGui customerGui = new Restaurant2CustomerGui(customerRole, p.getName(), 1);
		customerGui.setPresent(false);
		restaurant2.addGui(customerGui);
		customerRole.setGui(customerGui);
		p.addRole(customerRole, false);
		if(!job.equals("No job")){
			Role r = Role.getNewRole(job, p, this);
			if(job.contains("Restaurant2")){
				p.addFirstJob(r, "rest2", restaurant2);
			}
			if(r instanceof Restaurant2HostRole){
				rest2.setHost((Restaurant2HostRole)r);
				p.setRoleActive(r, true);
			}
			else if(r instanceof Restaurant2WaiterRole){
				rest2.addWaiters((Restaurant2WaiterRole) r);
			}
			else if(r instanceof Restaurant2CookRole){
				rest2.setCook((Restaurant2CookRole) r);
				p.setRoleActive(r, true);
			}
			else if(r instanceof Restaurant2CashierRole){
				rest2.setCashier((Restaurant2CashierRole) r);
				p.setRoleActive(r, true);
			}
		}
	}
	
	public void backToCityView() {
		changeView("City");
	}

	private void addBuildingPanel(BuildingPanel bp) {
		bp.setPreferredSize(new Dimension(ANIMATIONX, WINDOWY));
		buildingPanels.add(bp);
		bp.setCityGui(this);
	}

}