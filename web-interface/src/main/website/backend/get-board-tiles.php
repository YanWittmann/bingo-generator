<?php

include_once('util.php');

try {
    $boardId = post_or_get_or_die('boardId');

    include('db-login.php');

    // select the width and height from the bingoboards table
    $query = "SELECT width, height FROM bingoboards WHERE id = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch board width/height', $stmt->error);
    }

    $stmt->bind_result($width, $height);
    $stmt->fetch();
    $stmt->close();

    // select all the tiles for the boardId
    $query = "SELECT * FROM bingotiles WHERE board_id = ? ORDER BY x, y";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch tiles', $stmt->error);
    }

    $result = $stmt->get_result();
    $tiles = $result->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

    // create an array containing an array of tiles for each row
    $tilesByRow = array();
    foreach ($tiles as $tile) {
        $tilesByRow[$tile['y']][] = $tile;
    }

    $result = array(
        'width' => $width,
        'height' => $height,
        'tiles' => $tilesByRow
    );

    echo json_encode($result);
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
