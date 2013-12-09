package city.gui.Market;

import java.awt.*;
import java.util.ArrayList;

import city.gui.Gui;
import Role.MarketCustomerRole;
import Role.MarketWorker;

public class MarketCustomerGui implements Gui {

    private MarketCustomerRole agent = null;

	MarketGui gui;
	
	boolean isPresent = true;
    
    private int xPos = 230, yPos = 445;
    public int xDestination = 230, yDestination = 445, host_tableX, host_tableY;
    
    private static final int COOK_SIZE_X = 20;
    private static final int COOK_SIZE_Y = 20;

	boolean isAnimating = false;
	boolean hasDestination = false;
	
	String carryingOrderText = "";
	
	public ArrayList<String> platingFood;
	public ArrayList<String> cookingFood;
	
    public MarketCustomerGui(MarketCustomerRole a) {
    	agent = a;
    	carryingOrderText = "";
		platingFood = new ArrayList<String>();
		cookingFood = new ArrayList<String>();
    }
    
    public MarketCustomerGui(MarketCustomerRole a, MarketGui g) {
    	agent = a;
    	gui = g;
    	carryingOrderText = "";
		platingFood = new ArrayList<String>();
		cookingFood = new ArrayList<String>();
    }
    
    public MarketCustomerGui(MarketCustomerRole a, MarketGui g, int startX, int startY, int indexNum) {
    	agent = a;
    	gui = g;
    	xPos = startX;
    	yPos = startY;
    	xDestination = startX;
    	yDestination = startY;
    	carryingOrderText = "";
    	hasDestination = false;
		platingFood = new ArrayList<String>();
		cookingFood = new ArrayList<String>();
    }

	public void setDestination(int newX, int newY){
		xDestination = newX;
		yDestination = newY;
		hasDestination = true;
	}
    
    public void updatePosition() {
        if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;
        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination){
            yPos--;
        } else if (xPos == xDestination && yPos == yDestination){
        	if (isAnimating){
        		doneAnimating();
        	}	
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect(xPos, yPos, COOK_SIZE_X, COOK_SIZE_Y);
		if (!carryingOrderText.equals("")){
			g.drawString(carryingOrderText, xPos, yPos);
		}
		
		for (int i = 0; i < platingFood.size(); i++){
			g.drawString(platingFood.get(i), 225, 390 + (i * 10));
		}
		
    }
    
	public void beginAnimate(){
		isAnimating = true;
	}

	public void doneAnimating(){
		hasDestination = false;
		isAnimating = false;
		agent.releaseSemaphore();
	}

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    
    public void setCarryText(String carryText){
    	carryingOrderText = carryText;
    }

    public boolean isPresent() {
        return isPresent;
    }
    
	public void setPresent(boolean t) {
		if(t)
			isPresent = true;
		else
			isPresent = false;
	}
   
}