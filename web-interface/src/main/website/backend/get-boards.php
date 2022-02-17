<?php

include_once('util.php');

try {
    include('db-login.php');

    // select all columns containing the relevant information
    $query = "SELECT id, description, title FROM bingoboards";
    $result = $db->query($query);

    if (!$result) {
        die_with_message_and_error('failed to fetch board information', $db->error);
    }

    // the goal is to echo the boards as json array of json objects
    $boards = array();
    while ($row = $result->fetch_assoc()) {
        $board = array(
            'id' => $row['id'],
            'description' => $row['description'],
            'title' => $row['title']
        );
        array_push($boards, $board);
    }

    echo json_encode($boards);
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
