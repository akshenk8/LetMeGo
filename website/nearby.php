<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8" http-equiv="refresh" content="3">
	<script type="text/javascript"
	  src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDy0e3RJARTmxTytLFrq2Gy3TCFd0a2OuY&libraries=geometry">
	</script>
	<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<?php
	include 'db.php';		

	echo '<script type="text/javascript">';		
	echo 'function repeat(){';

		$table='amb';
		$qry="select `firebase`,`currentLat`,`currentLng` from `$table` where `ride`='1'";
		$qry=$cxn->query($qry);	
		$rowsA = $qry->num_rows;

		$result = array();
		while($row=$qry->fetch_assoc()){
			$result[]=$row;
		}

		//amb array 
		echo 'var amb='.json_encode($result).";";

		$table='drivers';
		$qry="select `firebase`,`currentLat`,`currentLng` from `$table` where `currentLat` not like '' and `currentLng` not like ''";
		$qry=$cxn->query($qry);
		$rows = $qry->num_rows;

		$result = array();
		while($row=$qry->fetch_assoc()){
			$result[]=$row;
		}

		//users arr
		echo 'var result='.json_encode($result).";";

		//outer loop
		echo 'var j=0;';
		echo "for(;j<". $rowsA .";j++){";
			
			//amb location
			echo 'var src = new google.maps.LatLng(amb[j].currentLat,amb[j].currentLng);';

			echo 'var i=0;';
			echo "for(;i<". $rows .";i++){";
				//user location
				echo 'var latLng = new google.maps.LatLng(result[i].currentLat,result[i].currentLng);';			

				echo 'var srcIn=google.maps.geometry.spherical.computeDistanceBetween(src, latLng);';				

				//echo 'document.write(srcIn);';
				echo 'if (srcIn<=750.0) {
						document.write("otput");
			   			$.post("driverNoti.php",{driver: result[i].firebase,amb: amb[j].firebase,type: "add"},function(data){});
					}else{
						$.post("driverNoti.php",{driver: result[i].firebase,amb: amb[j].firebase,type: "remove"},function(data){});
					}';
			echo '}';

		echo '}';

	echo '}';
	echo 'repeat();';
	//echo 'setInterval(repeat,5000);';
	echo '</script>';	
?>
</head>
</html>