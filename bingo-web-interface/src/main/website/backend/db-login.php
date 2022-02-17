<?php

$db = new mysqli('localhost', 'd036c300', 'rCK5spdFdNks4uUc', 'd036c300');

// check if the connection works
if ($db->connect_error) {
    die_with_message_and_error("connection with database failed", $db->connect_error);
}
