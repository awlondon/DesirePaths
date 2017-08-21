 <?php
if($_SERVER['REQUEST_METHOD']=='POST'){

 include 'DatabaseConfig.php';

 $con = mysqli_connect($HostName,$HostUser,$HostPass,$DatabaseName);

 $S_url = $_POST['url'];
 $S_title = $_POST['title'];
 $S_snippet = $_POST['snippet'];
 $S_sentiment = $_POST['sentiment'];
 $S_latitude = $_POST['latitude'];
 $S_longitude = $_POST['longitude'];
 $S_user = $_POST['user'];
 $S_timestamp = $_POST['timestamp'];

 $Sql_Query = "INSERT INTO PIEntry_table (url,title,snippet,sentiment,latitude,longitude,user,timestamp)"
                . "values ('$S_url','$S_title','$S_snippet','$S_sentiment','$S_latitude','$S_longitude','$S_user','$S_timestamp')";

 if(mysqli_query($con,$Sql_Query))
{
 echo 'Public Input entered successfully';
}
else
{
 echo 'Something went wrong';
 }
 }
 mysqli_close($con);
?>