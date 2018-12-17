var modal = document.getElementById('myModal');
var errmodal = document.getElementById('errmodal');
var submitButton = document.getElementById('sbmt-btn');
var inputedTitle;
var inputedAuthor;
var inputedYear;

$( "form#infoFormId" ).on( "submit", function( event ) {
    event.preventDefault();
    inputedTitle = $("input#inputTitle").val();
    inputedAuthor = $("input#inputAuthor").val();
    inputedYear = $("input#inputYear").val();
    inputedBookFiles = document.getElementById("inputBookFile").files;
    inputedImage = document.getElementById("inputImage").files;

    if (!(inputedAuthor && inputedTitle && inputedYear ) || inputedBookFiles.length === 0){
        console.log("missing input param(s)");  
        showError("Some fields are empty");
        return;
    }
    else {
        var ext = getExtension(inputedBookFiles[0].name);
        var formData = new FormData();
        var xhttp = new XMLHttpRequest();
        if (inputedImage.length === 1) {
            var extImg = getExtension(inputedImage[0].name);
            if(extImg.toLowerCase() != 'png' && extImg.toLowerCase() != 'jpeg' && extImg.toLowerCase() != 'jpg'){

                showError("Wrong image extention");
                return;
            }
            formData.append('bookImage', $('input[type=file]#inputImage')[0].files[0]); 
        }
        if (ext.toLowerCase() != "pdf" && ext.toLowerCase() != "fb2" && ext.toLowerCase() != "txt" && ext.toLowerCase() != "epub" ){
            showError("Wrong book format");
            return;
        }

        
        formData.append("bookTitle", inputedTitle);
        formData.append("bookAuthor", inputedAuthor);
        formData.append("bookYear", inputedYear);
        formData.append('bookFile', $('input[type=file]#inputBookFile')[0].files[0]); 

        console.log(formData.get('bookTitle'));
        console.log(formData.get('bookAuthor'));
        console.log(formData.get('bookFile'));
        xhttp.open("POST", "/bookUpload", true);
        xhttp.send(formData);
        document.getElementById("closeModal").click();
    }
    
});

function showError(errorString){
    document.getElementById("err").innerHTML = errorString;
    document.getElementById("err").style.display = 'block';
}

function getExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}