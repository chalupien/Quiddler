<?php

if (isset($_GET['words']) && isset($_GET['cardssent'])) {
	playerPlayWordsGOOD(0,0,"PHP",$_GET['cardssent'],$_GET['words'],$_GET['showncards'],$_GET['blindCards'],$_GET['usedextra'],1);	
}

function playerPlayWordsGOOD($gameid,$round,$nickname,$thesecardssent,$thesewords,$shownCard,$blindCard,$usedExtra,$echo) {
	
	if ($usedExtra=="") {
		$json_cardssenttoplayer = $thesecardssent;
	}
	else {
		$json_cardssenttoplayer = $thesecardssent .",". $usedExtra;
	}
	
	$json_words = $thesewords . " ";
	
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	 
	
	$dlcards = array ("in", "er", "qu","th", "cl");
	$dlcards_points = array(7,7,9,9,10);
	
	for ($c=0;$c<sizeof($dlcards);$c++) {
		$dlCardPoints[$dlcards[$c]] = $dlcards_points[$c];
	}
	
	
	
	$cards = array ("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",);
	$cards_points = array(2,8,8,5,2,6,6,7,2,13,8,3,5,5,2,6,15,5,3,3,4,11,10,12,4,14);
	
	for ($c=0;$c<sizeof($cards);$c++) {
		$slCardPoints[$cards[$c]] = $cards_points[$c];
	}
	
	$toecho .= "<h2>".$json_cardssenttoplayer."</h2>";
	
	$cardsSentToPlayer = ltrim($json_cardssenttoplayer,",");
	$cardsSentToPlayer = explode(",",$cardsSentToPlayer);
	
	
	$error = '';
	$scoreError = 0;
	
	$wordsPlayerSentTEST = str_replace(" ","",$json_words);
	
	
	$arrayofCardsToPlayer = $cardsSentToPlayer;
	
	

	function cmp_len($a, $b)
	{
		return (strlen($a) < strlen($b));
	}

	usort($arrayofCardsToPlayer, "cmp_len");
	
	
	
	//**
	//GET REAL WORD FROM USER AND MAKE STRING TO ADD POINTS
	$userWordsCorrect = "";
	$wordsPlayerSentWORDS = $json_words;
	$userWordsSENT = explode(" ",$wordsPlayerSentWORDS);
	
	if (sizeof($userWordsSENT)==1) {
		$userWord = $json_words;
		
		
		$file = file_get_contents('http://www.google.com/dictionary/json?callback=a&q='.$userWord.'&sl=en&tl=en&restrict=pr,de&client=te');

		$file = substr($file, 2, -10);
		$file = preg_replace("/\\\x[0-9a-f]{2}/", "", $file);

		$json = json_decode($file,true);
		//echo $json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'] . "<Br><Br>";

		
		
		if (strlen($json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'])>0 && $json['primaries'][0]['terms'][0]['labels'][0]['text']!="Abbreviation") {
			$userWordsByLetter .= $userWordsSENT[$x];
			$userWordsCorrect .= $json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'] . ",";
		}
	}
	
	else {

		for ($x=0;$x<sizeof($userWordsSENT);$x++) {
			
			$file = file_get_contents('http://www.google.com/dictionary/json?callback=a&q='.$userWordsSENT[$x].'&sl=en&tl=en&restrict=pr,de&client=te');
	
			$file = substr($file, 2, -10);
			$file = preg_replace("/\\\x[0-9a-f]{2}/", "", $file);
	
			$json = json_decode($file,true);
			
			if (strlen($json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'])>0 && $json['primaries'][0]['terms'][0]['labels'][0]['text']!="Abbreviation") {
				$userWordsByLetter .= $userWordsSENT[$x];
				$userWordsCorrect .= $json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'] . ",";}
		}
	}
	
	$numberOfCorrectWords = explode(",",rtrim($userWordsCorrect,","));
	$userWordsByLetterArray = explode(",",rtrim($userWordsByLetter,","));
	
	
	
		$showNumberOfCorrectWords = $userWordsCorrect;//sizeof($numberOfCorrectWords);
		for ($x=0;$x<sizeof($numberOfCorrectWords);$x++) {
			if (stripos($userWordsCorrect,"!")) {
				$showNumberOfCorrectWords = "0";
			}
			$correctWordsByLetter .= $userWordsByLetterArray[$x];
		}
	
	
	
	$toecho .= "Correct Words Alltogehter: ".$correctWordsByLetter . "<br><hr>";
	
	
	
	
	
	
	for ($c=0;$c<sizeof($arrayofCardsToPlayer);$c++) {
		
		if (strlen($arrayofCardsToPlayer[$c])==2) {
			
			if (stripos($correctWordsByLetter, $arrayofCardsToPlayer[$c]) !== false) {
			
				
				$posToRemove = stripos($correctWordsByLetter, $arrayofCardsToPlayer[$c]);
				
				$toecho .= $c ." Found: ".$arrayofCardsToPlayer[$c] . " - IN Word<br>";
			
				$toecho .= "<font color='red'>Removing: ".$arrayofCardsToPlayer[$c]."</font> - ";
				$correctWordsByLetter = substr_replace($correctWordsByLetter, "", $posToRemove, 2);
				
				$playerScore += $dlCardPoints[$arrayofCardsToPlayer[$c]];
				
				$toecho .= "<i>Double Points:".$dlCardPoints[$arrayofCardsToPlayer[$c]]."</i><br>";
				$toecho .= "Left: ".$correctWordsByLetter . "<br>";
				$toecho .= "Score: ".$playerScore . "<br><Br>";
				
			}
			
			else {
				$errors .= "Double: " .$arrayofCardsToPlayer[$c]."=".$dlCardPoints[$arrayofCardsToPlayer[$c]]."<br>";
				$cardErrors[] = $dlCardPoints[$arrayofCardsToPlayer[$c]];
				$discards .= $arrayofCardsToPlayer[$c].","; 
				$scoreError += $dlCardPoints[$arrayofCardsToPlayer[$c]];}
				
		}
		
		else {
			//slCardPoints
			
			if (stripos($correctWordsByLetter, $arrayofCardsToPlayer[$c]) !== false) {
			
				
				$posToRemove = stripos($correctWordsByLetter, $arrayofCardsToPlayer[$c]);
				
				$toecho .= $c ." Found: ".$arrayofCardsToPlayer[$c] . " - IN Word<br>";
			
				$toecho .= "<font color='red'>Removing: ".$arrayofCardsToPlayer[$c]."</font> - ";
				$correctWordsByLetter = substr_replace($correctWordsByLetter, "", $posToRemove, 1);
				
				$playerScore += $slCardPoints[$arrayofCardsToPlayer[$c]];
				
				$toecho .= "<i>Single Points:".$slCardPoints[$arrayofCardsToPlayer[$c]]."</i><br>";
				$toecho .= "Left: ".$correctWordsByLetter . "<br>";
				$toecho .= "Score: ".$playerScore . "<br><Br>";
				
			}
			
			else {
				$errors .= "Single: " .$arrayofCardsToPlayer[$c]."=".$slCardPoints[$arrayofCardsToPlayer[$c]]."<br>";
				$cardErrors[] = $slCardPoints[$arrayofCardsToPlayer[$c]];
				$discards .= $arrayofCardsToPlayer[$c].",";
				$scoreError += $slCardPoints[$arrayofCardsToPlayer[$c]];}
			
		}
		
	
		
	
	}
	$toecho .= "</h2>";
	
	$discards = rtrim($discards,",");
	
	$toecho .= "<hr>".$discards."<hr>";
	
	//$output = array_merge(array_diff($array1, $array2), array_diff($array2, $array1));
	
	$cardPointsToDiscard = 0;
	
	if ($usedExtra1="") {
	$cardPointsToDiscard = max($cardErrors);
	}
	
	$toecho .= "<h1>Errors:<blockquote>".$errors."</blockquote></h1>";
	$toecho .= "Score: ".$playerScore . "<br>";
	$totalscore = intval($playerScore-$scoreError+$cardPointsToDiscard);
	
	if ($totalscore<0 || $showNumberOfCorrectWords=="0") { $totalscore=0;}
	$toecho .= "<h1>TOTAL: ". $totalscore . "</h1>";
	
	
	
	
	
	
	
	
	//mysql_num_rows(mysql_query("SELECT * FROM dictionary WHERE word = '".$userWord."'"))>0
	
	if (strlen($correctWordsByLetter)>0) { $error=-5;}
	

	
	$toecho .= "<hr><h1>-(" . strlen($wordsPlayerSentTEST) . ") - Count Error: ".$error."</h1><hr>";
	/*
	Errors:
	>0 = Points Taken OFF 
	-1 = 
	-2 = 
	-3 = 
	-4 = 
	-5 = Sent is too long
	*/
	
	if ($echo==1) { echo $toecho; }
	
	//$toecho .= json_encode(array('nickname'=>$obj->{'nickname'},'goodwords'=>sizeof($numberOfCorrectWords),'points'=>$totalscore,'gameid'=>intval($obj->{'gameid'}),'round'=>intval($obj->{'round'}),'error'=>$error));
	
		echo json_encode(array('nickname'=>$nickname,'goodwords'=>$showNumberOfCorrectWords,'points'=>$totalscore,'gameid'=>intval($gameid),'round'=>intval($round),'error'=>$error,'discard'=>$discards,'cardstoplayer'=>$json_cardssenttoplayer));
		
		mysql_close($dbh);
	
		
		
}
?>