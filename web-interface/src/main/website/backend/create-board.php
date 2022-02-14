<?php

include_once('util.php');

try {
    $difficulty = post_or_get_or_die('difficulty');
    $game = post_or_get_or_die('game');
    $description = post_or_get_or_die('description');
    $title = post_or_get_or_die('title');
    $version = post_or_get_or_die('version');
    $authors = post_or_get_or_die('authors');
    $allow_multiple_claims = post_or_get_or_die('allow_multiple_claims');
    $width = post_or_get_or_die('width');
    $height = post_or_get_or_die('height');

    include('db-login.php');

    $query = "INSERT INTO bingoboards (difficulty, game, description, title, version, authors, allow_multiple_claims, width, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $db->prepare($query);
    $stmt->bind_param('sssssssss', $difficulty, $game, $description, $title, $version, $authors, $allow_multiple_claims, $width, $height);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to create board', $stmt->error);
    } else {
        $board_id = $stmt->insert_id;
        success_exit_with_message($board_id);
    }
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
