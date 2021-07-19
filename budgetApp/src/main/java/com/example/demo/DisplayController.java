package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystemNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

//controller for mapping all pages

//DECIDE ON REMOVING CATEGORIES
//ADD PASSWORD
//OPTIMIZE

@Controller
public class DisplayController {
    @Autowired
    private DatabaseRepo dataBaseRepo;
    
    List<TransactionCSV> importTransactionCSV = null;
    
    //get mapping for the overview page
    @GetMapping("/")
    public String showOverview(Model model) {
    	List<Period> periods = dataBaseRepo.getPeriodsByStart();
        List<Category> categories = dataBaseRepo.getCategories();
        ArrayList<List<Double>> allSubtotals = new ArrayList<List<Double>>();
        ArrayList<List<Double>> allGoals = new ArrayList<List<Double>>();
        ArrayList<Double> difference = new ArrayList<Double>();
        
        if (periods.size() > 12) {
        	try {
	        	while (periods.get(12) != null) {
	        		periods.remove(12);
	        	}
        	}
        	catch (Exception e) {}
        }
        
        
        for (Period period : periods) {
            List<Subtotal> subtotals = dataBaseRepo.getSubtotalsByPeriod(period.getId());
            
            double differenceSum = 0;
            for (Subtotal subtotal : subtotals) {
            	try {
            		differenceSum += subtotal.getAmount();
            	}
            	catch (Exception e) {}
            }
            
            difference.add((double)Math.round(differenceSum*100)/100);
        }
        
        for (Category category : categories) {
        	ArrayList<Double> subtotalRow = new ArrayList<Double>();
        	ArrayList<Double> goalRow = new ArrayList<Double>();
        	
	        for (Period period : periods) {
	        	double expenseSum = 0;
	        	try {
	        		if (!category.getName().equals("Other")) {
	        			subtotalRow.add(dataBaseRepo.getSubtotal(category.getId(), period.getId()).getAmount());
	        		}
	        		else {
	        			subtotalRow.add(dataBaseRepo.getSubtotal(null, period.getId()).getAmount());
	        		}
	        	}
	        	catch (Exception e) {
	        		subtotalRow.add(0.0);
	        	}
	        	
	        	try {
	        		if (!category.getName().equals("Other")) {
	        			goalRow.add(dataBaseRepo.getGoal(category.getId(), period.getId()).getAmount());
	        		}
	        		else {
	        			goalRow.add(dataBaseRepo.getGoal(null, period.getId()).getAmount());
	        		}
	        	}
	        	catch (Exception e) {
	        		goalRow.add(0.0);
	        	}
	        }
	        
	        allSubtotals.add(subtotalRow);
	        allGoals.add(goalRow);
        }
    	
        model.addAttribute("periods", periods);
        model.addAttribute("categories", categories);
        model.addAttribute("allSubtotals", allSubtotals);
        model.addAttribute("allGoals", allGoals);
        model.addAttribute("difference", difference);
    	return "overview";
    }
    
    //get mapping for the overview page, sends all table contents within the model, with MAPs as dictionaries to tie IDs to list indexes
	@GetMapping("/summary")
    public String findTransaction(@RequestParam(name = "period", required = false) String period, @RequestParam(name = "categoryFilter", required = false) String categoryFilter, @RequestParam(name = "dateFilter", required = false) String dateFilter, 
    		@RequestParam(name = "amountFilter", required = false) String amountFilter, @RequestParam(name = "descriptionFilter", required = false) String descriptionFilter, @RequestParam(name="merchantShow", required = false) String merchantShow, 
    		@RequestParam(name="focus1", required = false) String focus1, @RequestParam(name="focus2", required = false) String focus2, Model model) {
        List<Period> periods = dataBaseRepo.getPeriods();
        
        Long periodId = null;
    	if (periods.size() != 0) periodId = periods.get(0).getId();
		
		if (period != null) {
			periodId = Long.parseLong(period);
		}
		
		if (periodId != null) {
	        List<Merchant> merchants = dataBaseRepo.getMerchants();
	        List<Category> categories = dataBaseRepo.getCategories();
	        List<Subtotal> subtotals = dataBaseRepo.getSubtotalsByPeriod(periodId);
	        List<Goal> goals = dataBaseRepo.getGoalsByPeriod(periodId);
	        
	        Long categoryId = null;
	        if (categoryFilter != null) {
		        if (!categoryFilter.equals("All")) {
					for (Category category : categories) {
						if (category.getName().equals(categoryFilter)) {
					        categoryId = category.getId();
							break;
						}
					}
		        }
	        }
	        
	        if (dateFilter != null) {
	        	if (dateFilter.equals("")) {
	        		dateFilter = null;
	        	}
	        }
	        
	        Double amount = null;
	        if (amountFilter != null) {
	        	if (!amountFilter.equals("")) amount = Double.parseDouble(amountFilter);
	        }
	        
	        if (descriptionFilter != null) {
	        	if (descriptionFilter.equals("")) {
	        		descriptionFilter = null;
	        	}
	        }
	        
	        if (merchantShow == null || merchantShow.equals("")) {
	        	merchantShow = "false";
	        }
	        
	        List<Transaction> transactions = dataBaseRepo.searchTransactions(periodId, dateFilter, amount, descriptionFilter, categoryId);
	        
	        HashMap<Long, Integer> merchantMap = new HashMap<Long, Integer>();
	        HashMap<Long, Integer> categoryMap = new HashMap<Long, Integer>();
	        HashMap<Long, Integer> goalMap = new HashMap<Long, Integer>();
	        HashMap<Long, Integer> subtotalMap = new HashMap<Long, Integer>();
	        HashMap<Long, Integer> periodMap = new HashMap<Long, Integer>();
	        
	        for (int i = 0; i < merchants.size(); i++) {
	        	merchantMap.put(merchants.get(i).getId(), i);
	        }
	        
	        for (int i = 0; i < categories.size(); i++) {
	        	categoryMap.put(categories.get(i).getId(), i);
	        }
	        
	        for (int i = 0; i < goals.size(); i++) {
	        	goalMap.put(goals.get(i).getCategoryId(), i);
	        }
	        
	        for (int i = 0; i < subtotals.size(); i++) {
        		subtotalMap.put(subtotals.get(i).getCategoryId(), i);
	        }
	        
	        for (int i = 0; i < periods.size(); i++) {
	        	periodMap.put(periods.get(i).getId(), i);
	        }

	        model.addAttribute("focus1", focus1);
	        model.addAttribute("focus2", focus2);
	        model.addAttribute("transactions", transactions);
	        model.addAttribute("merchants", merchants);
	        model.addAttribute("merchantMap", merchantMap);
	        model.addAttribute("categories", categories);
	        model.addAttribute("categoryMap", categoryMap);
	        model.addAttribute("subtotals", subtotals);
	        model.addAttribute("subtotalMap", subtotalMap);
	        model.addAttribute("goals", goals);
	        model.addAttribute("goalMap", goalMap);
	        model.addAttribute("periods", periods);
	        model.addAttribute("periodMap", periodMap);
	        model.addAttribute("periodId", periodId);
	        model.addAttribute("categoryFilter", categoryFilter);
	        model.addAttribute("dateFilter", dateFilter);
	        model.addAttribute("amountFilter", amountFilter);
	        model.addAttribute("descriptionFilter", descriptionFilter);
	        model.addAttribute("merchantShow", merchantShow);
		}
        
        return "show";
    }
	
	//POST mapping for overview pages, handles updating the transactions based on any modifications made, keeps same filters as get mapping
	@PostMapping("/summary")
	public RedirectView saveTranscation(@RequestParam(name = "transactionId[]", required = false) String[] ids, @RequestParam(name = "date[]", required = false) String[] dates, 
			@RequestParam(name = "amount[]", required = false) String[] amounts, @RequestParam(name = "description[]", required = false) String[] descriptions, @RequestParam(name = "merchantName[]", required = false) String[] merchants, 
			@RequestParam(name = "categoryName[]", required = false) String[] categories, @RequestParam(name = "period", required = false) String period, @RequestParam(name = "categoryFilter", required = false) String categoryFilter, 
			@RequestParam(name = "dateFilter", required = false) String dateFilter, @RequestParam(name = "amountFilter", required = false) String amountFilter, @RequestParam(name = "descriptionFilter", required = false) String descriptionFilter,
			@RequestParam(name = "remove", required = false) String remove) {
		Long periodId = null;
		
		if (period != null) {
			periodId = Long.parseLong(period);
		}
		
		List<Transaction> transactions = dataBaseRepo.searchTransactions(periodId, null, null, null, null);
        List<Merchant> merchantsDB = dataBaseRepo.getMerchants();
        List<Category> categoriesDB = dataBaseRepo.getCategories();
        HashMap<Long, Integer> merchantMap = new HashMap<Long, Integer>();
        HashMap<Long, Integer> categoryMap = new HashMap<Long, Integer>();
		
        for (int i = 0; i < merchantsDB.size(); i++) {
        	merchantMap.put(merchantsDB.get(i).getId(), i);
        }
        
        for (int i = 0; i < categoriesDB.size(); i++) {
        	categoryMap.put(categoriesDB.get(i).getId(), i);
        }
        
        if (remove == null) {
			if (ids != null) {
				//check if there is a change has been made, and if so, execute it
				for (int i = 0; i < ids.length; i++) {
					for (Transaction transaction : transactions) {
						try {
							if (transaction.getId() == Long.parseLong(ids[i])) {
								if (transaction.getMerchantId() != null) {
									if (!(transaction.getDate().equals(dates[i]) && transaction.getAmount() == Double.parseDouble(amounts[i]) && 
											merchantsDB.get(merchantMap.get(transaction.getMerchantId())).getName().equals(merchants[i]) &&
											merchantsDB.get(merchantMap.get(transaction.getMerchantId())).getCategoryId().equals(Long.parseLong(categories[i])))) {
										
										//System.out.println(ids[i] + "|" + dates[i] + "|" + amounts[i] + "|" + merchants[i] + "|" + categories[i]);
										//System.out.println(transaction);
										
										dataBaseRepo.updateTransaction(dates[i], amounts[i], descriptions[i], Long.parseLong(ids[i]));
										dataBaseRepo.updateMerchant(merchants[i], Long.parseLong(categories[i]), transaction.getMerchantId());
									
										dataBaseRepo.updateMerchantId();
										dataBaseRepo.calculateSubtotals();
									}
								}
								else {
									if (!(transaction.getDate().equals(dates[i]) && transaction.getAmount() == Double.parseDouble(amounts[i]) && transaction.getDescription().equals(descriptions[i]) &&
											categoriesDB.get(categoryMap.get(Long.parseLong(categories[i]))).getName().equals("Other"))) {
										
										//System.out.println(ids[i] + "|" + dates[i] + "|" + amounts[i] + "|" + descriptions[i] + "|" + categories[i]);
										//System.out.println(transaction);
										
										Manage manage = new Manage();
										String newDes = manage.parseDescription(transaction);
										//System.out.println(newDes);
										
										dataBaseRepo.insertMerchant(newDes, newDes, Long.parseLong(categories[i]));
										dataBaseRepo.updateTransaction(dates[i], amounts[i], descriptions[i], Long.parseLong(ids[i]));
										dataBaseRepo.updateTransactionMerchant(Long.parseLong(ids[i]), dataBaseRepo.getMerchants().get(merchantsDB.size()).getId());

										updateMerchants();
									}
								}
							}
						}
						catch (Exception e) {}
					}
					
				}
			}
        }
        else {
        	dataBaseRepo.deleteTransaction(Long.parseLong(remove));
        	dataBaseRepo.calculateSubtotals();
        }
		
		String periodString = "?period=";
		
		if (periodId != null) {
			periodString += period;
		}
		else {
			periodString = "";
		}
		
		if (dateFilter == null) {
			dateFilter = "";
		}
		if (amountFilter == null) {
			amountFilter = "";
		}
		if (descriptionFilter == null) {
			descriptionFilter = "";
		}
		if (categoryFilter == null) {
			categoryFilter = "";
		}
		
		return new RedirectView("/summary" + periodString + "&dateFilter=" + dateFilter + "&amountFilter=" + amountFilter + "&descriptionFilter=" + descriptionFilter + "&categoryFilter=" + categoryFilter);
	}
	
	//get mapping for import page, displays a message based on whether there are any cached transactions
	@GetMapping("/import")
    public String importTransactions(Model model) {
		if (importTransactionCSV != null) {
			model.addAttribute("transactions", importTransactionCSV);
			model.addAttribute("message", "Upload Successful. Please check that info is correct, then click sort.");
		}
		else {
			model.addAttribute("message", "Please select a file to upload.");
		}
		
        return "import";
    }
    
	//post mapping for import page, handles uploads and saving that to the database, along with starting the sort routine
    @PostMapping("/import")
    public String uploadCSVFile(@RequestParam(name="file", required=false) MultipartFile file, @RequestParam("preview") boolean preview, Model model) {
    	model.addAttribute("title", "File Upload");
    	
    	if (file == null) {
    		if (importTransactionCSV != null) {
    			dataBaseRepo.insertUpdate(java.time.LocalTime.now().toString(), java.time.LocalDate.now().toString());
	    		dataBaseRepo.insertTransactions(importTransactionCSV, dataBaseRepo.getLatestUpdate());
	        	importTransactionCSV = null;
	        	sorting();
	        	model.addAttribute("message", "Upload & Sort Successful.");
    		}
    		else {
	        	model.addAttribute("message", "No file preview selected.");
    		}
    	}
    	else if (file.isEmpty() && importTransactionCSV == null) {
            model.addAttribute("message", "Error: Please select a CSV file to upload.");
            model.addAttribute("displayTable", false);
        } else {
            try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            	if (!file.isEmpty()) {
            		importTransactionCSV = new CsvToBeanBuilder<TransactionCSV>(reader).withType(TransactionCSV.class).withIgnoreLeadingWhiteSpace(true).withSkipLines(1).build().parse();

	            	model.addAttribute("transactions", importTransactionCSV);
	            	
	                if (!importTransactionCSV.get(0).toString().contains("null")) {
	                    model.addAttribute("displayTable", true);
	                	
	                    model.addAttribute("message", "Preview Successful. Please check that info is correct, then click sort.");
	                }
	                else {
	                	model.addAttribute("message", "Error: File contents not correct.");
	                    model.addAttribute("displayTable", false);
	                }
	            }
            	else {
            		model.addAttribute("message", "Error: File Empty.");
                    model.addAttribute("displayTable", false);
            	}

            } catch (Exception ex) {
                model.addAttribute("message", "Error: An error occurred while processing the CSV file.");
                model.addAttribute("displayTable", false);
                System.out.println(ex);
            }
        }

        return "import";
    }
    
    //change to manage page, display potential duplicates
    //get mapping for import page, displays a message based on whether there are any cached transactions
  	@GetMapping("/manage")
	public String managePage(@RequestParam(name = "update", required = false) String update, @RequestParam(name = "type", required = false) String type, @RequestParam(name = "refresh", required = false) String refresh, Model model) {
		if (type == null) type = "upload";
		
		if (type.equals("upload")) {
			List<Update> updates = dataBaseRepo.getUpdates();
			
	  		Long updateId = null;
	    	if (updates.size() != 0) updateId = updates.get(0).getId();
			
			if (update != null) updateId = Long.parseLong(update);
			List<Transaction> transactions = dataBaseRepo.getTransactionsByUpdate(updateId);
			

			model.addAttribute("transactions", transactions);
			model.addAttribute("updates", updates);
			model.addAttribute("updateId", updateId);
		}
		else if (type.equals("duplicate")) {
			if (refresh != null) dataBaseRepo.findDuplicates();
			
			List<Duplicate> duplicates = dataBaseRepo.getDuplicates();
			
			ArrayList<Transaction[]> outputDupe = new ArrayList<Transaction[]>();
			
			for (Duplicate dupe : duplicates) {
				outputDupe.add(new Transaction[] {dataBaseRepo.getTransactionById(dupe.getFirstId()), dataBaseRepo.getTransactionById(dupe.getSecondId())});
			}
			
			model.addAttribute("duplicates", outputDupe);
		}
		
		model.addAttribute("type", type);
  		return "manage";
	}
  	
  	//get mapping for import page, displays a message based on whether there are any cached transactions
  	@PostMapping("/manage")
	public RedirectView historyPagePost(@RequestParam(name = "remove", required=false) String update, @RequestParam(name = "update", required=false) String currUpdate, 
			@RequestParam(name = "goto", required=false) String gotoTran, @RequestParam(name = "ignore", required=false) String ignore, 
			@RequestParam(name = "removeDupes", required=false) String removeDupes, Model model) {
  		if (update != null && currUpdate != null) {
	  		if (!update.equals("-1")) {
				Long updateId = Long.parseLong(update);
				
				dataBaseRepo.deleteTransactionsByUpdate(updateId);
				dataBaseRepo.deleteUpdateById(updateId);
				return new RedirectView("/manage");
	  		}
	  		else {
	  			return new RedirectView("/manage?update="+currUpdate);
	  		}
  		}
  		else if (gotoTran != null) {
  			Scanner seperator = new Scanner(gotoTran).useDelimiter(",");
  			Long gotoTran1 = seperator.nextLong();
  			Long gotoTran2 = seperator.nextLong();
  			
			return new RedirectView("/summary?period=" + dataBaseRepo.getPeriodByDate(dataBaseRepo.getTransactionById(gotoTran1).getDate()).getId()
					+ "&focus1=" + gotoTran1 + "&focus2=" + gotoTran2);
  		}
  		else if (ignore != null) {
  			Scanner seperator = new Scanner(ignore).useDelimiter(",");
  			Long tran1 = seperator.nextLong();
  			Long tran2 = seperator.nextLong();
  			
  			dataBaseRepo.removeDuplicate(tran2);
  			return new RedirectView("/manage?type=duplicate");
  		}
  		else if (removeDupes != null) {
  			dataBaseRepo.removeTransactionsFromDuplicates();
  			
  			return new RedirectView("/manage?type=duplicate");
  		}
  		else {
			return new RedirectView("/manage");
  		}
	}
    
    //handles the get mapping for the planning page, displays the goals, categories, and periods tables within the model along with maps
    @GetMapping("/planning")
    public String planningPage(@RequestParam(name = "period", required = false) String period, @RequestParam(name = "type", required = false) String type, Model model) {
    	List<Period> periods = dataBaseRepo.getPeriods();
    	
    	Long periodId = null;
    	if (periods.size() != 0) periodId = periods.get(0).getId();
		
		if (period != null) {
			periodId = Long.parseLong(period);
		}
		
		HashMap<Long, Integer> periodMap = new HashMap<Long, Integer>();
		
		if (type == null) {
			type = "insert";
		}
    	
		List<Category> categories = dataBaseRepo.getCategories();
		List<ApiMap> apiMappings = dataBaseRepo.getApiMaps();
        HashMap<Long, Integer> categoryMap = new HashMap<Long, Integer>();
        HashMap<Long, Integer> goalMap = new HashMap<Long, Integer>();
		
        for (int i = 0; i < categories.size(); i++) {
        	categoryMap.put(categories.get(i).getId(), i);
        }
        
		if (periodId != null) {
	        List<Goal> goals = dataBaseRepo.getGoalsByPeriod(periodId);
	        
	        List<Goal> sampleGoals = null;
	        if (periods.size() != 0) {
	        	sampleGoals = dataBaseRepo.getGoalsByPeriod(periods.get(0).getId());
	        }
	        
	        for (int i = 0; i < goals.size(); i++) {
	        	goalMap.put(goals.get(i).getCategoryId(), i);
	        }
	    
	        model.addAttribute("goals", goals);
	        model.addAttribute("sampleGoals", sampleGoals);
	        model.addAttribute("periods", periods);
	        model.addAttribute("periodId", periodId);
		}

        model.addAttribute("categories", categories);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("apiMappings", apiMappings);
        model.addAttribute("goalMap", goalMap);
        model.addAttribute("type", type);
        return "planning";
    }
    
    //post mapping for planning, handles updating goals, and deleting periods
    @PostMapping("/planning")
    public RedirectView planningSubmit(@RequestParam("start") String startDate, @RequestParam("end") String endDate, @RequestParam("amount[]") String[] amounts, @RequestParam("category[]") String[] category, 
    		@RequestParam("goalId[]") String[] goalIds, @RequestParam("new") boolean createNew, @RequestParam(name = "categories[]", required = false) String[] categoriesApi, @RequestParam(name = "apiMaps[]", required = false) String[] apiMaps,
    		@RequestParam(name = "categoriesID[]", required = false) String[] categoriesIdApi, @RequestParam(name = "removeCategory", required = false) String removeCategoryId, @RequestParam(name = "period", required = false) String period, 
    		@RequestParam(name = "type", required = false) String type, @RequestParam(name = "categoryNames[]", required = false) String[] categoryNames, Model model) {
    	List<Period> periods = dataBaseRepo.getPeriods();
    	List<Category> categories = dataBaseRepo.getCategories();
    	
    	Long periodId = null;
    	if (periods.size() != 0) periodId = periods.get(0).getId();
		
		if (period != null) {
			periodId = Long.parseLong(period);
		}
		
		
    	if (!type.equals("delete")) {
    		if (!type.equals("categories")) {
		    	if (createNew) dataBaseRepo.insertPeriod(startDate, endDate);
		    	
		    	periods = dataBaseRepo.getPeriods();
				periodId = periods.get(0).getId();
				
				if (period != null) {
					periodId = Long.parseLong(period);
				}
				
				int periodIndex = 0;
				for (int i = 0; i < periods.size(); i++) {
					if (periods.get(i).getId() == periodId) {
						periodIndex = i;
						break;
					}
				}
		        
		        for (int i = 0; i < amounts.length; i++) {
		        	if (amounts[i].equals("")) {
		        		amounts[i] = "0";
		        	}
		        	
		        	try {
			        	if (createNew || dataBaseRepo.getGoalsById(Long.parseLong(goalIds[i])).size() == 0) {
			        		dataBaseRepo.insertGoal(amounts[i], periods.get(periodIndex).getId(), Long.parseLong(category[i]));
			        	}
			        	else {
			        		dataBaseRepo.updateGoal(amounts[i], Long.parseLong(goalIds[i]));
			        	}
		        	}
		        	catch (Exception e) {}
		        }
	
				dataBaseRepo.calculateSubtotals();
    		}
    		else {
    			if (apiMaps != null) {
    				ArrayList<ArrayList<String>> newApiMaps = new ArrayList<ArrayList<String>>();
    				
	    			for (int i = 0; i < apiMaps.length; i++) {
	    				ArrayList<String> apiRow = new ArrayList<String>();
	    				
	    				Scanner scan = new Scanner(apiMaps[i]).useDelimiter(",");
	    				
	    				while (scan.hasNext()) {
	    					apiRow.add(scan.next());
	    				}
	    				newApiMaps.add(apiRow);
	    			}
    				
	    			for (int i = 0; i < categoriesApi.length; i++) {
	    				Long categoryId;
	    				
	    				if (categoriesIdApi.length <= i) {
	    					dataBaseRepo.insertCategory(categoriesApi[i]);
	    					
	    					categories = dataBaseRepo.getCategories();
	    					categoryId = dataBaseRepo.getLatestCategory().getId();
	    				}
	    				else {
	    					categoryId = Long.parseLong(categoriesIdApi[i]);
	    				}
	    				
	    				if (newApiMaps.get(i).size() != 0) {
		    				for (String elem : newApiMaps.get(i)) {
		    					//System.out.print(elem + " ");
		    					dataBaseRepo.updateApiMaps(categoryId, Long.parseLong(elem));
		    				}
		    				//System.out.println();
	    				}
	    			}
	    			
	    			System.out.println();
    				for (int i = 0; i < categoryNames.length; i++) {
    					if (!categoryNames[i].equals(categories.get(i).getName())) {
            				System.out.println(categoryNames[i] + "|" + categories.get(i).getName());
            				dataBaseRepo.updateCategory(categoryNames[i], categories.get(i).getId());
    					}
    				}
    			}
    			
    			if (removeCategoryId != null) {
    				//FIX MERCHANT ERROR
    				//ADD OPTION TO MOVE CATEGORY ORDER
    				//fix pre-14 thing
    				//add choice to view tran desc or merc desc
    				System.out.println(Long.parseLong(removeCategoryId));
    				List<Merchant> updateMerchants = dataBaseRepo.getMerchantsByCategory(Long.parseLong(removeCategoryId));
    				for (Merchant m : updateMerchants) {
        				dataBaseRepo.updateMerchantIds(m.getId(), null);
    				}
    				dataBaseRepo.deleteMerchantsByCategory(Long.parseLong(removeCategoryId));
    				dataBaseRepo.deleteSubtotalsByCategory(Long.parseLong(removeCategoryId));
    				dataBaseRepo.deleteGoalsByCategory(Long.parseLong(removeCategoryId));
    				dataBaseRepo.deleteCategoryById(Long.parseLong(removeCategoryId));
    			}
    		}
    	}
    	else {
    		dataBaseRepo.deleteSubtotalsByPeriod(periodId);
    		dataBaseRepo.deleteGoalsByPeriod(periodId);
    		dataBaseRepo.deletePeriodById(periodId);
    	}
        
        return new RedirectView("/planning?period=" + periodId + "&type=" + type);
    }
    
    //sorting method, handles running the sorting routine and inserting new merchants
    public void sorting() {
    	Manage manager = new Manage();
    	
		manager.setApiMap(dataBaseRepo.getApiMaps());
		manager.setCategories(dataBaseRepo.getCategories());
		manager.setUnsortedTransactions(dataBaseRepo.findUnsortedTransactions());
		
		for (Transaction t : dataBaseRepo.findUnsortedTransactions()) {
			System.out.println(t.getDescription());
		}
		
		for (Merchant newMerchant : manager.sort()) {
		   	System.out.println("Insert: " + newMerchant.getPattern() + " " + newMerchant.getCategoryId());
		   	dataBaseRepo.insertMerchant(newMerchant);
		}
		
		
		updateMerchants();
    }
   
    public void updateMerchants() {
    	Manage manager = new Manage();
    	List<Merchant> prevMerchants = dataBaseRepo.getMerchants();
		List<Merchant> removedMerchants = dataBaseRepo.getMerchants();
		manager.removeDuplicates(removedMerchants);
		
		for (Merchant m : prevMerchants) {
			if (!removedMerchants.contains(m)) {
				if (m.getId() != null) {
					dataBaseRepo.updateMerchantIds(m.getId(), null);
					dataBaseRepo.deleteMerchant(m.getId());
				}
			}
		}
		
		dataBaseRepo.updateMerchantId();
		dataBaseRepo.calculateSubtotals();
    }
}