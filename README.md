# CryptoInApp
This API allows for gamemakers and programmers alike to accept crypto-currencies for in app purchases. Any currency that supports a narration in a transaction is supported. As it stands, the server works on any host but is advised to be run on the same platform as the rpc wallet.

## Setup
1. Make sure to extract all files in this archive into the same directory. Only file that does not matter is this one. You may rename crypto.php to any name, just makesure you update the name in node_config.prop
	
2. Edit the setup.php file with the configuration that works with your HTTP and SQL server. Once you are done, move the setup.php and crypto.php to your web document root (htdocs for apache2).
	
3. Open up setup.php in your web browser and fill in all the info that shows up. On the last step it will tell you all the info you need to create an app with the CryptoPayAPI and what info to update crypto.php with as well as node_config.prop

4. Finally securely generate a 32 character password and paste this between the quotes on line 8 of crypto.php and write it down somewhere secure. This encryption makes sure that sensitive info sent over your network cannot be sniffed out. Your application must encrypt your raw username and password using this key before adding it as a POST argument.
	
5. After filling out all the info in crypto.php, get to work! There is a sample Application included in this library's source under the example package. Modify that source to your own or start from scratch. Either way enjoy.
	
6. It is possible to link CryptoPayAPI with any application that supports POST requests. Hint: look at the source of the Client Side API, located in the client package.
