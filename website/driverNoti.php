<?php
if(isset($_POST['driver']) &&
    isset($_POST['amb']) &&
    isset($_POST['type'])){    

    require_once __DIR__ . '/firebase.php';

    $firebase = new Firebase();    

    include 'db.php';
    extract($_POST);

    $qry="select `currentLat`,`currentLng` from `amb` where `firebase`='$amb'";
    $qry=$cxn->query($qry);
    $qry=$qry->fetch_assoc();

    $payload = array();
    $payload['lat'] = $qry['currentLat'];
    $payload['lng'] = $qry['currentLng'];
    $payload['amb']=$amb;
    $payload['type']=$type;

    // notification title
    $title = "Alert.";

    // notification message
    $message = "Ambulance NearBy.";        

    $res = array();
    $res['data']['title'] = $title;    
    $res['data']['message'] = $message;
    $res['data']['payload'] = $payload;        
    
    $response = $firebase->sendDriver($driver, $res);    

    //echo json_encode($res);
}
?>