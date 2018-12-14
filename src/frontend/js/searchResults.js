function addFoundBook(title, author, difficulty, id) {
    var block_to_insert ;
    var container_block ;
    var nothing_found_block ;
    

    nothing_found_block = document.getElementById('nothingFoundBlock');
    nothing_found_block.style.display = 'none';
    

    block_to_insert = document.createElement( 'div' );
    block_to_insert.className = 'bookEntity';
    block_to_insert.innerHTML = 
          '<a href="book-analysis.html?id='+ id +'">'
          + title + ',&nbsp;&nbsp;&nbsp;' 
          + author + '&nbsp;&nbsp;(<i>dificulty = ' 
          + difficulty + '</i>)'
          + '</a>';
    

    container_block = document.getElementById( 'insertedFoundBooks' );
    container_block.appendChild( block_to_insert );
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
        console.log("succsess ");
        console.log(json);

        for(var i in json)
        {
            addFoundBook(json[i].title, json[i].author, json[i].difficulty, json[i]._id)
        }
        
      }
      else {
        console.log(this.status)
      }
    };

}
 
  