package com.example.demo;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

//class that contains methods for performing SQL queries

@Repository
public class DatabaseRepo {
	@Autowired
    @PersistenceContext
    public EntityManager entityManager;
    
	//insert merchant into POSTGRES
    @Transactional
    public void insertMerchant(Merchant merchant) {
    	entityManager.createNativeQuery("insert into merchants (merchant_name, merchant_pattern, category_id) values (?,?,?)")
        .setParameter(1, merchant.getName())
        .setParameter(2, merchant.getPattern())
        .setParameter(3, merchant.getCategoryId())
        .executeUpdate();
    }
    
  //insert merchant into POSTGRES based on String parameter
    @Transactional
    public void insertMerchant(String name, String pattern, Long category) {
    	entityManager.createNativeQuery("insert into merchants (merchant_name, merchant_pattern, category_id) values (?,?,?)")
        .setParameter(1, name)
        .setParameter(2, pattern)
        .setParameter(3, category)
        .executeUpdate();
    }
    
    //update POSTGRES merchant ids
    @Transactional
    public void updateMerchantId() {
    	entityManager.createNativeQuery("update transactions set merchant_id = (select merchant_id from merchants where upper(transaction_description) "
    			+ "like  '%' || upper(merchant_pattern) || '%')").executeUpdate();
    }
    
    //insert transactions into POSTGRES
    @Transactional
    public void insertTransactions(List<TransactionCSV> transcations, Update update) {
    	for (TransactionCSV transcation : transcations) {
    		entityManager.createNativeQuery("insert into transactions (transaction_date, transaction_amount, transaction_description, update_id) \r\n"
    				+ "values (TO_DATE(?, 'MM/DD/YYYY'), -CAST(coalesce(nullif(?, ''), '0') AS numeric(9, 2)) + CAST(coalesce(nullif(?, ''), '0') AS numeric(9,2)), ?, ?)")
    		.setParameter(1, transcation.getDate())
    		.setParameter(2, transcation.getDebit())
    		.setParameter(3, transcation.getCredit())
    		.setParameter(4, transcation.getDescription())
    		.setParameter(5, update.getId())
    		.executeUpdate();
    	}
    }
    
    //insert transactions into POSTGRES
    @Transactional
    public void insertUpdate(String time, String date) {
		entityManager.createNativeQuery("insert into updates (update_time, update_date) \r\n"
				+ "values (TO_TIMESTAMP(?, 'HH24:MI:SS'), TO_DATE(?, 'YYYY-MM-DD'))")
		.setParameter(1, time)
		.setParameter(2, date)
		.executeUpdate();
    }
    
    //wipe the subtotals POSTGRES table, and recalculate them
    @Transactional
    public void calculateSubtotals() {
    	entityManager.createNativeQuery("truncate subtotals restart identity cascade").executeUpdate();
    	
    	entityManager.createNativeQuery("insert into subtotals (period_id, category_id, subtotal_amount)\r\n"
    			+ "select (select period_id from periods p where t.transaction_date between p.period_start and p.period_end), "
    			+ "(select category_id from merchants m where m.merchant_id = t.merchant_id), \r\n"
    			+ "sum(t.transaction_amount)   from transactions t\r\n"
    			+ "group by 1,2").executeUpdate();
    }
    
    //insert period into POSTGRES
    @Transactional
    public void insertPeriod(String start, String end) {
    	entityManager.createNativeQuery("insert into periods (period_start, period_end) values (TO_DATE(?1, 'YYYY-MM-DD'), TO_DATE(?2, 'YYYY-MM-DD'))")
    	.setParameter(1, start)
		.setParameter(2, end)
		.executeUpdate();
    }
 
    //insert goal into POSTGRES
    @Transactional
    public void insertGoal(String amount, Long period, Long category) {
    	entityManager.createNativeQuery("insert into goals (goal_amount, period_id, category_id) values (cast(?1 as numeric(9, 2)), ?2, ?3)")
    	.setParameter(1, amount)
		.setParameter(2, period)
		.setParameter(3, category)
		.executeUpdate();
    }
    
    @Transactional
    public void findDuplicates() {
    	entityManager.createNativeQuery("delete from duplicates;\r\n"
    			+ "insert into duplicates(transaction1_id, transaction2_id) (\r\n"
    			+ "	select t1.transaction_id, t2.transaction_id from transactions t1, transactions t2 where (t1.transaction_date = t2.transaction_date and \r\n"
    			+ "	t1.transaction_amount = t2.transaction_amount and t1.transaction_description = t2.transaction_description and t1.transaction_id <> t2.transaction_id \r\n"
    			+ "	and t2.transaction_id > t1.transaction_id)\r\n"
    			+ ");")
		.executeUpdate();
    }
    
    @Transactional
    public void removeDuplicate(Long tran2) {
    	entityManager.createNativeQuery("delete from duplicates where transaction2_id = ?1")
		.setParameter(1, tran2)
		.executeUpdate();
    }
    
    @Transactional
    public void removeTransactionsFromDuplicates() {
    	entityManager.createNativeQuery("delete from transactions t using duplicates d where d.transaction2_id = t.transaction_id")
		.executeUpdate();
    }
    
    //update goal amount on POSTGRES based on ID
    @Transactional
    public void updateGoal(String amount, Long id) {
    	entityManager.createNativeQuery("update goals set goal_amount = cast(?1 as numeric(9, 2)) where goal_id = ?2")
    	.setParameter(1, amount)
		.setParameter(2, id)
		.executeUpdate();
    }
    
  //insert goal into POSTGRES
    @Transactional
    public void insertCategory(String name) {
    	entityManager.createNativeQuery("insert into categories (category_name) values (?1)")
    	.setParameter(1, name)
		.executeUpdate();
    }
    
    //update transaction info on POSTGRES
    @Transactional
    public void updateTransaction(String date, String amount, String description, Long id) {
    	entityManager.createNativeQuery("update transactions set transaction_date = (TO_DATE(?1, 'YYYY-MM-DD')), transaction_amount = cast(?2 as numeric(9, 2)), transaction_description = ?3 where transaction_id = ?4")
    	.setParameter(1, date)
    	.setParameter(2, amount)
    	.setParameter(3, description)
		.setParameter(4, id)
		.executeUpdate();
    }
    
  //update transaction info on POSTGRES
    @Transactional
    public void updateTransactionMerchant(Long id, Long merchantId) {
    	entityManager.createNativeQuery("update transactions set merchant_id = ?1 where transaction_id = ?2")
    	.setParameter(1, merchantId)
    	.setParameter(2, id)
		.executeUpdate();
    }
    
    //reassign transaction's merchant ids from POSTGRES
    @Transactional
    public void updateMerchantIds(Long prevId, Long newId) {
    	if (newId != null) {
	    	entityManager.createNativeQuery("update transactions set merchant_id = ?1 where merchant_id = ?2")
	    	.setParameter(1, newId)
	    	.setParameter(2, prevId)
			.executeUpdate();
    	}
    	else {
    		entityManager.createNativeQuery("update transactions set merchant_id = null where merchant_id = ?1")
	    	.setParameter(1, prevId)
			.executeUpdate();
    	}
    }
    
    //reassign transaction's merchant ids from POSTGRES
    @Transactional
    public void updateMerchantCategoryIds(Long prevId, Long newId) {
    	if (newId != null) {
	    	entityManager.createNativeQuery("update merchants set category_id = ?1 where category_id = ?2")
	    	.setParameter(1, newId)
	    	.setParameter(2, prevId)
			.executeUpdate();
    	}
    	else {
    		entityManager.createNativeQuery("update merchants set category_id = null where category_id = ?1")
	    	.setParameter(1, prevId)
			.executeUpdate();
    	}
    }
    
    //update merchant info on POSTGRES
    @Transactional
    public void updateMerchant(String name, Long category, Long id) {
    	entityManager.createNativeQuery("update merchants set merchant_name = ?1, category_id = ?2 where merchant_id = ?3")
    	.setParameter(1, name)
    	.setParameter(2, category)
		.setParameter(3, id)
		.executeUpdate();
    }
    
    //update merchant info on POSTGRES
    @Transactional
    public void updateCategory(String name, Long id) {
    	entityManager.createNativeQuery("update categories set category_name = ?1 where category_id = ?2")
    	.setParameter(1, name)
		.setParameter(2, id)
		.executeUpdate();
    }
    
    //delete subtotals from POSTGRES
    @Transactional
    public void deleteSubtotalsByPeriod(Long periodId) {
    	entityManager.createNativeQuery("delete from subtotals where period_id = ?1")
    	.setParameter(1, periodId)
		.executeUpdate();
    }
    
    //delete subtotals from POSTGRES
    @Transactional
    public void deleteSubtotalsByCategory(Long categoryId) {
    	entityManager.createNativeQuery("delete from subtotals where category_id = ?1")
    	.setParameter(1, categoryId)
		.executeUpdate();
    }
    
    //delete merchants from POSTGRES
    @Transactional
    public void deleteMerchantsByCategory(Long categoryId) {
    	entityManager.createNativeQuery("delete from merchants where category_id = ?1")
    	.setParameter(1, categoryId)
		.executeUpdate();
    }
    
    //delete goals from POSTGRES
    @Transactional
    public void deleteGoalsByCategory(Long categoryId) {
    	entityManager.createNativeQuery("delete from goals where category_id = ?1")
    	.setParameter(1, categoryId)
		.executeUpdate();
    }
    
    //delete goals from POSTGRES
    @Transactional
    public void deleteGoalsByPeriod(Long periodId) {
    	entityManager.createNativeQuery("delete from goals where period_id = ?1")
    	.setParameter(1, periodId)
		.executeUpdate();
    }
    
    //delete period from POSTGRES
    @Transactional
    public void deletePeriodById(Long periodId) {
    	entityManager.createNativeQuery("delete from periods where period_id = ?1")
    	.setParameter(1, periodId)
		.executeUpdate();
    }
    
  //delete period from POSTGRES
    @Transactional
    public void deleteCategoryById(Long categoryId) {
    	entityManager.createNativeQuery("delete from categories where category_id = ?1")
    	.setParameter(1, categoryId)
		.executeUpdate();
    }
    
    //delete transactions from POSTGRES
    @Transactional
    public void deleteTransactionsByUpdate(Long updateId) {
    	entityManager.createNativeQuery("delete from duplicates; delete from transactions where update_id = ?1")
    	.setParameter(1, updateId)
		.executeUpdate();
    }
    
    //delete updateById from POSTGRES
    @Transactional
    public void deleteUpdateById(Long updateId) {
    	entityManager.createNativeQuery("delete from updates where update_id = ?1")
    	.setParameter(1, updateId)
		.executeUpdate();
    }
    
    //delete transaction from POSTGRES
    @Transactional
    public void deleteTransaction(Long id) {
    	entityManager.createNativeQuery("delete from duplicates; delete from transactions where transaction_id = ?1")
    	.setParameter(1, id)
		.executeUpdate();
    }
    
    //delete merchant from POSTGRES
    @Transactional
    public void deleteMerchant(Long id) {
    	entityManager.createNativeQuery("delete from merchants where merchant_id = ?1")
    	.setParameter(1, id)
		.executeUpdate();
    }
    
    @Transactional
    public void updateApiMaps(Long categoryId, Long fieldId) {
    	entityManager.createNativeQuery("update api_map set category_id = ?1 where field_id = ?2")
    	.setParameter(1, categoryId)
    	.setParameter(2, fieldId)
    	.executeUpdate();
    }
    
    //read subtotals from hibernate entities
    List<Subtotal> getSubtotals() {
    	return entityManager.createQuery("select s from Subtotal s", Subtotal.class)
    		.getResultList();
    }
    
    //read subtotals, filtered by period from hibernate entities
    List<Subtotal> getSubtotalsByPeriod(Long period) {
    	return entityManager.createQuery("select s from Subtotal s where s.period_id = ?1", Subtotal.class)
    		.setParameter(1, period)
    		.getResultList();
    }
    
    //read transactions, filtered by update from hibernate entities
    List<Transaction> getTransactionsByUpdate(Long update) {
    	return entityManager.createQuery("select t from Transaction t where t.update_id = ?1 order by transaction_date desc", Transaction.class)
    		.setParameter(1, update)
    		.getResultList();
    }
    
    //read transactions, filtered by update from hibernate entities
    Transaction getTransactionById(Long id) {
    	return entityManager.createQuery("select t from Transaction t where t.transaction_id = ?1", Transaction.class)
    		.setParameter(1, id)
    		.getSingleResult();
    }
    
    //read subtotal from hibernate entities
    Subtotal getSubtotal(Long category, Long period) {
    	if (category != null) {
	    	return entityManager.createQuery("select s from Subtotal s where category_id = ?1 and period_id = ?2", Subtotal.class)
	    		.setParameter(1, category)
	    		.setParameter(2, period)
	    		.getSingleResult();
    	}
    	else {
    		return entityManager.createQuery("select s from Subtotal s where category_id is null and period_id = ?1", Subtotal.class)
	    		.setParameter(1, period)
	    		.getSingleResult();
    	}
    }
    
    //read merchant from hibernate entities
    List<Merchant> getMerchantsByCategory(Long category) {
    	return entityManager.createQuery("select m from Merchant m where category_id = ?1", Merchant.class)
    		.setParameter(1, category)
    		.getResultList();
    	
    }
    
    //read goals from hibernate entities
    List<Goal> getGoals() {
    	return entityManager.createQuery("select g from Goal g", Goal.class)
    		.getResultList();
    }
    
    //read goals, filtered by period, from hibernate entities
    List<Goal> getGoalsByPeriod(Long period) {
    	return entityManager.createQuery("select g from Goal g where g.period_id = ?1", Goal.class)
			.setParameter(1, period)
    		.getResultList();
    }
    
    //read goals, filtered by id, from hibernate entities
    List<Goal> getGoalsById(Long id) {
    	return entityManager.createQuery("select g from Goal g where g.goal_id = ?1", Goal.class)
    		.setParameter(1, id)
    		.getResultList();
    }
    
    //read subtotal from hibernate entities
    Goal getGoal(Long category, Long period) {
    	if (category != null) {
    		return entityManager.createQuery("select g from Goal g where category_id = ?1 and period_id = ?2", Goal.class)
	    		.setParameter(1, category)
	    		.setParameter(2, period)
	    		.getSingleResult();
    	}
    	else {
    		return entityManager.createQuery("select g from Goal g where category_id is null and period_id = ?1", Goal.class)
	    		.setParameter(1, period)
	    		.getSingleResult();
    	}
    }
    
    //read periods from hibernate entities
    List<Period> getPeriods() {
    	return entityManager.createQuery("select p from Period p order by period_end desc", Period.class)
    		.getResultList();
    }
    
  //read periods from hibernate entities
    List<Period> getPeriodsByStart() {
    	return entityManager.createQuery("select p from Period p order by period_start", Period.class)
    		.getResultList();
    }
    
    //read categories from hibernate entities
    List<Category> getCategories() {
    	return entityManager.createQuery("select cat from Category cat order by category_id", Category.class)
        		.getResultList();
    }
    
    //read categories from hibernate entities
    Category getLatestCategory() {
    	return entityManager.createQuery("select cat from Category cat where category_id = (select max(category_id) from Category)", Category.class)
        		.getSingleResult();
    }
    
    //read latest update from hibernate entities
    Update getLatestUpdate() {
    	return entityManager.createQuery("select up from Update up where update_id = (select max(update_id) from Update up)", Update.class)
        		.getSingleResult();
    }
    
    //read merchants from hibernate entities
    List<Merchant> getMerchants() {
    	return entityManager.createQuery("select mer from Merchant mer", Merchant.class)
        		.getResultList();
    }
    
    //read APIMAPS from hibernate entities
    List<ApiMap> getApiMaps() {
    	return entityManager.createQuery("select api from ApiMap api order by field_id", ApiMap.class)
        		.getResultList();
    }
    
    //read transactions from hibernate entities
    List<Transaction> getTransactions() {
    	return entityManager.createQuery("select tran from Transaction tran order by transaction_date desc", Transaction.class)
        		.getResultList();
    }
    
  //read updates from hibernate entities
    List<Update> getUpdates() {
    	return entityManager.createQuery("select up from Update up order by update_id desc", Update.class)
        		.getResultList();
    }
    
  //read duplicates from hibernate entities
    List<Duplicate> getDuplicates() {
    	return entityManager.createQuery("select dup from Duplicate dup order by transaction1_id desc", Duplicate.class)
        		.getResultList();
    }
    
    Period getPeriodByDate(String date) {
    	return entityManager.createQuery("select p from Period p where TO_DATE(?1, 'YYYY-MM-DD') between p.period_start and p.period_end", Period.class)
    			.setParameter(1, date)
    			.getSingleResult();
    }
    
    //read transactions from hibernate entities, filtered by either period, date, amount, description, or category, or any combination
    List<Transaction> searchTransactions(Long period, String date, Double amount, String description, Long category) {
    	//period sorting is required, but if any of the other paramaters are not null, they will be added to the query
    	String query = " t.transaction_date between (select period_start from Period p where p.period_id = :period) and (select period_end from Period p where p.period_id = :period) ";
    	
    	if (date != null) {
    		query += "and t.transaction_date = TO_DATE(:date, 'YYYY-MM-DD') ";
    	}
    	if (amount != null) {
    		query += "and t.transaction_amount = :amount ";
    	}
    	if (description != null) {
    		query += "and (t.merchant_id is null and (upper(t.transaction_description) like '%' || upper(:description) || '%') or "
    				+ "((select upper(m.merchant_name) from Merchant m where m.merchant_id = t.merchant_id) like '%' || upper(:description) || '%')) ";
    	}
    	if (category != null) {
    		if (category.equals((long) 14)) {
    			query += "and t.merchant_id is null or ((select m.category_id from Merchant m where m.merchant_id = t.merchant_id) = :category)";
    		}
    		else {
    			query += "and (select m.category_id from Merchant m where m.merchant_id = t.merchant_id) = :category ";
    		}
    	}
    	
    	Query createdQuery = entityManager.createQuery("select t from Transaction t where " + query + "order by transaction_date desc", Transaction.class).setParameter("period", period);;
    	
    	if (date != null) {
    		createdQuery.setParameter("date", date);
    	}
    	if (amount != null) {
    		createdQuery.setParameter("amount", amount);
    	}
    	if (description != null) {
    		createdQuery.setParameter("description", description);
    	}
    	if (category != null) {
    		createdQuery.setParameter("category", category);
    	}
    	
    	return createdQuery.getResultList();
    }
    
    //read unsorted transactions from POSTGRES
    @SuppressWarnings("unchecked")
	List<Transaction> findUnsortedTransactions() {
    	return entityManager.createNativeQuery("select * from  transactions where transaction_id  not in "
    			+ "(select transaction_id from transactions, merchants where upper(transaction_description) like  '%' || upper(merchant_pattern) || '%' )", Transaction.class)
        		.getResultList();
    }
	
    //read all transctions from POSTGRES, ordered by id
	@SuppressWarnings("unchecked")
	List<Transaction> findTransactionsById() {
		return entityManager.createNativeQuery("select * from  transactions order by transaction_id", Transaction.class)
        		.getResultList();
	}
	
	
}