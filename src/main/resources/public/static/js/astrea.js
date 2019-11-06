// This function allows to dynamically include the ontology urls inputs
$(document).ready(function() {
  $("#add").click(function() {
      var lastField = $("#buildyourform div:last");
      var intId = (lastField && lastField.length && lastField.data("idx") + 1) || 1;
      var fieldWrapper = $("<div class=\"fieldwrapper input-group\" style=\"margin-bottom:10px;\" id=\"field" + intId + "\">");
      fieldWrapper.data("idx", intId);
      var fName = $("<input  class=\"form-control\" class=\"ontology form-control\" name=\"ontology\" id=\"ontology\" placeholder=\"http://iot.linkeddata.es/def/core\">");
      var removeButton = $("<span class=\"input-group-btn\"><button id=\"remove\"  class=\"btn btn-danger mb-2 btn-custom remove pull-left\" style=\"margin-right:5px;\"><span class=\"glyphicon glyphicon-trash\"></span></button></span></div>");
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
    var urls = [];
    var ontologyInputs = $("#buildyourform :input").each(function() {
        var url = this.value;
        if(url.length>0){
            urls.push(url);
        }
    });
    postUrls(urls);
}

// This function sends the urls as a json document to the backend
function postUrls(urls){
  var xhr = new XMLHttpRequest();
  var url = "/api/shacl";
  xhr.open("POST", url, true);
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.setRequestHeader("Accept", "text/rdf+turtle");
  xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
          hideAlerts();
          if(xhr.status==206){
              $('#alert-warning').css("display", "block");
          }else if(xhr.status==200){
              $('#alert-success').css("display", "block");
          }else{
              $('#alert-error').css("display", "block");
          }
          if(xhr.responseText.length>0){
              download("astrea-shapes.ttl", xhr.responseText);
          }
       }
  };
  var data = JSON.stringify({"ontologies": urls });
  xhr.send(data);
}

// This function hides the alerts in the page
function hideAlerts(){
    $('#alert-warning').css("display", "none");
    $('#alert-success').css("display", "none");
    $('#alert-error').css("display", "none");
}

// This funciton creates the downloadable file and then start the download
function download(filename, text) {
  var element = document.createElement('a');
  element.setAttribute('href', 'data:text/turtle;charset=utf-8,' + encodeURIComponent(text));
  element.setAttribute('download', filename);
  element.style.display = 'none';
  document.body.appendChild(element);
  element.click();
  document.body.removeChild(element);
}

// This function fetches the ontology URLs from the inputs and post them
function generateFor(url){
    var urls = [url];
    postUrls(urls);
}

