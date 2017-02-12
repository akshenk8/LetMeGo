<?php
	error_reporting(E_ALL);
	if(!(isset($_POST['type']) && 
		 isset($_POST['firebase']))){
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
				
		$qry="UPDATE `$table` set `route`='',`ride`='0',`currentLat`='',`currentLng`='' where `firebase` like '$firebase'";
		//echo $qry;
		$qry=$cxn->query($qry);
		//echo $cxn->error;
	}
?>