// the base directory where the php files are located in on the server (with a trailing '/')
const baseApiUrl = '';
const bingoBoard = document.getElementById('bingo-board');

function loadBoard(boardId) {
    apiCall('get-board-tiles.php', {boardId: boardId}, function (response) {
        createBoardFromApiResponse(response);
    });
    apiCall('get-board-metadata.php', {boardId: boardId}, function (response) {
        setBoardMetadataFromApiResponse(response);
    });
}

function setBoardMetadataFromApiResponse(json) {
    let difficulty = json['difficulty'];

    let bingoTitle = document.getElementById('bingo-title');
    if (bingoTitle) bingoTitle.innerHTML = json['title'];
    let bingoSubtitle = document.getElementById('bingo-subtitle');
    if (bingoSubtitle) {
        let subtitle = [];
        if (json['description']) subtitle.push(json['description'].replaceAll('\n', '<br>'));
        if (json['authors']) subtitle.push('Author(s): ' + json['authors']);
        if (json['game']) subtitle.push('Game: ' + json['game']);
        if (json['version']) subtitle.push('Version: ' + json['version']);
        if (difficulty) subtitle.push('Difficulty: ' + difficulty);
        bingoSubtitle.innerHTML = subtitle.join('<br>');
    }
}

function createBoardFromApiResponse(json) {
    let width = json['width'];
    let height = json['height'];
    let board = json['tiles'];

    // clear the board
    bingoBoard.innerHTML = '';
    // board is an array of arrays of strings, iterate over the outer array and inner arrays
    for (let row = 0; row < width; row++) {
        let tr = document.createElement('tr');
        bingoBoard.appendChild(tr);
        for (let col = 0; col < height; col++) {
            let cell = document.createElement('td');
            cell.setAttribute('x', col);
            cell.setAttribute('y', row);
            // set the cell's text to the value of the board at the current row and column
            // check if board[row][col] has text first
            if (board[row][col]['text']) {
                cell.innerHTML = board[row][col]['text'].replaceAll('\\n', '<br>');
            } else {
                cell.innerHTML = '&nbsp;';
            }
            tr.appendChild(cell);

            // add a tooltip hint element to the cell
            if (board[row][col]['tooltip']) {
                cell.setAttribute('tooltip', board[row][col]['tooltip']);
                // append a line break and a [?] to the cell's text
                let br = document.createElement('br');
                cell.appendChild(br);
                cell.append('[?]');
            }

            // add a click listener to the cell that calls the onCellClicked function and passes it the cell's x and y coordinates
            cell.addEventListener('click', function () {
                onCellClicked(this.getAttribute('x'), this.getAttribute('y'));
            });
        }
    }
}

function onCellClicked(x, y) {
    console.log('cell clicked at x: ' + x + ', y: ' + y);
}

function switchColor(color) {
    setLocalStorage('bingoColor', color);
}

function apiCall(file, data, callback) {
    fetch(baseApiUrl + file + '?' + requestDataToGet(data), {
        method: "GET",
        headers: {'Accept': 'application/json'}
    }).then(res => {
        let result = res.json();
        result.then(function (value) {
            callback(value);
        });
    });
}

function requestDataToGet(json) {
    let data = '';
    for (let key in json) {
        data += key + '=' + json[key] + '&';
    }
    return data;
}

function setLocalStorage(key, value) {
    localStorage.setItem(key, value);
}

function getLocalStorage(key) {
    return localStorage.getItem(key);
}

function clearLocalStorage() {
    localStorage.clear();
}

function hasLocalStorage(key) {
    return localStorage.getItem(key) !== null;
}

// create a global hover listener for all elements for the tooltips
// make sure that it refreshes as soon as the cursor moves
document.addEventListener('mousemove', function (e) {
    let tooltip = document.getElementById('tooltip');
    if (tooltip) {
        tooltip.remove();
    }
    let tooltipText = e.target.getAttribute('tooltip');
    if (tooltipText) {
        let tooltip = document.createElement('div');
        tooltip.setAttribute('id', 'tooltip');
        tooltip.innerText = tooltipText;
        document.body.appendChild(tooltip);
        // respect the bounding box of the tooltip and the screen
        // give it an offset of 10px
        let tooltipWidth = tooltip.offsetWidth + 10;
        let tooltipHeight = tooltip.offsetHeight + 10;
        let tooltipX = e.clientX + 10;
        let tooltipY = e.clientY + 10;
        // if the tooltip would be off the screen, move it to the left
        if (tooltipX + tooltipWidth > window.innerWidth) {
            tooltipX = e.clientX - tooltipWidth - 10;
        }
        // if the tooltip would be off the screen, move it to the top
        if (tooltipY + tooltipHeight > window.innerHeight) {
            tooltipY = e.clientY - tooltipHeight - 10;
        }
        tooltip.style.left = tooltipX + 'px';
        tooltip.style.top = tooltipY + 'px';
    }
});

loadBoard(5);
