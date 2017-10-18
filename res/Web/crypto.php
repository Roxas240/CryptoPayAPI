<?php
//Config for CryptoPayAPI
$port = 3306;
$table = "payments";
$var_table = "count_vars";
$hostname = "localhost";
$database = "xempaswq_crypto";
$enc_key  = ""; //Insert your encryption key
//End Config

$enc_user = $_POST['user'];
$enc_pass = $_POST['pass'];

//get parameter from INPUT
$user    =        trim(filter_var(xmp_decrypt($enc_user, $enc_key), FILTER_SANITIZE_STRING)) ?? NULL;
$pass    =        trim(filter_var(xmp_decrypt($enc_pass, $enc_key), FILTER_SANITIZE_STRING)) ?? NULL;

$method  =        trim(filter_input(INPUT_POST, "method",   FILTER_SANITIZE_STRING)) ?? NULL;
$id      =   (int)trim(filter_input(INPUT_POST, "id",       FILTER_SANITIZE_NUMBER_INT)) ?? 0;
$confirm = (float)trim(filter_input(INPUT_POST, "confirms", FILTER_SANITIZE_NUMBER_FLOAT, FILTER_FLAG_ALLOW_FRACTION)) ?? 0;
$pin     =        trim(filter_input(INPUT_POST, "pin",      FILTER_SANITIZE_STRING)) ?? NULL;
$txid    =        trim(filter_input(INPUT_POST, "txid",     FILTER_SANITIZE_STRING)) ?? NULL;
$amount  =        trim(filter_input(INPUT_POST, "amount",   FILTER_SANITIZE_STRING)) ?? NULL;
$notes   =        trim(filter_input(INPUT_POST, "notes",    FILTER_SANITIZE_STRING)) ?? NULL;
$varname =        trim(filter_input(INPUT_POST, "varname",  FILTER_SANITIZE_STRING)) ?? NULL;
$value   =        trim(filter_input(INPUT_POST, "value",    FILTER_SANITIZE_STRING)) ?? NULL;

//encryption handling
function xmp_encrypt($input, $key) {
   $size = mcrypt_get_block_size(MCRYPT_RIJNDAEL_128, MCRYPT_MODE_ECB);
   $input = pkcs5_pad($input, $size);
   $td = mcrypt_module_open(MCRYPT_RIJNDAEL_128, '', MCRYPT_MODE_ECB, '');
   $iv = mcrypt_create_iv (mcrypt_enc_get_iv_size($td), MCRYPT_RAND);
   mcrypt_generic_init($td, $key, $iv);
   $data = mcrypt_generic($td, $input);
   mcrypt_generic_deinit($td);
   mcrypt_module_close($td);
   $data = base64_encode($data);
   return $data;
}

function pkcs5_pad ($text, $blocksize) {
   $pad = $blocksize - (strlen($text) % $blocksize);
   return $text . str_repeat(chr($pad), $pad);
}

function xmp_decrypt($sStr, $sKey) {
   $decrypted= mcrypt_decrypt(
      MCRYPT_RIJNDAEL_128,
      $sKey,
      base64_decode($sStr),
      MCRYPT_MODE_ECB
   );
   $dec_s = strlen($decrypted);
   $padding = ord($decrypted[$dec_s-1]);
   $decrypted = substr($decrypted, 0, -$padding);
   return $decrypted;
}

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
            $query = "SELECT id, amount, filled, notes FROM $table WHERE (code='-1' OR filled='-1')";
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
	case "confirms":
            $query = "UPDATE $table SET confirms='$confirm' WHERE id='$id'";
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
            $query = "SELECT filled, confirms FROM $table WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			$array = $result->fetch_assoc();
            echo "Filled:".$array['filled'].":Confirms:".$array['confirms'];
        break;
	case "cancel":
            $query = "UPDATE $table SET filled='-1' WHERE id='$id'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			echo "ID:$id";
        break;

	case "postvar":
			$query = "UPDATE $var_table SET value='$value' WHERE name='$varname'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			echo "Done!";
        break;
	case "grabvar":
			$query = "SELECT `value` FROM $var_table WHERE name='$varname'";
            $result = mysqli_query($linkDB, $query) or die("Error: ".mysqli_error($linkDB));
			$array = $result->fetch_assoc();
			echo "Value:".$array['value'];
        break;
    default:
        echo "Error:-1";
        break;
}

logOutMysql($linkDB);
exit();
?>