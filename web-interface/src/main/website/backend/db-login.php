<?php

$conn = new mysqli('localhost', '', '', '');

// check if the connection works
if ($conn->connect_error) {
    $dbSuccess = false;
    $returnValue->code = "error";
    $returnValue->reason = "dbFailedConnection";
    $returnValue->details = $conn->connect_error;
} else {
    $dbSuccess = true;
}