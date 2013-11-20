package city.Restaurant4;

import Role.Role;
import city.gui.restaurant4.RestaurantGui4;
import city.gui.restaurant4.WaiterGui4;
import justinetesting.interfaces.Customer4;
import justinetesting.interfaces.Waiter4;
import city.Restaurant4.CashierRole4;

import java.util.*;
import java.util.concurrent.Semaphore;

public class WaiterRole4 extends Role implements Waiter4 {
	private String name;
	private  Menu menu;
	public List<MyCustomer> customers = new ArrayList<MyCustomer>();
	public enum customerState{waiting, seated, askedToOrder, ordered, reOrder, foodDone, eating, doneEating, askedForBill, readyForBill, done, none};
	public enum waiterState{atEntrance, atCook, atHome, atTable, onBreak};
	public enum breakState{none, wantToGoOnBreak, pending, deniedBreak, goOnBreak, onBreak, finishedBreak};
	private breakState bs;
	private waiterState ws= waiterState.atHome;
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0, true);
	private Semaphore atEntrance= new Semaphore(0, true);
	private Semaphore atHome= new Semaphore(0, true);
	Timer breakTimeLeft= new Timer();
	private boolean firstRun= true;
	private boolean closeRest= false;
	private static final int breakTime= 10000;
	private static final int yTable= 250;
	private static final int resetX= 100;
	private int xTable= 100;
	
	HostRole4 host;
	CookRole4 cook;
	CashierRole4 cashier;
	
	//Implement waiter gui
	public WaiterGui4 waiterGui = null;
	public RestaurantGui4 gui;

	
	public WaiterRole4(String name, RestaurantGui4 gui) {
		super();
		this.name= name;
		this.gui= gui;
		bs= breakState.none;
		menu= new Menu();
	}
	
	public String getName(){
		return name;
	}
	
	public String getSuperName(){
		return super.getName();
	}
	
	public void setHost(HostRole4 host){
		this.host= host;
	}
	
	public void setCook(CookRole4 cook){
		this.cook= cook;
	}
	
	public void setCashier(CashierRole4 cashier){
		this.cashier= cashier;
		if(name.equals("Robber")){
			cashier.msgImStealingEveryCent();
		}
	} 
	
	public boolean isOnBreak(){
		if (bs == breakState.onBreak || bs == breakState.pending || bs == breakState.goOnBreak || bs == breakState.wantToGoOnBreak){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	// MESSAGES
	public void msgNumber(int num){
		waiterGui.setHomePostion(num);
	}
	
	public void msgPleaseSeatCustomer(Customer4 c, int tableNum){
		customers.add(new MyCustomer(c, tableNum));
		
		MyCustomer mc= find(c);
		mc.s= customerState.waiting;

		stateChanged();
	}
	
	public void msgReadyToOrder(Customer4 c){
		MyCustomer mc= find(c); 
		mc.s= customerState.askedToOrder;
		stateChanged();
	}
	
	public void msgHereIsChoice(Customer4 c, String choice){
		MyCustomer mc= find(c);
		mc.setChoice(choice);
		mc.s= customerState.ordered;
		stateChanged();
	}
	
	public void msgOutOfFood(String choice, Customer4 c){
		MyCustomer mc= find(c);
		mc.s= customerState.reOrder;
		menu.foods.remove(choice);
		stateChanged();
	}
	
	public void msgRestocked(String choice){
		boolean alreadyOnMenu= false;
		for(Food food : menu.foods){
			if(food.type.equals(choice)){
				alreadyOnMenu= true;
			}
		}
		if(!alreadyOnMenu){
			menu.foods.add(new Food(choice));
		}
	}
	
	public void msgOrderDone(String choice, Customer4 c){
		MyCustomer mc= find(c);
		mc.s= customerState.foodDone;
		stateChanged();
	}
	
	public void msgDoneEating(Customer4 c){
		MyCustomer mc= find(c);
		mc.s= customerState.doneEating;
		stateChanged();
	}

	public void msgWantBreak(){
		bs= breakState.wantToGoOnBreak;
		stateChanged();
	}
	
	public void msgBreakApproved(){
		print("YES! i get to take my break now");
		bs= breakState.goOnBreak;
		stateChanged();
	}
	
	public void msgBreakDenied(){
		print("I really wish i could have taken my break :(");
		bs= breakState.deniedBreak;
		stateChanged();
	}
	
	public void msgReadyForBill(Customer4 c){
		MyCustomer mc = find(c);
		mc.s= customerState.askedForBill;
		stateChanged();
	}
	
	public void msgHereIsBill(double amount, Customer4 c){
		print("msgHereIsBill");
		MyCustomer mc= find(c);
		mc.amountOwed= amount;
		mc.s= customerState.readyForBill;
		stateChanged();
	}
	
	public void msgAllMarketsOut(){
		closeRest= true;
		stateChanged();
	}
	
	public void msgAtTable(){//from animation
		ws= waiterState.atTable;
		atTable.release();
		stateChanged();
	}
	
	public void msgAtCook(){
		if(atCook.availablePermits() == 0){
			ws= waiterState.atCook;
			atCook.release();
			stateChanged();
		}
	}

	public void msgAtEntrance(){
		ws= waiterState.atEntrance;
		atEntrance.release();
		stateChanged();
	}
	
	public void msgAtHome(){
		ws= waiterState.atHome;
		if(bs == breakState.onBreak){
			atHome.release();
		}
		stateChanged();
	}
	
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		try{
		if(closeRest){
			tellEveryoneToLeave();
			waiterGui.doGoBack();
			return false;
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.foodDone){
				print("Delivering " + customer.choice + " to the cutomer!");
				deliverFood(customer);
				return true;
			}
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.ordered){
				print("Sending order to cook.");
				sendOrderToCook(customer);
				return true;
			}
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.waiting){
				print("Time to seat this waiting customer");
				seatCustomer(customer);
				return true;
			}
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.askedToOrder){
				print("Taking order.");
				takeOrder(customer);
				return true;
			}
		}
		for( MyCustomer customer: customers){
			if(customer.s == customerState.reOrder){
				print("Whoops, we are all out of " + customer.choice + ", please re-order!");
				reOrder(customer, customer.choice);
				return true;
			}
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.doneEating){
				print("This table is empty now");
				updateHost(customer);
				return true;
			}
		}
		for( MyCustomer customer : customers){
			if(customer.s == customerState.askedForBill){
				print("This customer is ready for the bill, I should tell the cashier");
				tellCashier(customer);
				return true;
			}
		}
		for ( MyCustomer customer : customers){
			if(customer.s == customerState.readyForBill){
				print("I should give this customer the bill now.");
				giveBill(customer);
				return true;
			}
		}
		if (!firstRun){
			waiterGui.doGoBack();
		}
		if(bs == breakState.wantToGoOnBreak){
			boolean goodTime= true;
			for(MyCustomer customer : customers){
				if(customer.s != customerState.done){
					goodTime= false;
				}
			}
			if(goodTime){
				print("Can I please take a break now?");
				askHostForBreak();
			}
			return true;
		}
		if(bs == breakState.goOnBreak){
			print("Time to take my break!");
			goOnBreak();
			return true;
		}
		if(bs == breakState.finishedBreak){
			readyToWork();
			return true;
		}
		} catch(ConcurrentModificationException e){
			return false;
		}
		return false;
	}

	
	// ACTIONS
	private MyCustomer find(Customer4 c){
		MyCustomer temp= customers.get(0);  // initialize to the first customer to be updated in loop below
		for(MyCustomer customer : customers){
			if( c == customer.c){
				return customer;
			}
		}
		return temp; 
	}
	
	private void seatCustomer(MyCustomer c) {
		xTable= xTable * c.tableNum;
		if(ws != waiterState.atEntrance){
			waiterGui.doGoToEntrance();	
			try{
				atEntrance.acquire();
			} catch (InterruptedException e){}
		}
		c.c.msgSitAtTable(xTable, yTable, this, menu);
		host.msgSeatingCustomer(c.c);
		waiterGui.doGoToTable(c.c, c.tableNum);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.s= customerState.seated;
		ws= waiterState.atTable;
		xTable= resetX;
		firstRun= false;
		if(name.equals("OnBreak")){
			bs= breakState.wantToGoOnBreak;
			print("breakState: " + bs);
		}
		stateChanged();
	}

	private void takeOrder(MyCustomer c){
		waiterGui.doGoToTable(c.c, c.tableNum);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.c.msgWhatDoWant();
		c.s= customerState.none;
		stateChanged();
	}
	
	private void reOrder(MyCustomer c, String choice){
		waiterGui.doGoToTable(c.c, c.tableNum);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.c.msgWhatDoWant(choice);
		c.s= customerState.none;
		stateChanged();
	}
	
	private void sendOrderToCook(MyCustomer c){
		waiterGui.doGoToCook();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cook.msgHereIsOrder(this, c.choice, c.c);
		c.s= customerState.none;
		stateChanged();
	}
	
	private void deliverFood(MyCustomer c){
		waiterGui.doGoToCook();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiterGui.doBringFood(c.tableNum, c.choice);
		cook.msgPickedUpFood(c.c);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.c.msgHereIsFood(c.choice);
		c.s= customerState.eating;
		stateChanged();
	}
	
	private void updateHost(MyCustomer c){
		host.msgTableAvaliable(c.c);
		c.s= customerState.done;
		stateChanged();
	}
	
	private void tellCashier(MyCustomer c){
		cashier.msgComputeBill(this, c.c, c.choice);
		c.s= customerState.none;
		stateChanged();
	}
	
	private void giveBill(MyCustomer c){
		c.c.msgHereIsBill(c.amountOwed);
		c.s= customerState.none;
		customers.remove(c.c);
		stateChanged();
	}
	
	private void askHostForBreak(){
		print("Asking for break now");
		host.msgWantToGoOnBreak(this);
		bs= breakState.pending;
	}
	
	private void goOnBreak(){
		print("Initiating break!");
		bs= breakState.onBreak;
		gui.setWaiterEnabled(this, true);
		waiterGui.doGoBack();
		try{
			atHome.acquire();
		} catch (InterruptedException e){}
		breakTimeLeft.schedule(new TimerTask() {
			@Override public void run() { 
				bs= breakState.finishedBreak;
			}}, breakTime);
		stateChanged();
	}
	
	private void readyToWork(){
		gui.setWaiterEnabled(this, false);
		host.msgReadyToWork(this);
		bs= null;
	}
	
	private void tellEveryoneToLeave(){
		for(MyCustomer c : customers){
			c.c.msgRestClosed();
		}
		host.msgRestClosed();
	}
	
	
	// UTILITIES
	public void setGui(WaiterGui4 gui) {
		waiterGui = gui;
	}

	public WaiterGui4 getGui() {
		return waiterGui;
	}
	
	// CLASSES
	private static class MyCustomer {
		Customer4 c;
		int tableNum;
		private String choice;
		double amountOwed;
		
		customerState s;
		
		public MyCustomer(Customer4 c2, int table){
			c= c2;
			tableNum= table;
			amountOwed= 0;
		}
		
		public void setChoice(String choice){
			this.choice= choice;
		}
	}
	
	public class Menu {
		ArrayList<Food> foods;
		
		Menu(){
			foods= new ArrayList<Food>();
			foods.add(new Food("Steak"));
			foods.add(new Food("Chicken"));
			foods.add(new Food("Salad"));
			foods.add(new Food("Pizza"));
		}
		
		public String select(int num){
			if(foods.size() == 0){
				return "nothingInStock";
			}
			else if(num <= foods.size()){
				return foods.get(num).type;
			}
			else{
				return foods.get(0).type;
			}
		}
		
		public void remove(String choice){
			for(Food food : foods){
				if(food.type.equals(choice)){
					foods.remove(food);
				}
			}
		}
	}
	
	public class Food{
		String type;
		double amount;
		boolean inStock;
		
		Food(String type){
			this.type= type;
			inStock= true;
			if(type == "Steak"){
				amount= 15.99;
			}
			else if(type == "Chicken"){
				amount= 10.99;
			}
			else if(type == "Salad"){
				amount= 5.99;
			}
			else if(type == "Pizza"){
				amount= 8.99;
			}
		}
		
		public double getAmount(){
			return amount;
		}
	}	
}