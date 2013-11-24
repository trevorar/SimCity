package city.Restaurant2;

import java.util.List;

import city.PersonAgent;

public class Restaurant2 {
	
	String name;
	Restaurant2HostRole host;
	Restaurant2CashierRole cashier;
	Restaurant2CookRole cook;
	Restaurant2CustomerRole customer;
	List<Restaurant2WaiterRole> waiters;
	
	public Restaurant2(){
		//nothing here
	}
	
	public void setHost(Restaurant2HostRole h){
		host = h;
	}
	
	public Restaurant2HostRole getHost(){
		return host;
	}
	
	public Restaurant2CustomerRole getNewCustomerRole(PersonAgent p){
		customer = new Restaurant2CustomerRole(p);
		return customer;
	}
}
