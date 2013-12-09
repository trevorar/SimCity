package city.gui;

import interfaces.BusStop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import restaurant1.Restaurant1;
import city.Apartment;
import city.ApartmentBuilding;
import city.Bank;
import city.CityMap;
import city.House;
import city.Market;
import activityLog.ActivityPane;
import astar.AStarTraversal;
import Role.Role;
import city.Restaurant2.*;
import city.Restaurant3.Restaurant3;
import city.Restaurant4.Restaurant4;
import city.Restaurant5.Restaurant5;
import city.transportation.BusStopAgent;
import city.transportation.CarAgent;

public class ControlPanel extends JPanel implements ActionListener{

	public JScrollPane pane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private JPanel view = new JPanel();
    private List<JButton> personButtonList = new ArrayList<JButton>();
    private JButton addPersonButton = new JButton("Add");
    private JTabbedPane controlPane = new JTabbedPane();
    private JPanel worldControls = new JPanel();
    private JPanel timeSelectionPanel = new JPanel();
    private JPanel worldControlPanel = new JPanel();
    private JPanel addPerson = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JLabel clickBuildings = new JLabel("Click on a building to see inside!");
    private ActivityPane activityPane = new ActivityPane();
    private JButton backToCity = new JButton("Switch back to city view");
    private JButton startScenario = new JButton("Start scenario!");
    private JButton changeTime = new JButton("Change Time");
    private JPanel backButtonPanel = new JPanel();
    private JPanel personOptionsDisplay = new JPanel();
    private JButton buyCarButton = new JButton("Buy a Car");
    
    private String[] scenarios = {"[Please choose a test to run]", "Full Scenario", "Trader Joe's", "Restaurant1",
    		"Restaurant2", "Restaurant3", "Restaurant4", "Restaurant5", "Bank Test", "Car Test", "Landlord Test"
    };
    private JComboBox scenarioSelect = new JComboBox(scenarios);
    
    // Timer GUI display & control functionality
    private JLabel timeDisplay = new JLabel("12:00am  -  Monday  -  Week 1");
    
    private String[] hours = {"12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
    private JComboBox hourSelect = new JComboBox(hours);
    
    private String[] minutes = {"00", "15", "30", "45"};
    private JComboBox minuteSelect = new JComboBox(minutes);
    
    private String[] amPm = {"am", "pm"};
    private JComboBox amPmSelect = new JComboBox(amPm);
    
    
    private Timer timer = new Timer();
    
    private int WINDOWX = 370;
    private int WINDOWY = 750;
    private int SCROLLY = WINDOWY/4 - 20;
    private int ADDPERSONY = WINDOWY/5;
    private int BACKBUTTONY = 40;
    private int INFOPANELY = SCROLLY + 30;
    private int WINDOWXINSIDE = WINDOWX - 10;
    private int PERSONOPTIONSY = WINDOWY - (INFOPANELY + ADDPERSONY);
    
    private Dimension scrollDim = new Dimension(WINDOWXINSIDE, SCROLLY);
    private Dimension panelDim = new Dimension(WINDOWX, WINDOWY - BACKBUTTONY);
    private Dimension addPersonDim = new Dimension(WINDOWXINSIDE, ADDPERSONY);
    private Dimension infoPanelDim = new Dimension(WINDOWXINSIDE, INFOPANELY);
    private Dimension backButtonDim = new Dimension(WINDOWX, BACKBUTTONY);
    private Dimension personOptionsDim = new Dimension(WINDOWXINSIDE, PERSONOPTIONSY);

    private JTextField nameField;
    private JTextField errorDisplay = new JTextField();
    private JPanel personControlPanel = new JPanel();
    public JCheckBox isHungry;
    public JCheckBox takeBreak;
    private String[] jobs = {"[Please select a job]", "No job", "Bank Manager", "Bank Teller", "Market Manager", "Market Worker", "Landlord1", "Landlord2", 
    		"Restaurant1 Host", "Restaurant1 Cook", "Restaurant1 Waiter", "Restaurant1 Cashier","Restaurant2 Host", "Restaurant2 Cook",
    		"Restaurant2 Waiter", "Restaurant2 Cashier", "Restaurant3 Host", "Restaurant3 Cook", "Restaurant3 Waiter", "Restaurant3 Cashier",
    		"Restaurant4 Host", "Restaurant4 Cook", "Restaurant4 Waiter", "Restaurant4 Cashier", "Restaurant5 Host", "Restaurant5 Cook",
    		"Restaurant5 Waiter", "Restaurant5 Cashier"
    };
    private JComboBox jobField = new JComboBox(jobs);
    
    int houseAssignmentNumber = 0;
    
    //TODO populate this
    private Map<String, String> jobLocations = new HashMap<String, String>();
    
    /** Universal city map **/
    CityMap cityMap = new CityMap();
    //Houses and apartments
    private List<House> houses = new ArrayList<House>();
    
    //Bus stops
    private List<BusStop> busStops = new ArrayList<BusStop>();
    
    //Size of astar semaphore grid
    static int gridX = 25; //# of x-axis tiles
    static int gridY = 20; //# of y-axis tiles

    //Semaphore grid for astar animation
    Semaphore[][] streetGrid = new Semaphore[gridX+1][gridY+1];
    Semaphore[][] sidewalkGrid = new Semaphore[gridX+1][gridY+1];
    
    CityGui cityGui;

    /**
     * Constructor for ListPanel.  Sets up all the gui
     *
     * @param rp   reference to the restaurant panel
     * @param type indicates if this is for customers or waiters
     */
    public ControlPanel() {
    	
        view.setLayout(new FlowLayout());
        setLayout(new BoxLayout((Container) this, BoxLayout.PAGE_AXIS));

        setBorder(BorderFactory.createLineBorder(Color.black, 5));
        
        backButtonPanel.add(backToCity);
    	backToCity.addActionListener(this);
    	backToCity.setEnabled(false);
        add(backButtonPanel);
        
        addPersonSection();
        
        setupWorldControls();
                
        controlPane.setPreferredSize(panelDim);
        worldControlPanel.setPreferredSize(panelDim);
        backButtonPanel.setPreferredSize(backButtonDim);
        worldControlPanel.setLayout(new BoxLayout(worldControlPanel, BoxLayout.PAGE_AXIS));
        worldControlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPane.addTab("World", worldControlPanel);
        controlPane.addTab("People", personControlPanel);
        controlPane.addTab("Activity Log", activityPane);
        add(controlPane);
        
        //Set up the grids of semaphores
        populateSemaphoreGrids();
      	
        //Creation of houses and apartments
        createHouses();
      	//Creation of bus stops
        createBusStops();
                
        scenarioSelect.setSelectedIndex(1);
    }
    
    public void addRest1ToCityMap(Restaurant1 r) {
    	cityMap.setRestaurant1(r);
    }
    
    public void addRest2ToCityMap(Restaurant2 r){
        cityMap.setRestaurant2(r);
    }
    
    public void addRest3ToCityMap(Restaurant3 r){
    	cityMap.setRestaurant3(r);
    }
    
    public void addRest4ToCityMap(Restaurant4 r) {
    	cityMap.setRestaurant4(r);
    }
    
    public void addRest5ToCityMap(Restaurant5 r) {
    	cityMap.seRestaurant5(r);
    }
    
    public void addMarketToCityMap(Market m) {
    	cityMap.setMarket(m);
    }
    
    public void addBankToCityMap(Bank b) {
    	cityMap.setBank(b);
    }
    
    public void addApartment1ToCityMap(ApartmentBuilding b){
    	cityMap.setApartment1(b);
    }
    
    public void addApartment2ToCityMap(ApartmentBuilding b){
    	cityMap.setApartment2(b);
    }
    
    public void setCityGui(CityGui c){
    	cityGui = c;
    }
    
    public List<BusStop> getBusStops() {
    	return busStops;
    }
    
    public CityMap getCityMap() {
    	return cityMap;
    }
    
    private void setupWorldControls(){
    	
    	// Scenario selection
    	Dimension dropDownSize = new Dimension(WINDOWX, 30);
    	startScenario.addActionListener(this);
    	scenarioSelect.addActionListener(this);
    	scenarioSelect.setPreferredSize(dropDownSize);
    	scenarioSelect.setMaximumSize(dropDownSize);
    	
    	// Manual timer Controls
    	Dimension timerControlDropdownSize = new Dimension(70, 30);
    	changeTime.addActionListener(this);
    	
    	// Hour dropdown
    	hourSelect.addActionListener(this);
    	hourSelect.setPreferredSize(timerControlDropdownSize);
    	hourSelect.setMaximumSize(timerControlDropdownSize);
    	
    	// Minute Dropdown
    	minuteSelect.addActionListener(this);
    	minuteSelect.setPreferredSize(timerControlDropdownSize);
    	minuteSelect.setMaximumSize(timerControlDropdownSize);
    	
    	// am/pm Select Dropdown
    	amPmSelect.addActionListener(this);
    	amPmSelect.setPreferredSize(timerControlDropdownSize);
    	amPmSelect.setMaximumSize(timerControlDropdownSize);
    	
    	// Add all to single panel
    	changeTime.setEnabled(false);
    	timeSelectionPanel.add(hourSelect);
    	timeSelectionPanel.add(minuteSelect);
    	timeSelectionPanel.add(amPmSelect);
    	timeSelectionPanel.add(Box.createVerticalStrut(1));
    	timeSelectionPanel.add(changeTime);
    	
    	//This add(Box) function creates a space on the JPanel - using it here for spacing the buttons out to look nice
    	worldControlPanel.add(Box.createVerticalStrut(10));
    	clickBuildings.setFont(new Font("Trebuchet", Font.BOLD, 14));
    	worldControlPanel.add(clickBuildings);
    	worldControlPanel.add(Box.createVerticalStrut(10));
    	JLabel title = new JLabel("Running a scenario: ");
    	title.setAlignmentX(Component.CENTER_ALIGNMENT);
    	worldControlPanel.add(title);
    	worldControlPanel.add(scenarioSelect);
    	clickBuildings.setAlignmentX(Component.CENTER_ALIGNMENT);
    	backToCity.setAlignmentX(Component.CENTER_ALIGNMENT);
    	worldControlPanel.add(Box.createVerticalStrut(10));
    	worldControlPanel.add(startScenario);
    	startScenario.setAlignmentX(Component.CENTER_ALIGNMENT);
    	worldControlPanel.add(Box.createVerticalStrut(10));
    	worldControlPanel.add(timeDisplay);
    	timeDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
    	worldControlPanel.add(timeSelectionPanel);
    }
    
    private void addPersonSection(){
    	//personControls.add(new JLabel("<html><br><u>Add People</u><br></html>"));
    	
    	//addPerson.setAlignmentX(Component.CENTER_ALIGNMENT);
    	
    	personControlPanel.setPreferredSize(panelDim);
    	
    	addPerson.setPreferredSize(addPersonDim);
    	infoPanel.setPreferredSize(infoPanelDim);
    	personOptionsDisplay.setPreferredSize(personOptionsDim);
        pane.setViewportView(view);
        
        infoPanel.add(new JLabel("List of people in SimCity"));
    	infoPanel.add(pane);
    	
    	//Add AddPerson panel and info panel to main panel
    	FlowLayout controlsFlow = new FlowLayout();
    	personControlPanel.setLayout(controlsFlow);
    	personControlPanel.add(addPerson, controlsFlow);
    	//personControlPanel.add(personOptionsDisplay, controlsFlow);
    	personControlPanel.add(infoPanel, controlsFlow);
    	personControlPanel.add(personOptionsDisplay, controlsFlow);
    	
    	personOptionsDisplay.setBorder(BorderFactory.createLineBorder(Color.black));
    	
    	buyCarButton.addActionListener(this);
    	personOptionsDisplay.add(new JLabel("Person Options"));
		addPerson.add(Box.createVerticalStrut(10));
    	//personOptionsDisplay.add(buyCarButton);
        
        pane.setMinimumSize(scrollDim);
        pane.setMaximumSize(scrollDim);
        pane.setPreferredSize(scrollDim);
        
        //set layout of control panel
        FlowLayout flow = new FlowLayout();
        addPerson.setLayout(new BoxLayout(addPerson, BoxLayout.PAGE_AXIS));
        
        //Adding enter name section
        addPerson.add(new JLabel("Name:"));
        nameField = new JTextField();
        nameField.setColumns(16);
        addPerson.add(nameField, flow);
        
        //Adding enter job section
        addPerson.add(new JLabel("Job: "));

        addPerson.add(jobField, flow);
        
        isHungry = new JCheckBox("Hungry?");
        isHungry.setEnabled(false);
        isHungry.addActionListener(this);
        //enterNames.add(isHungry);
        
        nameField.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(nameField.getText().length() > 0){
					isHungry.setEnabled(true);
				}
				else isHungry.setEnabled(false);
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(nameField.getText().length() > 0){
					isHungry.setEnabled(true);
				}
				else isHungry.setEnabled(false);
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				if(nameField.getText().length() > 0){
					isHungry.setEnabled(true);
				}
				else{
					isHungry.setEnabled(false);
				}
			}

		});

		addPersonButton.addActionListener(this);
		addPerson.add(Box.createVerticalStrut(10));
		addPerson.add(addPersonButton, flow);
		addPerson.add(Box.createVerticalStrut(10));

		errorDisplay.setEditable(false);
		addPerson.add(errorDisplay, flow);
		
		view.setLayout(new BoxLayout((Container) view, BoxLayout.Y_AXIS));
		this.add(personControlPanel);
	}


	/**
	 * Method from the ActionListener interface.
	 * Handles the event of the add button being pressed
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addPersonButton) {
			// Chapter 2.19 describes showInputDialog()
			if(!nameField.getText().equals("")){
				//TODO Should we add a check here for duplicate names?
				//Might matter for the car buying, if two people have the same name
				String job = null;
				if(jobField.getSelectedIndex() == 0){
					errorDisplay.setText("Please select a job");
				}
				else{
					errorDisplay.setText("");
					job = (String)jobField.getSelectedItem();
					addPerson(nameField.getText(), job);
					nameField.setText("");
					isHungry.setSelected(false);
				}
			}
			else{
				errorDisplay.setText("Please enter a name for the person");
			}
		}
		else if(e.getSource() == startScenario){
			if(scenarioSelect.getSelectedIndex() != 0){
				populateCity((String)scenarioSelect.getSelectedItem());
				cityGui.startMasterClock();
				startScenario.setEnabled(false);
				changeTime.setEnabled(true);
			}
		}
		else if(e.getSource() == backToCity) {
			cityGui.backToCityView();
			backToCity.setEnabled(false);
		}
		else if(e.getSource() == changeTime) {
			
			cityGui.setTime(hourSelect.getSelectedItem().toString(), minuteSelect.getSelectedItem().toString(), amPmSelect.getSelectedItem().toString());
		
		} else if(e.getSource() == buyCarButton){
			// Coming soon
		}
		
	}

	/**
	 * If the add button is pressed, this function creates
	 * a spot for it in the scroll pane, and tells the restaurant panel
	 * to add a new person.
	 *
	 * @param name name of new person
	 */
	public void addPerson(String name, String job) {
		if (name != null) {
			JButton button = new JButton(name);
			button.setBackground(Color.white);

			AStarTraversal aStarTraversal = new AStarTraversal(sidewalkGrid);

			House house = houses.get(houseAssignmentNumber);
			if(houseAssignmentNumber == 62){
				houseAssignmentNumber = 61;
			}
			else{
				houseAssignmentNumber++;
			}

			cityGui.addPerson(name, aStarTraversal, job, cityMap, house, null);
			System.out.println("Adding person " + name + " with job " + job);

			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension((paneSize.width - 20),
					(int) (paneSize.height / 7));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener(this);
			personButtonList.add(button);
			view.add(button);


			isHungry.setEnabled(false);
			validate();
		}
	}

	public void addPersonNoHouse(String name, String job) {
		if (name != null) {
			JButton button = new JButton(name);
			button.setBackground(Color.white);

			AStarTraversal aStarTraversal = new AStarTraversal(sidewalkGrid);

			cityGui.addPerson(name, aStarTraversal, job, cityMap, null, null);
			System.out.println("Adding person " + name + " with job " + job);

			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension((paneSize.width - 20),
					(int) (paneSize.height / 7));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener(this);
			personButtonList.add(button);
			view.add(button);
			isHungry.setEnabled(false);
			validate();
		}
	}

	public void addPersonWithCar(String name, String job) {
		if (name != null) {
			JButton button = new JButton(name);
			button.setBackground(Color.white);

			AStarTraversal streetTraversal = new AStarTraversal(streetGrid);              
			CarAgent car = new CarAgent(streetTraversal, cityMap);
			House house = houses.get(houseAssignmentNumber);
			if(houseAssignmentNumber == 62){
				houseAssignmentNumber = 61;
			}
			else{
				houseAssignmentNumber++;
			}

			AStarTraversal aStarTraversal = new AStarTraversal(sidewalkGrid);

			cityGui.addPerson(name, aStarTraversal, job, cityMap, house, car);
			System.out.println("Adding person " + name + " with job " + job);

			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension((paneSize.width - 20),
					(int) (paneSize.height / 7));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener(this);
			personButtonList.add(button);
			view.add(button);
			isHungry.setEnabled(false);
			validate();
		}
	}

	public void addVehicle(String type) {
		AStarTraversal aStarTraversal = new AStarTraversal(streetGrid); //Create new aStarTraversal using streetGrid instead of sidewalkGrid

		cityGui.addVehicle(type, aStarTraversal);
	}

	private void populateSemaphoreGrids() {

		/*********Setting up semaphore grid***********/
		for (int i = 0; i <= gridX; i++) {
			for (int j = 0; j <= gridY; j++) {
				streetGrid[i][j] = new Semaphore(0,true);
				sidewalkGrid[i][j] = new Semaphore(0,true);
			}
		}

		//Releasing all roads and sidewalks so guis can move around on them.
		//First, the roads
		for(int i = 3; i < 19; i++) { //Top and bottom portions of road
			for(int j = 3; j < 7; j++)
				streetGrid[i][j].release();
			for(int j = 12; j < 16; j++)
				streetGrid[i][j].release();
		}
		for(int i = 7; i < 12; i++) { //Left and right portions of road
			for(int j = 3; j < 7; j++)
				streetGrid[j][i].release();
			for(int j = 15; j < 19; j++)
				streetGrid[j][i].release();
		}

		for(int i = 15; i < 19; i++) //Extra portions of road in bottom right corner
			for(int j = 16; j < 21; j++)
				streetGrid[i][j].release();
		for(int i =  19; i < 26; i++)
			for(int j = 12; j < 16; j++)
				streetGrid[i][j].release();

		//Release sidewalk semaphores
		for(int i = 1; i < 21; i++) //Top sidewalk
			for(int j = 1; j < 3; j++)
				sidewalkGrid[i][j].release();

		for(int i = 1; i < 15; i++) //Bottom sidewalk
			for(int j = 16; j < 18; j++)
				sidewalkGrid[i][j].release();

		for(int i = 3; i < 16; i++) //Left sidewalk
			for(int j = 1; j < 3; j++)
				sidewalkGrid[j][i].release();

		for(int i = 3; i < 12; i++) //Right sidewalk
			for(int j = 19; j < 21; j++)
				sidewalkGrid[j][i].release();

		for(int i = 21; i < 25; i++) { //Two rightmost chunks of sidewalk
			for(int j = 10; j < 12; j++) 
				sidewalkGrid[i][j].release();
			for(int j = 16; j < 18; j++)
				sidewalkGrid[i][j].release();
		}

		for(int i = 19; i < 22; i++) //Sidewalk below right crosswalk
			for(int j = 16; j < 21; j++)
				sidewalkGrid[i][j].release();

		for(int i = 13; i < 15; i++) //Sidewalk at bottom under left crosswalk
			for(int j = 19; j < 21; j++)
				sidewalkGrid[i][j].release();

		for(int i = 7; i < 15; i++) //Island sidewalk
			for(int j = 10; j < 12; j++)
				sidewalkGrid[i][j].release();

		//End of sidewalk grid releasing

		//Adding in crosswalks (shared semaphores between street grid and sidewalk grid)
		for(int i = 15; i < 19; i++) //Bottom crosswalk
			for(int j = 16; j < 18; j++)
				sidewalkGrid[i][j] = streetGrid[i][j];
		for(int i = 19; i < 21; i++) //Right crosswalk
			for(int j = 12; j < 16; j++)
				sidewalkGrid[i][j] = streetGrid[i][j];
		//These crosswalks are not used anymore.
		/*for(int i = 13; i < 15; i++) //Left crosswalk
      		for(int j = 12; j < 16; j++)
      			sidewalkGrid[i][j] = streetGrid[i][j];
      	for(int i = 15; i < 19; i++) //Top crosswalk
      		for(int j = 10; j < 12; j++)
      			sidewalkGrid[i][j] = streetGrid[i][j];*/

		//Releasing many semaphores on building entrances so multiple guis can "go in" to buildings
		sidewalkGrid[20][0].release(100); //rest1
		sidewalkGrid[0][3].release(100); //rest2
		sidewalkGrid[0][17].release(100); //rest3
		sidewalkGrid[10][18].release(100); //rest4
		sidewalkGrid[21][18].release(100); //rest5
		sidewalkGrid[24][10].release(100); //mark1
		sidewalkGrid[5][0].release(100); //mark2
		sidewalkGrid[5][18].release(100); //mark3
		sidewalkGrid[21][1].release(100); //bank1
		sidewalkGrid[0][12].release(100); //bank2
		sidewalkGrid[21][4].release(100); //apart1
		sidewalkGrid[21][3].release(20); //these two lines open up spots if multiple people are leaving apartment
		sidewalkGrid[21][5].release(20);
		sidewalkGrid[1][18].release(100); //apart2
		sidewalkGrid[0][18].release(20); //these two lines open up spots if multiple people are leaving apartment
		sidewalkGrid[2][18].release(20);
		sidewalkGrid[21][8].release(100); //stop0
		sidewalkGrid[11][0].release(100); //stop1
		sidewalkGrid[0][8].release(100); //stop2
		sidewalkGrid[18][7].release(100); //stop3

		sidewalkGrid[20][18].release(100); //starting point for agents
		sidewalkGrid[21][18].release(100);
		sidewalkGrid[19][18].release(100);

		sidewalkGrid[21][20].release(5); //opening up permits in front of people's houses
		sidewalkGrid[24][17].release(5);
		sidewalkGrid[25][17].release(5);
		sidewalkGrid[21][6].release(5);
		sidewalkGrid[21][2].release(5);
		sidewalkGrid[19][0].release(5);
		sidewalkGrid[17][0].release(5);
		sidewalkGrid[15][0].release(5);
		sidewalkGrid[13][0].release(5);
		sidewalkGrid[9][0].release(5);
		sidewalkGrid[7][0].release(5);
		sidewalkGrid[3][0].release(5);
		sidewalkGrid[1][0].release(5);
		sidewalkGrid[0][0].release(5);
		sidewalkGrid[0][4].release(5);
		sidewalkGrid[0][6].release(5);
		sidewalkGrid[0][10].release(5);
		sidewalkGrid[0][14].release(5);
		sidewalkGrid[0][16].release(5);
		sidewalkGrid[3][18].release(5);
		sidewalkGrid[4][18].release(5);
		sidewalkGrid[11][18].release(5);      	

		streetGrid[17][20].release(100); //starting point for vehicles

		streetGrid[7][9].release(100); //Parking entrances + tiles right outside
		streetGrid[14][9].release(100); 
		streetGrid[10][7].release(100); 
		streetGrid[11][11].release(100); 

		/********Finished setting up semaphore grid***********/
	}

	private void createBusStops() {
		for(int i = 0; i < 4; i++) {
			busStops.add(new BusStopAgent(i));
			cityMap.addBusStop(busStops.get(i));
			BusStopAgent newBus = (BusStopAgent)busStops.get(i);
			newBus.startThread();
		}
	}

	private void createHouses() {
		for(int i = 0; i < 22; i++) {
			houses.add(new House("house" + Integer.toString(i + 1)));
		}
		for(int i = 0; i < 20; i++) {
			Apartment a= new Apartment("apart1 " + i, i);
			a.setRoom(i);
			a.setBuilding(1);
			houses.add(a);
		}
		for(int i = 0; i < 20; i++) {
			Apartment a= new Apartment("apart2 " + i, i);
			a.setRoom(i);
			a.setBuilding(2);
			houses.add(a);
		}
		System.out.println("Created " + houses.size() + " houses including apartments");
	}

	public List<House> getHouses(){
		return houses; 
	}

	public void populateCity(String scenario){
		/*
		 * This will call different functions based on which scenario was chosen
		 */
		if(scenario.equals("Full Scenario"))
			runFullTest();
		else if(scenario.equals("Trader Joe's"))
			runMarketVisitTest();
		else if(scenario.equals("Restaurant1"))
			runRestaurant1Test();
		else if(scenario.equals("Restaurant2"))
			runRestaurant2Test();
		else if (scenario.equals("Restaurant3"))
			runRestaurant3Test();
		else if(scenario.equals("Restaurant4"))
			runRestaurant4Test();
		else if(scenario.equals("Restaurant5"))
			runRestaurant5Test();
		else if(scenario.equals("Bank Test"))
			runBankTest();
		else if(scenario.equals("Car Test"))
			runCarTest();
		else if(scenario.equals("Landlord Test"))
			runLandlordTest();
	}

	public void runFullTest(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);		

		//Add two trucks at an interval
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("truck");
			}
		}, 5000	);

		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("truck");
			}
		}, 13000	);

		addPersonWithCar("rest1Test", "No Job");

		addPersonNoHouse("host", "Restaurant2 Host");
		addPersonNoHouse("cashier", "Restaurant2 Cashier");
		addPersonNoHouse("cook", "Restaurant2 Cook");
		addPerson("waiter", "Restaurant2 Waiter");
		addPerson("rest2Test", "No job");

		addPerson("joe", "No Job");
		addPerson("marketManager", "Market Manager");
		addPerson("marketWorker", "Market Worker");

		addPersonNoHouse("host1", "Restaurant1 Host");
		addPersonNoHouse("cashier1", "Restaurant1 Cashier");
		addPersonNoHouse("cook1", "Restaurant1 Cook");
		addPerson("waiter1", "Restaurant1 Waiter");
		addPerson("rest1Test", "No job");

		addPersonNoHouse("host4", "Restaurant4 Host");
		addPersonNoHouse("cashier4", "Restaurant4 Cashier");
		addPersonNoHouse("cook4", "Restaurant4 Cook");
		addPerson("waiter4", "Restaurant4 Waiter");
		addPerson("rest4Test", "No job");
		addPersonWithCar("rest4Test", "No job");

	}

	public void runRestaurant1Test(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPersonNoHouse("host1", "Restaurant1 Host");
		addPersonNoHouse("cashier1", "Restaurant1 Cashier");
		addPersonNoHouse("cook1", "Restaurant1 Cook");
		addPerson("waiter1", "Restaurant1 Waiter");
		addPerson("rest1Test", "No job");

	}

	public void runRestaurant2Test(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPersonNoHouse("host", "Restaurant2 Host");
		addPersonNoHouse("cashier", "Restaurant2 Cashier");
		addPersonNoHouse("cook", "Restaurant2 Cook");
		addPersonWithCar("waiter", "Restaurant2 Waiter");
		addPersonWithCar("rest2Test", "No job");

	}

	public void runRestaurant3Test(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000);

		addPersonNoHouse("host", "Restaurant3 Host");
		addPersonNoHouse("cashier", "Restaurant3 Cashier");
		addPersonNoHouse("cook", "Restaurant3 Cook");
		addPerson("waiter", "Restaurant3 Waiter");
		addPerson("rest3Test", "No job");

	}

	public void runRestaurant4Test(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPersonNoHouse("host4", "Restaurant4 Host");
		addPersonNoHouse("cashier4", "Restaurant4 Cashier");
		addPersonNoHouse("cook4", "Restaurant4 Cook");
		addPerson("waiter4", "Restaurant4 Waiter");
		addPerson("rest4Test", "No job");

	}

	public void runRestaurant5Test(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPersonNoHouse("host5", "Restaurant5 Host");
		addPersonNoHouse("cashier5", "Restaurant5 Cashier");
		addPersonNoHouse("cook5", "Restaurant5 Cook");
		addPerson("waiter5", "Restaurant5 Waiter");
		addPerson("rest5Test", "No job");

	}

	public void runBankTest() {
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPersonNoHouse("bank manager", "Bank Manager");
		addPerson("bank teller", "Bank Teller");
		addPerson("bankCustomerTest", "No job");

	}



	public void runRegularJoeTest(){
		//Add two buses at an interval
		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000	);

		addPerson("joe", "No Job");
		addPerson("marketManager", "Market Manager");
		addPerson("marketWorker", "Market Worker");
	}
	
	public void runMarketVisitTest(){

		addVehicle("bus");
		timer.schedule(new TimerTask() {
			public void run() {
				addVehicle("bus");
			}
		}, 16000);

		addPerson("marketClient", "No Job");
		addPerson("marketManager", "Market Manager");
		addPerson("marketWorker", "Market Worker");
		addPerson("marketWorker", "Market Worker");
		addPerson("marketWorker", "Market Worker");
		addPerson("marketWorker", "Market Worker");
		addPerson("marketWorker", "Market Worker");

	}

	public void runCarTest() {
		//Add one person who should use their car
		addPersonWithCar("rest1Test", "No Job");
	}

	public void runLandlordTest(){
		addPerson("Landlord", "Landlord1");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("Joe", "No Job");
		addPerson("brokenApplianceTest", "No Job");
	}
	
	public void setTimeDisplay(String timeToDisplay){
		timeDisplay.setText(timeToDisplay);
	}

	public void enableBackToCity() {
		backToCity.setEnabled(true);
	}
}