
var modal = document.getElementById('myModal');
var errmodal = document.getElementById('erModal');
var span = document.getElementsByClassName("propmodal-close")[0];
var span2 = document.getElementsByClassName("propmodal-close2")[0];
var submitButton = document.getElementById('sbmt-btn');
var inputedTitle;
var inputedAuthor;
var sendingFiles = 0;
var flag = false;

// DROPZONE ==================================
var dropzoneConfig = 
    { 
        url: "/bookUpload",
        maxFiles: "1",
        acceptedFiles: "application/pdf,application/epub+zip,.fb2",
        addRemoveLinks: "true"
    };
var bookDropzone = new Dropzone("div#drop-zone", dropzoneConfig);

Dropzone.autoDiscover = false;

$(".dz-hidden-input").prop("disabled",true);

bookDropzone.on("addedfile", function(file) {
    document.getElementById('dz-msg').style.display = 'none'
});

bookDropzone.on("reset", function(file) {
    sendingFiles = 0;
    document.getElementById('dz-msg').style.display = 'block';
});


bookDropzone.on("sending", function(file, xhrObj, formData) {
    console.log("drz")
    $(".dz-hidden-input").prop("disabled",true);
    sendingFiles++;
    flag = false;
    formData.append("bookTitle", inputedTitle);
    formData.append("bookAuthor", inputedAuthor);
    if (!(inputedAuthor && inputedTitle)){
        console.log("missing input param(s)");
        bookDropzone.removeAllFiles(true);
        return;
    }
});








// =========================

span.onclick = function() { // When the user clicks on <span> (x), close the modal
    modal.style.display = "none";
}
span2.onclick = function() { // When the user clicks on <span> (x), close the modal
    errmodal.style.display = "none";
}
submitButton.onclick = function() {
    modal.style.display = "none";
}

window.onclick = function(event) { // When the user clicks anywhere outside of the modal, close it
    if (event.target == modal) {
        bookDropzone.removeAllFiles();
        modal.style.display = "none";
    }
}

$( "#drop-zone" ).on("click", function(event) {
    if(sendingFiles > 0){
        errmodal.style.display = "block";
        return;
    }
    if(!flag){
        modal.style.display = "block";
    } 
});

$( "#drop-zone" ).on("drop", function(event) {
    if(sendingFiles > 0){
        errmodal.style.display = "block";
        return;
    }
    if(!flag){
        modal.style.display = "block";
    }
});

$( "form#infoFormId" ).on( "submit", function( event ) {
    event.preventDefault();
    inputedTitle = $("input#btId").val();
    inputedAuthor = $("input#baId").val();
    $(".dz-hidden-input").prop("disabled",false);
    flag = true;
    document.getElementById("drop-zone").click();
});
