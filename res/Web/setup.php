<?php
//Config Vars
$hostname = "localhost";
$port = 3306;
//End Config Vars

session_start();
$stag = $_POST['stage'];

if(isset($_SESSION['user']) and isset($_SESSION['pass'])){
	$user = $_SESSION['user'];
	$pass = $_SESSION['pass'];

	if($stag < 1) $stag = 1;
} else {
	$user = trim(filter_input(INPUT_POST, "user", FILTER_SANITIZE_STRING)) ?? NULL;
    $pass = trim(filter_input(INPUT_POST, "pass", FILTER_SANITIZE_STRING)) ?? NULL;

	$_SESSION['user'] = $user;
	$_SESSION['pass'] = $pass;
}

$error = "";
$success = "";

$fail = (str_equal($user, "") or str_equal($pass, ""));
if(!$fail){
	if($stag == 2){
		$dbname = trim(filter_input(INPUT_POST, "dbname", FILTER_SANITIZE_STRING)) ?? NULL;
		$dbuser = trim(filter_input(INPUT_POST, "dbuser", FILTER_SANITIZE_STRING)) ?? NULL;
		$dbpass = trim(filter_input(INPUT_POST, "dbpass", FILTER_SANITIZE_STRING)) ?? NULL;
		$dbconf = trim(filter_input(INPUT_POST, "dbconf", FILTER_SANITIZE_STRING)) ?? NULL;

		if(strlen($dbname) < 2){
			$stag = 1;
			$error .= "DB name is to short. <br>";
		}
		if(strlen($dbuser) < 2){
			$stag = 1;
			$error .= "DB user name is to short. <br>";
		}
		if(strlen($dbpass) < 2){
			$stag = 1;
			$error .= "DB user pass is to short. <br>";
		}
		if(!str_equal($dbpass, $dbconf)){
			$stag = 1;
			$error .= "Passwords do not match. <br>";
		}

		if($stag == 2){
			$conn = loginMysqlStale($user, $pass);
			$query = "CREATE DATABASE $dbname";

			$res = mysqli_query($conn, $query);
			if(!$res){
				$error .= $conn->error;
				$fail = true;
			} else {
				$success .= "Database '$dbname' created successfully! <br>";

				$query = "GRANT ALL PRIVILEGES ON $dbname.* To '$dbuser'@'localhost' IDENTIFIED BY '$dbpass';";
				$res = mysqli_query($conn, $query);
			}

			if(!$res){
				$error .= $conn->error;
				$fail = true;
			} else {
				$success .= "User '$dbuser' created successfully! <br>";
				$success .= "User '$dbuser' given permissions successfully! <br>";

				$_SESSION['dbname'] = $dbname;
				$_SESSION['dbuser'] = $dbuser;
				$_SESSION['dbpass'] = $dbpass;
			}
			logOutMysql($conn);
		}
	} elseif($stag == 3){
		$tablename = trim(filter_input(INPUT_POST, "table", FILTER_SANITIZE_STRING)) ?? NULL;
		if(strlen($tablename) < 2){
			$stag = 2;
			$error .= "Table name is to short. <br>";
		}

		if($stag == 3){
			$conn = loginMysql($user, $pass, $_SESSION['dbname']);
			$query = "CREATE TABLE `$tablename` (
						`id` int(11) NOT NULL,
						`txid` text NOT NULL,
						`amount` tinytext NOT NULL,
						`code` varchar(6) NOT NULL,
						`notes` tinytext NOT NULL,
						`filled` int(11) NOT NULL DEFAULT 0
						`confirms` float NOT NULL DEFAULT 0
					) ENGINE=InnoDB DEFAULT CHARSET=utf8;";

			$res = mysqli_query($conn, $query);
			if(!$res){
				$error .= $conn->error;
				$fail = true;
			} else {
				$success .= "Table '$tablename' created successfully! <br>";
				$_SESSION['table'] = $tablename;
			}
		}
	}
}

function loginMysql($userName = \NULL, $passWord = \NULL, $database = \NULL) {
	global $hostname, $port;
    $con = mysqli_connect($hostname, $userName, $passWord, $database, $port);
    if(!$con)
    {
        die("Connection failure: ".mysqli_connect_error() . PHP_EOL);
    }
    return $con;
}

function loginMysqlStale($userName = \NULL, $passWord = \NULL) {
	global $hostname, $port;
    $con = mysqli_connect($hostname, $userName, $passWord, "", $port);
    if(!$con)
    {
        die("Connection failure: ".mysqli_connect_error() . PHP_EOL);
    }
    return $con;
}

function logOutMysql($var = NULL) {
    mysqli_close($var);
}

function str_equal($str1, $str2){
   if(strcmp($str1, $str2) == 0){
      return true;
   } else {
      return false;
   }
}
?>
<!DOCTYPE html>
<html>
<body>
  <h1>Setup Database and Table</h1>
  <?php
  echo '<p style="color: #A22;">'.$error.'</p>';
  echo '<p style="color: #2A2;">'.$success.'</p>';

  if($fail){
	echo '
  <form action="setup.php" method="post">
  <table>
    <tr>
      <td colspan="2"><h3>Root Sign In</h3></td>
    </tr>
    <tr>
      <td>MySQL User: </td>
      <td><input type="text" id="user" name="user" value="root" /></td>
    </tr>
    <tr>
      <td>MySQL Pass: </td>
      <td><input type="password" id="pass" name="pass"/></td>
    </tr>
    <tr>
	  <input type="hidden" id="stage" name="stage" value="1" />
      <td colspan="2"><button type="submit">Submit</button></td>
    </tr>
  </table>
  </form>';
  } elseif($stag == 1) {
	echo '
  <form action="setup.php" method="post">
  <table>
    <tr>
      <td colspan="2"><h3>Database Setup</h3></td>
    </tr>
    <tr>
      <td>Database Name: </td>
      <td><input type="text" id="dbname" name="dbname" value="" /></td>
    </tr>
	<tr>
      <td>DB User Name: </td>
      <td><input type="text" id="dbuser" name="dbuser" value="root" /></td>
    </tr>
    <tr>
      <td>DB User Pass: </td>
      <td><input type="password" id="dbpass" name="dbpass"/></td>
    </tr>
	<tr>
      <td>Confirm: </td>
      <td><input type="password" id="dbconf" name="dbconf"/></td>
    </tr>
    <tr>
	  <input type="hidden" id="stage" name="stage" value="2" />
      <td colspan="2"><button type="submit">Submit</button></td>
    </tr>
  </table>
  </form>';
  } elseif($stag == 2) {
	echo '
  <form action="setup.php" method="post">
  <table>
    <tr>
      <td colspan="2"><h3>Table Setup</h3></td>
    </tr>
    <tr>
      <td>Table Name: </td>
      <td><input type="text" id="table" name="table" value="" /></td>
    </tr>
	</tr>
	  <input type="hidden" id="stage" name="stage" value="3" />
      <td colspan="2"><button type="submit">Submit</button></td>
    </tr>
  </table>
  </form>';
  } elseif($stag == 3) {
	echo '
  <h3>Setup Complete!</h3>
  <p>
	Below are your credentials for when you start your CryptoPayAPI server. The url for the
	CryptoPayAPI server should be whatever url is displayed above but replace setup.php with
	index.php and keep the slash.
  </p>
  <ul>
    <li>User: '.$_SESSION['dbuser'].'</li>
	<li>Pass: '.$_SESSION['dbpass'].'</li>
  </ul>
  <p>Below are your credentials for filling out index.php:</p>
  <ul>
    <li>Port: '.$port.'</li>
	<li>Table: '.$_SESSION['table'].'</li>
	<li>Hostname: '.$hostname.'</li>
	<li>Database: '.$_SESSION['dbname'].'</li>
  </ul>
  ';

  }

  ?>
</body>
</html>