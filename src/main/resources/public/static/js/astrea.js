// This function allows to dynamically include the ontology urls inputs
$(document).ready(function() {
  $("#add").click(function() {
      var lastField = $("#buildyourform div:last");
      var intId = (lastField && lastField.length && lastField.data("idx") + 1) || 1;
      var fieldWrapper = $("<div class=\"fieldwrapper input-group\" style=\"margin-bottom:10px;\" id=\"field" + intId + "\">");
      fieldWrapper.data("idx", intId);
      var fName = $("<input  class=\"form-control\" class=\"ontology form-control\" name=\"ontology\" id=\"ontology\" placeholder=\"put here your ontology url\">");
      var removeButton = $("<span class=\"input-group-btn\"><button id=\"remove\"  class=\"btn btn-danger mb-2 btn-custom remove pull-left\" style=\"margin-right:5px;\"><span class=\"glyphicon glyphicon-trash\"></span> Remove</button></span></div>");
      removeButton.click(function() {
          $(this).parent().remove();
      });
      fieldWrapper.append(fName);
      fieldWrapper.append(removeButton);
      $("#buildyourform").append(fieldWrapper);
  });
});

// This function fetches the ontology URLs from the inputs and post them
function loadontOlogies(){
    hideAlerts();
    $('#spinner').css("display", "block");
    var urls = [];
    var ontologyInputs = $("#buildyourform :input").each(function() {
        var url = this.value;
        if(url.length>0)
            urls.push(url);
    });
    var fileContentPresent = typeof(ontologyFileContent) != 'undefined' && ontologyFileContent.length > 0;
    var urlsLoaded = urls.length > 0;
    if(fileContentPresent && !urlsLoaded){
      postOntologyContent(ontologyFileContent, format);
    }else if(!fileContentPresent && urlsLoaded){
      postUrls(urls);
    }else if(fileContentPresent && urlsLoaded){
      $('#spinner').css("display", "none");
      $('#alert-error-dobuledragon').css("display", "block");
    }else{
      $('#spinner').css("display", "none");
      $('#alert-warning-input').css("display", "block");
    }
    
}

//This function reads the file from the input
function readOntologyFileContent(){
  return ontologyFileContent; // this variable is defined as static in the index.html
}


// This function sends the urls as a json document to the backend
function postUrls(urls){
  var xhr = new XMLHttpRequest();
  var url = "/api/shacl/url";
  xhr.open("POST", url, true);
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.setRequestHeader("Accept", "text/rdf+turtle");
  xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
          $('#spinner').css("display", "none");
          if(xhr.status==206){
              $('#alert-warning').css("display", "block");
          }else if(xhr.status==200){
              $('#alert-success').css("display", "block");
          }else{
              $('#alert-error').css("display", "block");
          }
          if(xhr.responseText.length>0){
               $('#modalLoading').modal('hide');
              download("astrea-shapes.ttl", xhr.responseText);
          }
       }
  };
  var data = JSON.stringify({"ontologies": urls });
  xhr.send(data);
}
// This function postst the ontology body with its format
function postOntologyContent(ontology, format){
  var xhr = new XMLHttpRequest();
  var url = "/api/shacl/document";
  xhr.open("POST", url, true);
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.setRequestHeader("Accept", "text/rdf+turtle");
  xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
          $('#spinner').css("display", "none");
          if(xhr.status==206){
              $('#alert-warning').css("display", "block");
          }else if(xhr.status==200){
              $('#alert-success').css("display", "block");
          }else{
              $('#generation_error_format').css("display", "block");
          }
          if(xhr.responseText.length>0){
               $('#modalLoading').modal('hide');
              download("astrea-shapes.ttl", xhr.responseText);
          }
       }
  };

  payload = JSON.parse("{\"ontology\":\"\", \"serialisation\" : \"\"}");
  payload["ontology"] = ontology
  payload["serialisation"] = format  
  ontologyFileContent="";
  format="";
  var data = JSON.stringify(payload);
  xhr.send(data);
}


// This function hides the alerts in the page
function hideAlerts(){
   $('#alert-error-format').css("display", "none");
    $('#alert-error-dobuledragon').css("display", "none");
    $('#alert-warning-input').css("display", "none");
    $('#alert-warning').css("display", "none");
    $('#alert-success').css("display", "none");
    $('#alert-error').css("display", "none");
}

// This funciton creates the downloadable file and then start the download
function download(filename, text) {
  createBlob(text, filename);
}

function createBlob(content, filename){
   var blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    if (navigator.msSaveBlob) { // IE 10+
        navigator.msSaveBlob(blob, filename);
    } else {
        var link = document.createElement("a");
        if (link.download !== undefined) { // feature detection
            // Browsers that support HTML5 download attribute
            var url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
}

// This function fetches the ontology URLs from the inputs and post them
function generateFor(url){
    $('#modalLoading').modal('show');
    var urls = [url];
    postUrls(urls);
}

