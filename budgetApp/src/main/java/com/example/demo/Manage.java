package com.example.demo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


//Manage class handles sorting

public class Manage {
	List<Transaction> unsortedTransactions;
	List<Category> categories;
	List<ApiMap> apiMapping;
	ArrayList<String> searched = new ArrayList<String>();
	
	//constructer and getters + setters
	public Manage() {}
	
	public void setApiMap(List<ApiMap> newMap) {
		this.apiMapping = newMap;
	}
	
	public void setCategories(List<Category> newCategories) {
		this.categories = newCategories;
	}
 	
	public void setUnsortedTransactions(List<Transaction> transactions) {
		this.unsortedTransactions = transactions;
	}
	
	//sort method, logic includes finding new merchants to add based on API data
	public ArrayList<Merchant> sort() {
		ArrayList<Merchant> newMerchants = new ArrayList<Merchant>();
		
		for (String description : parsed()) {
			if (!searched.contains(description)) {
				System.out.print("Sort: " + description + " \t\t");
				
				Long categoryId = apiCall(description);
				
				if (categoryId != null) {
					Merchant nextMerchant = new Merchant();
					nextMerchant.setName(description);
					nextMerchant.setPattern(description);
					nextMerchant.setCategoryId(categoryId);
					
					System.out.print(nextMerchant.getCategoryId());
					
					newMerchants.add(nextMerchant);
				}
				searched.add(description);
				
				System.out.println();
			}
		}
		
		removeDuplicates(newMerchants);
		
    	return newMerchants;
	}
	
	//parsing method, based on known strings and lengths
	public ArrayList<String> parsed() {
		ArrayList<String> parsedDescriptions = new ArrayList<String>();
		
    	final String electronicCredit = "ACH Electronic Credit ";
    	final String electronicDebit  = "ACH Electronic Debit - ";
    	final String debitCardPurchase = "Debit Card Purchase ";
    	final String debitPinPurchase = "Debit PIN Purchase ";
    	final String mobilePurchasePin = "Mobile Purchase PIN Based ";
    	final String mobilePurchaseSign = "Mobile Purchase Sign Based ";
    	final String mobilePurchaseReturn = "Mobile Purchase Returns ";

    	final String electronicContent = "CAPITAL ONE N.A. ";
    	final String timeStampEnding = "DD/MM HH:MMa #XXXX ";
    	
    	final String debitEnding = "SAMMAMISH     WA 20289";
    	final String mobileEnding = "SAMMAMISH    WAUS07";
    	final String payPalStarter = "PAYPAL *";
    	final String dStarterString = "DXXXX ";
    	Pattern dStarter = Pattern.compile("D\\d\\d\\d\\d ");
    	
    	final String credit = "Credit ";
    	
    	
    	for (Transaction transaction : unsortedTransactions) {
    		String parsedDescription = transaction.getDescription();
    		
    		try {
	    		if (parsedDescription.substring(0, electronicCredit.length()).equals(electronicCredit)) {
	    			parsedDescription = parsedDescription.substring(electronicCredit.length(), electronicCredit.length() + electronicContent.length());
	    		}
	    		else if (parsedDescription.substring(0, electronicDebit.length()).equals(electronicDebit)) {
	    			parsedDescription = parsedDescription.substring(electronicDebit.length(), electronicDebit.length() + electronicContent.length());
	    		}
	    		else if (parsedDescription.substring(0, debitCardPurchase.length()).equals(debitCardPurchase)) {
	    			parsedDescription = parsedDescription.substring(debitCardPurchase.length() + timeStampEnding.length(), parsedDescription.length()-debitEnding.length());
	    		}
	    		else if (parsedDescription.substring(0, debitPinPurchase.length()).equals(debitPinPurchase)) {
	    			parsedDescription = parsedDescription.substring(debitPinPurchase.length(), parsedDescription.length()-debitEnding.length());
	    		}
	    		else if (parsedDescription.substring(0, mobilePurchasePin.length()).equals(mobilePurchasePin)) {
	    			parsedDescription = parsedDescription.substring(mobilePurchasePin.length() + "DXXXX ".length(), parsedDescription.length()-mobileEnding.length());
	    		}
	    		else if (parsedDescription.substring(0, mobilePurchaseSign.length()).equals(mobilePurchaseSign)) {
	    			
	    			parsedDescription = parsedDescription.substring(mobilePurchaseSign.length() + timeStampEnding.length(), parsedDescription.length());
	    			
	    			if (parsedDescription.substring(0, payPalStarter.length()).equals(payPalStarter)) {
	    				parsedDescription = parsedDescription.substring(payPalStarter.length(), parsedDescription.length());
	    			}
	    			else if (dStarter.matcher(parsedDescription.substring(0, dStarterString.length())).find()) {
	    				parsedDescription = parsedDescription.substring(dStarterString.length(), parsedDescription.length());
	    			}
	    		}
	    		else if (parsedDescription.substring(0, credit.length()).equals(credit)) {
	    			parsedDescription = parsedDescription.substring(credit.length(), parsedDescription.length());
	    		}
	    		else if (parsedDescription.substring(0, mobilePurchaseReturn.length()).equals(mobilePurchaseReturn)) {
	    			parsedDescription = parsedDescription.substring(mobilePurchaseReturn.length() + "MM/DD #XXXX ".length(), parsedDescription.length());
	    		}
    		}
    		catch (Exception e) {}
    		finally {
    			parsedDescription = parsedDescription.trim();
	    		
	    		Scanner whiteSpaceSplit = new Scanner(parsedDescription).useDelimiter("  ");
    			parsedDescription = whiteSpaceSplit.next();
    			
    			Scanner hashtagSplit = new Scanner(parsedDescription).useDelimiter(" #");
    			parsedDescription = hashtagSplit.next();
    			
    			parsedDescriptions.add(parsedDescription);
    		}
    	}
    	
    	return parsedDescriptions;
    }
	
	public String parseDescription(Transaction transaction) {
		ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
		transactionList.add(transaction);
		
		this.setUnsortedTransactions(transactionList);
		
		return this.parsed().get(0);
	}
	
	//api call method, calls the google maps API and returns the top result for the type
	private Long apiCall (String description) {
		try {
    		String inputURL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=";
    		String inputType = "&inputtype=textquery&fields=types";
    		String key = "&key=AIzaSyCIs8VblMBK-Gse9f-bqBzDJr7UoucEigw";
    		
    		String searchString = "";
    		
    		Scanner formatter = new Scanner(description).useDelimiter(" ");
    		while (formatter.hasNext()) {
    			searchString += formatter.next();
    			if (formatter.hasNext()) {
    				searchString += "%20";
    			}
    		}
    		formatter.close();
    		
			URL url = new URL(inputURL + searchString + inputType + key);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			
			if (con.getResponseCode() == 200) {
				String condensed = "";
				
			    Scanner condensor = new Scanner(url.openStream());
			    while (condensor.hasNext()) {
			    	condensed += condensor.nextLine();
			    }
			    condensor.close();
				
				JSONArray output = (JSONArray) ((JSONObject)((JSONArray)((JSONObject) new JSONParser().parse(condensed)).get("candidates")).get(0)).get("types");
				
				con.disconnect();
				
				//System.out.println(output.get(0));
				
				for (ApiMap map : apiMapping) {
		    		if (map.getName().equals(output.get(0))) {
		    			return categories.get((map.getCategoryId()-1)).getId();
		    		}
		    	}
			}
			else {
				System.out.println("API Error, code: " + con.getResponseCode());
			}
		}
		catch (Exception e) {}
		
		return null;
	}
	
	//remmove duplicates method narrows down strings that contain each other ("THE HOME DEPOT" and "HOME DEPOT") to ("HOME DEPOT")
	public void removeDuplicates(List<Merchant> arr) {
		for (int i = 0; i < arr.size(); i++) {
			boolean isIn = false;
			String searchA = arr.get(i).getPattern();
			
			for (int j = 0; j < arr.size(); j++) {
				String searchB = arr.get(j).getPattern();
				
				if (searchB.contains(searchA) && i != j && searchA.length() <= searchB.length()) {
					arr.remove(j);
					
					if (j < i) {
						i -= 2;
						break;
					}
					else {
						j -= 1;
					}
				}
			}
		}
	}
}