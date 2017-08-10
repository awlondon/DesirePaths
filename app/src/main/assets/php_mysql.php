<?php
   $con=mysqli_connect("unsucked-parts.000webhostapp.com","id2380250_alexlondon","Anchorage_0616");
   $sql="CREATE DATABASE my_db"
   if (mysqli_query($con,$sql)){
       echo "Database my_db has been created successfully";
    }
 ?>

 <?php
    $con=mysqli_connect("unsucked-parts.000webhostapp.com","id2380250_alexlondon","Anchorage_0616");
    $sql="CREATE TABLE public_input(url CHAR(200), title CHAR(100), snippet CHAR(500), sentiment CHAR(30) latitude DOUBLE, longitude DOUBLE"
    if(mysqli_query($con,$sql)){
        echo "Table has been created successfully."
    }
 ?>

 <?php
    $con=mysqli_connect("unsucked-parts.000webhostapp.com","id2380250_alexlondon","Anchorage_0616");
    $sql="INSERT INTO public_input (url, title, snippet, sentiment, latitude, longitude) VALUES ('http://akonthego.com/blog/wp-content/uploads/2015/08/4th-G-Street-looking-east.jpg', 'Beginning of project','Not so great', 'Bad', 61.3,-149.5)";
    if (mysqli_query($con,$sql)) {
       echo "Values have been inserted successfully";
    }
 ?>

 <?php
    $con=mysqli_connect("unsucked-parts.000webhostapp.com","id2380250_alexlondon","Anchorage_0616");

    if(mysqli_connect_errno($con)){
        echo "Failed to connect to MySql: " . mysqli_connect_error();
    }

    $title = $_GET['title'];
    $snippet = $_GET['snippet'];
    $result = mysqli_query($con, "SELECT Role FROM public_input where title='$title' and snippet='$snippet'");
    $row = mysqli_fetch_array($result);
    $data = $row[0];

    if($data){
        echo $data;
    }

   mysqli_close($con);
   ?>
