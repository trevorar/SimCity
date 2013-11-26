package city.gui.restaurant4;

import city.Restaurant4.CustomerRole4;
import city.gui.Gui;
import java.awt.*;

public class CustomerGui4 implements Gui{
	
	private CustomerRole4 agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;
	//RestaurantGui4 gui;

	private int xPos, yPos, customerDimensions;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToSeat, LeaveRestaurant};
	private Command command=Command.noCommand;

	public static final int xTable = 100;
	public static final int yTable = 250;
	private static final int foodDisplacement= 30;
	private static final int movement= -40;

	public CustomerGui4(CustomerRole4 c/*, RestaurantGui4 gui*/){
		agent = c;
		setxPos(20);
		setyPos(40);
		customerDimensions= 20;
		setxDestination(20);
		setyDestination(40);
		//this.gui = gui;
	}

	public void updatePosition() {
		if (getxPos() < getxDestination())
			setxPos(getxPos() + 1);
		else if (getxPos() > getxDestination())
			setxPos(getxPos() - 1);

		if (getyPos() < getyDestination())
			setyPos(getyPos() + 1);
		else if (getyPos() > getyDestination())
			setyPos(getyPos() - 1);

		if (getxPos() == getxDestination() && getyPos() == getyDestination()) {
			if (command==Command.GoToSeat) agent.msgAnimationFinishedGoToSeat();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				isHungry = false;
				//gui.setCustomerEnabled(agent);
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.fillRect(getxPos(), getyPos(), customerDimensions, customerDimensions);
		if(agent.getState() == "eating"){
			g.setColor(Color.BLACK);
			if(agent.getChoice() == "Eggs"){
				g.drawString("Eggs", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Waffels"){
				g.drawString("Waffels", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Pancakes"){
				g.drawString("Pancakes", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Bacon"){
				g.drawString("Bacon", getxPos(), getyPos() + foodDisplacement);
			}
		}
		else if(agent.getState() == "deciding"){
			g.setColor(Color.GRAY);
			if(agent.getChoice() == "Eggs"){
				g.drawString("Eggs?", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Waffels"){
				g.drawString("Waffels?", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Pancakes"){
				g.drawString("Pancakes?", getxPos(), getyPos() + foodDisplacement);
			}
			else if(agent.getChoice() == "Bacon"){
				g.drawString("Bacon?", getxPos(), getyPos() + foodDisplacement);
			}
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

	public void DoGoToSeat(int seatnumber, int xTable, int yTable) {//later you will map seatnumber to table coordinates.
		setxDestination(xTable);
		setyDestination(yTable);
		command = Command.GoToSeat;
	}

	public void DoExitRestaurant() {
		setxDestination(movement);
		setyDestination(movement);
		command = Command.LeaveRestaurant;
	}

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public int getxDestination() {
		return xDestination;
	}

	public void setxDestination(int xDestination) {
		this.xDestination = xDestination;
	}

	public int getyDestination() {
		return yDestination;
	}

	public void setyDestination(int yDestination) {
		this.yDestination = yDestination;
	}
}
