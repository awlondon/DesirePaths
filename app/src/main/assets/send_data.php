<?php

$databasehost = "153.92.6.4";
$databasename = "u665689347_dp";
$databaseusername ="u665689347_guest";
$databasepassword = "guest_123@USA";

//$json=$_GET ['json'];
$json = file_get_contents('php://input');
$obj = json_decode($json);
//echo $json;

//Save
$con = mysql_connect($databasehost,@databaseusername,$databasepassword)
       or die('Cannot connect to the DB');
mysql_select_db($databasename,$con);
  /* grab the posts from the db */
  //$query = "SELECT post_title, guid FROM wp_posts WHERE
  //  post_author = $user_id AND post_status = 'publish'
  // ORDER BY ID DESC LIMIT $number_of_posts";
mysql_query("INSERT INTO `" . $databasename . "`.`users` (UserName, FullName)
VALUES ('".$obj->{'UserName'}."', '".$obj->{'FullName'}."')");
mysql_close($con);
//
  //$posts = array($json);
  $posts = array(1);
    header('Content-type: application/json');
    echo json_encode(array('posts'=>$posts));
  ?>