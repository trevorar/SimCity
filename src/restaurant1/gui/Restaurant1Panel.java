package restaurant1.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import restaurant1.Restaurant1;
import restaurant1.Restaurant1NormalWaiterRole;
import restaurant1.Restaurant1SharedDataWaiterRole;
import restaurant1.Restaurant1WaiterRole;

import city.CityMap;
import city.PersonAgent;
import city.gui.ControlPanel;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class Restaurant1Panel extends JPanel implements ActionListener {

    private ControlPanel cp;
    
    private CityMap cityMap = CityMap.getInstance();
    
    private JLabel title;
    
    private JButton normalWaiter;
    private JButton sharedDataWaiter;
    
    private JButton depleteInventory;
    private JButton closeRestaurant;
    
    private JButton fireHost;
    
    public Restaurant1Panel(ControlPanel cp) {
    	this.cp = cp;

    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
        title = new JLabel("Restaurant 1");
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        
    	add(Box.createVerticalStrut(10));
    	
    	normalWaiter = new JButton("Add normal waiter");
    	sharedDataWaiter = new JButton("Add shared data waiter");
    	
    	depleteInventory = new JButton("Deplete inventory");
    	closeRestaurant = new JButton("Close restaurant");
    	
    	fireHost = new JButton("Fire host");
    	
    	normalWaiter.addActionListener(this);
    	normalWaiter.setAlignmentX(CENTER_ALIGNMENT);
    	sharedDataWaiter.addActionListener(this);
    	sharedDataWaiter.setAlignmentX(CENTER_ALIGNMENT);
    	closeRestaurant.addActionListener(this);
    	closeRestaurant.setAlignmentX(CENTER_ALIGNMENT);
    	depleteInventory.addActionListener(this);
    	depleteInventory.setAlignmentX(CENTER_ALIGNMENT);
    	fireHost.addActionListener(this);
    	fireHost.setAlignmentX(CENTER_ALIGNMENT);
    	
    	add(normalWaiter);
    	add(Box.createVerticalStrut(20));
    	add(sharedDataWaiter);
    	add(Box.createVerticalStrut(20));
    	add(depleteInventory);
    	add(Box.createVerticalStrut(20));
    	add(closeRestaurant);
    	add(Box.createVerticalStrut(20));
    	add(fireHost);
    	add(Box.createVerticalStrut(20));
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == normalWaiter) {
			addNormalWaiter();
		} else if(e.getSource() == sharedDataWaiter) {
			addSharedDataWaiter();
		} else if(e.getSource() == depleteInventory) {
			depleteInventory();
		} else if(e.getSource() == closeRestaurant) {
			closeRestaurant();
		} else if(e.getSource() == fireHost) {
			cityMap.getRest1().fireHost();
			cp.addPerson("New Host1", "No job");
			fireHost.setEnabled(false);
		}
		
	}

	private void depleteInventory() {
		cityMap.getRest1().getCook().depleteInventory();
	}
	
	private void closeRestaurant() {
		if(cityMap.getRest1().isOpen()) {
			cityMap.getRest1().closeRestaurant();
			closeRestaurant.setText("Open Restaurant");
		} else {
			cityMap.getRest1().openRestaurant();
			closeRestaurant.setText("Close Restaurant");
		}
	}

	private void addSharedDataWaiter() {
		Restaurant1 rest1 = cityMap.getRest1();
		PersonAgent p = new PersonAgent("Shared Data Waiter", null, cityMap, null);
		
		Restaurant1WaiterRole role = new Restaurant1SharedDataWaiterRole(p.getName(), p);
		Restaurant1WaiterGui gui = new Restaurant1WaiterGui(role);
		gui.setHome(rest1.getWaiterListSize() * 40 + 200, 60);
		rest1.addWaiter(role);
		role.setGui(gui);
		cp.getRest1Animation().addGui(gui);
		gui.setPresent(true);
		
		p.addRole(role, true);
		
		p.startThread();
	}

	private void addNormalWaiter() {
		Restaurant1 rest1 = cityMap.getRest1();
		PersonAgent p = new PersonAgent("Normal Waiter", null, cityMap, null);
		
		Restaurant1WaiterRole role = new Restaurant1NormalWaiterRole(p.getName(), p);
		Restaurant1WaiterGui gui = new Restaurant1WaiterGui(role);
		gui.setHome(rest1.getWaiterListSize() * 40 + 200, 60);
		rest1.addWaiter(role);
		role.setGui(gui);
		cp.getRest1Animation().addGui(gui);
		gui.setPresent(true);
		
		p.addRole(role, true);
		
		p.startThread();
	}   
}