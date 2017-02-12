<?php

$dbname="dbname";
$cxn = new mysqli("localhost","username","pass",$dbname);

if($cxn->connect_error)
{
	echo "Connection Error";
}

define ('SITE_ROOT', realpath(dirname(__FILE__)));
?>
