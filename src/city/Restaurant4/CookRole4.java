package city.Restaurant4;

import Role.Role;
import interfaces.MarketManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import test.mock.LoggedEvent;
import activityLog.ActivityLog;
import activityLog.ActivityTag;
import justinetesting.interfaces.Cook4;
import justinetesting.interfaces.Customer4;
import justinetesting.interfaces.Waiter4;
import city.MarketOrder;
import city.OrderItem;
import city.PersonAgent;
import city.gui.restaurant4.CookGui4;
import city.transportation.TruckAgent;

public class CookRole4 extends Role implements Cook4 {
	
	String roleName = "Restaurant4CookRole";
	
	String name;
	WaiterRole4 waiter;
	PersonAgent p;
	ArrayList<Waiter4> waiters= new ArrayList<Waiter4>();
	Timer cook= new Timer();
	Timer checkForOrders= new Timer();
	//javax.swing.Timer checkForOrders;
	Order o= new Order();
	List<Order> orders= Collections.synchronizedList(new ArrayList<Order>());;
	ArrayList<Food> foods;
	MarketManager market;
	SharedOrders4 sharedOrders;
	public enum orderState {none, pending, cooking, outOfItem, done, finished};
	public enum marketState{none, checkForRestock, ready, ordered, fulfilled, partiallyFullfilled, allMarketsOut, inventoryZero};
	static final int eggTime= 2500;
	static final int waffelTime= 2200;
	static final int pancakeTime= 2000;
	static final int baconTime= 2250;
	static final int randSelector= 3;
	public int id= 0;
	Map<String, Integer> delivery= new HashMap<String, Integer>();
	boolean successful;
	boolean checkOrders;
	
	ActivityTag tag = ActivityTag.RESTAURANT4COOK;
	
	// Implement cook gui
	public CookGui4 cookGui = null;

	public CookRole4(String name, PersonAgent p) {
		super();
		building = "rest4";
		this.name= name;
		this.p= p;
		foods= new ArrayList<Food>();
		foods.add(new Food("Eggs"));
		foods.add(new Food("Waffels"));
		foods.add(new Food("Pancakes"));
		foods.add(new Food("Bacon"));
		delivery.put("Eggs", 0);
		delivery.put("Waffels", 0);
		delivery.put("Pancakes", 0);
		delivery.put("Bacon", 0);
		checkOrders= true;
	
		/*checkForOrders = new javax.swing.Timer(2000,
				new ActionListener() { public void actionPerformed(ActionEvent event) {
					checkSharedOrders();
					checkOrders= true;
					checkForOrders.restart();
		      }
		});*/
	}

	public String getName(){
		return name;
	}
	
	public void addMarket(MarketManager m){
		market= m;
	}
	
	public void setOrders(SharedOrders4 o){
		sharedOrders= o;
	}
	
	public void addWaiter(Waiter4 w){
		boolean newWaiter= true;
		for(Waiter4 wait : waiters){
			if(wait == w){
				newWaiter= false;
			}
		}
		if(newWaiter){
			waiters.add(w);
		}
	}
	
	// MESSAGES 
	public void msgHereIsOrder(Waiter4 w, String choice, Customer4 c){
		System.out.println("I GOT A NEW ORDER TO PROCESS");
		
		Order o= new Order(w, choice, c, "pending", id++);
		orders.add(o);
		boolean newWaiter= true;
		for(Waiter4 wait : waiters){
			if(wait == w){
				newWaiter= false;
			}
		}
		if(newWaiter){
			waiters.add(w);
		}
		p.stateChanged();
	}

	public void msgReadyForConsumption(){
		checkOrders= true;
		p.stateChanged(); //wakes up the cook and tells him there is something for him to do now
	}
	
	public void msgPickedUpFood(Customer4 c){
		for(Order o : orders){
			if(o.c.equals(c)){
				//cookGui.itemPickedUp(o.choice, find(o));
				cookGui.itemPickedUp(o.id);
				orders.remove(o);
			}
		}
	}
	
	public void msgHereIsYourOrder(TruckAgent t, MarketOrder mo){ 
		log("recieved order from truck, processin' time y'all");
		List<OrderItem> order = mo.getOrders();
		for(int i=0; i<order.size(); i++){
			log("Order name: " + order.get(i).name);
			if(order.get(i).name.equals("Eggs")){
				delivery.put("Eggs", order.get(i).quantity);
			} else if (order.get(i).name.equals("Waffels")){
				delivery.put("Waffels", order.get(i).quantity);
			} else if(order.get(i).name.equals("Pancakes")){
				delivery.put("Pancakes", order.get(i).quantity);
			} else if(order.get(i).name.equals("Bacon")){
				delivery.put("Bacon", order.get(i).quantity);
			}
		}
		successful= true;
		o.ms= marketState.fulfilled;
		this.p.stateChanged();
	}
	
	public void msgEmptyInventory(){
		log("Oh no, I don't have any more food! What a tragedy");
		for(Food f : foods){
			f.setAmount(0);
		}
		o.ms= marketState.inventoryZero;
		p.stateChanged();
	}
	
	/*public void msgBackInBusiness(){
		p.addTask("goToWork");
	}*/
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		//First check if there are any new shared data orders to be added to the cook's personal list
		if(checkOrders == true){
			checkSharedOrders();
			checkOrders= false;
		}
		if(orders != null){
			synchronized(orders){ 
				for(Order order : orders){
					if(order.s == orderState.done){
						log("Order up!");
						plateIt(order);
						return true;
					}
				}
			}
			synchronized(orders){
				for(Order order : orders){
					if(order.s == orderState.pending){
						log("I should cook this food.");
						order.s= orderState.cooking;
						cookIt(order);
						return true;
					}
				}
			}
		}
		if(o.ms == marketState.checkForRestock || o.ms == marketState.inventoryZero){
			log("Looking through inventory to calcualte order to send to market");
			calculateOrder();
			return true;
		}
		if(o.ms == marketState.ready || o.ms == marketState.partiallyFullfilled){
			log("Ready to send order to the market now.");
			sendOrder();
			return true;
		}
		if(o.ms == marketState.fulfilled){
			restock();
			return true;
		}
		if(o.ms == marketState.allMarketsOut){
			closeRestaurant();
		}
		return false;
	}

	
	// ACTIONS
	private int find(Order o){
		for(int i=0; i<orders.size(); i++){
			if(orders.get(i).equals(o)){
				return i;
			}
		}
		log("COULDNT FIND THE RIGHT ORDER, WHOOPS");
		return -1;
	}
	
	private void checkSharedOrders(){
		log("Inside cook check shared orders");
		Order o= sharedOrders.fillOrder();
		if(o != null){
			orders.add(o);
		}
		
		checkForOrders.schedule(new TimerTask() {
			public void run() {
				checkSharedOrders();
			}
		}, 4000);
	}
	
	private void plateIt(Order o){
		cookGui.doPlating(o.choice, find(o), o.id);
		o.w.msgOrderDone(o.choice, o.c);
		o.s= orderState.finished;
		p.stateChanged();
	}
	
	private void cookIt(final Order o){
		int cookingTime= 0;
		for(Food food : foods){
			if(food.type == o.choice){	 
				if(food.getAmount() == 0){
					o.w.msgOutOfFood(food.type, o.c);
					o.s= orderState.outOfItem;
					if(this.o.ms != marketState.ordered ){
						this.o.ms= marketState.checkForRestock;
					}
					p.stateChanged();
					return;
				}
				else {
					cookGui.doCooking(o.choice, find(o), o.id);
					food.decrementAmount();
					cookingTime= food.getTime();	
					cook.schedule(new TimerTask() {
						@Override public void run() {
							o.s= orderState.done;
							p.stateChanged();
						}}, cookingTime);
					return;
				}
			}
		}
	}

	public void sendOrder(){
		List<OrderItem> orders= new ArrayList<OrderItem>();
		OrderItem eggs= new OrderItem("Eggs", o.eggs);
		OrderItem waffels= new OrderItem("Waffels", o.waffels);
		OrderItem pancakes= new OrderItem("Pancakes", o.pancakes);
		OrderItem bacon= new OrderItem("Bacon", o.bacon);
		
		log("Sending order to market now. I WANT " + o.eggs + " EGGS, " + o.waffels + " WAFFELS " + o.pancakes + " PANCAKES + " + o.bacon + " BACON");
		
		orders.add(eggs);
		orders.add(waffels);
		orders.add(pancakes);
		orders.add(bacon);
		
		MarketOrder order= new MarketOrder(orders, "rest4", p);
		boolean isOpen= p.getCityMap().msgMarketHereIsTruckOrder(4, order);
		o.ms= marketState.ordered;
		if(!isOpen){
			log("OH NO, THERE MUST HAVE BEEN AN ERROR! MY ORDER TO THE MARKET DIDNT GO THROUGH");
		}
		p.stateChanged();
	}
	
	public void calculateOrder(){
		for(Food food : foods){
			if(food.type == "Eggs"){
				if(food.currAmount <= food.low){
					int e= food.capacity - food.currAmount;
					log("Eggs are low, I need " + e + " more!");
					o.add("Eggs", e);
				}
			}
			if(food.type == "Waffels"){
				if(food.currAmount <= food.low){
					int w= food.capacity - food.currAmount;
					log("Waffels are low, I need " + w + " more!");
					o.add("Waffels", w);
				}
			}
			if(food.type == "Pancakes"){
				if(food.currAmount <= food.low){
					int p= food.capacity - food.currAmount;
					log("Pancakes are low, I need " + p + " more!");
					o.add("Pancakes", p);
				}
			}
			if(food.type == "Bacon"){
				if(food.currAmount <= food.low){
					int b= food.capacity - food.currAmount;
					log("Bacon is low, I need " + b + " more!");
					o.add("Bacon", b);
				}
			}
		}
		o.ms= marketState.ready;
		log("I WANT " + o.eggs + " EGGS, " + o.waffels + " WAFFELS " + o.pancakes + " PANCAKES + " + o.bacon + " BACON");
	}
	
	public void restock(){
		for(Food food : foods){
			food.currAmount += delivery.get(food.type);
			log("Restocked " + food.type + ": " + food.currAmount);
			for(Waiter4 w : waiters){
				w.msgRestocked(food.type);
			}
		}
		if(successful){
			log("Oh good, I got everything I needed!");
			o.eggs= 0;
			o.waffels= 0;
			o.pancakes= 0;
			o.bacon= 0;
			delivery.put("Eggs", 0);
			delivery.put("Waffels", 0);
			delivery.put("Pancakes", 0);
			delivery.put("Bacon", 0);
			o.ms= marketState.none;
		}
		else{
			log("Oh no, I'm missing a few items! I should ask another market this time");
			if(delivery.get("Eggs") < o.eggs){
				o.add("Eggs", (o.eggs - delivery.get("Eggs")));
			}
			else if(delivery.get("Waffels") < o.waffels){
				o.add("Waffels", (o.waffels - delivery.get("Waffels")));
			}
			else if(delivery.get("Pancakes") < o.pancakes){
				o.add("Pancakes", (o.pancakes - delivery.get("Pancakes")));
			}
			else if(delivery.get("Bacon") < o.bacon){
				o.add("Bacon", (o.bacon - delivery.get("Bacon")));
			}
			o.ms= marketState.partiallyFullfilled;
		}
		
		p.stateChanged();
	}
	
	public void closeRestaurant(){
		for(Waiter4 wait : waiters){
			wait.msgAllMarketsOut();
		}
		cookGui.doExit();
		p.setGuiVisible();
		p.setRoleInactive(this);
	}

	
	// UTILITIES
	public void setGui(CookGui4 gui) {
		cookGui = gui;
	}

	public CookGui4 getGui() {
		return cookGui;
	}

	
	// CLASSES
	public static class Order{
		Waiter4 w;
		String choice;
		Customer4 c;
		orderState s;
		marketState ms;
		int eggs=0;
		int waffels=0;
		int pancakes=0;
		int bacon=0;
		int id;
		
		Order(Waiter4 w2, String choice, Customer4 c2, String state, int id){
			this.w= w2;
			this.choice= choice;
			this.c= c2;
			if(state == "pending"){
				s= orderState.pending;
			}
			else{
				s= orderState.none;
			}	
		}
		
		Order(){
			eggs= 0;
			waffels= 0;
			pancakes= 0;
			bacon= 0;
			ms= marketState.none;
		}

		public void add(String type, int amount){
			ms= marketState.ready;
			if(type == "Eggs"){
				eggs= amount;
			}
			else if(type == "Waffels"){
				waffels= amount;
			}
			else if(type == "Pancakes"){
				pancakes= amount;
			}
			else if(type == "Bacon"){
				bacon= amount;
			}
		}
	}
	
	public class Food{
		String type;
		private int cookingTime;
		private int currAmount;
		private final int capacity= 5;
		private final int low= 2;
		
		Food(String type){
			this.type= type;
			currAmount= 1;
			if(type == "Eggs"){
				cookingTime= eggTime;
			}
			else if(type == "Waffels"){
				cookingTime= waffelTime;
			}
			else if(type == "Pancakes"){
				cookingTime= pancakeTime;
			}
			else if(type == "Bacon"){
				cookingTime= baconTime;
			}
		}
		
		public int getTime(){
			return cookingTime;
		}
		
		public int getAmount(){
			return currAmount;
		}
		
		public void decrementAmount(){
			currAmount--;
		}
		
		public void setAmount(int x){
			currAmount= x;
		}
	}	
	
	
	//ACTIVITY LOG
	private void log(String msg){
		print(msg);
        ActivityLog.getInstance().logActivity(tag, msg, name, false);
	}

	@Override
	public String getRoleName() {
		return roleName;
	}

	@Override
	public PersonAgent getPerson() {
		return p;
	}
}