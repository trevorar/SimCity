package city;

import test.mock.LoggedEvent;
import interfaces.Bus;
import interfaces.Car;
import interfaces.House;
import interfaces.Landlord;
import interfaces.Person;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

import test.mock.EventLog;
import city.gui.PersonGui;
import city.transportation.BusAgent;
import city.transportation.BusStopAgent;
import city.transportation.CarAgent;
import Role.BankTellerRole;
import Role.LandlordRole;
import Role.Role;
import agent.Agent;
import astar.AStarNode;
import astar.AStarTraversal;
import astar.Position;

public class PersonAgent extends Agent implements Person{
	
	//DATA
	String name;
	public List<String> events = Collections.synchronizedList(new ArrayList<String>());
	public List<String> foodsToEat = new ArrayList<String>();
	public List<Role> roles = Collections.synchronizedList(new ArrayList<Role>());
	enum PersonState {idle, hungry, choosingFood, destinationSet, payRent};
	PersonState state;
	
	//House
	House house;
	public List<MyMeal> meals = Collections.synchronizedList(new ArrayList<MyMeal>());
	public enum FoodState {initial, cooking, done};
	List<MyAppliance> appliancesToFix = Collections.synchronizedList(new ArrayList<MyAppliance>());
	enum ApplianceState {broken, beingFixed, fixed};
	LandlordRole landlord;
	
	//Transportation
	CarAgent car;
	String destination;
	enum TransportationState{takingCar, takingBus, walking, chooseTransport};
	TransportationState transportationState;
	CityMap cityMap;
	BusStopAgent busStop;
	BusAgent bus;
	public List<Car> cars = new ArrayList<Car>();
	public List<BusRide> busRides = Collections.synchronizedList(new ArrayList<BusRide>());
	public enum BusRideState {initial, waiting, busIsHere, onBus, done, paidFare, getOffBus};
	public List<CarRide> carRides = Collections.synchronizedList(new ArrayList<CarRide>());
	public enum CarRideState {initial, arrived};
	
	//Money
	public List<Bill> billsToPay = Collections.synchronizedList(new ArrayList<Bill>());
	double takeHome; 		//some amount to take out of every paycheck and put in wallet
	double wallet;
	double moneyToDeposit;
	
	//Bank
	BankAgent bank;
	BankTellerRole bankTeller;
	enum BankState {none, deposit, withdraw, loan};   //so we know what the person is doing at the bank
	BankState bankState;
	Boolean firstTimeAtBank = true;	//determines whether person needs to create account
	int accountNumber;
	List<BankEvent> bankEvents = Collections.synchronizedList(new ArrayList<BankEvent>());
	enum BankEventType {withrawal, deposit, loan, openAccount};
	
	//Other
	List<MarketOrder> recievedOrders = Collections.synchronizedList(new ArrayList<MarketOrder>());   //orders the person has gotten that they need to deal with
	//List<MarketAgent> markets;
	//List<Restaurant> restaurants;
	//Restaurant recentlyVisitedRestaurant; 	//so the person won't go there twice in a row
	List<String> groceryList;
	
	//Testing
	public EventLog log = new EventLog();
	public boolean goToRestaurantTest = false;
	public boolean takeBus = false;
	
	//Job
	Job myJob;
	
	Semaphore atDestination = new Semaphore(0, true);
	AStarTraversal aStar;
    Position currentPosition; 
    Position originalPosition;
    
	PersonGui gui;
	

	public PersonAgent(String n, AStarTraversal aStarTraversal){
		super();
		
		name = n;
		this.aStar = aStarTraversal;
		currentPosition = new Position(22, 19);
		if(aStar != null)
			currentPosition.moveInto(aStar.getGrid());
        originalPosition = currentPosition;//save this for moving into
        
        cityMap = new CityMap();
		
		//populate foods list -- need to make sure this matches up with market
		foodsToEat.add("Chicken");
		foodsToEat.add("Steak");
		foodsToEat.add("Salad");
		foodsToEat.add("Pizza");

	}
	
	/*
	 * Constructor without astar traversal for testing purposes 
	 */
	
	public PersonAgent(String n){
		super();
		
		name = n;
      
        cityMap = new CityMap();
		
		//populate foods list -- need to make sure this matches up with market
		foodsToEat.add("Chicken");
		foodsToEat.add("Steak");
		foodsToEat.add("Salad");
		foodsToEat.add("Pizza");
				
	}
	
	public String getName(){
		return name;
	}
	
	public void setGoToRestaurant(){	//for testing purposes
		goToRestaurantTest = true;
	}
	
	public void setTakeBus(){	//for testing purposes
		takeBus = true;
	}
	
	public void msgAtDestination() {
		atDestination.release();
	}
	
	public void setGui(PersonGui g){
		gui = g;
	}
	
	public void addRole(Role r, boolean active){
		roles.add(r);
		if(active){
			r.setActive(true);
		}
	}
	
	public void addFirstJob(Role r, String location){
		myJob = new Job(r, location);
	}
	
	public void changeJob(Role r, String location){
		myJob.changeJob(r, location);
	}
	
	public void setHouse(House h){
		house = h;
	}
	
	/*
	 * MESSAGES
	 */
	public void msgImHungry(){	//sent from GUI ?
		events.add("GotHungry");
		print("Recieved msgImHungry");
		log.add(new LoggedEvent("Recieved message Im Hungry"));
		stateChanged();
	}
	
	//From house
	public void msgImBroken(String type) {
		appliancesToFix.add(new MyAppliance(type));
		stateChanged();
	}
	
	public void msgItemInStock(String type) {
		meals.add(new MyMeal(type));
		stateChanged();
	}

	public void msgDontHaveItem(String food) {
		groceryList.add(food);
		stateChanged();
	}

	public void msgFoodDone(String food) {
		log.add(new LoggedEvent("Recieved message food is done"));
		synchronized(meals){
			for(MyMeal m : meals){
				if(m.type == food){
					m.state = FoodState.done;
				}
			}
		}
		stateChanged();
	}
	
	//Messages from bus/bus stop
	public void msgArrivedAtStop(int stop) {
		synchronized(busRides){
			for(BusRide br : busRides){
				if(br.busStop == stop){
					br.state = BusRideState.getOffBus;
				}
			}
		}
		stateChanged();
	}
	
	public void msgPleasePayFare(Bus b, double fare) {
		synchronized(busRides){
			for(BusRide br : busRides){
				if(br.bus == b){
					br.addFare(fare);
				}
			}
		}
		stateChanged();
	}
	
	public void msgBusIsHere(Bus b) { //Sent from bus stop
		log.add(new LoggedEvent("Recieved message bus is here"));
		events.add("BusIsHere");
		BusRide busride = new BusRide(b);
		busride.state = BusRideState.busIsHere;
		busRides.add(busride);
		stateChanged();
	}
	
	public void msgArrived(Car car) { //Sent from person's car
		log.add(new LoggedEvent("Recieved message arrived by car"));
		synchronized(carRides){
			for(CarRide cr : carRides){
				if(cr.car == car){
					cr.state = CarRideState.arrived;
				}
			}
		}
		stateChanged();
	}
	
	//from landlord
	public void msgFixed(String appliance) {
		synchronized(appliancesToFix){
			for(MyAppliance a : appliancesToFix){
				if(a.type == appliance){
					a.state = ApplianceState.fixed; 
				}
			}
		}
		
	}
	
	public void msgRentDue(Landlord r, double rate) {
		billsToPay.add(new Bill("rent", rate, r));
		stateChanged();
	}
	
	public void msgHereIsYourOrder(Car car){		//order for a car
		cars.add(car);
		stateChanged();
	}
	
	public void msgHereIsYourOrder(MarketOrder order){		//order for groceries
		recievedOrders.add(order);
		stateChanged();
	}
	
	
	/*
	 * Scheduler
	 * @see agent.Agent#pickAndExecuteAnAction()
	 * Scheduler events are order as follows:
	 * 1. Role schedulers - if a person has a role active, these actions need to be taken care of first
	 * 2. Things that need to be done immediately, i.e. paying bus fare
	 * 3. All other actions (i.e. eat food, go to bank), in order of importance/urgency
	 */
	public boolean pickAndExecuteAnAction() {
		
		//Uncomment this and create people named a, b, c, and d to see basic animation.
		movementTest();
		//TODO figure out place for grocery shopping

		//DoGoTo("restaurant1");
		
		boolean anytrue = false;
		synchronized(roles){
			for(Role r : roles){
				if(r.isActive){
					anytrue = r.pickAndExecuteAnAction();
					return anytrue;
				}
			}
		}
		/*
		 * This is first because the person needs to pay their fare before they get off the bus
		 */
		synchronized(busRides){
			for(BusRide br : busRides){
				if(br.fare != 0){
					payBusFare(br);
					return true;
				}
			}
		}
		synchronized(busRides){
			for(BusRide br : busRides){
				if(br.state == BusRideState.busIsHere){
					getOnBus(br);
					return true;
				}
			}
		}
		synchronized(busRides){
			for(BusRide br : busRides){
				if(br.state == BusRideState.getOffBus){
					getOffBus(br);
					return true;
				}
			}
		}
		synchronized(carRides){
			for(CarRide cr : carRides){
				if(cr.state == CarRideState.arrived){
					getOutOfCar(cr);
				}
			}
		}
		synchronized(events){
			for(String e : events){
				if(e.equals("GotHungry")){
					Eat();
					return true;
				}
			}
		}
		synchronized(events){
			for(String e : events){
				if(e.equals("GoToBank"));
				goToBank();
				return true;
			}
		}
		
		synchronized(billsToPay){
			if(!billsToPay.isEmpty()){
				payBills();
				return true;
			}
		}
		synchronized(meals){
			for(MyMeal m : meals){
				if(m.state == FoodState.initial){
					cookMeal(m);
					return true;
				}
			}
		}
		synchronized(meals){
			for(MyMeal m : meals){
				if(m.state == FoodState.done){
					eatMeal(m);
					return true;
				}
			}
		}
		synchronized(recievedOrders){
			if(!recievedOrders.isEmpty()){
				handleRecievedOrders();
				return true;
			}
		}
		synchronized(appliancesToFix){
			for(MyAppliance a : appliancesToFix){
				if(a.state == ApplianceState.broken){
					notifyLandlordBroken(a);
					return true;
				}
			}
		}
		synchronized(appliancesToFix){
			for(MyAppliance a : appliancesToFix){
				if(a.state == ApplianceState.fixed){
					notifyHouseFixed(a);
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	//ACTIONS
	
	public void Eat(){	//hacked for now so that it randomly picks eating at home or going out
		print("Inside method EAT");
		synchronized(events){
			for(String e : events){
				if(e.equals("GotHungry")){
					events.remove(e);
					break;
				}
			}
		}
		Random rand = new Random();
		//int x = rand.nextInt(1);
		//int x = (Math.random()<0.5) ? 0:1;
		int x = 0;		//HACK for testing TODO fix this
		if(x == 1){
			int y = rand.nextInt(foodsToEat.size());
			String food = foodsToEat.get(y);
			house.checkFridge(food);
			print("I'm going to eat " + food + " in my house.");
			log.add(new LoggedEvent("Decided to eat something from my house."));
		}
		else{
			goToRestaurant();
		}
	}
	
	public void goToBank(){
		String bank;
		synchronized(bankEvents){
			bank = cityMap.getClosestBank();
		}
	}
	
	public void goToRestaurant(){
		print("Going to go to a restaurant");
		log.add(new LoggedEvent("Decided to go to a restaurant"));
		//Restaurant restaurant2 = new Restaurant();
		//restaurant2.host.msgIWantFood(restaurant2.customer);
		
		//gui.goToRestaurant(2);	//Removed for agent testing TODO uncomment for running
		if(!cars.isEmpty()){	//Extremely hack-y TODO fix this
			//String destination = restaurant.name;
			String destination = "Restaurant2";
			CarRide ride = new CarRide((Car) cars.get(0), destination);
			carRides.add(ride);
			ride.car.msgDriveTo(this, destination);
		}
		else{	//take bus
			//cityMap.getNearestBusStop();	TODO make this a thing
		}
	}
	
	public void notifyLandlordBroken(MyAppliance a){
		print("Telling landlord that appliance " + a.type + " is broken");
		landlord.msgFixAppliance(this, a.type);
		a.state = ApplianceState.beingFixed;
	}
	
	public void payBills(){
		synchronized(billsToPay){
			for(Bill b : billsToPay){
				if(b.payTo == landlord){
					if(wallet > b.amount){
						landlord.msgHereIsMyRent(this, b.amount);
						wallet -= b.amount;
					}
					else{
						events.add("GoToBank");
					}
				}
			}
		}
	}
	
	public void getOnBus(BusRide ride){
		ride.state = BusRideState.onBus;
		log.add(new LoggedEvent("Getting on the bus"));
	}
	
	/*
	 * This is assuming the person will always have enough to pay the fare.
	 * May need to fix this later in non-norm scenario
	 */
	public void payBusFare(BusRide br){
		br.bus.msgHereIsFare(this, br.fare);
		br.state = BusRideState.paidFare;
		br.fare = 0;
		wallet -= br.fare;
	}
	
	public void getOffBus(BusRide busride){
		busride.bus.msgImGettingOff(this);
		//gui.doGetOffBus();
		busRides.remove(busride);
	}
	
	public void getOutOfCar(CarRide ride){
		ride.car.msgParkCar(this);
		log.add(new LoggedEvent("Telling car to park"));
	}
	
	public void notifyHouseFixed(MyAppliance a){
		house.fixedAppliance(a.type);
		appliancesToFix.remove(a);	//no longer needed on this list
	}
	
	public void handleRecievedOrders(){
		synchronized(recievedOrders){
			for(MarketOrder o : recievedOrders){
				for(int i = 0; i < o.orders.size(); i ++){
					Food f = new Food(o.orders.get(i).type, "Stove", o.orders.get(i).quantity);
					//TODO change the appliance type
				}
			}
		}
	}
	
	public void movementTest() {
		if(name.equals("a"))
			moveTo(1, 1);
		
		if(name.equals("b"))
			moveTo(3, 19);
		
		if(name.equals("c"))
			moveTo(4, 2);
		
		if(name.equals("d"))
			moveTo(2, 7);
	}
	
	public void cookMeal(MyMeal meal){
		log.add(new LoggedEvent("Cooking meal"));
		house.cookFood(meal.type);
		meal.state = FoodState.cooking;
		//TODO add gui
	}
	
	public void eatMeal(MyMeal m){
		log.add(new LoggedEvent("Eating meal"));
		//TODO make gui function
		//gui.eatMeal();
		meals.remove(m);
	}
	
	void moveTo(int x, int y) {
		Position p = new Position(x, y);
		
		if(currentPosition.distance(p) > 16) {
			//intermediate movement.
		}
		guiMoveFromCurrentPositionTo(p);
	}
	
	void DoGoTo(String location) {
		int x = cityMap.getX(location);
		int y = cityMap.getY(location);

	    gui.moveTo(130 + x * 30, 70 + y * 30);
	    
	    //Give animation time to move to square.
	    try {
			atDestination.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void guiMoveFromCurrentPositionTo(Position to){
		//System.out.println("[Gaut] " + guiWaiter.getName() + " moving from " + currentPosition.toString() + " to " + to.toString());

		AStarNode aStarNode = (AStarNode)aStar.generalSearch(currentPosition, to);
		List<Position> path = aStarNode.getPath();
		Boolean firstStep   = true;
		Boolean gotPermit   = true;

		for (Position tmpPath: path) {
		    //The first node in the path is the current node. So skip it.
		    if (firstStep) {
			firstStep   = false;
			continue;
		    }

		    //Try and get lock for the next step.
		    int attempts    = 1;
		    gotPermit       = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());

		    //Did not get lock. Lets make n attempts.
		    while (!gotPermit && attempts < 3) {
			//System.out.println("[Gaut] " + guiWaiter.getName() + " got NO permit for " + tmpPath.toString() + " on attempt " + attempts);

			//Wait for 1sec and try again to get lock.
			try { Thread.sleep(1000); }
			catch (Exception e){}

			gotPermit   = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());
			attempts ++;
		    }

		    //Did not get lock after trying n attempts. So recalculating path.            
		    if (!gotPermit) {
			//System.out.println("[Gaut] " + guiWaiter.getName() + " No Luck even after " + attempts + " attempts! Lets recalculate");
		    	path.clear(); aStarNode=null;
		    	guiMoveFromCurrentPositionTo(to);
		    	break;
		    }

		    //Got the required lock. Lets move.
		    //System.out.println("[Gaut] " + guiWaiter.getName() + " got permit for " + tmpPath.toString());
		    currentPosition.release(aStar.getGrid());
		    currentPosition = new Position(tmpPath.getX(), tmpPath.getY ());
		    print("Moving to " + currentPosition.getX() + ", " + currentPosition.getY());
		    gui.moveTo(130 + (currentPosition.getX() * 30), 70 + (currentPosition.getY() * 30));
		    
		    //Give animation time to move to square.
		    try {
				atDestination.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		boolean pathTaken = false;
		while (!pathTaken) {
		    pathTaken = true;
		    //print("A* search from " + currentPosition + "to "+to);
		    AStarNode a = (AStarNode)aStar.generalSearch(currentPosition,to);
		    if (a == null) {//generally won't happen. A* will run out of space first.
			System.out.println("no path found. What should we do?");
			break; //dw for now
		    }
		    //dw coming. Get the table position for table 4 from the gui
		    //now we have a path. We should try to move there
		    List<Position> ps = a.getPath();
		    Do("Moving to position " + to + " via " + ps);
		    for (int i=1; i<ps.size();i++){//i=0 is where we are
			//we will try to move to each position from where we are.
			//this should work unless someone has moved into our way
			//during our calculation. This could easily happen. If it
			//does we need to recompute another A* on the fly.
			Position next = ps.get(i);
			if (next.moveInto(aStar.getGrid())){
			    //tell the layout gui
			    guiWaiter.move(next.getX(),next.getY());
			    currentPosition.release(aStar.getGrid());
			    currentPosition = next;
			}
			else {
			    System.out.println("going to break out path-moving");
			    pathTaken = false;
			    break;
			}
		    }
		}
		*/
	    }
	
	
	//CLASSES
	
	class Bill{
		String type;
		double amount;
		Role payTo;
		Landlord landlord;
		
		public Bill(String t, double a, Role r){
			type = t;
			amount = a;
			payTo = r;
		}
		
		/*
		 * Constructor for testing
		 */
		public Bill(String t, double a, Landlord r){
			type = t;
			amount = a;
			landlord = r;
		}
		
	}
	
	class MyAppliance{
		String type;
		ApplianceState state;
		
		public MyAppliance(String t){
			type = t;
			state = ApplianceState.broken;
		}
		
	}
	
	public class MyMeal{
		public String type;
		public FoodState state;
		
		public MyMeal(String t){
			type = t;
			state = FoodState.initial;
		}
	}
	
	public class BusRide{
		public Bus bus;
		public double fare;
		public BusRideState state;
		public int busStop;
		
		public BusRide(Bus b){
			bus = b;
			fare = 0;
			state = BusRideState.initial;
		}
		
		public void addFare(double f){
			fare = f;
		}
	}
	
	public class CarRide{
		public Car car;
		public String destination;
		public CarRideState state;
		
		public CarRide(Car c, String dest){
			car = c;
			destination = dest;
			state = CarRideState.initial;
		}
	}
	
	public class BankEvent{
		public BankEventType type;
		public double amount;
		
		public BankEvent(BankEventType t, double a){
			type = t;
			amount = a;
		}
	}
	
	public class Job{
		Role role;
		String location;
		
		public Job(Role r, String l){
			role = r;
			location = l;
		}
		
		public void startJob(){
			role.setActive(true);
		}
		
		public void endJob(){
			role.setActive(false);
		}
		
		public void changeJob(Role r, String l){
			role = r;
			location = l;
		}
	}

	@Override
	public void msgFridgeFull() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgSpaceInFridge(int spaceLeft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgApplianceBrokeCantCook() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgBusIsHere(BusAgent b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgArrived() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgRentDue(LandlordRole r, double rate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsYourOrder(CarAgent car) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPleasePayFare(BusAgent b, double fare) {
		// TODO Auto-generated method stub
		
	}
	
}