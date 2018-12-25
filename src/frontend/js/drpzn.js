
Dropzone.autoDiscover = false;
dropzoneOptions = {

    url: '/import',
    dictDefaultMessage: 'Drop <i>yout_db_name.json</i> file here or click to choose from a file system',
    acceptedFiles: ".json",
    maxFiles: 1,
    addRemoveLinks: true
  };

var myDropzone = new Dropzone("div#drpznID", dropzoneOptions);