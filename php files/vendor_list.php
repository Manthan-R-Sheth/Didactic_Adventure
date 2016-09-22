<?php
include("db_settings.php");
$con = mysqli_connect($servername, $username, $password, $dbname);
$sql = "SELECT * FROM vendorInfo";
$r = mysqli_query($con, $sql);
$result = array();
while($res = mysqli_fetch_array($r)) {
	array_push($result, array("sno"=> $res['sno'], "name"=> $res['name'], "lat" => $res['lat'], "long" => $res['long']));
}
echo json_encode(array("result" => $result));

?>	