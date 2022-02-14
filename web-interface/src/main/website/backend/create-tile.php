<?php

include_once('util.php');

try {
    $boardId = post_or_get_or_die('boardId');
    $tileJson = post_or_get_or_die('tileJson');
    $tileJson = json_decode($tileJson);

    include('db-login.php');

    $x = 0;
    $y = 0;
    // $tileJson is a JSON array of JSON arrays of JSON objects
    foreach ($tileJson as $row) {
        // iterate over the inner array
        foreach ($row as $tile) {
            if (isset($tile->difficulty)) {
                $difficulty = $tile->difficulty;
            } else {
                $difficulty = 0;
            }
            if (isset($tile->text)) {
                $text = $tile->text;
            } else {
                $text = "";
            }
            if (isset($tile->tooltip)) {
                $tooltip = $tile->tooltip;
            } else {
                $tooltip = "";
            }

            $claimed = '';
            $query = "INSERT INTO bingotiles (board_id, x, y, text, tooltip, difficulty, claimed) VALUES (?, ?, ?, ?, ?, ?, ?)";
            $stmt = $db->prepare($query);
            $stmt->bind_param('iiissds', $boardId, $x, $y, $text, $tooltip, $difficulty, $claimed);
            $stmt->execute();
            if ($stmt->errno) {
                die_with_message_and_error('failed to create tile at x=' . $x . ', y=' . $y . 'for board ' . $boardId, $stmt->error);
            } else {
                $stmt->close();
            }

            $x++;
        }
        $y++;
    }

    success_exit_with_message('created tiles');
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
