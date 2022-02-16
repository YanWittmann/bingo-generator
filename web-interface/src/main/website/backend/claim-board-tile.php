<?php

include_once('util.php');

try {
    $boardId = post_or_get_or_die('boardId');
    $x = post_or_get_or_die('x');
    $y = post_or_get_or_die('y');
    $claim = post_or_get_or_die('claim');

    include('db-login.php');

    // first check if multiple persons may claim the same tile by getting allow_multiple_claims from bingoboards
    $query = "SELECT allow_multiple_claims FROM bingoboards WHERE id = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch board allow_multiple_claims', $stmt->error);
    }

    $allow_multiple_claims = $stmt->get_result()->fetch_assoc()['allow_multiple_claims'];


    // check if the tile is already claimed on bingotiles
    $query = "SELECT claimed FROM bingotiles WHERE board_id = ? AND x = ? AND y = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('iii', $boardId, $x, $y);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch tile', $stmt->error);
    }

    // get the result
    $claimed = $stmt->get_result()->fetch_assoc()['claimed'];

    // check if $allow_multiple_claims is true
    if ($allow_multiple_claims) {
        // claimed is a string containing up to 8 characters: 12345678
        // check if the claimer is already in the string
        if (strpos($claimed, $claim) !== false) {
            // if so, remove the claimer from the string
            $claimed = str_replace($claim, '', $claimed);
        } else {
            // if not, add the claimer to the string
            $claimed .= $claim;
        }

    } else {
        if (strlen($claimed) > 0 and strcmp($claimed, $claim) == 0) {
            $claimed = '';
        } else if (strlen($claimed) == 0) {
            $claimed = $claim;
        }
    }

    // update the claimed value in bingotiles
    $query = "UPDATE bingotiles SET claimed = ? WHERE board_id = ? AND x = ? AND y = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('siii', $claimed, $boardId, $x, $y);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to update tile claimed', $stmt->error);
    }

    $stmt->close();

    success_exit_with_message('claim updated');
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
