package restaurant1;

import Role.MarketManager;
import Role.Role;
import activityLog.ActivityLog;
import activityLog.ActivityTag;
import agent.Agent;
import restaurant1.gui.Restaurant1CookGui;
import test.mock.LoggedEvent;

import java.util.*;
import java.util.concurrent.Semaphore;

import city.MarketOrder;
import city.OrderItem;
import city.PersonAgent;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class Restaurant1CookRole extends Role {
	
	String roleName = "Restaurant1CookRole";
	
	public List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());

	private MarketManager market;
	
	public enum orderState { pending, cooking, cooked, pickedUp, finished };
	
	int orderCount = 0;
	
	private enum foodOrderingState { notYetOrdered, ordered };
	
	boolean restaurantOpening = true; // A bool to deal with the initial inventory check when restaurant opens.
	
	boolean needToReorder = false;
	
	private String name;
	
	PersonAgent person;
	
	Timer timer = new Timer();
	
	private Map<String, Food> foods = Collections.synchronizedMap(new HashMap<String, Food>());

	private Semaphore atDestination = new Semaphore(0, true); // For gui movements
	
	public Restaurant1CookGui cookGui = null;
	
	ActivityTag tag = ActivityTag.RESTAURANT1COOK;

	public Restaurant1CookRole(String name, PersonAgent p) {
		super();
		building = "rest1";
		
		person = p;
		
				// usage: new Food(String type, int cookTime, int amount, int low, int capacity);
		foods.put("steak", new Food("steak", 8, 100, 5, 8));
		foods.put("fish", new Food("fish", 6, 100, 5, 8));
		foods.put("chicken", new Food("chicken", 4, 100, 5, 8));
		
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setGui(Restaurant1CookGui gui) {
		cookGui = gui;
	}

	// Messages
	public void msgHereIsOrder(Restaurant1WaiterRole w, String choice, int table) {
		orders.add(new Order(w, choice, table, orderState.pending, orderCount++));
		person.stateChanged();
	}
	
	public void msgFoodDoneCooking(Order o) {
		o.s = orderState.cooked;
		person.stateChanged();
	}
	
	public void msgPickedUpOrder(int orderNumber) {
		synchronized(orders) {
			for(Order o : orders) {
				if(o.orderNumber == orderNumber) {
					o.s = orderState.pickedUp;
				}
			}
		}
		person.stateChanged();
	}
	
	public void msgHereIsYourOrder(MarketManager m, MarketOrder o) {
		List<OrderItem> orderItems = o.getOrders();
		for(int i = 0; i < orderItems.size(); i++) {
				OrderItem tempOrder = orderItems.get(i);
				String type = tempOrder.getName();
				int amount = tempOrder.getQuantity();
				
				Food tempFood = foods.get(type);
				tempFood.state = foodOrderingState.notYetOrdered;
				log("Received delivery of " + amount + " units of " + type);
				tempFood.amount += amount;
		}
	}
	
	public void msgHereIsBill(MarketManager m, double amount) {
		log("Received a bill! What do I do?!");
	}
	
	public void msgRecheckInventory() {
		Food temp = foods.get("steak");
		temp.amount = temp.low - 1;
		
		temp = foods.get("fish");
		temp.amount = temp.low - 1;
		
		temp = foods.get("chicken");
		temp.amount = temp.low - 1;
		
		restaurantOpening = true;
		person.stateChanged();
	}
	
	public void msgAtDestination() {
		atDestination.release();
		person.stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		if(restaurantOpening) {
			initialInventoryCheck();
			return true;
		}
		
		if(needToReorder) {
			//reorderFood();
			needToReorder = false;
			return true;
		}
		
		synchronized(orders) {
			for(Order o : orders) {
				if(o.s == orderState.pickedUp) {
					finishIt(o);
					return true;
				}
			}
		}

		synchronized(orders) {
			for(Order o : orders) {
				if(o.s == orderState.cooked) {
					plateIt(o);
					return true;
				}
			}
		}

		synchronized(orders) {
			for(Order o : orders) {
				if(o.s == orderState.pending) {
					cookIt(o);
					return true;
				}
			}
		}
		
		DoGoToHome();

		return false;
	}

	// Actions

	private void cookIt(final Order o) {
		//Animation
		//DoCooking(o) 

		
		Food thisFood = foods.get(o.choice);
		
		DoGoToFridge();
		try {
			atDestination.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(thisFood.amount == 0) {
			log("We're all out of " + o.choice + "!");
			
			o.w.msgOutOf(o.choice, o.table);
			
			o.s = orderState.finished;
			
			if(thisFood.state != foodOrderingState.ordered) {
				orderMoreFood();
			}

			return;
		}
		
		if(thisFood.amount <= thisFood.low) {
			orderMoreFood();
		}
		

		cookGui.msgNewOrder(o.choice, o.orderNumber);
		
		log("Cooking up an order of " + o.choice + "!");
		
		DoGoToGrill();
		try {
			atDestination.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		o.s = orderState.cooking;
		thisFood.amount--;
		cookGui.msgOrderCooking(o.orderNumber);
		
		int cookTime = thisFood.cookingTime * 1000;
				
		timer.schedule(new TimerTask() {
							public void run() {
								 msgFoodDoneCooking(o);
							}
						}, cookTime	);
	}

	private void plateIt(Order o) {
		DoGoToGrill();
		try {
			atDestination.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cookGui.msgOrderBeingCarried(o.orderNumber);
		
		o.s = orderState.finished;
		log(o.choice + " done cooking, time to plate it!");
		
		DoGoToCounter();
		try {
			atDestination.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		cookGui.msgOrderWaiting(o.orderNumber);
		o.w.msgOrderDone(o.choice, o.table, o.orderNumber);
	}
	
	private void finishIt(Order o) {
		cookGui.msgOrderPickedUp(o.orderNumber);
		o.s = orderState.finished;
	}
	
	private void initialInventoryCheck() {
		log("Checking initial inventory levels.");
		Food steak = foods.get("steak");
		Food chicken = foods.get("chicken");
		Food fish = foods.get("fish");
		
		if((steak.amount < steak.low) || (chicken.amount < chicken.low) || (fish.amount < fish.low)) {
			orderMoreFood();
		} else {
			log("All foods are in stock! We're ready to go!");
		}
		restaurantOpening = false;
	}
	
	private void orderMoreFood() {
		List<OrderItem> orderList = Collections.synchronizedList(new ArrayList<OrderItem>());
		
		Food temp = foods.get("steak");
		if(temp.amount < temp.low && temp.state == foodOrderingState.notYetOrdered) {
			orderList.add(new OrderItem(temp.type, temp.capacity - temp.amount));
			temp.state = foodOrderingState.ordered;
		}
		
		temp = foods.get("chicken");
		if(temp.amount < temp.low && temp.state == foodOrderingState.notYetOrdered) {
			orderList.add(new OrderItem(temp.type, temp.capacity - temp.amount));
			temp.state = foodOrderingState.ordered;
		}
		
		temp = foods.get("fish");
		if(temp.amount < temp.low && temp.state == foodOrderingState.notYetOrdered) {
			orderList.add(new OrderItem(temp.type, temp.capacity - temp.amount));
			temp.state = foodOrderingState.ordered;
		}
		
		if(orderList.isEmpty()) {
			return;
		}
		
		MarketOrder newOrder = new MarketOrder(orderList, "rest1", this.person);
		
		log("Sending order for more food to the market!");
		market.msgHereIsOrder(newOrder);
	}
	
	private void DoGoToHome() {
		cookGui.DoGoToHome();
	}
	
	private void DoGoToFridge() {
		cookGui.DoGoToFridge();
	}
	
	private void DoGoToGrill() {
		cookGui.DoGoToGrill();
	}
	
	private void DoGoToCounter() {
		cookGui.DoGoToCounter();
	}
	
	public void addMarket(MarketManager m) {
		market = m;
	}

	private class Order {
		Restaurant1WaiterRole w;
		String choice;
		int table;
		orderState s;
		int orderNumber;
		
		Order(Restaurant1WaiterRole w, String choice, int table, orderState s, int number) {
			this.w = w;
			this.choice = choice;
			this.table = table;
			this.s = s;
			this.orderNumber = number;
		}
	}
	
	private class Food {
		String type;
		int cookingTime;
		int amount;
		int low;
		int capacity;
		foodOrderingState state;
		
		Food(String type, int cookingTime, int amount, int low, int capacity) {
			this.type = type;
			this.cookingTime = cookingTime;
			this.amount = amount;
			this.low = low;
			this.capacity = capacity;
			state = foodOrderingState.notYetOrdered;
		}
	}
	
	private void log(String msg){
		print(msg);
        ActivityLog.getInstance().logActivity(tag, msg, name);
	}

	@Override
	public String getRoleName() {
		return roleName;
	}
	
}

