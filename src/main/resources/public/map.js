function initMap(){
    var options = {zoom:8, center:{lat:42.2827, lng:-123.1207}}
    var map = new google.maps.Map(document.getElementById("map"), options);
    //document.getElementById("test").innerHTML = "testing";
    console.log("initMap() running")
    }