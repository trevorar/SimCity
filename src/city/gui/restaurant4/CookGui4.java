package city.gui.restaurant4;

import city.Restaurant4.CookRole4;
import city.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.swing.ImageIcon;

public class CookGui4 implements Gui {
	CookRole4 agent;

    private int xPos = 400, yPos = 500;//default cook position
    private int xDestination = 400, yDestination = 500;//default start position
    private int cookDimensions= 20;
    private int movement= 20;
    ArrayList<MyGrillSpot> grillSpots = new ArrayList<MyGrillSpot>();
    ArrayList<MyPlateSpot> plateSpots = new ArrayList<MyPlateSpot>();
    
	ImageIcon icon1 = new ImageIcon("images/cook1.png");
	ImageIcon icon2 = new ImageIcon("images/cook2.png");
	ImageIcon icon3 = new ImageIcon("images/cook3.png");
	
	private int movementCounter = 0;
	private final int iconSwitch = 10; //Rate at which icons switch during movement
	
	ImageIcon icon = icon1;
    
    boolean isPresent = true;
    
    public CookGui4(CookRole4 agent) {
        this.agent = agent;
    }

    public void updatePosition() {
		//Code for switching pictures to create animation
		movementCounter = (movementCounter + 1) % (4 * iconSwitch);
		
		if(xPos != xDestination || yPos != yDestination) {
            if(movementCounter < iconSwitch)
        		icon = icon1;
        	else if(movementCounter < iconSwitch * 2)
        		icon = icon2;
        	else if(movementCounter < iconSwitch * 3)
        		icon = icon3;
        	else
        		icon = icon2;
    	} else icon = icon2;
    	
    	if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;

        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination)
            yPos--;
    }

    public void draw(Graphics2D g) {
        //g.setColor(Color.CYAN);
        //g.fillRect(xPos, yPos, cookDimensions, cookDimensions);
    	g.drawImage(icon.getImage(), xPos, yPos, 30, 30, null);
    	
        try{
        	for(MyGrillSpot gs : grillSpots){
        		g.setColor(Color.BLACK);
        		g.drawString(gs.choice, gs.spotX, gs.spotY);
        	}
        
        	for(MyPlateSpot ps : plateSpots){
        		g.setColor(Color.BLACK);
        		g.drawString(ps.choice, ps.spotX, ps.spotY);
        	}
        } catch (ConcurrentModificationException e){
        	
        }
    }

    public boolean isPresent() {
        return isPresent;
    }
    
	public void setPresent(boolean t) {
		if(t){
			isPresent = true;
			xPos = 400;
			yPos = 500;
			xDestination = 400;
			yDestination = 500;
		}
		else
			isPresent = false;
	}
    
    public MyGrillSpot findGrill(int id){
    	for(MyGrillSpot gs : grillSpots){
    		if(gs.id == id){
    			return gs;
    		}
    	}
    	return null;
    }
    
    public MyPlateSpot findPlate(int id){
    	for(MyPlateSpot ps : plateSpots){
    		if(ps.id == id){
    			return ps;
    		}
    	}
    	return null;
    }
    
    public void doCooking(String choice, int num, int id){
    	for(int i=0; i< grillSpots.size(); i++){
    		if(grillSpots.get(i).spot == num){
    			++num;
    		}
    	}
    	grillSpots.add(new MyGrillSpot(choice, num, id));
    }
    
    public void doPlating(String choice, int num, int id){
    	MyGrillSpot temp = findGrill(id);
    	if(temp != null){
    		grillSpots.remove(temp);
    	}
    	plateSpots.add(new MyPlateSpot(choice, num, id));
    }
    
    public void itemPickedUp(int id){
    	MyPlateSpot temp = findPlate(id);
    	if(temp != null){
    		plateSpots.remove(temp);
    	}
    }
    
    public void doExit(){
    	xDestination= -35;
    	yDestination= -35;
    }
    
    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    
    public void setXPos(int x){
    	xPos= x;
    }
    
    public void setYPos(int y){
    	yPos= y;
    }
    
    class MyGrillSpot{
    	private String choice;
    	private int spotX;
    	private int spotY;
    	private int spot;
    	private int id;
    	
    	MyGrillSpot(String choice, int spot, int id){
    		this.id= id;
    		this.choice= choice;
    		this.spot= spot;
    		if(spot == 0){
    			spotX= 300;
    			spotY= 570;
    		}
    		else if(spot == 1){
    			spotX= 350;
    			spotY= 570;
    		}
    		else if(spot == 2){
    			spotX= 400;
    			spotY= 570;
    		}
    		else if(spot == 3){
    			spotX= 435;
    			spotY= 540;
    		}
    	}
    }
    
    class MyPlateSpot{
    	private String choice;
    	private int spotX;
    	private int spotY;
    	private int spot;
    	private int id;
    	
    	MyPlateSpot(String choice, int spot, int id){
    		this.id= id;
    		this.choice= choice;
    		this.spot= spot;
    		if(spot == 0){
    			spotX= 325;
    			spotY= 470;
    		}
    		else if(spot == 1){
    			spotX= 375;
    			spotY= 470;
    		}
    		else if(spot == 2){
    			spotX= 425;
    			spotY= 470;
    		}
    		else if(spot == 3){
    			spotX= 475;
    			spotY= 470;
    		}
    	}
    }
}
