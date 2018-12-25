var rowsCounterBook = 0;
var rowsCounterAuth = 0;
document.getElementById("topBooksTableId").style.display = 'block';
document.getElementById("topAuthTableId").style.display = 'none';
document.getElementById("graphId").style.display = 'none';

function requestTopBooks() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "/TopBooksServlet", true);
    xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhttp.send();

    xhttp.onreadystatechange = function() {
    
        if (this.readyState == 4 && this.status == 200) {
            var jsonData = JSON.parse(this.responseText);
            console.log("succsess");
            
            for(var i in jsonData)
            {
                insertBookTableRow(jsonData[i].title, jsonData[i].author, jsonData[i].difficulty, jsonData[i]._id, jsonData[i].year)
            }
    
        } else {
            console.log(this.status)
        }   
    }
    
}

function requestTopAuth() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "/TopAuthServlet", true);
    xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhttp.send();

    xhttp.onreadystatechange = function() {
    
        if (this.readyState == 4 && this.status == 200) {
            var jsonData = JSON.parse(this.responseText);
            console.log("succsess");
            
            for(var i in jsonData)
            {
                insertAuthTableRow(jsonData[i]._id, jsonData[i].difficulty)
            }
    
        } else {
            console.log(this.status)
        }   
    }
}

function requestAvgDifficulty() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "/AvgDifficultyServlet", true);
    xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhttp.send();

    xhttp.onreadystatechange = function() {
    
        if (this.readyState == 4 && this.status == 200) {
            var jsonData = JSON.parse(this.responseText);
            console.log("succsess");
            
            drawGraphic(jsonData);
    
        } else {
            console.log(this.status)
        }   
    }
}

function insertBookTableRow(title, author, difficulty, id, year) {
    var newRow;
    var containerBlock ;
    

    newRow = document.createElement( 'tr' );
    newRow.onclick = function () {
        window.location.href = "book-analysis.html?id=" + id;
    
    }

    rowsCounterBook++;
    containerBlock = document.getElementById( 'tBodyIdBook' );
    newRow.className = "tableRow";
    newRow.innerHTML = 
            '<th scope="row">' + rowsCounterBook.toString() + '</th>'
        + '<td>'+ author + '</td>'
        + '<td>"'+ title + ' ('+ year +')'+'"</td>'
        + '<td>'+ difficulty + '</td>'    

    containerBlock.appendChild( newRow );
}

function insertAuthTableRow(author, difficulty) {
    var newRow;
    var containerBlock ;
    

    newRow = document.createElement( 'tr' );
    
    rowsCounterAuth++;
    containerBlock = document.getElementById( 'tBodyIdAuth' );
    newRow.className = "tableRow";
    newRow.innerHTML = 
          '<th scope="row">' + rowsCounterAuth.toString() + '</th>'
        + '<td>'+ author + '</td>'
        + '<td>'+ difficulty + '</td>'

    containerBlock.appendChild( newRow );
}

function drawGraphic(dataArray){
    var xArr = new Array();
    var yArr = new Array();

    var arrayLength;

    if (dataArray.length > 52){
        arrayLength = 52;
    } else {
        arrayLength = dataArray.length;
    }

    for (var i = 0; i < arrayLength; i++) {
        yArr[i] = dataArray[i];
        xArr[i] = 1500 + i * 10;
    }

    var trace1 = {

        y: yArr,
        x: xArr,
        type: 'scatter'
    };


    var data = [trace1];

    var layout = {
        title: "<b>Average book's difficulty</b>",
        titlefont: {
            size: 20
        },
        xaxis: {
        title: 'year',
        titlefont: {
            family: 'Courier New, monospace',
            size: 18,
            color: '#7f7f7f'
        }
        },
        yaxis: {
        title: 'difficulty',
        titlefont: {
            family: 'Courier New, monospace',
            size: 18,
            color: '#7f7f7f'
        }
        }
    };

    Plotly.newPlot('graphId', data, layout);
}

document.getElementById("bookBtn").onclick = function() {
    document.getElementById("topBooksTableId").style.display = 'block';
    document.getElementById("topAuthTableId").style.display = 'none';
    document.getElementById("graphId").style.display = 'none';

    document.getElementById("bookBtn").className = "nav-link btn btn-dark";
    document.getElementById("authBtn").className = "nav-link btn btn-light";
    document.getElementById("graphBtn").className = "nav-link btn btn-light";
}

document.getElementById("authBtn").onclick = function() {
    document.getElementById("topBooksTableId").style.display = 'none';
    document.getElementById("topAuthTableId").style.display = 'block';
    document.getElementById("graphId").style.display = 'none';

    document.getElementById("bookBtn").className = "nav-link btn btn-light";
    document.getElementById("authBtn").className = "nav-link btn btn-dark";
    document.getElementById("graphBtn").className = "nav-link btn btn-light";
}

document.getElementById("graphBtn").onclick = function() {
    document.getElementById("topBooksTableId").style.display = 'none';
    document.getElementById("topAuthTableId").style.display = 'none';
    document.getElementById("graphId").style.display = 'block';

    document.getElementById("bookBtn").className = "nav-link btn btn-light";
    document.getElementById("authBtn").className = "nav-link btn btn-light";
    document.getElementById("graphBtn").className = "nav-link btn btn-dark";
}