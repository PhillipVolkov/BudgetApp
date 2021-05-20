# BudgetApp

IB CS SL IA project

Instructions:
All dependencies should be installed EXCEPT the postgres database

Once postgreSQL has been installed, it needs to be set up to the 
"localhost:5433" URL, with a new database titled "BudgetDB". Both
the username and password of the specific server (not the master keys)
should be set to "postgres". 


Furthermore, the SQL file "TableCreation.sql" needs to be run in the
query tool. Within the file "TableFlow.sql", the line

options (filename 'E:\Documents\School Docs\IA\CS_SL\api_mapping.csv', format 'csv', header 'true');

needs to be modified to the directory in which the included "api_mapping.csv"
file is located. This is necessary for the sorting algorithm to correctly
map the API results to categories.


Once the "budgetApp.jar" has been run,the webapp should be accessible 
on the "10.0.0.149:8080" IP address, but this may have to be modified 
in the application.properties file, located in the following directory: 
"Submission\Product\budgetApp_source\src\main\resources"
