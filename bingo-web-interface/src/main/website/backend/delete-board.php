<?php

include_once('util.php');

try {
    $boardId = post_or_get_or_die('boardId');

    include('db-login.php');

    // remove all tiles with that boardId from the table bingotiles
    $query = "DELETE FROM bingotiles WHERE board_id = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to delete tiles', $stmt->error);
    }

    // delete the entry in the table bingoboards with the given boardId
    $query = "DELETE FROM bingoboards WHERE id = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to delete board', $stmt->error);
    }

    success_exit_with_message('board ' . $boardId . ' deleted');
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
