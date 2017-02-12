<?php	
	if(!(isset($_POST['type']) && 
		 isset($_POST['firebase']) )){
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

		if(isset($oldF)){
			$oldF=$cxn->real_escape_string($oldF);
			$qry="UPDATE `$table` set `firebase`='$firebase' where `firebase`='$oldF'";
		}
		else{
			$qry="INSERT INTO `$table`(`firebase`,`route`,`currentLat`,`currentLng`,`ride`) values ('$firebase','','','',0)";
		}
		$qry=$cxn->query($qry);

		if($qry){
			$arr['msg']="success";
			echo json_encode($arr);
		}
		else{
			$arr['msg']="error";
			echo json_encode($arr);
		}

	}
?>