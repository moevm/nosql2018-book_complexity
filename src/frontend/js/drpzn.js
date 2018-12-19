
Dropzone.autoDiscover = false;
dropzoneOptions = {

    url: '/import',
    dictDefaultMessage: 'Drop <i>yout_db_name.bson</i> file here or click to choose from a file system',
    acceptedFiles: ".bson",
    maxFiles: 1,
    addRemoveLinks: true
  };

var myDropzone = new Dropzone("div#drpznID", dropzoneOptions);