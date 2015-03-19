<?php 

switch ($_GET['action']) {
	case "playerLogin": playerLogin(); break;
	case "createNewGame": createNewGame(); break;
	case "resumeExistingGame": resumeExistingGame(); break;
	case "joinGame": joinGame(); break;
	case "getCards": getCards(); break;
	case "viewAvailableGames": viewAvailableGames(); break;
	case "viewMyGames": viewMyGames($_GET['nickname']); break;
	case "viewPlayers": viewPlayers(); break;
	case "playerPlayWords": playerPlayWords(); break;
	case "playNextRound": playNextRound(); break;
	case "pullExtraCard": pullExtraCard(); break;
	case "discardCard": discardCard(); break;
	case "checkExtraCards": checkExtraCards(); break;
	case "getFinalScore": getFinalScore(); break;
	case "deleteGame": deleteGame(); break;
	
	
}

function discardCard() {
	
	//pullExtraCard NEED gameid,roundno,nickname,cardpulled,stack (shown/blind)
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	//json_encode(array("error"=>'I cannot connect to the database.'))
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$decodeplayercards = json_decode($row['playercards'],true);
		
		$getShownCards = $row['stackshown'];
		$getBlindCards = $row['stackleft'];
		
			for ($x=0; $x<sizeof($decodeplayercards); $x++) {
			if (strpos($decodeplayercards[$x][$obj->{'nickname'}],",")>0) {
					$gotplayercards = $decodeplayercards[$x][$obj->{'nickname'}];
			
					$updatedplayercards = str_replace('{"'.$obj->{'nickname'}.'":"'.$gotplayercards.'"}','{"'.$obj->{'nickname'}.'":"'.str_replace(",,",",",str_replace($obj->{'cardpulled'},"",$gotplayercards)).'"}',$row['playercards']);
					
				}
			}
			
		
		}
	
	
	mysql_query("UPDATE round SET playercards='".$updatedplayercards."' WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'");
	
	
	
	
		
	switch($obj->{'stack'}) {
	
		case "discard":
		
		$newStackLeft = $getBlindCards.",".$getShownCards;
		
	
		mysql_query("UPDATE round SET stackshown='".$obj->{'cardpulled'}."', stackleft='".$newStackLeft."' WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'");
		
		break;
		
		
		
	}// (shown/blind)
	
	
	mysql_close($dbh);
	
	
	


}

function pullExtraCard() {
	
	
	
	//pullExtraCard NEED gameid,roundno,nickname,cardpulled,stack (shown/blind)
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	/*
	$obj->{'roundno'} = 0;
	$obj->{'gameid'} = 46;
	$obj->{'nickname'} = 'shineon';
	*/
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	//json_encode(array("error"=>'I cannot connect to the database.'))
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$decodeplayercards = json_decode($row['playercards'],true);
		
		$getShownCards = $row['stackshown'];
		$getBlindCards = $row['stackleft'];
		
			for ($x=0; $x<sizeof($decodeplayercards); $x++) {
			if (strpos($decodeplayercards[$x][$obj->{'nickname'}],",")>0) {
					$gotplayercards = $decodeplayercards[$x][$obj->{'nickname'}];
					
					
					$updatedplayercards = str_replace('{"'.$obj->{'nickname'}.'":"'.$gotplayercards.'"}','{"'.$obj->{'nickname'}.'":"'.$gotplayercards . "," . $obj->{'cardpulled'}.'"}',$row['playercards']);
					
				}
			}
			
		
		}
	
	
	mysql_query("UPDATE round SET playercards='".$updatedplayercards."' WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'");
	
	
	
	
		
	switch($obj->{'stack'}) {
	
		case "shown":
		$theseShownCards = explode(",",$getShownCards);
		$removeFromShown = array_pop($theseShownCards);
		
		$theseBlindCards = explode(",",$getBlindCards);
		$removeFromBlind = array_pop($theseBlindCards);
		
		$pushToShown = array_push($theseShownCards,$removeFromBlind);
		
		$theseShownCards = implode(",",$theseShownCards);
		$theseBlindCards = implode(",",$theseBlindCards);
		
		mysql_query("UPDATE round SET stackshown='".$theseShownCards."' ,stackleft='".$theseBlindCards."' WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'");
		
		break;
		
		case "blind":
		$theseBlindCards = explode(",",$getBlindCards);
		$removeFromBlind = array_pop($theseBlindCards);
		mysql_query("UPDATE round SET stackleft='".implode(",",$theseBlindCards)."' WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'");
		
		break;
		
	}// (shown/blind)
	
	
	mysql_close($dbh);
	
	
	

	
	
}

function playNextRound() {
	
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	/*
		$obj->{'round'} = 1;
		$obj->{'gameid'} = 46;
		$obj->{'nickname'} = "chalupien";
		$obj->{'points'} = 20;
	*/
	

	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	 
	 mysql_query("DELETE FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".(intval($obj->{'roundno'})-1)."'") or die(mysql_error()); 
	 

	
	
	$result_players_games = mysql_query("SELECT * FROM games WHERE id = '".trim($obj->{'gameid'})."'") or die(mysql_error());  
	
	while($row = mysql_fetch_array($result_players_games)){
		$playersinGame = explode("|",$row['players']);
		
	}
		
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'") or die(mysql_error()); 
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$deleteThisRound = $row['id'];
		
		$decodeplayercards = json_decode($row['playercards'],true);
		
		$getShownCards = $row['stackshown'];
		$getBlindCards = $row['stackleft'];
		
			for ($y=0; $y<sizeof($playersinGame); $y++) {
				for ($x=0; $x<sizeof($decodeplayercards); $x++) {
				if (strpos($decodeplayercards[$x][$playersinGame[$y]],",")>0) {
						$allusedCards .= $decodeplayercards[$x][$playersinGame[$y]] . ",";
					}
				}
			}
		}
		
		$allCardsInGame = $allusedCards.$getBlindCards;
		$allCardsInGame = explode(",",$allCardsInGame);
		
		//$deckofcards = explode("|||",$deck);
		$b=0;
		$numbers = range(0, sizeof($allCardsInGame)); //119
		shuffle($numbers);
		
		foreach ($numbers as $number) {
		$stack[$b] = $number; $b++;
		}
		
		for ($c=0;$c<sizeof($stack);$c++)
		{
		$deckshuffled[$c] = $allCardsInGame[$stack[$c]];
		}
		
		
		$allCardsInGame = $deckshuffled;
		
		for ($x=0;$x<intval($obj->{'round'}+3);$x++) {
			for($y=0;$y<sizeof($playersinGame);$y++) {
				$taken = array_pop($allCardsInGame);
				$eachPlayer[$y] .= $taken . ",";
			}	
		}
		
		for ($c=0;$c<sizeof($eachPlayer);$c++) {
			
			$eachPlayer[$c] = rtrim($eachPlayer[$c],",");
			$playercardsdata[] = array($playersinGame[$c]=>$eachPlayer[$c]);
			
			$playerscores[] = array($playersinGame[$c]=>$obj->{'points'});
			
			
		}
		
		$allCardsInGame = implode(",",$allCardsInGame);
		
		
		
		mysql_query("INSERT INTO round (gameid, roundno, playercards, stackleft, stackshown, playerscores) VALUES ('".$obj->{'gameid'}."', '".$obj->{'round'}."', '".json_encode($playercardsdata)."', '".$allCardsInGame."', '".$getShownCards."', '".json_encode($playerscores)."') ") or die(mysql_error()); 
	
	mysql_query("UPDATE games SET round = '".$obj->{'round'}."', started='1' WHERE id = '".$obj->{'gameid'}."'") or die(mysql_error()); 
		
	
	//
	
	/*
		$obj->{'round'}
		$obj->{'gameid'}
		$obj->{'nickname'}
		$obj->{'points'}
	*/
	
	//Put all cards in front of blind list
	//Pop new cards to each player
	//Score for PLayers
	
	
	

	mysql_close($dbh);
	
	
	
}

function viewAvailableGames() {
	
	header('Content-type: application/json');
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	mysql_select_db ("nicklupi_quidder");
	
	if (isset($_GET['nickname'])) {
	$result_existing_games = mysql_query("SELECT * FROM games WHERE players LIKE '%".$_GET['nickname']."%' AND round !=8"); //  
	}
	else {
		$result_existing_games = mysql_query("SELECT * FROM games WHERE started = 0 AND round = 0"); //  
	}
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$numofplayers = explode("|",$row['players']);
		if (sizeof($numofplayers)>1) {$s="s"; $areis="are";} else {$s=""; $areis="is";}
		
		$timeago = time_elapsed_string($row['datestarted']);
		
		$gamedesc = sizeof($numofplayers)." player".$s." ".$areis." ready to play. Created ".$timeago." ago.";
		
		$roundNum = $row['round'];
		$roundNum++;
		
		if (intval($row['round'])==0) {
		$roundNOO = " ";
		}
		else {
		$roundNOO = "Round #". $roundNum . " ";
		 }
		
		if (isset($_GET['nickname'])) {
	$jsondata[] = array('id'=>$row['id'],'game'=>$roundNOO." Started by " . $numofplayers[0],'desc'=>$gamedesc,'players'=>$row['players'],'round'=>$row['round'],'started'=>$row['started']);
	}
	else {
		$jsondata[] = array('id'=>$row['id'],'game'=>$roundNOO."Started by " . $numofplayers[0],'desc'=>$gamedesc,'players'=>$row['players'],'round'=>$row['round'],'started'=>$row['started']);
	}
			
		
			
		}
	
	mysql_close($dbh);

	if (sizeof($jsondata)==0) {} else {

echo json_encode(array('games'=>$jsondata));  }
//echo json_encode($jsondata);        
      	
}



function checkExtraCards() {
	
	
	
	//checkExtraCards NEED gameid,roundno
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){

		$getShownCards = explode(",",$row['stackshown']);
		$getBlindCards = explode(",",$row['stackleft']);
	
	}
	
	mysql_close($dbh);
	
	
	$gotplayercardsarr["extrablind"] = $getBlindCards[sizeof($getBlindCards)-1];
	$gotplayercardsarr["extrashown"] = $getShownCards[sizeof($getShownCards)-1];
	
	
		echo json_encode($gotplayercardsarr);  
}

function getCards() {
	
	
	
	//getCards NEED gameid,roundno,nickname
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	/*
	$obj->{'roundno'} = 0;
	$obj->{'gameid'} = 46;
	$obj->{'nickname'} = 'shineon';
	*/
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	//json_encode(array("error"=>'I cannot connect to the database.'))
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."' AND roundno = '".$obj->{'roundno'}."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$decodeplayercards = json_decode($row['playercards'],true);
		
		$getShownCards = $row['stackshown'];
		$getBlindCards = $row['stackleft'];
		
			for ($x=0; $x<sizeof($decodeplayercards); $x++) {
			if (strpos($decodeplayercards[$x][$obj->{'nickname'}],",")>0) {
					$gotplayercards = $decodeplayercards[$x][$obj->{'nickname'}];
				}
			}
			
		
		}
	
	mysql_close($dbh);
	
	
	$gotplayercardsarr["cards"] = $gotplayercards;
	$gotplayercardsarr["stackleft"] = $getBlindCards;
	$gotplayercardsarr["stackshown"] = $getShownCards;
	
	if (sizeof($gotplayercards)<1) {
		echo "";
	}
	else {
		echo json_encode(array("0"=>$gotplayercardsarr));  
	}
	
	
}


function getFinalScore() {
	
	
	
	//getFinalScore NEED gameid,nickname
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
		

	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	//json_encode(array("error"=>'I cannot connect to the database.'))
	mysql_select_db ("nicklupi_quidder");
	
	$result_getplayers_games = mysql_query("SELECT * FROM games WHERE id = '".$obj->{'gameid'}."'"); //
	
	while($row = mysql_fetch_array($result_getplayers_games)){
		$players = explode("|",$row['players']);
	}
	
	$result_existing_games = mysql_query("SELECT * FROM round WHERE gameid = '".$obj->{'gameid'}."'"); // 
	$totalPlayerScore= array(); 
	while($row = mysql_fetch_array($result_existing_games)){
		
		if ($row['playerscores']!="") {
		
		$decodeplayerscores = json_decode($row['playerscores'],true);
		
		
		for ($x=0; $x<sizeof($players); $x++) {
			
			$totalPlayerScore[$players[$x]] += $decodeplayerscores[$x][$players[$x]];
		}
	
			
		
		}
		
	}
	
	mysql_close($dbh);
	
	
	$playerscoretotal["score"] = $totalPlayerScore;
	
	echo json_encode(array("0"=>$totalPlayerScore));  
	
	
}

function viewPlayers() {
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM games WHERE id = '".trim($obj->{'gameid'})."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){
		
		$numofplayers = explode("|",$row['players']);
		if (sizeof($numofplayers)>1) {$s="s"; $areis="are";} else {$s=""; $areis="is";}
		
		$gamedesc = sizeof($numofplayers)." player".$s." ".$areis." ready to play.";
			
		$jsondata[] = array('id'=>$row['id'],'game'=>"Started by " . $numofplayers[0],'desc'=>$gamedesc,'players'=>$row['players'],'round'=>$row['round'],'started'=>$row['started']);
			
		}
	
	mysql_close($dbh);

	

echo json_encode(array('playersingame'=>$jsondata));  
	
}


function deleteGame() {
	
	
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	if (isset($_GET['gameid'])) { $obj->{'gameid'} = $_GET['gameid'];}
	
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	
	mysql_query("DELETE FROM games WHERE id = '".$obj->{'gameid'}."'")or die(mysql_error());
	
	 mysql_query("DELETE FROM round WHERE gameid = '".$obj->{'gameid'}."'") or die(mysql_error()); 

	mysql_close($dbh);
	
	
	
}


function joinGame() {
	
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	
	mysql_query("UPDATE games SET players=concat(players,'|".$obj->{'nickname'}."') WHERE id = '".$obj->{'gameid'}."'");
	
	$jsondata[]= array('id'=>mysql_insert_id(), 
		'name'=>$obj->{'gamename'}, 
		'userNickName'=>$obj->{'nickname'});
	
	echo json_encode(array('joingame'=>$jsondata));  
	

	mysql_close($dbh);
	
	
	
}

function createNewGame() {
	
	
	//table: games
	//cols: id,name,round,players,playersscore,playerscards,chat,status,datestarted,lastmove
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	 
	 //`id`, `name`, `round`, `players`, `chat`, `status`, `datestarted`, `lastdealer`, `started`SELECT * FROM `games` WHERE 1
	
	mysql_query("INSERT INTO games (name,round,players,datestarted)
	VALUES ('".$obj->{'gamename'}."', 0, '".$obj->{'nickname'}."','".date('U')."')") or die(mysql_error());  
	
	$jsondata[]= array('id'=>mysql_insert_id(), 
		'name'=>$obj->{'gamename'}, 
		'creator'=>$obj->{'nickname'},
		'players'=>$obj->{'nickname'},
		'round'=>"0",
		'datestarted'=>date('U'));
	
	echo json_encode(array('newgame'=>$jsondata));  


	mysql_close($dbh);
	
	
	
}

function resumeExistingGame() {
	
	
	//table: games
	//cols: id,name,round,players,playersscore,playerscards,chat,status,datestarted,lastmove
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);

	$jsondata[]= array('id'=>$obj->{'gameid'}, 
		'name'=>$obj->{'gamename'}, 
		'creator'=>$obj->{'nickname'},
		'players'=>$obj->{'nickname'},
		'round'=>"0",
		'datestarted'=>date('U'));
	
	echo json_encode(array('newgame'=>$jsondata));  


	
	
	
}

function playerLogin() {
	
	header('Content-type: application/json');
	
	//$json=$_GET ['json'];
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	//echo $json;
	
	//Save
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	   or die ('I cannot connect to the database.');
	 mysql_select_db ("nicklupi_quidder");
	
	
	$result_existing = mysql_query("SELECT * FROM players WHERE nickname = '".$obj->{'nickname'}."' AND active > '".$date2hoursbefore."'")
	or die(mysql_error()); 
	
	$num_active_players = mysql_num_rows($result_existing);
	$date2hoursbefore = intval(date('U')-(60*60*2));
	
	if ($num_active_players==1) {
		
		while($row = mysql_fetch_array($result_existing)){
			$winlossrec = $row['winlossrec'];
			$thisID = $row['id'];
		}
	
	if ($obj->{'email'}=="") {
		mysql_query("UPDATE players SET nickname = '".$obj->{'nickname'}."', phone = '', active = '".date('U')."' WHERE id = '".$thisID."'");
	}
	else {
		mysql_query("UPDATE players SET nickname = '".$obj->{'nickname'}."', phone = '', email = '".$obj->{'email'}."', active = '".date('U')."' WHERE id = '".$thisID."'");
	}
		
		
		
		$posts = array(4);
		echo json_encode(array('email'=>$obj->{'email'},'nickname'=>$obj->{'nickname'},'userid'=>$thisID,'winlossrec'=>$winlossrec,'activeplayers'=>$num_active_players,'playerstatus'=>'1'));
	
	}
	
	else {
		
		$winlossrec = '0-0';
		
		mysql_query("INSERT INTO players (nickname, phone, email, active, winlossrec)
	VALUES ('".$obj->{'nickname'}."', '', '".$obj->{'email'}."',".date('U').",'0-0')");
	
	$posts = array(4);
		echo json_encode(array('email'=>$obj->{'email'},'nickname'=>$obj->{'nickname'},'winlossrec'=>'0-0','activeplayers'=>$num_active_players,'playerstatus'=>'0'));
	
	}
	
		mysql_close($dbh);
	
	  //$posts = array($json);
		
		
}








function playerPlayWords() {
	
	header('Content-type: application/json');
	$json = file_get_contents('php://input');
	$obj = json_decode($json);
	include('testplayerfunctions.php');

	playerPlayWordsGOOD($obj->{'gameid'},$obj->{'round'},$obj->{'nickname'},$obj->{'cardssenttoplayer'},$obj->{'words'},$obj->{'shownCards'},$obj->{'blindCards'},$obj->{'usedExtra'},0);
	
	//array('nickname'=>$nickname,'goodwords'=>$showNumberOfCorrectWords,'points'=>$totalscore,'gameid'=>intval($gameid),'round'=>intval($round),'error'=>$error)

		
}



function time_elapsed_string($original) {
    // array of time period chunks
    $chunks = array(
    array(60 * 60 * 24 * 365 , 'year'),
    array(60 * 60 * 24 * 30 , 'month'),
    array(60 * 60 * 24 * 7, 'week'),
    array(60 * 60 * 24 , 'day'),
    array(60 * 60 , 'hour'),
    array(60 , 'min'),
    array(1 , 'sec'),
    );
 
    $today = time(); /* Current unix time  */
    $since = $today - $original;
 
    // $j saves performing the count function each time around the loop
    for ($i = 0, $j = count($chunks); $i < $j; $i++) {
 
    $seconds = $chunks[$i][0];
    $name = $chunks[$i][1];
 
    // finding the biggest chunk (if the chunk fits, break)
    if (($count = floor($since / $seconds)) != 0) {
        break;
    }
    }
 
    $print = ($count == 1) ? '1 '.$name : "$count {$name}s";
 
    if ($i + 1 < $j) {
    // now getting the second item
    $seconds2 = $chunks[$i + 1][0];
    $name2 = $chunks[$i + 1][1];
 
    // add second item if its greater than 0
    if (($count2 = floor(($since - ($seconds * $count)) / $seconds2)) != 0) {
        $print .= ($count2 == 1) ? ', 1 '.$name2 : " $count2 {$name2}s";
    }
    }
    return $print;

}

?>