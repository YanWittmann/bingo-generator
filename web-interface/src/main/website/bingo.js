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
        if (metadata['description']) subtitle.push('Description: ' + metadata['description']);
        if (metadata['authors']) subtitle.push('Author(s): ' + metadata['authors'].join(', '));
        if (metadata['game']) subtitle.push('Game: ' + metadata['game']);
        if (metadata['version']) subtitle.push('Version: ' + metadata['version']);
        if (difficulty) subtitle.push('Difficulty: ' + difficulty);
        bingoSubtitle.innerHTML = subtitle.join('<br>');
    }

    let board = tilesJson['board'];
    // board is an array of arrays of strings, iterate over the outer array and inner arrays
    for (let row = 0; row < height; row++) {
        let tr = document.createElement('tr');
        bingoBoard.appendChild(tr);
        for (let col = 0; col < width; col++) {
            let cell = document.createElement('td');
            cell.setAttribute('x', col);
            cell.setAttribute('y', row);
            // set the cell's text to the value of the board at the current row and column
            cell.innerText = board[row][col]['text'].replaceAll('\\n', '<br>');
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

function Sleep(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}

let boardJson = JSON.parse('{"difficulty":0,"metadata":{"game":"Outer Wilds","description":"Bingo for Outer Wilds","title":"Outer Wilds Bingo","version":"0.1","authors":["Yan Wittmann"]},"width":5,"categories":{"arbitrary_travel":1,"die":1,"traveler":0,"endless_canyon":0,"raft":0,"dark_bramble":0,"ship":0,"strange_flames":0,"translate":1,"timber_hearth":4,"starlit_cove":0,"skeleton":1,"damaged_laboratory":0,"anglerfish":0,"dream_world":0,"black_hole_forge":0,"telekinetic":0,"warp_pad":0,"projection_pool":1,"campfire":1,"gabbro":0,"forbidden_archive":0,"high_energy_lab":1,"orbital_probe_cannon":2,"island":0,"hollows_lantern":0,"water":3,"refuel":0,"sunless_city":1,"esker":0,"quatum_moon":0,"beacon":0,"museum":0,"ash_twin":5,"slide":0,"hidden_gorge":0,"chert":0,"mural":1,"activity":10,"hanging_city":0,"riebek":0,"prisoner":0,"gravity_cannon":1,"abandoned_temple":0,"hearthian":0,"feldspar":0,"quantum":2,"brittle_hollow":4,"interloper":1,"telekinetic_orb":1,"nomai_ship":0,"ash_twin_tower":4,"village":1,"river_lowlands":0,"ember_twin":6,"solanum":0,"escape_pod":2,"attlerock":0,"straight_line_travel":0,"ash_twin_project":1,"early_in_loop":0,"giants_deep":2,"zero_g_cave":0,"brim_hollow":0,"subterranean_lake":0,"shrouded_woodlands":0,"stranger":1,"location":12,"cinder_isles":0,"reservoir":0,"hearthian_notes":0,"incident":1,"quantum_moon":0},"board":[[{"difficulty":0,"tooltip":"Cross at least the first bridge to the central room on the upper level.","text":"Reach Old Settlement (upper level) on Brittle Hollow","categories":["location","brittle_hollow"]},{"difficulty":0,"tooltip":"The building must be entered and the ghost matter pit crossed.","text":"Reach The Sunless City: Eye Shrine District on Ember Twin","categories":["location","ember_twin","sunless_city"]},{"difficulty":0,"tooltip":"Time difference must be visible to the naked eye.","text":"Ember Twin activity: Perform the experiment in the High Energy Lab","categories":["activity","ember_twin","high_energy_lab"]},{"difficulty":0,"tooltip":"You cannot get credit for the same location twice.","text":"Touch 6 Nomai skeletons","categories":["activity","skeleton"]},{"difficulty":0,"tooltip":"Must be standing inside the tower.","text":"Reach The Giant\'s Deep Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]}],[{"difficulty":0,"tooltip":"The module that sank to the core.","text":"Use a projection pool at Orbital Probe Cannon: Probe Tracking Module","categories":["projection_pool","orbital_probe_cannon","giants_deep"]},{"difficulty":0,"tooltip":"Must be standing inside one of the towers.","text":"Reach The Hourglass Twins Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]},{"difficulty":0,"tooltip":"Must be standing inside the tower (not at the warp pad).","text":"Reach The Sun Station Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]},{"difficulty":0,"tooltip":"You don\'t have to take the core out of the casing.","text":"Ash Twin activity: Turn off the gravity and open the casing.","categories":["activity","ash_twin","ash_twin_project"]},{"difficulty":0,"tooltip":"Be on either of the two levels of the Crossroads.","text":"Reach The Crossroads on Brittle Hollow","categories":["location","brittle_hollow"]}],[{"difficulty":0,"tooltip":"You cannot get credit for the same location twice.\\\\nFor example Radio Tower, Gravity Crystal Workshop, Quantum Moon Locator.","text":"Light 3 Campfires","categories":["activity","campfire"]},{"difficulty":0,"tooltip":"Large mountains scattered around Timber Hearth that periodically spout water columns from their geyser peaks.","text":"Reach The Geyser Mountains on Timber Hearth","categories":["location","timber_hearth","water"]},{"difficulty":0,"text":"Ember Twin activity: Shoot something from the Gravity Cannon","categories":["activity","ember_twin","gravity_cannon"]},{"difficulty":0,"text":"Timber Hearth activity: Land the model ship on a geyser platform","categories":["activity","timber_hearth","village"]},{"difficulty":0,"tooltip":"Must happen in a single loop in that order, but detours are allowed.\\nStand in front of the quatum shard.\\nThe module with the fractured window.","text":"Travel from The Quantum Caves to Orbital Probe Cannon: Launch Module","categories":["arbitrary_travel","quantum","orbital_probe_cannon","giants_deep","ember_twin"]}],[{"difficulty":0,"text":"Stranger activity: (fail to) translate any text","categories":["activity","stranger","translate"]},{"difficulty":0,"text":"Ember Twin activity: Touch a cactus","categories":["activity","ember_twin"]},{"difficulty":0,"tooltip":"Must be standing inside the tower.","text":"Reach The Timber Hearth Tower on Ash Twin","categories":["location","ash_twin","ash_twin_tower"]},{"difficulty":0,"tooltip":"Enter a water-filled cave via the geysers.","text":"Reach The Geyser Cave System on Timber Hearth","categories":["location","timber_hearth","water"]},{"difficulty":0,"tooltip":"You cannot get credit for the same location twice.","text":"Touch 3 Quantum Shards","categories":["activity","quantum"]}],[{"difficulty":0,"tooltip":"The reservoir under the bridge to the Nomai Mines at the bottom of the large Geyser Mountain.","text":"Reach The First Encounter Mural on Timber Hearth","categories":["location","timber_hearth","water","mural"]},{"difficulty":0,"text":"Reach Escape Pod 2 on Ember Twin on Ember Twin","categories":["location","ember_twin","escape_pod"]},{"difficulty":0,"text":"Brittle Hollow activity: Fall inside the black hole.","categories":["activity","brittle_hollow"]},{"difficulty":0,"text":"Reach Escape Pod 1 on Brittle Hollow on Brittle Hollow","categories":["location","brittle_hollow","escape_pod","telekinetic_orb"]},{"difficulty":0,"text":"Die on The Interloper","categories":["incident","die","interloper"]}]],"height":5}');
loadBoard(boardJson);
