package hollytesting.test.mock;


import interfaces.HouseInterface;
import interfaces.Person;

import java.util.*;

import Role.LandlordRole;
import test.mock.EventLog;
import test.mock.LoggedEvent;
import test.mock.Mock;
import city.Food;
import city.PersonAgent;
import city.gui.House.HouseAnimationPanel;

public class MockHouse extends Mock implements HouseInterface{
        
        public EventLog log = new EventLog();
        PersonAgent person;
		public HouseAnimationPanel h;

        public MockHouse(String name, PersonAgent p) {
                super(name);
                person = p;
        }

        public void boughtGroceries(List<Food> groceries) {
                log.add(new LoggedEvent("Recieved grocery list."));
        }

        public void checkFridge(String type) {
                log.add(new LoggedEvent("Recieved message check fridge."));
                person.msgItemInStock(type);
        }

        public void spaceInFridge() {
                // TODO Auto-generated method stub
                
        }

        public void cookFood(String type) {
                person.msgFoodDone(type);
        }

        public void fixedAppliance(String appliance) {
                // TODO Auto-generated method stub
                
        }

		@Override
		public void setOwner(Person p) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHouseAnimationPanel(HouseAnimationPanel p) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HouseAnimationPanel getAnimationPanel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setLandlord(LandlordRole r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public LandlordRole getLandlord() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getNum() {
			// TODO Auto-generated method stub
			return 0;
		}
}