// the base directory where the php files are located in on the server (with a trailing '/')
const baseApiUrl = '';
const bingoBoard = document.getElementById('bingo-board');

let currentBoardId = 0;

function loadBoard(boardId) {
    setLocalStorage('boardId', boardId);
    currentBoardId = boardId;
    apiCall('get-board-tiles.php', {boardId: boardId}, function (response) {
        createBoardFromApiResponse(response);
    });
    apiCall('get-board-metadata.php', {boardId: boardId}, function (response) {
        setBoardMetadataFromApiResponse(response);
    });
    updateClaims();
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
    for (let col = 0; col < height; col++) {
        let tr = document.createElement('tr');
        bingoBoard.appendChild(tr);
        for (let row = 0; row < width; row++) {
            let cell = document.createElement('td');
            cell.setAttribute('x', row);
            cell.setAttribute('y', col);
            // set the cell's text to the value of the board at the current row and column
            if (board[col][row]['text']) {
                cell.innerHTML = board[col][row]['text'].replaceAll('\\n', '<br>');
            } else {
                cell.innerHTML = '&nbsp;';
            }
            tr.appendChild(cell);

            // add a tooltip hint element to the cell
            if (board[col][row]['tooltip']) {
                cell.setAttribute('tooltip', board[col][row]['tooltip']);
                cell.appendChild(document.createElement('br'));
                cell.append('[?]');
            }

            // add a click listener to the cell that calls the onCellClicked function and passes it the cell's x and y coordinates
            cell.addEventListener('click', function () {
                onCellClicked(this.getAttribute('x'), this.getAttribute('y'));
            });
        }
    }
}

const colorClaimConversion = {
    "red": "1",
    "orange": "2",
    "yellow": "3",
    "green": "4",
    "aqua": "5",
    "blue": "6",
    "purple": "7",
    "pink": "8",
}

function convertIdToColor(id) {
    for (let color in colorClaimConversion) {
        if (colorClaimConversion[color] === id) {
            return color;
        }
    }
    return null;
}

function updateClaimsFromApiResponse(json) {
    let claims = json['claims'];
    let width = json['width'];
    let height = json['height'];

    for (let i = 0; i < width; i++) {
        for (let j = 0; j < height; j++) {
            let cell = getCell(i, j);
            if (cell) {
                for (let color in colorClaimConversion) {
                    cell.classList.remove(color);
                }
                if (claims[j][i] && claims[j][i].length) {
                    cell.classList.add(convertIdToColor(claims[j][i]));
                }
            }
        }
    }
}

function getCell(x, y) {
    let cells = bingoBoard.getElementsByTagName('td');
    for (let i = 0; i < cells.length; i++) {
        if (cells[i].getAttribute('x') === '' + x && cells[i].getAttribute('y') === '' + y) {
            return cells[i];
        }
    }
    return null;
}

function updateClaims() {
    apiCall('get-board-claims.php', {boardId: currentBoardId}, function (response) {
        updateClaimsFromApiResponse(response);
    });
}

function onCellClicked(x, y) {
    apiCall('claim-board-tile.php', {
        boardId: currentBoardId,
        x: x,
        y: y,
        claim: getCurrentColorId()
    }, function (response) {
        if (response.code === 'success') {
            updateClaims();
        }
    });
}

function switchColor(color) {
    setLocalStorage('bingoColor', color);
}

function getCurrentColorId() {
    if (hasLocalStorage('bingoColor')) {
        return colorClaimConversion[getLocalStorage('bingoColor')];
    } else {
        return '1';
    }
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

function init() {
    if (hasLocalStorage('boardId')) {
        loadBoard(getLocalStorage('boardId'));
    }
}

// call the init function when the page is loaded
window.addEventListener('load', init);
