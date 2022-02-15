<?php

include_once('util.php');

try {
    $boardJson = post_or_get_or_die('boardJson');
    $boardJson = json_decode($boardJson, true);
    $allow_multiple_claims = post_or_get_or_default('allow_multiple_claims', false);

    include('db-login.php');

    $boardMetadata = isset_or_die($boardJson, 'metadata');
    $title = isset_or_die($boardMetadata, 'title');
    $description = isset_or_die($boardMetadata, 'description');
    $authors = isset_or_die($boardMetadata, 'authors');
    $game = isset_or_die($boardMetadata, 'game');
    $version = isset_or_die($boardMetadata, 'version');

    $difficulty = $boardJson['difficulty'];
    $width = $boardJson['width'];
    $height = $boardJson['height'];
    $tileJson = $boardJson['board'];

    // upload the board metadata first
    $boardId = -1;
    $query = "INSERT INTO bingoboards (difficulty, game, description, title, version, authors, allow_multiple_claims, width, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $db->prepare($query);
    $stmt->bind_param('sssssssss', $difficulty, $game, $description, $title, $version, $authors, $allow_multiple_claims, $width, $height);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to create board', $stmt->error);
    } else {
        $boardId = $stmt->insert_id;
    }

    // upload the individual tiles
    $x = 0;
    $y = 0;
    // $tileJson is a JSON array of JSON arrays of JSON objects
    foreach ($tileJson as $row) {
        // iterate over the inner array
        foreach ($row as $tile) {
            if (isset($tile['difficulty'])) {
                $difficulty = $tile['difficulty'];
            } else {
                $difficulty = 0;
            }
            if (isset($tile['text'])) {
                $text = $tile['text'];
            } else {
                $text = '';
            }
            if (isset($tile['tooltip'])) {
                $tooltip = $tile['tooltip'];
            } else {
                $tooltip = '';
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

    success_exit_with_message($boardId);
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
