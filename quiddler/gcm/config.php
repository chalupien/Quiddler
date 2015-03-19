<?php
/**
 * Database config variables
 
 $dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	//json_encode(array("error"=>'I cannot connect to the database.'))
	mysql_select_db ("nicklupi_quidder");
 
 */
define("DB_HOST", "localhost");
define("DB_USER", "nicklupi");
define("DB_PASSWORD", "58@Y6L*Rga");
define("DB_DATABASE", "nicklupi_quidder");

/*
 * Google API Key
 */
define("GOOGLE_API_KEY", "AIzaSyBpE7s3eLsKCpy5l5eqSfZutDsRtDm9a44"); // Place your Google API Key
?>