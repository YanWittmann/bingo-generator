<?php

include_once('util.php');

try {
    $boardId = post_or_get_or_die('boardId');

    include('db-login.php');

    // select all columns containing the relevant information
    $query = "SELECT width, height, difficulty, game, description, title, version, authors, allow_multiple_claims FROM bingoboards WHERE id = ?";
    $stmt = $db->prepare($query);
    $stmt->bind_param('i', $boardId);
    $stmt->execute();

    if ($stmt->errno) {
        die_with_message_and_error('failed to fetch board information', $stmt->error);
    }

    $stmt->bind_result($width, $height, $difficulty, $game, $description, $title, $version, $authors, $allow_multiple_claims);
    $stmt->fetch();
    $stmt->close();

    $result = array(
        'difficulty' => $difficulty,
        'game' => $game,
        'description' => $description,
        'title' => $title,
        'version' => $version,
        'authors' => $authors,
        'allow_multiple_claims' => $allow_multiple_claims
    );

    echo json_encode($result);
} catch (Exception $e) {
    die_with_message_and_error('unknown error', $e->getMessage());
}
