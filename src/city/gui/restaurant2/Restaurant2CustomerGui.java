package city.gui.restaurant2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import city.Restaurant2.Restaurant2CustomerRole;
import city.gui.Gui;

public class Restaurant2CustomerGui implements Gui{
	
	private Restaurant2CustomerRole agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private final int HOMEX, HOMEY;
	private enum Command {noCommand, GoToSeat, goToCashier, LeaveRestaurant, goToWaiter};
	private Command command=Command.noCommand;
	
	private String name;
	private String foodChoice;
	
	int customerNum;
	ImageIcon icon = new ImageIcon("images/person_flat1.png");
	ImageIcon flat1 = new ImageIcon("images/person_flat1.png");
	ImageIcon flat2 = new ImageIcon("images/person_flat2.png");
	ImageIcon flat3 = new ImageIcon("images/person_flat3.png");
	
	private int movementCounter = 0;
	private final int iconSwitch = 10; //Rate at which icons switch during movement

    private final int WINDOWY = 750 - 20;
	public Map<Integer, Integer> TABLEX = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> TABLEY = new HashMap<Integer, Integer>();

	private enum State {notOrdered, foodOrdered, HasFood, finishedFood, foodGone, leaving};
	private State state = State.notOrdered;
	
	boolean foodDone = false;
	
	Restaurant2AnimationPanel restaurant2panel;

	public Restaurant2CustomerGui(Restaurant2CustomerRole c, String n, int i){
		agent = c;
		xPos = 0;
		yPos = WINDOWY/2;
		
		name = n;
		
		customerNum = i;
		
		TABLEY.put(1, 270);
		TABLEY.put(2, 270);
		TABLEY.put(3, 420);
		TABLEY.put(4, 420);
		
		TABLEX.put(1, 185);
		TABLEX.put(2, 455);
		TABLEX.put(3, 185);
		TABLEX.put(4, 455);
		
        if(customerNum % 4 == 0) HOMEY = WINDOWY/2 + 140;
        else if(customerNum % 4 == 3) HOMEY = WINDOWY/2 + 100;
        else if(customerNum % 4 == 2) HOMEY = WINDOWY/2 + 60;
        else if(customerNum % 4 == 1) HOMEY = WINDOWY/2 + 20;
        else HOMEY = 0;
        
        if(customerNum < 5) HOMEX = 30;
        else if(customerNum < 9) HOMEX = 60;
        else if(customerNum < 13) HOMEX = 90;
        else if(customerNum < 17) HOMEX = 120;
        else HOMEX = 0;
        
        xDestination = HOMEX;
        yDestination = HOMEY;
		
	}

	public void updatePosition() {
		movementCounter = (movementCounter + 1) % (4 * iconSwitch);
		if (xPos < xDestination){
			xPos ++;
			
            if(movementCounter < iconSwitch)
        		icon = flat1;
        	else if(movementCounter < iconSwitch * 2)
        		icon = flat2;
        	else if(movementCounter < iconSwitch * 3)
        		icon = flat3;
        	else
        		icon = flat2;
        		
		}

		else if (xPos > xDestination){
			xPos --;
        	if(movementCounter < iconSwitch && icon != flat1)
        		icon = flat1;
        	else if(movementCounter < iconSwitch * 2 && icon != flat2)
        		icon = flat2;
        	else if(movementCounter < iconSwitch * 3 && icon != flat3)
        		icon = flat3;
        	else if(icon != flat2)
        		icon = flat2;
		}

		if (yPos < yDestination){
			yPos ++;
        	if(movementCounter < iconSwitch && icon != flat1)
        		icon = flat1;
        	else if(movementCounter < iconSwitch * 2 && icon != flat2)
        		icon = flat2;
        	else if(movementCounter < iconSwitch * 3 && icon != flat3)
        		icon = flat3;
        	else if(icon != flat2)
        		icon = flat2;
		}
		
		else if (yPos > yDestination){
			yPos --;
        	if(movementCounter < iconSwitch && icon != flat1)
        		icon = flat1;
        	else if(movementCounter < iconSwitch * 2 && icon != flat2)
        		icon = flat2;
        	else if(movementCounter < iconSwitch * 3 && icon != flat3)
        		icon = flat3;
        	else if(icon != flat2)
        		icon = flat2;
		}

		if (xPos == xDestination && yPos == yDestination) {
			if(command == Command.goToWaiter){
				agent.msgAtDestination();
			}
			if (command==Command.GoToSeat) agent.msgAnimationFinishedGoToSeat();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent);");
				isHungry = false;
				//gui.setEnabled(agent);		TODO: FIX THIS!
			}
			else if(command == Command.goToCashier){
				agent.msgAtDestination();			
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {
		g.drawImage(icon.getImage(), xPos, yPos, 26, 34, restaurant2panel);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
		g.drawString(name, xPos, yPos);
		if(state == State.foodOrdered){
			g.setColor(Color.WHITE);
			g.fillOval(xPos+20, yPos, 16, 16);
			g.setColor(Color.BLACK);
			g.drawString(foodChoice + "?", xPos+20, yPos+26);
			
		}
		if(state == State.HasFood){
			g.setColor(Color.WHITE);
			g.fillOval(xPos+20, yPos, 16, 16);
			g.setColor(Color.BLACK);
			g.fillOval(xPos+20, yPos, 10, 10);
			g.setColor(Color.BLACK);
			g.drawString(foodChoice, xPos+20, yPos+26);
		}
		if(state == State.finishedFood){
			g.setColor(Color.WHITE);
			g.fillOval(xPos+20, yPos, 16, 16);
		}
	}

	public boolean isPresent() {
		return isPresent;
	}
	public void setHungry() {
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
	}
	public boolean isHungry() {
		return isHungry;
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}

	public void DoGoToWaiter(int waiternum){
		xDestination = 20;
		yDestination = 30*waiternum;
		command = Command.goToWaiter;
	}
	
	public void DoGoToSeat(int seatnumber) {//later you will map seatnumber to table coordinates.
		xDestination = TABLEX.get(seatnumber);
		yDestination = TABLEY.get(seatnumber);
		command = Command.GoToSeat;
	}

	public void DoExitRestaurant() {
		xDestination = -40;
		yDestination = -40;
		command = Command.LeaveRestaurant;
		state = State.leaving;
	}
	
	public void DoGoToCashier(){
		state = State.foodGone;
		command = Command.goToCashier;
		xDestination = 30;
		yDestination = 30;
	}
	
	public void setFoodOrdered(String choice){
		foodChoice = choice;
		state = State.foodOrdered;
	}
	
	public void setHasFood(){
		state = State.HasFood;
	}
	
	public void setDoneEating(){
		state = State.finishedFood;
	}
	
	//TODO: FIX THIS
	public void setEnabled(){
		//gui.setEnabled(agent);
	}

	public void DoEnterRestaurant() {
        xDestination = HOMEX;
        yDestination = HOMEY;
	}


}
