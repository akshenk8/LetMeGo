<!DOCTYPE html>
<html>
<head>	
	<script type="text/javascript"
	  src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDy0e3RJARTmxTytLFrq2Gy3TCFd0a2OuY&libraries=geometry">
	</script>
	<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<?php
	if(!(isset($_POST['driver']) &&
    isset($_POST['amb']) )){
		$arr['msg']="error";
		echo json_encode($arr);
    }
    else{
		include 'db.php';	
		extract($_POST);	

		echo '<script type="text/javascript">';		
		echo 'function repeat(){';

			$table='amb';
			$amb=$cxn->real_escape_string($amb);
			$qry="select * from `$table` where `firebase`='$amb' and `ride`='1'";
			$qry=$cxn->query($qry);							
			if($qry->num_rows != 1){
				$arr['msg']="error";
				echo json_encode($arr);
				die();
			}
			$result = $qry->fetch_assoc();
			//amb array 
			echo 'var amb='.json_encode($result).";";
			//echo  'document.write("<br>"+amb+"<br>");';

			$table='drivers';
			$driver=$cxn->real_escape_string($driver);
			$qry="select * from `$table` where `currentLat` not like '' and `currentLng` not like '' and `ride`='1' and `firebase`='$driver'";
			$qry=$cxn->query($qry);
			if($qry->num_rows != 1){
				$arr['msg']="error";
				echo json_encode($arr);
				die();
			}		
			$result = $qry->fetch_assoc();
			//users arr
			echo 'var result='.json_encode($result).";";
			//echo  'document.write("<br>"+result+"<br>");';
			//user location
			echo 'var latLng = new google.maps.LatLng(result.currentLat,result.currentLng);';

			//amb location
			echo 'var src = new google.maps.LatLng(amb.currentLat,amb.currentLng);';

			echo 'var route = google.maps.geometry.encoding.decodePath(amb.route);';
			echo 'var poly = new google.maps.Polyline({ path: route });';
			//echo  'document.write("<br>"+poly+"<br>");';
			
			echo 'var arr=poly.getPath().getArray();';
			//echo  'document.write("<br>"+arr+"<br>");';
			//echo  'document.write(":"+arr.length);';

			echo 'var loc=-1;';
			echo 'for(var i=0;i<arr.length;i++){';
				//echo  'document.write("<br>i:"+i);';		
				echo 'arr[i]=new google.maps.LatLng(Number(arr[i].lat()),
                                        Number(arr[i].lng()));';
				echo 'if(i!=0 && loc==-1){';					
					echo 'var l=[arr[i-1],arr[i]];';
					echo 'var p = new google.maps.Polyline({ path: l });';
					echo 'var inY=google.maps.geometry.poly.isLocationOnEdge(src, p,  0.001);';
					//echo  'document.write("loc:"+loc+":"+inY);';
					echo 'if(inY){';
						echo 'loc=i;';
					echo '}';	
				echo '}';
			echo '}';

			echo 'if(loc==-1){';
			echo 'loc=0;}';

			//echo  'document.write("loc:"+loc);';
			echo 'var newArray=arr.slice(loc);';
			//echo  'document.write("lololol<br><br><br>"+newArray+"<br>");';
			echo 'var newPoly = new google.maps.Polyline({ path: newArray });';
			echo 'var enc=google.maps.geometry.encoding.encodePath(newPoly.getPath());';
			echo 'document.write(enc);';


		echo '}';
		echo 'repeat();';		
		echo '</script>';
	}
?>
</head>
</html>