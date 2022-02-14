<?php

$conn = new mysqli('localhost', '', '', '');

// check if the connection works
if ($db->connect_error) {
    die_with_message_and_error("connection with database failed", $db->connect_error);
}
