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
                insertTableRow(jsonData[i].title, jsonData[i].author, jsonData[i].difficulty, jsonData[i]._id, "books")
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
                insertTableRow(jsonData[i].title, jsonData[i].author, jsonData[i].difficulty, jsonData[i]._id, "authors")
            }
    
        } else {
            console.log(this.status)
        }   
    }
}


function insertTableRow(title, author, difficulty, id, table) {
    var newRow;
    var containerBlock ;
    

    newRow = document.createElement( 'tr' );
    newRow.onclick = function () {
        window.location.href = "book-analysis.html?id=" + id;
    
    }
    

    if(table === "books"){
        rowsCounterBook++;
        containerBlock = document.getElementById( 'tBodyIdBook' );
        newRow.className = "tableRow";
        newRow.innerHTML = 
              '<th scope="row">' + rowsCounterBook.toString() + '</th>'
            + '<td>'+ author + '</td>'
            + '<td>'+ title + '</td>'
            + '<td>'+ difficulty + '</td>'    
    } else {
        rowsCounterAuth++;
        containerBlock = document.getElementById( 'tBodyIdAuth' );
        newRow.className = "tableRow";
        newRow.innerHTML = 
              '<th scope="row">' + rowsCounterAuth.toString() + '</th>'
            + '<td>'+ author + '</td>'
            + '<td>'+ title + '</td>'
            + '<td>'+ difficulty + '</td>'
    }

    containerBlock.appendChild( newRow );
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