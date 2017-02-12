<?php
	include 'db.php';
	$qry= "Select * from `drivers`";
	$qry=$cxn->query($qry);
	while ($row=$qry->fetch_assoc()) {
		echo json_encode($row);
		echo '<br>';
	}
	echo '<br>';echo '<br>';echo '<br>';
	echo "amb";
	echo '<br>';
	$qry= "Select * from `amb`";
	$qry=$cxn->query($qry);
	while ($row=$qry->fetch_assoc()) {
		echo json_encode($row);
		echo '<br>';
	}
?>