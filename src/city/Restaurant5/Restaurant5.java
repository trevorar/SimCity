package city.Restaurant5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import city.PersonAgent;
import city.Restaurant4.WaiterRole4;

public class Restaurant5 {
	
	String name;
	Restaurant5HostRole host;
	Restaurant5CashierRole cashier;
	Restaurant5CookRole cook;
	Restaurant5CustomerRole customer;
	List<Restaurant5WaiterRole> waiters;
	OrderSpindle5 orderspindle;
	private boolean isOpen;
	
	public Restaurant5(){
		isOpen = true;
		waiters = Collections.synchronizedList(new ArrayList<Restaurant5WaiterRole>());
		orderspindle = new OrderSpindle5();
	}
	
	public void setHost(Restaurant5HostRole h){
		host = h;
	}
	
	public Restaurant5HostRole getHost(){
		if(isOpen != true)
		{
			return null;
		}
		else
		{
			return host;
		}
	}
	
	public Restaurant5CashierRole getCashier() {
		return cashier;
	}
	
	public Restaurant5CustomerRole getNewCustomerRole(PersonAgent p){
		customer = new Restaurant5CustomerRole("",p);
		return customer;
	}

	public void setCook(Restaurant5CookRole c) {
		cook = c;
		cook.setOrderSpindle(orderspindle);
		
	}

	public void setCashier(Restaurant5CashierRole c) {
		cashier = c;
		
		
	}
	public int getWaiterSize() {
		return waiters.size(); 
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	public void addWaiters(Restaurant5WaiterRole w) {
		waiters.add(w);
		host.addwaiter(w);
		System.out.println("THE COOK SHOULD NOT BE NULL: " + host);
		w.setCook(cook);
		w.setCashier(cashier);
		w.setHost(host);
		w.setOrderSpindle(orderspindle);
	}
	
	public void toggleOpen() {
		if(isOpen == true){
			isOpen = false;
		} else {
			isOpen = true;
		}
	}

	public void emptyStock() {
		cook.emptyStock();
	}
	
	
	
	
}
