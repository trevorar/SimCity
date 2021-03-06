package Role;

import interfaces.MarketManager;
import java.util.*;
import java.util.concurrent.Semaphore;
import test.mock.LoggedEvent;
import activityLog.ActivityLog;
import activityLog.ActivityTag;
import city.Market;
import city.MarketOrder;
import city.OrderItem;
import city.PersonAgent;
import city.gui.Market.MarketManagerGui;
import city.transportation.TruckAgent;
import Role.Role;


public class MarketManagerRole extends Role implements MarketManager {
	
	String roleName = "MarketManagerRole";

	// Data
	private String name;
	private double marketMoney;
	public List<myMarketWorker> myWorkers;
	public List<myMarketOrder> myOrders;
	public List<MarketCustomerRole> marketCustomers;
	public Hashtable<String, MarketItem> marketStock;

	public enum orderState {pendingWorkerAssignment, assignedToWorker, pickedReady, givenToTruck, pendingBilling, billed, done};
	public enum deliveryType {inPerson, truckOrder};
	
	public enum itemType {food, car};
	
	ActivityTag tag = ActivityTag.MARKETMANAGER;
	
	MarketManagerGui marketMgrGui;
	public TruckAgent myTruck;

	PersonAgent p;
	
	private Semaphore isAnimating = new Semaphore(0,true);
	
	Market myMarket;
	boolean test = false;
	
	public MarketManagerRole(String initialName, PersonAgent person, Market mkt){
		
		name = initialName;
		p = person;
		myMarket = mkt;
		
		mkt.setManager(this); // I am the market manager!
		
		// List initialization
		myOrders = Collections.synchronizedList(new ArrayList<myMarketOrder>());
		myWorkers = Collections.synchronizedList(new ArrayList<myMarketWorker>());
		marketStock = new Hashtable<String, MarketItem>();
		marketCustomers = Collections.synchronizedList(new ArrayList<MarketCustomerRole>());
		
		// Sample market stock
		marketStock = new Hashtable<String, MarketItem>();
		marketStock.put("Pasta", new MarketItem("Pasta", 500, itemType.food, 5.00));
		marketStock.put("Pizza", new MarketItem("Pizza", 500, itemType.food, 5.00));
		marketStock.put("Chicken", new MarketItem("Chicken", 500, itemType.food, 5.00));		
		marketStock.put("Honda Accord", new MarketItem("Honda Accord", 500, itemType.car, 50.00));
		marketStock.put("Honda Civic", new MarketItem("Honda Accord", 500, itemType.car, 50.00));
		
		// Market stock for restaurant1 orders
		marketStock.put("steak", new MarketItem("steak", 500, itemType.food, 5.00));
		marketStock.put("fish", new MarketItem("fish", 500, itemType.food, 5.00));
		marketStock.put("chicken", new MarketItem("chicken", 500, itemType.food, 5.00));
		
		//Market stock for restaurant2 orders
		marketStock.put("Steak", new MarketItem("Chicken", 500, itemType.food, 5.00));		
		marketStock.put("Salad", new MarketItem("Chicken", 500, itemType.food, 5.00));		
		
		// Market stock for restaurant3 orders
		marketStock.put("Mac & Cheese", new MarketItem("steak", 500, itemType.food, 5.00));
		marketStock.put("French Fries", new MarketItem("steak", 500, itemType.food, 5.00));
		marketStock.put("Pizza", new MarketItem("steak", 500, itemType.food, 5.00));
		marketStock.put("Pasta", new MarketItem("steak", 500, itemType.food, 5.00));
		marketStock.put("Cobbler", new MarketItem("steak", 500, itemType.food, 5.00));
		
		// Market stock for restaurant4 orders
		marketStock.put("Eggs", new MarketItem("Eggs", 500, itemType.food, 5.00));
		marketStock.put("Waffels", new MarketItem("Waffels", 500, itemType.food, 5.00));
		marketStock.put("Pancakes", new MarketItem("Pancakes", 500, itemType.food, 5.00));
		marketStock.put("Bacon", new MarketItem("Bacon", 500, itemType.food, 5.00));
	}

	public class myMarketOrder {
		MarketOrder order; // Contains recipient, destination, list of OrderItems
		public orderState state;
		deliveryType type;
		PersonAgent assignedWorker;
		
		myMarketOrder(MarketOrder incomingOrder, orderState initialState, deliveryType initialType){
			order = incomingOrder;
			state = initialState;
			type = initialType;
		}
		
	}

	private class MarketItem { // Used for internal stock-tracking within the market
		
		public String itemName;
		public int quantity;
		public itemType type;
		public double itemPrice;
		
		MarketItem(String initItemName, int initialQuantity, itemType initialType, double initialPrice){
			itemName = initItemName;
			quantity = initialQuantity;
			type = initialType;
			itemPrice = initialPrice;
		}
		
	}

	public class myMarketWorker { // Used for internal stock-tracking within the market
		
		public MarketWorkerRole worker;
		public int numWorkingOrders;
		
		public myMarketWorker(MarketWorkerRole w){
			worker = w;
			numWorkingOrders = 0;
		}
		
	}
	
	// Messages
	public void msgHereIsOrder(MarketOrder o){
		log("Received order from " + o.getRecipient().getName());
		myMarketOrder mo = new myMarketOrder(o, orderState.pendingWorkerAssignment, deliveryType.inPerson);
		myOrders.add(mo);
		p.stateChanged();
	}
	
	public void msgHereIsTruckOrder(MarketOrder o){
		log("Recieved order from " + o.getRecipient().getName());
		//log("Current order size is:" + o.orders.size());
		myMarketOrder mo = new myMarketOrder(o, orderState.pendingWorkerAssignment, deliveryType.truckOrder);
		myOrders.add(mo);
		p.stateChanged();
	}
	
	public void msgOrderPicked(MarketOrder o){
		log("Received orderPicked message");
		synchronized(myOrders){
			for (myMarketOrder order : myOrders) {
				if (order.order.equals(o)){
					order.state = orderState.pickedReady;
				}
			}
		}
		p.stateChanged();
	}
	
	public void msgFinishedDelivery(MarketOrder o){
		log("Truck finished its delievery, now I will bill the person for the order");
		synchronized(myOrders){
			for (myMarketOrder order : myOrders) {
				if (order.order == o){
					order.state = orderState.pendingBilling;
					return;
				}
			}
		}
		p.stateChanged();
	}
	
	public void msgAcceptPayment(double incomingPayment){
		log("Recieving a payment for an order");
		marketMoney = incomingPayment + marketMoney;
		p.stateChanged();
	}
	
	public void msgCustomerArrivedToMarket(MarketCustomerRole person) {
		synchronized(marketCustomers){
			marketCustomers.add(person);
		}
		p.stateChanged();
	}
	
	// Scheduler
	public boolean pickAndExecuteAnAction(){
		synchronized(myOrders){
			if (!myOrders.isEmpty()) {
				for (myMarketOrder order : myOrders) {
					if (order.state == orderState.pendingWorkerAssignment){
						log("I'll delegate this order to one of my workers.");
						makeWorkerPrepareOrder(order);
						return true;
					}
					if (order.state == orderState.pickedReady){
						log("Preparing to deliver order.");
						deliverOrder(order);
						return true;
					}
					if (order.state == orderState.pendingBilling){
						log("Preparing to bill orderer.");
						billRecipient(order);
						return true;
					}
				}
			}
		}
		return false;
	}

	// Actions
	private void makeWorkerPrepareOrder(myMarketOrder o){ // Distribute load of incoming orders to all workers
		log("Getting a market worker to prepare the order.");
		o.state = orderState.assignedToWorker;
		double orderTotal = 0;
		// Decrement quantity of things in each order
		for (OrderItem item : o.order.orders){
			MarketItem selectedMarketItem = marketStock.get(item.name);
			selectedMarketItem.quantity = selectedMarketItem.quantity - 1;
			marketStock.put(item.name, selectedMarketItem);
			orderTotal = orderTotal + marketStock.get(item.name).itemPrice;
		}
		
		o.order.orderPrice = orderTotal;
		
		if (myWorkers.size() != 0) {
			int initOrders = myWorkers.get(0).numWorkingOrders;
			myMarketWorker w_selected = null;
			synchronized(myWorkers){
				for (myMarketWorker w : myWorkers){
					if (w.numWorkingOrders <= initOrders){
						initOrders = w.numWorkingOrders;
						w_selected = w;
					}
				}
			}
			w_selected.worker.msgPrepareOrder(o.order, this);
			w_selected.numWorkingOrders++;
		}
		else{
			log("I have no market workers in my shop right now");
		}
		p.stateChanged();
	}
	
	private void deliverOrder(myMarketOrder o){
		log("In deliver order function, order is " + o.type);
		if (o.type == deliveryType.inPerson){
			log("All done, here is your order");
			o.order.getRecipient().msgHereIsYourOrder(o.order);
			synchronized(marketCustomers){
				for(MarketCustomerRole cust : marketCustomers){
					if (cust.getPerson().equals(o.order.getRecipient())){
						cust.msgHereIsYourOrder(o.order);
					}
				}
			}
			o.state = orderState.done;
		} else if (o.type == deliveryType.truckOrder){
			myTruck.msgPleaseDeliver(o.order);
			o.state = orderState.givenToTruck;
		}
		p.stateChanged();
	}
	
	private void billRecipient(myMarketOrder o){
		log("Billing " + o.order.getRecipient().getName());
		o.order.getRecipient().msgMarketBill(o.order.orderPrice, this);
		o.state = orderState.billed;
		p.stateChanged();
	}
	
	private void log(String msg){
		if (!test){
			print(msg);
			ActivityLog.getInstance().logActivity(tag, msg, name, false);
		}
	}
	
	public void addWorker(MarketWorkerRole w){
		myMarketWorker newWorker = new myMarketWorker(w);
		myWorkers.add(newWorker);
		p.stateChanged();
	}
	
	public String getName() {
		return name;
	}

	public String getRoleName() {
		return roleName;
	}
	
	public void releaseSemaphore(){
		isAnimating.release();
	}
	
	public void setGui(MarketManagerGui gui) {
		marketMgrGui = gui;
	}

	public MarketManagerGui getGui() {
		return marketMgrGui;
	}

	public void setPerson(PersonAgent workerPerson) {
		p = workerPerson;
	}
	
	public String getMarketName(){
		return myMarket.getName();
	}

	public void setTruck(TruckAgent newTruck) {
		myTruck = newTruck;
	}

	public PersonAgent getPerson() {
		return p;
	}
	
	public void setTesting(){
		test = true;
	}

}