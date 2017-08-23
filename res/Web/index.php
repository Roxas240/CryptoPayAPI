<?php
//Config for DB
$port = 3306;
$table = "payments";
$hostname = "localhost";
$database = "crypto_payments";
//End Config


//get parameter from INPUT
$user   =      trim(filter_input(INPUT_POST, "user",    FILTER_SANITIZE_STRING)) ?? NULL;
$pass   =      trim(filter_input(INPUT_POST, "pass",    FILTER_SANITIZE_STRING)) ?? NULL;
$method =      trim(filter_input(INPUT_POST, "method",  FILTER_SANITIZE_STRING)) ?? NULL;
$id     = (int)trim(filter_input(INPUT_POST, "id",      FILTER_SANITIZE_NUMBER_INT)) ?? 0;
$pin    =      trim(filter_input(INPUT_POST, "pin",     FILTER_SANITIZE_STRING)) ?? NULL;
$txid   =      trim(filter_input(INPUT_POST, "txid",    FILTER_SANITIZE_STRING)) ?? NULL;
$amount =      trim(filter_input(INPUT_POST, "amount",  FILTER_SANITIZE_STRING)) ?? NULL;
$notes  =      trim(filter_input(INPUT_POST, "notes",   FILTER_SANITIZE_STRING)) ?? NULL;

//function create conection DB
function loginMysql($userName = \NULL, $passWord = \NULL) {
	global $hostname, $port, $database;
    $con = mysqli_connect($hostname, $userName, $passWord, $database, $port);
    if(!$con)
    {
        die("Connection failure: ".mysqli_connect_error() . PHP_EOL);
    }
    return $con;
}

//function close conection DB
function logOutMysql($var = NULL) {
    mysqli_close($var);
}

//create object to database
$linkDB = loginMysql($user,$pass);

// switching by method var
switch ($method) {
    case "requests":
            $query = "SELECT id, amount, filled FROM $table WHERE (code='-1' OR filled='-1')";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
            for ($set = Array(); $row = $result->fetch_assoc(); $set[] = $row);
            echo json_encode($set);
        break;
    case "setpin":
            $query = "UPDATE $table SET code='$pin' WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
            echo "Done: $pin and ID: $id";
        break;
    case "setpaid":
            $query = "UPDATE $table SET txid='$txid', filled='1' WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
            echo "Done!";
        break;
	case "remove":
            $query = "DELETE FROM $table WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
            echo "Done!";
        break;


	case "addrequ":
            $query = "INSERT INTO $table (amount, code, notes, filled, txid) VALUES ('$amount', '-1', '$notes', '0', '')";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			$newID = mysqli_insert_id($linkDB);
            echo "ID:$newID";
        break;
	case "chkpin":
            $query = "SELECT code FROM $table WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			$array = $result->fetch_assoc();
            echo "Code:".$array['code'].":$id";
        break;
	case "chkpay":
            $query = "SELECT filled FROM $table WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			$array = $result->fetch_assoc();
            echo "Filled:".$array['filled'];
        break;
	case "cancel":
            $query = "UPDATE $table SET filled='-1' WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			echo "ID:$id";
        break;
    default:
        echo "Error:-1";
        break;
}

logOutMysql($linkDB);
exit();
?>