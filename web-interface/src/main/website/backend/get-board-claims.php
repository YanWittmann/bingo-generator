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
    $query = "SELECT x, y, claimed FROM bingotiles WHERE board_id = ? ORDER BY x, y";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch tiles', $stmt->error);
    }

    $result = $stmt->get_result();

    $tiles = array();
    while ($row = $result->fetch_assoc()) {
        $tiles[] = $row;
    }
    $stmt->close();
    $db->close();

    // echo only the claimed part of the result but use the x and y coordinates to order them into an array of arrays
    $claimed = array();
    foreach ($tiles as $tile) {
        $claimed[$tile['y']][$tile['x']] = $tile['claimed'];
    }

    echo json_encode([
        'width' => $width,
        'height' => $height,
        'claims' => $claimed
    ]);
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
