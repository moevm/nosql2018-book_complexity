function addFoundBook(title, author, difficulty, id, year, searchedBook) {
    
  var newRow;
  var containerBlock ;
  var nothing_found_block ;

    
  nothing_found_block = document.getElementById('nothingFoundBlock');
  nothing_found_block.style.display = 'none';
  

  newRow = document.createElement( 'tr' );
  newRow.onclick = function () {
      window.location.href = "book-analysis.html?id=" + id + "&searchedBook=" + searchedBook;
  }
  
  containerBlock = document.getElementById( 'tBodyIdBook' );
  newRow.className = "tableRow";
  newRow.id = "tdID";
  newRow.innerHTML = 
      '<td>'+ author + '</td>'
      + '<td>"'+ title + ' ('+ year +')'+'"</td>'
      + '<td>'+ difficulty + '</td>'    


  containerBlock.appendChild( newRow );
}
  
function ajaxDataGet() {
  var url_string = window.location.href; 
  var url = new URL(url_string);
  var bookName = url.searchParams.get("searchedBook");
  
  var xhttp = new XMLHttpRequest();

  xhttp.open("POST", "/SearchResultServlet", true);
  xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xhttp.send("searchedBook=" + bookName);

  

  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      var json = JSON.parse(this.responseText);
      console.log("succsess");
      console.log(json);

      for(var i in json)
      {
          addFoundBook(json[i].title, json[i].author, json[i].difficulty, json[i]._id, json[i].year, bookName)
      }
      
    }
    else {
      console.log(this.status)
    }
  };

}
 
  