// the base directory where the php files are located in on the server (with a trailing '/')
const baseApiUrl = '';
const bingoBoard = document.getElementById('bingo-board');

function loadBoard(tilesJson) {
    // clear the board
    bingoBoard.innerHTML = '';

    // load the data from the tilesJson json object
    let difficulty = tilesJson['difficulty'];
    let metadata = tilesJson['metadata'];
    let width = tilesJson['width'];
    let height = tilesJson['height'];

    let bingoTitle = document.getElementById('bingo-title');
    if (bingoTitle) bingoTitle.innerHTML = metadata['title'];
    let bingoSubtitle = document.getElementById('bingo-subtitle');
    if (bingoSubtitle) {
        let subtitle = [];
        if (metadata['description']) subtitle.push(metadata['description'].replaceAll('\n', '<br>'));
        if (metadata['authors']) subtitle.push('Author' + (metadata['authors'].length !== 1 ? 's' : '') + ': ' + metadata['authors'].join(', '));
        if (metadata['game']) subtitle.push('Game: ' + metadata['game']);
        if (metadata['version']) subtitle.push('Version: ' + metadata['version']);
        if (difficulty) subtitle.push('Difficulty: ' + difficulty);
        bingoSubtitle.innerHTML = subtitle.join('<br>');
    }

    let board = tilesJson['board'];
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
    apiCall('http://yanwittmann.de/', {color: color}, function (response) {
        console.log(response);
    });
}

function apiCall(file, postData, callback) {
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            callback(JSON.parse(this.responseText));
        }
    };
    xhr.open('POST', baseApiUrl + file, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(postData));
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

let boardJson = JSON.parse('{"difficulty":0,"metadata":{"game":"Outer Wilds","description":"Bingo for Outer Wilds","title":"Outer Wilds Bingo","version":"0.1","authors":["Yan Wittmann"]},"width":5,"categories":{"arbitrary_travel":1,"die":0,"traveler":0,"endless_canyon":0,"raft":0,"dark_bramble":0,"ship":2,"strange_flames":0,"translate":1,"timber_hearth":7,"starlit_cove":0,"skeleton":1,"damaged_laboratory":0,"anglerfish":0,"dream_world":0,"black_hole_forge":1,"telekinetic":0,"warp_pad":1,"projection_pool":1,"campfire":1,"gabbro":0,"forbidden_archive":0,"high_energy_lab":0,"orbital_probe_cannon":0,"island":0,"hollows_lantern":1,"water":1,"refuel":0,"sunless_city":2,"esker":0,"quatum_moon":1,"beacon":0,"museum":0,"ash_twin":5,"slide":0,"hidden_gorge":1,"chert":0,"mural":1,"activity":9,"hanging_city":2,"riebek":0,"prisoner":0,"gravity_cannon":1,"abandoned_temple":0,"hearthian":0,"feldspar":0,"quantum":2,"brittle_hollow":4,"interloper":0,"telekinetic_orb":0,"nomai_ship":0,"ash_twin_tower":2,"village":1,"river_lowlands":0,"ember_twin":4,"solanum":0,"escape_pod":0,"attlerock":0,"straight_line_travel":0,"ash_twin_project":1,"early_in_loop":1,"giants_deep":1,"zero_g_cave":1,"brim_hollow":0,"subterranean_lake":0,"shrouded_woodlands":0,"stranger":3,"location":12,"cinder_isles":0,"reservoir":1,"hearthian_notes":0,"incident":1,"quantum_moon":0},"board":[[{"difficulty":0,"text":"Use a projection pool at Statue Island Workshop","categories":["projection_pool","giants_deep"]},{"difficulty":0,"tooltip":"Standing on the ceiling suffices.","text":"Reach The Hanging City: Outside the Black Hole Forge on Brittle Hollow","categories":["location","brittle_hollow","black_hole_forge","hanging_city"]},{"difficulty":0,"tooltip":"The reservoir under the bridge to the Nomai Mines at the bottom of the large Geyser Mountain.","text":"Translate any text at The First Encounter Mural","categories":["translate","water","timber_hearth","mural"]},{"difficulty":0,"text":"Ash Twin activity: Make a full loop around the Ash Twin Tower band.","categories":["activity","ash_twin"]},{"difficulty":0,"text":"Ash Twin activity: Touch the core material from the inside","categories":["activity","ash_twin","ash_twin_project"]}],[{"difficulty":0,"tooltip":"Use the Quantum Moon Locator to observe the Moon.","text":"Ember Twin activity: Locate the Moon around Timber Hearth, The Ember Twin and Brittle Hollow","categories":["activity","ember_twin","quantum","brittle_hollow","timber_hearth"]},{"difficulty":0,"tooltip":"The Gravity Cannon on Brittle Hollow.","text":"Reach The Brittle Hollow Gravity Cannon on Brittle Hollow","categories":["location","brittle_hollow","gravity_cannon"]},{"difficulty":0,"tooltip":"Damaged or not, it must be standing on the surface.","text":"Land your ship on Hollows Lantern","categories":["location","ship","hollows_lantern"]},{"difficulty":0,"text":"Shock yourself or your ship","categories":["incident"]},{"difficulty":0,"text":"Rest at a campfire on The Stranger","categories":["activity","campfire","stranger"]}],[{"difficulty":0,"tooltip":"Damaged or not, it must be standing on the surface.","text":"Land your ship on The Quatum Moon","categories":["location","ship","quatum_moon"]},{"difficulty":0,"text":"Reach The Sunless City: Stepping Stone District on Ember Twin","categories":["location","ember_twin","sunless_city"]},{"difficulty":0,"tooltip":"Must be standing inside the tower.","text":"Reach The Timber Hearth Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]},{"difficulty":0,"tooltip":"Must use the elevator to reach the top.","text":"Reach The Hidden Gorge on The Stranger","categories":["location","stranger","hidden_gorge"]},{"difficulty":0,"text":"Ember Twin activity: Touch a cactus","categories":["activity","ember_twin"]}],[{"difficulty":0,"text":"Ash Twin activity: Touch the core material from the outside","categories":["activity","ash_twin"]},{"difficulty":0,"tooltip":"The Ash Twin Tower Warp Pad Receiver.","text":"Reach The Timber Hearth Ash Twin Tower Receiver on Timber Hearth","categories":["location","timber_hearth","warp_pad"]},{"difficulty":0,"tooltip":"You cannot get credit for the same location twice.","text":"Touch 5 Nomai skeletons","categories":["activity","skeleton"]},{"difficulty":0,"text":"Timber Hearth activity: Read 4 quantum poems","categories":["activity","timber_hearth","quantum"]},{"difficulty":0,"text":"Timber Hearth activity: Jump across the bridge to the Mining Site 2b","categories":["activity","timber_hearth"]}],[{"difficulty":0,"tooltip":"Enter the Zero-G-Cave via the elevator.","text":"Reach The Zero-G Cave on Timber Hearth","categories":["location","timber_hearth","village","zero_g_cave"]},{"difficulty":0,"text":"Reach The Sunless City: Anglerfish Overlook District on Ember Twin","categories":["location","ember_twin","sunless_city"]},{"difficulty":0,"tooltip":"Open and enter the mining site inside the huge Geyser Mountain.","text":"Reach The Mining Site 2b on Timber Hearth","categories":["location","timber_hearth"]},{"difficulty":0,"tooltip":"Must be standing inside the tower.","text":"Reach The Giant\'s Deep Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]},{"difficulty":0,"tooltip":"Must happen in a single loop in that order, but detours are allowed.\\nMust walk over to the central room with the tree.\\nThe area behind the warming gates and before/on top the dam.","text":"Travel from The Hanging City: School District to The Reservoir","categories":["arbitrary_travel","early_in_loop","stranger","hanging_city","brittle_hollow","reservoir"]}]],"height":5}');
loadBoard(boardJson);
