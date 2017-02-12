
<?php
	error_reporting(E_ALL);
	if(!(isset($_POST['type']) && 
		 isset($_POST['firebase']) &&
		 isset($_POST['currentLat']) &&
		 isset($_POST['currentLng']) )){
		$arr['msg']="error";
		echo json_encode($arr);
	}
	else{
		include 'db.php';
		extract($_POST);

		if($type=='driver'){
			$table='drivers';
		}
		else if($type='amb'){
			$table='amb';
		}
		else{
			$arr['msg']="error";
			echo json_encode($arr);
			die();
		}

		$firebase=$cxn->real_escape_string($firebase);
				
		$qry="UPDATE `$table` set `currentLat`='$currentLat',`currentLng`='$currentLng' where `firebase` like '$firebase'";
		//echo $qry;
		$qry=$cxn->query($qry);
		//echo $cxn->error;
	}
?>
