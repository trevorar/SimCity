package city;

import java.util.ArrayList;
import java.util.List;

import Role.BankTellerRole;
import Role.PersonAgent;
import Role.BankManagerRole;

public class Bank {

        BankManagerRole bankmanager;
        public List<account> accounts;
        public static List<bankstation> bankstations;
        public static int uniqueaccountnumber = 0;
        int initialxcofbank = 200;
        int initialycofbank = 100;
        
        public Bank() {
                
                accounts = new ArrayList<account>();
                bankmanager = new BankManagerRole(this);
                bankstations = new ArrayList<bankstation>();
                for(int i = 0; i < 4; i++)
                {
                	bankstations.add(new bankstation(null, i+1, initialxcofbank += 100, initialycofbank));
                }
                
        }
        
        public BankManagerRole getBankManager()
        {
                return bankmanager;
        }
              
        public class bankstation {
        	
        	public int stationnumber;
        	BankTellerRole bankteller;
        	int xcoordinate;
        	int ycoordinate;
        	
        	public bankstation(BankTellerRole setbankteller, int setstationnumber, int setxcoordinate, int setycoordinate)
        	{
        		bankteller = setbankteller;
        		stationnumber = setstationnumber;
        		xcoordinate = setxcoordinate;
        		ycoordinate = setycoordinate;
        		
        	}
        	
        	public boolean isOccupied()
        	{
        		if(bankteller != null)
        		return false;
        		else
        		return true;
        		
        	}
        	
        	public void setBankTeller(BankTellerRole setbankteller)
        	{
        		bankteller = setbankteller;
        	}
        	
        	
        	
        }

}


