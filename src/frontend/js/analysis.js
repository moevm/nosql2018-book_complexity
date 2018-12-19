function ajaxDataGet() {
var url_string = window.location.href; 
var url = new URL(url_string);
var id = url.searchParams.get("id");
var searchedBook = url.searchParams.get("searchedBook");

document.getElementById('arrowID').onclick = function(){
    console.log("arrow");
    window.location.href = "search-results.html?searchedBook=" + searchedBook;
}

var xhttp = new XMLHttpRequest();

xhttp.open("POST", "/BookAnalysisServlet", true);
xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
xhttp.send("id=" + id);



xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
    var jsonData = JSON.parse(this.responseText);
    var dif = jsonData.difficulty 
    console.log("succsess ");
    drawGraphic(jsonData.lexicon_years);
    document.getElementById('bookImg').innerHTML = '<img class="book-image-block" src="../cover?id=' + jsonData.cover +'">'

    document.getElementById('name').innerHTML = 'Title: &nbsp;&nbsp;"' + jsonData.title + '" (' + jsonData.year + ')' + '<br>';
    document.getElementById('auth').innerHTML = 'Author: &nbsp;&nbsp;' + jsonData.author + '<br>';
    document.getElementById('uwords').innerHTML = 'Unique words: &nbsp;&nbsp;' + jsonData.unique_words_count + '<br>';
    document.getElementById('nwords').innerHTML = 'Number of words: &nbsp;&nbsp;' + jsonData.words_count + '<br>';
    
    if (dif < 0.75) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating05.png">' + '<br>';  
    } else if (dif < 1.25) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating10.png">' + '<br>';  
    } else if (dif < 1.75) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating15.png">' + '<br>';  
    } else if (dif < 2.25) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating20.png">' + '<br>';  
    } else if (dif < 2.75) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating25.png">' + '<br>';  
    } else if (dif < 3.25) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating30.png">' + '<br>';  
    } else if (dif < 3.75) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating35.png">' + '<br>';  
    } else if (dif < 4.25) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating40.png">' + '<br>';  
    } else if (dif < 4.75) {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating45.png">' + '<br>';  
    } else {
        document.getElementById('complexity').innerHTML = 'Complexity: &nbsp;&nbsp; <img class="stars" src="../img/rating50.png">' + '<br>';  
    }
    
    
                
    }
    else {
    console.log(this.status)
    }
};
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
    yArr[i] = dataArray[i] * 100 / 255;
    xArr[i] = 1500 + i * 10;
}

var trace1 = {

    y: yArr,
    x: xArr,
    type: 'scatter'
};


var data = [trace1];

var layout = {
    title: "Book's vocabulary relevance mapping to years",
    xaxis: {
      title: 'year',
      titlefont: {
        family: 'Courier New, monospace',
        size: 18,
        color: '#7f7f7f'
      }
    },
    yaxis: {
      title: '% ',
      titlefont: {
        family: 'Courier New, monospace',
        size: 18,
        color: '#7f7f7f'
      }
    }
  };

Plotly.newPlot('graphic', data, layout);
}
