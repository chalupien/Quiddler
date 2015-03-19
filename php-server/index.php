<?php

header('Content-type: application/json');

switch ($_GET['action']) {
	case "startRound": startRound(); break;
	
}

/*
120 Cards
Game begins, players known... 1st player is the one who started the game, 
Begins round with 3 cards per player
Onscreen to see whos turn it is.
On player turn they can see the card that is avail to choose or select a new card (from stack)
if player puts down words, everyone else must do so.
Next round procedes with shuffle.

creategame (loadplayers)

game play (shuffle,deal) 

***game table
id
round (id from roundtable)
players (comma seperated id) (p1-p8)
playersscore (comma seperated)

***player table
id
Name
Email
Phone #
icon/image? (for player icon)
currentround (id from roundtable)
winlossrec

***round table
id
roundno
game (id from game table)
cardstack (comma seperated) letter-points ex. e-2 or z-14
turnover
p1 (words played comma(,) seperated or cards dealt)
p2
p3
p4
p5
p6
p7
p8


*/
	



function startRound() {
	
	header('Content-type: application/json');
	
	$json = file_get_contents('php://input');
	$obj = json_decode($json);

	$gameID = $obj->{'gameid'};
	$roundNo = $obj->{'roundno'};
	
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
	or die ('I cannot connect to the database.');
	mysql_select_db ("nicklupi_quidder");
	
	$result_existing_games = mysql_query("SELECT * FROM games WHERE id = '".trim($gameID)."'"); //  
	
	while($row = mysql_fetch_array($result_existing_games)){
		$playersInGame = $row['players'];	
	}
	
	mysql_close($dbh);
	

	
	$playersInGame = explode("|",$playersInGame);
	$numOfPlayersInGame = sizeof($playersInGame);
	
	
	
	extract(dealcards($roundNo+3,$numOfPlayersInGame)); //roundno,numofplayers
	
	for($x=0;$x<=$numOfPlayersInGame;$x++) {
	$playercards[] = $p[$x];
	}
	
	
	$x=0;
	foreach($playercards as $playercardsX ){
		$playercardsX = explode('|', $playercardsX, -1);
		if (count($playercardsX)>0) {
			for ($c=0;$c<sizeof($playercardsX);$c++) {
				$rr = explode("-",$playercardsX[$c]);
				$playerCards[$x] .= $rr[0].",";
			}
			$player[$x] = rtrim($playerCards[$x],",");
			$jsondata[] = array($playersInGame[$x]=>$player[$x]);
			$playercardsdata[] = array($playersInGame[$x]=>$player[$x]);
			$x++;
		}
	}
	
	
	// echo "LEFT IN DECK: <br>";
	$deckleft = explode('|', $deckleft, -1);

	for ($c=0;$c<sizeof($deckleft);$c++) {
		$rr = explode("-",$deckleft[$c]);
		$cardsLeftFromDeal .= $rr[0].",";
	}
	$cardsLeftFromDeal = rtrim($cardsLeftFromDeal,",");
	$jsondata[]= array('left'=>$cardsLeftFromDeal);
	
	// echo "DRAWN: <br>";
	$outt = explode('|', $outt, -1);
	for ($c=0;$c<sizeof($outt);$c++) {
		$rr = explode("-",$outt[$c]);
		$cardsDealt .= $rr[0].",";
	} 
	$cardsDealt = rtrim($cardsDealt,",");
	$jsondata[]= array('dealt'=>$cardsDealt);
	
	// echo "SHOWN STACK: <br>";
	$showStack = explode('|', $showStack, -1);
	for ($c=0;$c<sizeof($showStack);$c++) {
		$rr = explode("-",$showStack[$c]);
		$shownStack .= $rr[0].",";
	} 
	$shownStack = rtrim($shownStack,",");
	$jsondata[]= array('shownstack'=>$shownStack);
	
	
	//DB Work
	$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") or die ('I cannot connect to the database.');
	mysql_select_db ("nicklupi_quidder");
	
	mysql_query("INSERT INTO round (gameid, roundno, playercards, stackleft, stackshown) VALUES ('".$gameID."', '".$roundNo."', '".json_encode($playercardsdata)."', '".$cardsLeftFromDeal."', '".$shownStack."')");
	
	mysql_query("UPDATE games SET round = '".$roundNo."', started = '1' WHERE id = '".$gameID."'");
	
	mysql_close($dbh);

	
	echo json_encode($jsondata);
	

	
	
}



/*

$dbh=mysql_connect ("localhost", "nicklupi", "58@Y6L*Rga") 
or die ('I cannot connect to the database.');
mysql_select_db ("nicklupi_quidder");

$result = mysql_query("SELECT * FROM games")
or die(mysql_error());  


while($row = mysql_fetch_array($result)){
$row[id];
}

mysql_query("UPDATE Persons SET Age = '36'
WHERE FirstName = 'Peter' AND LastName = 'Griffin'");

mysql_query("INSERT INTO Persons (FirstName, LastName, Age)
VALUES ('Glenn', 'Quagmire', '33')");

mysql_close($dbh);

*/







function shufflecards($decks) {
	
	/*
	letters a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,cl,er,in,qu,th
	points
	number  
	*/

	$cards = array ("x", "h", "f", "m", "v", "in", "er", "qu", "q", "th", "cl", "x","w", "j", "k", "p", "b", "c", "z", "l", "g", "d", "y", "s", "r", "t", "n", "u", "i", "o", "a", "e");
	$numofcards = array(2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,4,4,4,4,6,6,6,6,8,8,10,12);
	$pointvalue = array(7,7,6,5,11,7,7,9,5,9,10,12,10,13,8,6,8,8,14,3,6,5,4,3,5,3,5,4,2,2,2,2);


	for ($t=0;$t<=$decks;$t++) {

		for ($x=0;$x<sizeof($cards);$x++) {
			for ($y=0;$y<$numofcards[$x];$y++) {
				$deck .= $cards[$x] . "-" . $pointvalue[$x] . "|||";
			}
		}
	}
	
	$deckofcards = explode("|||",$deck);
	$b=0;
	$numbers = range(0, $decks*119); //119
	shuffle($numbers);

	foreach ($numbers as $number) {
		$stack[$b] = $number; $b++;
	}

	for ($c=0;$c<sizeof($stack);$c++)
	{
		$deckshuffled[$c] = $deckofcards[$stack[$c]];
	}
	

	return $deckshuffled; /// letter-points ex. e-2 or z-14

}



function dealcards($roundno,$numplayers) { //roundno starts at 3
	
	
	$shuffleddeck = shufflecards(intval(ceil($numplayers/4))); // 4 players per deck
	$deck = $shuffleddeck;
	
	if ($roundno<3) { $roundno = 3; }

	for ($x=0;$x<$roundno;$x++) {
		for($y=0;$y<$numplayers;$y++) {
			$taken = array_pop($shuffleddeck);
			$p[$y] .= $taken . "|";
			$out .= $taken . "|";
		}	
	}

	$cardsOut = explode("|",$out);
	for ($z=0;$z<sizeof($cardsOut)-1;$z++) {
		$takeCardOut = array_pop($deck);
		$outt .=  $cardsOut[$z]. "|";
	}
	
	for ($zz=0;$zz<sizeof($deck)-1;$zz++) {
		$deckleft .= $deck[$zz] . "|";
	}
	
	$leftInDeck = rtrim($deckleft,"|");
	$leftInDeck = explode("|",$deckleft);
	$showStack = array_shift($leftInDeck) . "|";

	return compact('p','deckleft','outt','showStack');
}



?>