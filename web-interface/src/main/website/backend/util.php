<?php

function die_with_message($message) {
    echo json_encode(array('code' => 'error', 'message' => $message));
    exit_safely(1);
}

function die_with_message_and_error($message, $error) {
    echo json_encode(array('code' => 'error', 'message' => $message, 'error' => $error));
    exit_safely(1);
}

function success_exit_with_message($message) {
    echo json_encode(array('code' => 'success', 'message' => $message));
    exit_safely(0);
}

function exit_safely($code) {
    // if the database is set, close the connection
    if (isset($db)) {
        $db->close();
    }
    if (isset($stmt)) {
        $stmt->close();
    }
    exit($code);
}

function post_or_get_or_die($var_name) {
    if (isset($_POST[$var_name])) {
        $var = $_POST[$var_name];
        if (isset($db)) $var = $db->real_escape_string($var);
        return $var;
    } else if (isset($_GET[$var_name])) {
        $var = $_GET[$var_name];
        if (isset($db)) $var = $db->real_escape_string($var);
        return $var;
    } else {
        die_with_message("Missing parameter: $var_name");
    }
    return null;
}

function post_or_get($var_name) {
    if (isset($_POST[$var_name])) {
        $var = $_POST[$var_name];
        if (isset($db)) $var = $db->real_escape_string($var);
        return $var;
    } else if (isset($_GET[$var_name])) {
        $var = $_GET[$var_name];
        if (isset($db)) $var = $db->real_escape_string($var);
        return $var;
    }
    return null;
}
