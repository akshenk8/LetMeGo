/*
		$qry="select * from `$table` where `ride`='1' and `firebase`='$firebase'";
		$qry=$cxn->query($qry);
		if($qry->num_rows != 1){		
			die();
		}		
		$result = $qry->fetch_assoc();
		echo '<script type="text/javascript">';		
		echo 'var result='.json_encode($result).";";

		echo 'var route = google.maps.geometry.encoding.decodePath(result.route);';
		echo 'var poly = new google.maps.Polyline({ path: route });';

		echo 'var arr=poly.getPath().getArray();';
		echo 'var loc=arr.length;';
		echo 'for(var i=0;i<arr.length;i++){';		
			echo 'arr[i]=new google.maps.LatLng(Number(arr[i].lat()),
	                                Number(arr[i].lng()));';
			echo 'if(i!=0 && loc>=i){';					
				echo 'var l=[arr[i-1],arr[i]];';
				echo 'var p = new google.maps.Polyline({ path: l });';
				echo 'var inY=google.maps.geometry.poly.isLocationOnEdge(src, p,  0.001);';			
				echo 'if(inY){';
					echo 'loc=i;';
				echo '}';	
			echo '}';
		echo '}';
		
		echo 'var newArray=arr.slice(loc);';
		echo 'var newPoly = new google.maps.Polyline({ path: newArray });';
		echo 'var enc=google.maps.geometry.encoding.encodePath(newPoly.getPath());';
		//echo 'document.write(enc);';

		echo '$.post("updateRide.php",{type: "$type",firebase: "$firebase",type: enc},function(data){});';

		echo '</script>';
	}*/